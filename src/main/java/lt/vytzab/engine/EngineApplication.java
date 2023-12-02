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
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        String clOrdId = message.getString(ClOrdID.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        char ordType = message.getChar(OrdType.FIELD);

        double price = 0;
        if (ordType == OrdType.LIMIT) {
            price = message.getDouble(Price.FIELD);
        }

        double qty = message.getDouble(OrderQty.FIELD);
        char timeInForce = TimeInForce.DAY;
        if (message.isSetField(TimeInForce.FIELD)) {
            timeInForce = message.getChar(TimeInForce.FIELD);
        }

        try {
            if (timeInForce != TimeInForce.DAY) {
                throw new RuntimeException("Unsupported TIF, use Day");
            }

            MarketOrder order = new MarketOrder(clOrdId, symbol, senderCompId, targetCompId, side, ordType, price, (int) qty);

            processOrder(order);
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
        }
    }

    private void processOrder(MarketOrder order) {
        if (marketController.insert(order)) {
            acceptOrder(order);

            ArrayList<MarketOrder> orders = new ArrayList<>();
            marketController.match(order.getSymbol(), orders);

            while (orders.size() > 0) {
                fillOrder(orders.remove(0));
            }
        } else {
            rejectOrder(order);
        }
    }

    private void rejectOrder(String senderCompId, String targetCompId, String clOrdId, String symbol, char side, String message) {
        ExecutionReport fixOrder = new ExecutionReport(new OrderID(clOrdId), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.REJECTED), new OrdStatus(ExecType.REJECTED), new Symbol(symbol),
                new Side(side), new LeavesQty(0), new CumQty(0), new AvgPx(0));

        fixOrder.setString(ClOrdID.FIELD, clOrdId);
        fixOrder.setString(Text.FIELD, message);
        fixOrder.setInt(OrdRejReason.FIELD, OrdRejReason.BROKER_EXCHANGE_OPTION);

        try {
            Session.sendToTarget(fixOrder, senderCompId, targetCompId);
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
        //TODO add to table
    }

    private void rejectOrder(MarketOrder order) {
        updateOrder(order, OrdStatus.REJECTED);
    }

    private void acceptOrder(MarketOrder order) {
        updateOrder(order, OrdStatus.NEW);
    }

    private void cancelOrder(MarketOrder order) {
        updateOrder(order, OrdStatus.CANCELED);
    }

    private void updateOrder(MarketOrder order, char status) {
        String targetCompId = order.getOwner();
        String senderCompId = order.getTarget();

        ExecutionReport fixOrder = new ExecutionReport(new OrderID(order.getClientOrderId()),
                new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW),
                new ExecType(status), new OrdStatus(status), new Symbol(order.getSymbol()),
                new Side(order.getSide()), new LeavesQty(order.getOpenQuantity()), new CumQty(order
                .getExecutedQuantity()), new AvgPx(order.getAvgExecutedPrice()));

        fixOrder.setString(ClOrdID.FIELD, order.getClientOrderId());
        fixOrder.setDouble(OrderQty.FIELD, order.getQuantity());

        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        }

        try {
            Session.sendToTarget(fixOrder, senderCompId, targetCompId);
        } catch (SessionNotFound e) {
        }
    }

    private void fillOrder(MarketOrder order) {
        updateOrder(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        String id = message.getString(OrigClOrdID.FIELD);
        MarketOrder order = marketController.find(symbol, side, id);
        if (order != null) {
            order.cancel();
            cancelOrder(order);
            marketController.erase(order);
        } else {
            OrderCancelReject fixOrderReject = new OrderCancelReject(new OrderID("NONE"), new ClOrdID(message.getString(ClOrdID.FIELD)),
                    new OrigClOrdID(message.getString(OrigClOrdID.FIELD)), new OrdStatus(OrdStatus.REJECTED), new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));

            String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
            String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
            fixOrderReject.getHeader().setString(SenderCompID.FIELD, targetCompId);
            fixOrderReject.getHeader().setString(TargetCompID.FIELD, senderCompId);
            try {
                Session.sendToTarget(fixOrderReject, targetCompId, senderCompId);
            } catch (SessionNotFound e) {
            }
        }
    }

    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();
        char subscriptionRequestType = message.getChar(SubscriptionRequestType.FIELD);

        if (subscriptionRequestType != SubscriptionRequestType.SNAPSHOT)
            throw new IncorrectTagValue(SubscriptionRequestType.FIELD);
        int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);

        MarketDataSnapshotFullRefresh fixMD = new MarketDataSnapshotFullRefresh();
        fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));

        for (int i = 1; i <= relatedSymbolCount; ++i) {
            message.getGroup(i, noRelatedSyms);
            String symbol = noRelatedSyms.getString(Symbol.FIELD);
            fixMD.setString(Symbol.FIELD, symbol);
        }

        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        noMDEntries.setChar(MDEntryType.FIELD, '0');
        noMDEntries.setDouble(MDEntryPx.FIELD, 123.45);
        fixMD.addGroup(noMDEntries);
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);
        try {
            Session.sendToTarget(fixMD, targetCompId, senderCompId);
        } catch (SessionNotFound e) {
        }
    }

    public MarketController orderMatcher() {
        return marketController;
    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });
    }
}
