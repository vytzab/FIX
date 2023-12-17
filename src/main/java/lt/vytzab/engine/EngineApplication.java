package lt.vytzab.engine;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.OrderIdGenerator;
import lt.vytzab.engine.market.MarketController;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.panels.LogPanel;
import quickfix.*;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.fix42.*;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;

import lt.vytzab.engine.helpers.CustomFixMessageParser;

public class EngineApplication extends MessageCracker implements quickfix.Application {
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private OrderTableModel openOrderTableModel = null;
    private OrderTableModel allOrderTableModel = null;
    private final MarketController marketController = new MarketController();
    private final OrderIdGenerator generator = new OrderIdGenerator();
    private final LogPanel logPanel;

    public EngineApplication(OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel) {
        this.openOrderTableModel = openOrderTableModel;
        this.allOrderTableModel = allOrderTableModel;
        this.logPanel = logPanel;
    }

    public void onCreate(SessionID sessionId) {
    }

    public void onLogon(SessionID sessionId) {
    }

    public void onLogout(SessionID sessionId) {
    }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("toAdmin:" + message.toString());
    }

    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
        displayFixMessageInLogs("toApp:" + message.toString());
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        displayFixMessageInLogs("fromAdmin:" + message.toString());
    }

    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        displayFixMessageInLogs("fromApp:" + message.toString());
        crack(message, sessionID);
        marketController.display();
    }

    public void onMessage(NewOrderSingle newOrderSingle, SessionID sessionID) throws FieldNotFound{
        if (marketController.checkIfMarketExists(newOrderSingle.getString(Symbol.FIELD))) {
            Order order = orderFromNewOrderSingle(newOrderSingle);
            try {
                processOrder(order);
            } catch (Exception e) {
                rejectNewOrderSingle(newOrderSingle, e.getMessage());
            }
        } else {
            rejectNewOrderSingle(newOrderSingle, "Market does not exist.");
        }
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        Order order = marketController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        if (order != null) {
            order.cancel();
            marketController.deleteOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
            cancelOrder(order);
        } else {
            OrderCancelReject orderCancelReject = new OrderCancelReject(new OrderID(generator.genOrderID()), new ClOrdID(message.getString(ClOrdID.FIELD)), new OrigClOrdID(message.getString(OrigClOrdID.FIELD)), new OrdStatus(OrdStatus.REJECTED), new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));
            try {
                Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
            } catch (SessionNotFound e) {
                //TODO implement better logging
            }
        }
    }

//    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
//        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();
//        char subscriptionRequestType = message.getChar(SubscriptionRequestType.FIELD);
//
//        if (subscriptionRequestType != SubscriptionRequestType.SNAPSHOT)
//            throw new IncorrectTagValue(SubscriptionRequestType.FIELD);
//        int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);
//
//        MarketDataSnapshotFullRefresh fixMD = new MarketDataSnapshotFullRefresh();
//        fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));
//
//        for (int i = 1; i <= relatedSymbolCount; ++i) {
//            message.getGroup(i, noRelatedSyms);
//            String symbol = noRelatedSyms.getString(Symbol.FIELD);
//            fixMD.setString(Symbol.FIELD, symbol);
//        }
//
//        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
//        noMDEntries.setChar(MDEntryType.FIELD, '0');
//        noMDEntries.setDouble(MDEntryPx.FIELD, 123.45);
//        fixMD.addGroup(noMDEntries);
//        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
//        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
//        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
//        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);
//        try {
//            Session.sendToTarget(fixMD, targetCompId, senderCompId);
//        } catch (SessionNotFound e) {
//        }
//    }

    private void processOrder(Order order) {
        //If order is added
        if (marketController.insert(order)) {
            //send accepted execution report
            acceptOrder(order);

            //try to match
            marketController.match(marketController.getMarket(order.getSymbol()));
            // Remove fully executed orders from the OrderTableModel
            openOrderTableModel.removeFullyExecutedOrders();
        } else {
            rejectOrder(order);
        }
    }

    private void rejectNewOrderSingle(NewOrderSingle newOrderSingle, String message) throws FieldNotFound {
        ExecutionReport rejectExecutionReport = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(ExecType.REJECTED), new OrdStatus(ExecType.REJECTED), new Symbol(newOrderSingle.getString(Symbol.FIELD)), new Side(newOrderSingle.getChar(Side.FIELD)), new LeavesQty(0), new CumQty(0), new AvgPx(0));

        rejectExecutionReport.setString(ClOrdID.FIELD, newOrderSingle.getString(ClOrdID.FIELD));
        rejectExecutionReport.setString(Text.FIELD, message);
        rejectExecutionReport.setInt(OrdRejReason.FIELD, OrdRejReason.BROKER_EXCHANGE_OPTION);

        Order order = orderFromNewOrderSingle(newOrderSingle);
        order.setRejected(true);
        order.setEntryDate(LocalDate.now());
        allOrderTableModel.addOrder(order);

        try {
            Session.sendToTarget(rejectExecutionReport, newOrderSingle.getString(SenderCompID.FIELD), newOrderSingle.getString(TargetCompID.FIELD));
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void rejectOrder(Order order) {
        sendExecutionReport(order, OrdStatus.REJECTED);
    }

    private void acceptOrder(Order order) {
        sendExecutionReport(order, OrdStatus.NEW);
    }

    private void cancelOrder(Order order) {
        sendExecutionReport(order, OrdStatus.CANCELED);
    }

    private void sendExecutionReport(Order order, char status) {
        ExecutionReport executionReport = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(status), new OrdStatus(status), new Symbol(order.getSymbol()), new Side(order.getSide()), new LeavesQty(order.getOpenQuantity()), new CumQty(order.getExecutedQuantity()), new AvgPx(order.getAvgExecutedPrice()));

        executionReport.setString(ClOrdID.FIELD, order.getClOrdID());
        executionReport.setDouble(OrderQty.FIELD, order.getQuantity());
        order.setEntryDate(LocalDate.now());

        if (status == OrdStatus.CANCELED) {
            order.setCanceled(true);
        } else if (status == OrdStatus.REJECTED) {
            order.setRejected(true);
        }

        openOrderTableModel.addOrder(order);
        allOrderTableModel.addOrder(order);
        MarketOrderDAO.createMarketOrder(order);


        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
            executionReport.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            executionReport.setDouble(LastPx.FIELD, order.getPrice());
        }

        try {
            Session.sendToTarget(executionReport, order.getTargetCompID(), order.getSenderCompID());
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void fillOrder(Order order) {
        sendExecutionReport(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });
    }

    public Order orderFromNewOrderSingle(NewOrderSingle newOrderSingle) throws FieldNotFound {
        double price = 0;
        if (newOrderSingle.getChar(OrdType.FIELD) == OrdType.LIMIT) {
            price = newOrderSingle.getDouble(Price.FIELD);
        } else if (newOrderSingle.getChar(OrdType.FIELD) == OrdType.MARKET) {
            if (marketController.checkIfMarketExists(newOrderSingle.getString(Symbol.FIELD))) {
                price = marketController.getMarket(newOrderSingle.getString(Symbol.FIELD)).getLastPrice();
            } else {
                price = 0;
            }
        }
        Order order = new Order(newOrderSingle.getString(ClOrdID.FIELD), newOrderSingle.getString(Symbol.FIELD), newOrderSingle.getHeader().getString(SenderCompID.FIELD), newOrderSingle.getHeader().getString(TargetCompID.FIELD),
                newOrderSingle.getChar(Side.FIELD), newOrderSingle.getChar(OrdType.FIELD), price, (long) newOrderSingle.getDouble(OrderQty.FIELD));

        char timeInForce = TimeInForce.DAY;
        //TODO implement TIF
        return order;
    }
}
