package lt.vytzab.engine;

import quickfix.*;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.fix42.*;

import javax.swing.*;
import java.util.ArrayList;

import lt.vytzab.utils.CustomFixMessageParser;

public class EngineApplication extends MessageCracker implements quickfix.Application {
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private OrderTableModel orderTableModel = null;
    private final MarketController marketController = new MarketController();
    private final OrderIdGenerator generator = new OrderIdGenerator();
    private final LogPanel logPanel;

    public EngineApplication(OrderTableModel orderTableModel, LogPanel logPanel) {
        this.orderTableModel = orderTableModel;
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

    public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        String clOrdID = message.getString(ClOrdID.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        char side = message.getChar(Side.FIELD);
        char ordType = message.getChar(OrdType.FIELD);
        double price = 0;
        if (ordType == OrdType.LIMIT) {
            price = message.getDouble(Price.FIELD);
        } else if (ordType == OrdType.MARKET) {
            price = 0;
            //TODO implement last market price
        }
        long quantity = (long) message.getDouble(OrderQty.FIELD);

//        char timeInForce = TimeInForce.DAY;
//        if (message.isSetField(TimeInForce.FIELD)) {
//            timeInForce = message.getChar(TimeInForce.FIELD);
//        }
        //TODO implement TIF

        try {
//            if (timeInForce != TimeInForce.DAY) {
//                throw new RuntimeException("Unsupported TIF, use Day");
//            }

            MarketOrder order = new MarketOrder(clOrdID, symbol, senderCompId, targetCompId, side, ordType, price, quantity);

            processOrder(order);
            orderTableModel.addOrder(order);

            // Remove fully executed orders from the OrderTableModel
            orderTableModel.removeFullyExecutedOrders();
        } catch (Exception e) {
            rejectNewOrderSingle(targetCompId, senderCompId, clOrdID, symbol, side, e.getMessage());
        }
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        MarketOrder order = marketController.find(message.getString(Symbol.FIELD), message.getChar(Side.FIELD), message.getString(OrigClOrdID.FIELD));
        if (order != null) {
            order.cancel();
            marketController.erase(order);
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

    private void processOrder(MarketOrder order) {
        //If order is added
        if (marketController.insert(order)) {
            //send accepted execution report
            acceptOrder(order);

            ArrayList<MarketOrder> orders = new ArrayList<>();
            //try to match
            marketController.match(order.getSymbol(), orders);

            while (!orders.isEmpty()) {
                fillOrder(orders.remove(0));
            }
        } else {
            rejectOrder(order);
        }
    }

    private void rejectNewOrderSingle(String senderCompId, String targetCompId, String clOrdID, String symbol, char side, String message) {
        ExecutionReport rejectExecutionReport = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(ExecType.REJECTED), new OrdStatus(ExecType.REJECTED), new Symbol(symbol), new Side(side), new LeavesQty(0), new CumQty(0), new AvgPx(0));

        rejectExecutionReport.setString(ClOrdID.FIELD, clOrdID);
        rejectExecutionReport.setString(Text.FIELD, message);
        rejectExecutionReport.setInt(OrdRejReason.FIELD, OrdRejReason.BROKER_EXCHANGE_OPTION);

        try {
            Session.sendToTarget(rejectExecutionReport, senderCompId, targetCompId);
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
        //TODO add rejected order to table
    }

    private void rejectOrder(MarketOrder order) {
        sendExecutionReport(order, OrdStatus.REJECTED);
    }

    private void acceptOrder(MarketOrder order) {
        sendExecutionReport(order, OrdStatus.NEW);
    }

    private void cancelOrder(MarketOrder order) {
        sendExecutionReport(order, OrdStatus.CANCELED);
    }

    private void sendExecutionReport(MarketOrder order, char status) {
        ExecutionReport executionReport = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(status), new OrdStatus(status), new Symbol(order.getSymbol()), new Side(order.getSide()), new LeavesQty(order.getOpenQuantity()), new CumQty(order.getExecutedQuantity()), new AvgPx(order.getAvgExecutedPrice()));

        executionReport.setString(ClOrdID.FIELD, order.getClOrdID());
        executionReport.setDouble(OrderQty.FIELD, order.getQuantity());

        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
            executionReport.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            executionReport.setDouble(LastPx.FIELD, order.getPrice());
        }

        try {
            Session.sendToTarget(executionReport, order.getSenderCompID(), order.getTargetCompID());
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void fillOrder(MarketOrder order) {
        sendExecutionReport(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

//    public MarketController orderMatcher() {
//        return marketController;
//    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });
    }
}
