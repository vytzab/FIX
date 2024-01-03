package lt.vytzab.engine;

import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.OrderController;
import lt.vytzab.engine.order.OrderIdGenerator;
import lt.vytzab.engine.market.MarketController;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.panels.LogPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lt.vytzab.engine.helpers.CustomFixMessageParser;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.*;
import quickfix.fix42.Message;
import quickfix.MessageCracker;
import quickfix.fix42.SecurityStatus;

public class EngineApplication extends MessageCracker implements quickfix.Application {
    private OrderTableModel openOrderTableModel = null;
    private OrderTableModel allOrderTableModel = null;
    private MarketTableModel marketTableModel = null;
    private final MarketController marketController = new MarketController();
    private final OrderController orderController = new OrderController();
    private final OrderIdGenerator generator = new OrderIdGenerator();
    private final LogPanel logPanel;
    private List<SessionID> sessionIDs = new ArrayList<>();

    public EngineApplication(MarketTableModel marketTableModel, OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel) {
        this.marketTableModel = marketTableModel;
        this.openOrderTableModel = openOrderTableModel;
        this.allOrderTableModel = allOrderTableModel;
        this.logPanel = logPanel;
    }

    public void onCreate(SessionID sessionId) {
    }

    public void onLogon(SessionID sessionId) {
        sessionIDs.add(sessionId);
        Session session = Session.lookupSession(sessionId);
        if (session != null && session.isLoggedOn()) {
            try {
                sendSecurityStatusOnLogon(session);
            } catch (FieldNotFound | UnsupportedMessageType | IncorrectTagValue e) {
                throw new RuntimeException(e);
            }
        }
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

    public void onMessage(quickfix.fix42.NewOrderSingle message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (marketController.checkIfMarketExists(message.getString(Symbol.FIELD))) {
            try {
                processNewOrder(message);
            } catch (Exception e) {
                messageExecutionReport(message, '8');
            }
        } else {
            messageExecutionReport(message, '8');
        }
    }

    public void onMessage(quickfix.fix42.OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        Order order = orderController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        if (order != null) {
            order.cancel();
            orderController.deleteOrderByClOrdID(order.getClOrdID());
            openOrderTableModel.removeOrder(order.getClOrdID());
            messageExecutionReport(message, OrdStatus.CANCELED);
        } else {
            OrderCancelReject orderCancelReject = new OrderCancelReject(
                    new OrderID(generator.genOrderID()),
                    new ClOrdID(message.getString(ClOrdID.FIELD)),
                    new OrigClOrdID(message.getString(OrigClOrdID.FIELD)),
                    new OrdStatus(OrdStatus.REJECTED),
                    new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));
            try {
                Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
            } catch (SessionNotFound e) {
                //TODO implement better logging
            }
        }
    }

    public void onMessage(quickfix.fix42.OrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        Order order = orderController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        if (order != null) {
            order.setQuantity((long)(message.getDouble(OrderQty.FIELD)));
            order.setOpenQuantity((long)(message.getDouble(OrderQty.FIELD)));
            orderController.updateOrder(order);
            openOrderTableModel.replaceOrder(order);
            messageExecutionReport(message, '5');
        } else {
            OrderCancelReject orderCancelReject = new OrderCancelReject(
                    new OrderID(generator.genOrderID()),
                    new ClOrdID(message.getString(ClOrdID.FIELD)),
                    new OrigClOrdID(message.getString(OrigClOrdID.FIELD)),
                    new OrdStatus(OrdStatus.REJECTED),
                    new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST));
            try {
                Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
            } catch (SessionNotFound e) {
                //TODO implement better logging
            }
        }
    }

    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();

        lt.vytzab.engine.messages.MarketDataSnapshotFullRefresh fixMD = new lt.vytzab.engine.messages.MarketDataSnapshotFullRefresh();
        fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries;
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);

        message.getGroup(1, noRelatedSyms);
        String symbol = noRelatedSyms.getString(Symbol.FIELD);
        fixMD.setString(Symbol.FIELD, symbol);
        List<Order> symbolOrders = orderController.getAllOrdersBySymbolAndSender(symbol, senderCompId);
        if (!symbolOrders.isEmpty()) {
            for (Order order : symbolOrders) {
                if (!order.getCanceled()){
                    noMDEntries = noMDEntriesFromOrder(order);
                    fixMD.addGroup(noMDEntries);
                }
            }
            try {
                Session.sendToTarget(fixMD, targetCompId, senderCompId);
            } catch (SessionNotFound e) {
            }
        }
    }

    public void onMessage(SecurityStatusRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        SecurityStatus securityStatus = securityStatusFromMarket(marketController.getMarket(message.getSymbol().toString()), 0);
        try {
            Session.sendToTarget(securityStatus, message.getHeader().getString(quickfix.field.TargetCompID.FIELD), message.getHeader().getString(quickfix.field.SenderCompID.FIELD));
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void processNewOrder(quickfix.fix42.NewOrderSingle newOrderSingle) throws FieldNotFound, SessionNotFound {
        marketController.refreshMarkets();
        Order order = orderFromNewOrderSingle(newOrderSingle);
        if (orderController.createOrder(order)) {
            messageExecutionReport(newOrderSingle, '0');

            openOrderTableModel.addOrder(order);
            allOrderTableModel.addOrder(order);

            ArrayList<Order> orders = new ArrayList<>();
            marketController.matchMarketOrders(marketController.getMarket(order.getSymbol()), orders);
            while (!orders.isEmpty()) {
                openOrderTableModel.replaceOrder(orders.get(0));
                allOrderTableModel.replaceOrder(orders.get(0));
                orderExecutionReport(orders.get(0), orders.get(0).isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
                orders.remove(0);
            }
            openOrderTableModel.removeFullyExecutedOrders();
            marketTableModel.addMarket(marketController.getMarket(order.getSymbol()));
            sendSecurityStatusFromMarket(marketController.getMarket(order.getSymbol()), 1);
        } else {
            messageExecutionReport(newOrderSingle, '8');
        }
    }

    public void sendSecurityStatusOnLogon(Session session) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        for (Market market : marketController.getMarkets()) {
            SecurityStatus securityStatus = securityStatusFromMarket(market, 0);
            try {
                session.sendToTarget(securityStatus, session.getSessionID().getSenderCompID(), session.getSessionID().getTargetCompID());
            } catch (SessionNotFound e) {
                //TODO implement better logging
            }
        }
    }

    private void messageExecutionReport(Message message, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport();
        switch (message.getClass().getSimpleName()) {
            case "NewOrderSingle":
                executionReport = NewOrderSingleER((NewOrderSingle) message, ordStatus);
                break;
            case "OrderCancelRequest":
                executionReport = OrderCancelRequestER((OrderCancelRequest) message, ordStatus);
                break;
            case "OrderCancelReplaceRequest":
                executionReport = OrderCancelReplaceRequestER((OrderCancelReplaceRequest) message, ordStatus);
                break;
        }
        try {
            Session.sendToTarget(executionReport, message.getHeader().getString(quickfix.field.TargetCompID.FIELD), message.getHeader().getString(quickfix.field.SenderCompID.FIELD));
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void orderExecutionReport(Order order, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport(
                new OrderID(generator.genOrderID()),
                new ExecID(generator.genExecutionID()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ordStatus),
                new OrdStatus(ordStatus),
                new Symbol(order.getSymbol()),
                new Side(order.getSide()),
                new LeavesQty(order.getOpenQuantity()),
                new CumQty(order.getExecutedQuantity()),
                new AvgPx(order.getAvgExecutedPrice()));
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
        ExecutionReport executionReport = new ExecutionReport(
                new OrderID(generator.genOrderID()),
                new ExecID(generator.genExecutionID()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.REJECTED),
                new OrdStatus(OrdStatus.REJECTED),
                new Symbol(message.getString(Symbol.FIELD)),
                new Side(message.getChar(Side.FIELD)),
                new LeavesQty(0),
                new CumQty(0),
                new AvgPx(0));
        executionReport.set(message.getClOrdID());
        switch (ordStatus) {
            case '8':
                break;
            case '0':
                executionReport.set(new ExecType(ExecType.NEW));
                executionReport.set(new OrdStatus(OrdStatus.NEW));
                executionReport.set(new LeavesQty(message.getDouble(OrderQty.FIELD)));
                executionReport.set(new CumQty(0));
                executionReport.set(new AvgPx(0));
                break;
        }
        return executionReport;
    }

    private ExecutionReport OrderCancelRequestER(OrderCancelRequest message, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport(
                new OrderID(generator.genOrderID()),
                new ExecID(generator.genExecutionID()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.CANCELED),
                new OrdStatus(OrdStatus.CANCELED),
                new Symbol(message.getString(Symbol.FIELD)),
                new Side(message.getChar(Side.FIELD)),
                new LeavesQty(0),
                new CumQty(0),
                new AvgPx(0));
        executionReport.set(message.getClOrdID());
        executionReport.set(message.getOrigClOrdID());

        return executionReport;
    }

    private ExecutionReport OrderCancelReplaceRequestER(OrderCancelReplaceRequest message, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport(
                new OrderID(generator.genOrderID()),
                new ExecID(generator.genExecutionID()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.REJECTED),
                new OrdStatus(OrdStatus.REJECTED),
                new Symbol(message.getString(Symbol.FIELD)),
                new Side(message.getChar(Side.FIELD)),
                new LeavesQty(message.getDouble(OrderQty.FIELD)),
                new CumQty(0),
                new AvgPx(0));
        executionReport.set(message.getClOrdID());
        switch (ordStatus) {
            case '8':
                break;
            case '5':
                Order order = orderController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
                executionReport.set(new ExecType(ExecType.REPLACED));
                executionReport.set(new OrdStatus(OrdStatus.REPLACED));
                executionReport.set(new LeavesQty(order.getOpenQuantity()));
                executionReport.set(new CumQty(order.getExecutedQuantity()));
                executionReport.set(new AvgPx(order.getAvgExecutedPrice()));
                executionReport.set(new OrigClOrdID(message.getString(OrigClOrdID.FIELD)));
                break;
        }
        return executionReport;
    }

    private SecurityStatus securityStatusFromMarket(Market market, int status) throws FieldNotFound {
        SecurityStatus securityStatus = new SecurityStatus();
        securityStatus.set(new Symbol(market.getSymbol()));
        securityStatus.set(new HighPx(market.getDayHigh()));
        securityStatus.set(new LowPx(market.getDayLow()));
        securityStatus.set(new LastPx(market.getLastPrice()));
        securityStatus.set(new BuyVolume(market.getBuyVolume()));
        securityStatus.set(new SellVolume(market.getSellVolume()));
        securityStatus.set(new SecurityTradingStatus(status));
        return securityStatus;
    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });
    }

    public Order orderFromNewOrderSingle(quickfix.fix42.NewOrderSingle newOrderSingle) throws FieldNotFound {
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
        Order order = new Order(
                System.currentTimeMillis(),
                newOrderSingle.getString(ClOrdID.FIELD),
                newOrderSingle.getString(Symbol.FIELD),
                newOrderSingle.getHeader().getString(SenderCompID.FIELD),
                newOrderSingle.getHeader().getString(TargetCompID.FIELD),
                newOrderSingle.getChar(Side.FIELD),
                newOrderSingle.getChar(OrdType.FIELD),
                price,
                (long) newOrderSingle.getDouble(OrderQty.FIELD),
                (long) newOrderSingle.getDouble(OrderQty.FIELD),
                0,
                0,
                0,
                0,
                false,
                false,
                LocalDate.now(),
                LocalDate.now(),
                newOrderSingle.getChar(TimeInForce.FIELD));
        if (newOrderSingle.getChar(TimeInForce.FIELD) == '0') {
            order.setGoodTillDate(LocalDate.now());
        } else if (newOrderSingle.getChar(TimeInForce.FIELD) == '6') {
            order.setGoodTillDate(newOrderSingle.getUtcDateOnly(ExpireDate.FIELD));
        }
        return order;
    }

    public MarketDataSnapshotFullRefresh.NoMDEntries noMDEntriesFromOrder(Order order) throws FieldNotFound {
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();

        noMDEntries.setChar(MDEntryType.FIELD, order.getSide());
        noMDEntries.setDouble(MDEntryPx.FIELD, order.getPrice());
        noMDEntries.setDouble(MDEntrySize.FIELD, order.getQuantity());
        noMDEntries.setUtcDateOnly(MDEntryDate.FIELD, order.getEntryDate());
        noMDEntries.setUtcTimeOnly(MDEntryTime.FIELD, LocalTime.now());
        noMDEntries.setDouble(CumQty.FIELD, order.getExecutedQuantity());
        noMDEntries.setChar(OrdType.FIELD, order.getOrdType());
        noMDEntries.setDouble(AvgPx.FIELD, order.getAvgExecutedPrice());
        noMDEntries.setDouble(LeavesQty.FIELD, order.getOpenQuantity());
        noMDEntries.setUtcDateOnly(ExpireDate.FIELD, order.getGoodTillDate());
        noMDEntries.setString(OrderID.FIELD, order.getClOrdID());
        noMDEntries.setString(Text.FIELD, "");
        noMDEntries.setString(Text.FIELD, "MDEntry for " + order.getSymbol());

        return noMDEntries;
    }

    public void sendSecurityStatusFromMarket(Market market, int status) throws FieldNotFound, SessionNotFound {
        for (SessionID sessionID : sessionIDs) {
            Session session = Session.lookupSession(sessionID);
            if (session != null && session.isLoggedOn()) {
                Session.sendToTarget(securityStatusFromMarket(market, status), sessionID.getSenderCompID(), sessionID.getTargetCompID());
            }
        }
    }
}
