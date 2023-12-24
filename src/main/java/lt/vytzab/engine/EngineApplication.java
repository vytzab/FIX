package lt.vytzab.engine;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.market.Market;
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
import java.util.Observable;
import java.util.Observer;

import lt.vytzab.engine.helpers.CustomFixMessageParser;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.*;
import quickfix.fix42.Message;
import quickfix.MessageCracker;
import quickfix.fix42.SecurityStatus;

import static lt.vytzab.engine.Variables.MARKET_ORDERS_DB;

public class EngineApplication extends MessageCracker implements quickfix.Application {
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private OrderTableModel openOrderTableModel = null;
    private OrderTableModel allOrderTableModel = null;
    private final ObservableMarket observableMarket = new ObservableMarket();
    private final MarketController marketController = new MarketController();
    private final OrderIdGenerator generator = new OrderIdGenerator();
    private final LogPanel logPanel;
    private List<SessionID> sessionIDs = new ArrayList<>();

    public EngineApplication(OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel) {
        this.openOrderTableModel = openOrderTableModel;
        this.allOrderTableModel = allOrderTableModel;
        this.logPanel = logPanel;
    }

    public void onCreate(SessionID sessionId) {
    }

    public void onLogon(SessionID sessionId) {
        sessionIDs.add(sessionId);
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

    public void onMessage(quickfix.fix42.NewOrderSingle newOrderSingle, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
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

    private void processNewOrder(quickfix.fix42.NewOrderSingle newOrderSingle) throws FieldNotFound {
        Order order = orderFromNewOrderSingle(newOrderSingle);
        //If order is added
        if (marketController.insertOrder(order)) {
            //send accepted execution report
            messageExecutionReport(newOrderSingle, '0');

            openOrderTableModel.addOrder(order);
            allOrderTableModel.addOrder(order);

            ArrayList<Order> orders = new ArrayList<>();
            //try to match
            marketController.matchMarketOrders(marketController.getMarket(order.getSymbol()), orders);
            while (!orders.isEmpty()) {
                openOrderTableModel.replaceOrder(orders.get(0));
                orderExecutionReport(orders.get(0), orders.get(0).isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
                orders.remove(0);
            }
            // Remove fully executed orders from the OrderTableModel
            openOrderTableModel.removeFullyExecutedOrders();
        } else {
            messageExecutionReport(newOrderSingle, '8');
        }
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        Order order = marketController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD), MARKET_ORDERS_DB);
        if (order != null) {
            order.cancel();
            marketController.deleteOrderByClOrdID(message.getString(OrigClOrdID.FIELD), MARKET_ORDERS_DB);
//            cancelOrder(order);
        } else {
            OrderCancelReject orderCancelReject = new OrderCancelReject(new OrderID(generator.genOrderID()), new ClOrdID(message.getString(ClOrdID.FIELD)), new OrigClOrdID(message.getString(OrigClOrdID.FIELD)),
                    new OrdStatus(OrdStatus.REJECTED), new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));
            try {
                Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
            } catch (SessionNotFound e) {
                //TODO implement better logging
            }
        }
    }

    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();
        int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);

        MarketDataSnapshotFullRefresh fixMD = new MarketDataSnapshotFullRefresh();
        fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries;
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);


        message.getGroup(0, noRelatedSyms);
        String symbol = noRelatedSyms.getString(Symbol.FIELD);
        fixMD.setString(Symbol.FIELD, symbol);
        List<Order> symbolOrders = MarketOrderDAO.readAllMarketOrdersBySymbol(symbol, MARKET_ORDERS_DB);
        for (Order order : symbolOrders) {
            noMDEntries = noMDEntriesFromOrder(order);
            fixMD.addGroup(noMDEntries);
        }
        try {
            Session.sendToTarget(fixMD, targetCompId, senderCompId);
        } catch (SessionNotFound e) {
        }

    }

    public void onMessage(SecurityStatusRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        for (Market market : marketController.getMarkets()) {
            SecurityStatus securityStatus = securityStatusFromMarket(market, 0);
            try {
                Session.sendToTarget(securityStatus, message.getHeader().getString(quickfix.field.TargetCompID.FIELD), message.getHeader().getString(quickfix.field.SenderCompID.FIELD));
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
            Session.sendToTarget(executionReport, message.getHeader().getString(quickfix.field.TargetCompID.FIELD), message.getHeader().getString(quickfix.field.SenderCompID.FIELD));
        } catch (SessionNotFound e) {
            //TODO implement better logging
        }
    }

    private void orderExecutionReport(Order order, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(ordStatus), new OrdStatus(ordStatus),
                new Symbol(order.getSymbol()), new Side(order.getSide()), new LeavesQty(order.getOpenQuantity()), new CumQty(order.getExecutedQuantity()), new AvgPx(order.getAvgExecutedPrice()));
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
        ExecutionReport executionReport = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(ExecType.REJECTED),
                new OrdStatus(OrdStatus.REJECTED), new Symbol(message.getString(Symbol.FIELD)), new Side(message.getChar(Side.FIELD)), new LeavesQty(0), new CumQty(0), new AvgPx(0));
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
//                = new ExecutionReport(new OrderID(generator.genOrderID()), new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(ExecType.REJECTED), new OrdStatus(OrdStatus.REJECTED),
//                        new Symbol(message.getString(Symbol.FIELD)), new Side(message.getChar(Side.FIELD)), new LeavesQty(message.getDouble(LeavesQty.FIELD)), new CumQty(message.getDouble(CumQty.FIELD)), new AvgPx(message.getDouble(AvgPx.FIELD)));
                // TODO in process newOrder, setField(LeavesQty.FIELD), setField(CumQty.FIELD), setField(AvgPx.FIELD)
                break;
        }
        return executionReport;
    }

    private void OrderCancelRequestER(OrderCancelRequest message, char ordStatus) {
    }

    private void OrderCancelReplaceRequestER(OrderCancelReplaceRequest message, char ordStatus) {
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
        Order order = new Order(System.currentTimeMillis(), newOrderSingle.getString(ClOrdID.FIELD), newOrderSingle.getString(Symbol.FIELD), newOrderSingle.getHeader().getString(SenderCompID.FIELD), newOrderSingle.getHeader().getString(TargetCompID.FIELD),
                newOrderSingle.getChar(Side.FIELD), newOrderSingle.getChar(OrdType.FIELD), price, (long) newOrderSingle.getDouble(OrderQty.FIELD),
                (long) newOrderSingle.getDouble(OrderQty.FIELD), 0, 0, 0, 0,
                false, false, LocalDate.now(), LocalDate.now(), newOrderSingle.getChar(TimeInForce.FIELD));
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
        noMDEntries.setDouble(CumQty.FIELD, order.getExecutedQuantity());
        noMDEntries.setChar(OrdType.FIELD, order.getOrdType());
        noMDEntries.setDouble(MDEntrySize.FIELD, order.getQuantity());
        noMDEntries.setDouble(LeavesQty.FIELD, order.getOpenQuantity());
        noMDEntries.setUtcDateOnly(MDEntryDate.FIELD, order.getEntryDate());
        noMDEntries.setUtcTimeOnly(MDEntryTime.FIELD, LocalTime.now());
        noMDEntries.setUtcDateOnly(ExpireDate.FIELD, order.getGoodTillDate());
        noMDEntries.setString(OrderID.FIELD, order.getClOrdID());
        noMDEntries.setString(Text.FIELD, "");

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

    private static class ObservableMarket extends Observable {
        public void update(Order order) {
            setChanged();
            notifyObservers(order);
            clearChanged();
        }
    }

    public void addMarketObserver(Observer observer) {
        observableMarket.addObserver(observer);
    }

    public void deleteMarketObserver(Observer observer) {
        observableMarket.deleteObserver(observer);
    }

    public List<SessionID> getSessionIDs() {
        return sessionIDs;
    }
}
