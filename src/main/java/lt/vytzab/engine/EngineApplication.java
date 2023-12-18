package lt.vytzab.engine;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.messages.ExecutionReport;
import lt.vytzab.engine.messages.NewOrderSingle;
import lt.vytzab.engine.messages.OrderCancelReject;
import lt.vytzab.engine.messages.OrderCancelReplaceRequest;
import lt.vytzab.engine.messages.OrderCancelRequest;
import lt.vytzab.engine.order.OrderIdGenerator;
import lt.vytzab.engine.market.MarketController;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.panels.LogPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;

import lt.vytzab.engine.helpers.CustomFixMessageParser;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.*;
import quickfix.fix42.BusinessMessageReject;
import quickfix.fix42.Message;
import quickfix.fix42.MessageCracker;
import quickfix.fix42.OrderStatusRequest;

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
    }

    // System messages  /\
    //                  ||
    // Messages from OrderEntry ||
    //                          \/

//    public void onMessage(NewOrderSingle newOrderSingle, SessionID sessionID) throws FieldNotFound{
//        if (marketController.checkIfMarketExists(newOrderSingle.getString(Symbol.FIELD))) {
//            Order order = orderFromNewOrderSingle(newOrderSingle);
//            try {
//                processOrder(order);
//            } catch (Exception e) {
//                rejectNewOrderSingle(newOrderSingle, e.getMessage());
//            }
//        } else {
//            rejectNewOrderSingle(newOrderSingle, "Market does not exist.");
//        }
//    }

    public void onMessage(NewOrderSingle newOrderSingle, SessionID sessionID) throws FieldNotFound{
        if (marketController.checkIfMarketExists(newOrderSingle.getString(Symbol.FIELD))) {
            try {
                processNewOrder(newOrderSingle);
            } catch (Exception e) {
                messageExecutionReport(newOrderSingle, '8');
            }
        } else {
            messageExecutionReport(newOrderSingle, '8');
        }
    }

    private void processNewOrder(NewOrderSingle newOrderSingle) throws FieldNotFound {
        Order order = orderFromNewOrderSingle(newOrderSingle);
        //If order is added
        if (marketController.insert(order)) {
            //send accepted execution report
            messageExecutionReport(newOrderSingle, '0');

            openOrderTableModel.addOrder(order);
            allOrderTableModel.addOrder(order);
            MarketOrderDAO.createMarketOrder(order);

            ArrayList<Order> orders = new ArrayList<>();
            //try to match
            marketController.match(marketController.getMarket(order.getSymbol()), orders);
            while (!orders.isEmpty()) {
                order = orders.remove(0);
                orderExecutionReport(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
            }
            // Remove fully executed orders from the OrderTableModel
            openOrderTableModel.removeFullyExecutedOrders();
        } else {
            messageExecutionReport(newOrderSingle, '8');
        }
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        Order order = marketController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        if (order != null) {
            order.cancel();
            marketController.deleteOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
//            cancelOrder(order);
        } else {
            OrderCancelReject orderCancelReject = new OrderCancelReject(generator.genOrderID(), message.getString(ClOrdID.FIELD), message.getString(OrigClOrdID.FIELD), OrdStatus.REJECTED, CxlRejResponseTo.ORDER_CANCEL_REQUEST);
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

//    private void rejectNewOrderSingle(NewOrderSingle newOrderSingle, String message) throws FieldNotFound {
//        ExecutionReport rejectExecutionReport = new ExecutionReport(generator.genOrderID(), generator.genExecutionID(), ExecTransType.NEW, ExecType.REJECTED, OrdStatus.REJECTED,
//                newOrderSingle.getString(Symbol.FIELD), newOrderSingle.getChar(Side.FIELD), 0, 0, 0);
//
//        rejectExecutionReport.setString(ClOrdID.FIELD, newOrderSingle.getString(ClOrdID.FIELD));
//        rejectExecutionReport.setString(Text.FIELD, message);
//        rejectExecutionReport.setInt(OrdRejReason.FIELD, OrdRejReason.BROKER_EXCHANGE_OPTION);
//
//        Order order = orderFromNewOrderSingle(newOrderSingle);
//        order.setRejected(true);
//        order.setEntryDate(LocalDate.now());
//        allOrderTableModel.addOrder(order);
//
//        try {
//            Session.sendToTarget(rejectExecutionReport, newOrderSingle.getString(SenderCompID.FIELD), newOrderSingle.getString(TargetCompID.FIELD));
//        } catch (SessionNotFound e) {
//            //TODO implement better logging
//        }
//    }

//    private void cancelOrder(Order order) {
//        sendExecutionReport(order, OrdStatus.CANCELED);
//    }

    private void messageExecutionReport(Message message, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport();
        switch (message.getClass().getSimpleName()) {
            case "NewOrderSingle":
                executionReport = NewOrderSingleER((NewOrderSingle) message, ordStatus);
                break;
            case "OrderCancelRequest":
                OrderCancelRequestER((OrderCancelRequest) message, ordStatus);
                break;
            case "OrderCancelReplaceRequest":
                OrderCancelReplaceRequestER((OrderCancelReplaceRequest) message, ordStatus);
                break;
            case "MarketDataRequest":
                // TODO implement
//                processMarketDataRequest((MarketDataRequest) message);
                break;
            default:
                // Handle other message types or provide a default behavior
                break;
        }
        try {
            Session.sendToTarget(executionReport, message.getString(TargetCompID.FIELD), message.getString(SenderCompID.FIELD));
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void orderExecutionReport(Order order, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport(generator.genOrderID(), generator.genExecutionID(), ExecTransType.NEW, ordStatus,
                ordStatus, order.getSymbol(), order.getSide(), order.getOpenQuantity(), order.getExecutedQuantity(), order.getAvgExecutedPrice());
        executionReport.setString(ClOrdID.FIELD, order.getClOrdID());
        executionReport.setDouble(OrderQty.FIELD, order.getQuantity());
        order.setEntryDate(LocalDate.now());
        if (ordStatus == OrdStatus.CANCELED) {
            order.setCanceled(true);
        } else if (ordStatus == OrdStatus.REJECTED) {
            order.setRejected(true);
        }
        if (ordStatus == OrdStatus.FILLED || ordStatus == OrdStatus.PARTIALLY_FILLED) {
            executionReport.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            executionReport.setDouble(LastPx.FIELD, order.getPrice());
        }
        try {
            Session.sendToTarget(executionReport, order.getTargetCompID(), order.getSenderCompID());
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private ExecutionReport NewOrderSingleER(NewOrderSingle message, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport();
        switch (ordStatus) {
            case '8':
                executionReport = new ExecutionReport(generator.genOrderID(), generator.genExecutionID(), ExecTransType.NEW, ExecType.REJECTED,
                        OrdStatus.REJECTED, message.getString(Symbol.FIELD), message.getChar(Side.FIELD), 0, 0, 0);
//                executionReport.setClOrdID(message.getString(ClOrdID.FIELD));
                // TODO maybe implement TargetCompID SenderCompID
                break;
            case '0':
                executionReport = new ExecutionReport(generator.genOrderID(), generator.genExecutionID(), ExecTransType.NEW, ExecType.NEW,
                        OrdStatus.NEW, message.getString(Symbol.FIELD), message.getChar(Side.FIELD), message.getDouble(LeavesQty.FIELD), message.getDouble(CumQty.FIELD), message.getDouble(AvgPx.FIELD));
                // TODO in process newOrder, setField(LeavesQty.FIELD), setField(CumQty.FIELD), setField(AvgPx.FIELD)
                break;
        }
        return executionReport;
    }

    private void OrderCancelRequestER(OrderCancelRequest message, char ordStatus) {
    }

    private void OrderCancelReplaceRequestER(OrderCancelReplaceRequest message, char ordStatus) {
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

    @Override
    public void onMessage(quickfix.fix42.NewOrderSingle message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        Order order = marketController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        if (order != null) {
            order.cancel();
            marketController.deleteOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
//            cancelOrder(order);
        } else {
            OrderCancelReject orderCancelReject = new OrderCancelReject(generator.genOrderID(), message.getString(ClOrdID.FIELD), message.getString(OrigClOrdID.FIELD), OrdStatus.REJECTED, CxlRejResponseTo.ORDER_CANCEL_REQUEST);
            try {
                Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
            } catch (SessionNotFound e) {
                //TODO implement better logging
            }
        }
    }

    public void crack(Message message, SessionID sessionID) throws UnsupportedMessageType, FieldNotFound, IncorrectTagValue {
        String type = message.getHeader().getString(35);
        switch (type) {
            case "D" -> this.onMessage((NewOrderSingle) message, sessionID);
            case "8" -> this.onMessage(message, sessionID);
            case "F" -> this.onMessage(message, sessionID);
            default -> this.onMessage(message, sessionID);
        }
    }
}
