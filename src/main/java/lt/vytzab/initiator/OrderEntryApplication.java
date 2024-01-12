package lt.vytzab.initiator;

import lt.vytzab.initiator.helpers.IDGenerator;
import lt.vytzab.initiator.helpers.LogonEvent;
import lt.vytzab.initiator.helpers.TwoWayMap;
import lt.vytzab.initiator.market.Market;
import lt.vytzab.initiator.market.MarketTableModel;
import lt.vytzab.initiator.messages.NewOrderSingle;
import lt.vytzab.initiator.order.*;
import lt.vytzab.initiator.ui.panels.LogPanel;
import lt.vytzab.initiator.helpers.CustomFixMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.*;

import javax.swing.*;
import java.util.*;

public class OrderEntryApplication implements Application {
    private static final Logger ordEntAppLogger = LoggerFactory.getLogger(OrderEntryApplication.class);
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final OrderTableModel orderTableModel;
    private final OrderTableModel executedOrdersTableModel;
    private final MarketTableModel marketTableModel;
    private final ObservableOrder observableOrder = new ObservableOrder();
    private final ObservableLogon observableLogon = new ObservableLogon();
    private final LogPanel logPanel;
    private static final TwoWayMap sideMap = new TwoWayMap();
    private static final TwoWayMap typeMap = new TwoWayMap();
    private static final TwoWayMap tifMap = new TwoWayMap();
    private static final HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<>();
    private SessionID sessionID = null;
    private final IDGenerator idGenerator;

    public OrderEntryApplication(MarketTableModel marketTableModel, OrderTableModel orderTableModel, OrderTableModel executedOrdersTableModel, LogPanel logPanel, IDGenerator idGenerator) {
        this.marketTableModel = marketTableModel;
        this.orderTableModel = orderTableModel;
        this.executedOrdersTableModel = executedOrdersTableModel;
        this.logPanel = logPanel;
        this.idGenerator = idGenerator;
    }

    public void onCreate(SessionID sessionID) {
    }

    public void onLogon(SessionID sessionID) {
        observableLogon.logon(sessionID);
        this.sessionID = sessionID;
        this.idGenerator.setSenderCompID(sessionID.getSenderCompID());
    }

    public void onLogout(SessionID sessionID) {
        observableLogon.logoff(sessionID);
        orderTableModel.clearOrders();
        executedOrdersTableModel.clearOrders();
        marketTableModel.clearMarkets();
    }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("toAdmin:" + message.toString());
        ordEntAppLogger.info("toAdmin:" + message);
    }

    public void toApp(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("toApp:" + message.toString());
        ordEntAppLogger.info("toApp:" + message);
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("fromAdmin:" + message.toString());
        ordEntAppLogger.info("fromAdmin:" + message);
    }

    public void fromApp(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("fromApp:" + message.toString());
        ordEntAppLogger.info("fromApp:" + message);

        SwingUtilities.invokeLater(new MessageProcessor(message, sessionID));
    }

    public class MessageProcessor implements Runnable {
        private final quickfix.Message message;
        private final SessionID sessionID;

        public MessageProcessor(quickfix.Message message, SessionID sessionID) {
            this.message = message;
            this.sessionID = sessionID;
        }

        public void run() {
            try {
                MsgType msgType = new MsgType();
                //Execution Report
                if (message.getHeader().getField(msgType).valueEquals("8")) {
                    executionReport(message, sessionID);
                    //SECURITY_STATUS
                } else if (message.getHeader().getField(msgType).valueEquals("f")) {
                    securityStatus(message);
                    //MARKET_DATA_SNAPSHOT
                } else if (message.getHeader().getField(msgType).valueEquals("W")) {
                    marketSnapshot(message);
                } else {
                    sendBusinessReject(message);
                }
            } catch (FieldNotFound e) {
                ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " MsgType is missing from the received message.");
            }
        }
    }

    private void sendSessionReject(Message message, int rejectReason) throws FieldNotFound {
        Message reply = null;
        try {
            reply = (messageFactory.create(message.getHeader().getString(BeginString.FIELD), MsgType.REJECT));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting BeginString field from message.");
        }
        try {
            assert reply != null;
            reply.getHeader().setString(SenderCompID.FIELD, message.getHeader().getString(TargetCompID.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting TargetCompID field from message.");
        }
        try {
            reply.getHeader().setString(TargetCompID.FIELD, message.getHeader().getString(SenderCompID.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting SenderCompID field from message.");
        }
        String refSeqNum = null;
        try {
            refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MsgSeqNum field from message.");
        }
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        try {
            reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MsgType field from message.");
        }
        reply.setInt(SessionRejectReason.FIELD, rejectReason);
        try {
            Session.sendToTarget(reply);
        } catch (SessionNotFound e) {
            ordEntAppLogger.error("Session not found while trying to send session reject to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
        }
    }

    private void sendBusinessReject(Message message) throws FieldNotFound {
        Message reply = null;
        try {
            reply = (messageFactory.create(message.getHeader().getString(BeginString.FIELD), MsgType.BUSINESS_MESSAGE_REJECT));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting BeginString field from message.");
        }
        try {
            assert reply != null;
            reply.getHeader().setString(SenderCompID.FIELD, message.getHeader().getString(TargetCompID.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting TargetCompID field from message.");
        }
        try {
            reply.getHeader().setString(TargetCompID.FIELD, message.getHeader().getString(SenderCompID.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting SenderCompID field from message.");
        }
        String refSeqNum = null;
        try {
            refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MsgSeqNum field from message.");
        }
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        try {
            reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MsgType field from message.");
        }
        reply.setInt(BusinessRejectReason.FIELD, BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE);
        reply.setString(Text.FIELD, "Unsupported Message Type");
        try {
            Session.sendToTarget(reply);
        } catch (SessionNotFound e) {
            ordEntAppLogger.error("Session not found while trying to send business reject to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
        }
    }

    public void sendNewOrderSingle(Order order, SessionID sessionID) {
        NewOrderSingle newOrderSingle = new NewOrderSingle(
                new ClOrdID(order.getClOrdID()),
                new HandlInst('1'),
                new Symbol(order.getSymbol()),
                new Side(order.getSide()),
                new TransactTime(),
                new OrdType(order.getType()));
        newOrderSingle.setOrderQty(order.getQuantity());
        newOrderSingle.setField(new TimeInForce(order.getTif()));
        if (order.getType() == '2') {
            newOrderSingle.setField(new Price(order.getLimit()));
        }
        newOrderSingle.setField(new ExpireDate(order.getGoodTillDate().toString()));

        try {
            Session.sendToTarget(newOrderSingle, sessionID);
        } catch (SessionNotFound e) {
            ordEntAppLogger.error("Session not found while trying to send new order single to " + sessionID.getTargetCompID() + " from " + sessionID.getSenderCompID());
        }
    }

    public void sendOrderCancelRequest(Order order) {
        OrderCancelRequest orderCancelRequest = new OrderCancelRequest(
                new OrigClOrdID(order.getClOrdID()),
                new ClOrdID(idGenerator.genOrderID()),
                new Symbol(order.getSymbol()),
                new Side(order.getSide()),
                new TransactTime());
        orderCancelRequest.setField(new OrderQty(order.getQuantity()));

        try {
            Session.sendToTarget(orderCancelRequest, sessionID);
        } catch (SessionNotFound e) {
            ordEntAppLogger.error("Session not found while trying to send order cancel request to " + sessionID.getTargetCompID() + " from " + sessionID.getSenderCompID());
        }
    }

    public void sendOrderCancelReplaceRequest(Order order, double newQuantity) {
        OrderCancelReplaceRequest orderCancelReplaceRequest = new OrderCancelReplaceRequest(
                new OrigClOrdID(order.getClOrdID()),
                new ClOrdID(idGenerator.genOrderID()),
                new HandlInst('1'),
                new Symbol(order.getSymbol()),
                new Side(order.getSide()),
                new TransactTime(),
                new OrdType(order.getType()));
        orderCancelReplaceRequest.set(new OrderQty(newQuantity));
        try {
            Session.sendToTarget(orderCancelReplaceRequest, sessionID);
        } catch (SessionNotFound e) {
            ordEntAppLogger.error("Session not found while trying to send order cancel replace request to " + sessionID.getTargetCompID() + " from " + sessionID.getSenderCompID());
        }
    }

    public void sendMarketDataRequest(Market market) {
        MarketDataRequest marketDataRequest = new MarketDataRequest(
                new MDReqID(idGenerator.genMarketRequestID()),
                new SubscriptionRequestType('1'),
                new MarketDepth(1));
        MarketDataRequest.NoMDEntryTypes noMDEntryTypes = new MarketDataRequest.NoMDEntryTypes();
        noMDEntryTypes.set(new MDEntryType('0'));
        marketDataRequest.addGroup(noMDEntryTypes);
        noMDEntryTypes.set(new MDEntryType('1'));
        marketDataRequest.addGroup(noMDEntryTypes);
        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol(market.getSymbol()));
        marketDataRequest.addGroup(noRelatedSym);

        try {
            Session.sendToTarget(marketDataRequest, sessionID);
        } catch (SessionNotFound e) {
            ordEntAppLogger.error("Session not found while trying to send market data request to " + sessionID.getTargetCompID() + " from " + sessionID.getSenderCompID());
        }
    }

    private void executionReport(Message message, SessionID sessionID) {
        ExecID execID = null;
        try {
            execID = (ExecID) message.getField(new ExecID());
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting ExecID field from message.");
        }
        if (alreadyProcessed(execID, sessionID)) {
            return;
        }
        try {
            if (message.getChar(OrdStatus.FIELD) == '4') {
                Order order = orderTableModel.getOrder(message.getString(OrigClOrdID.FIELD));
                order.setOpenQuantity(0);
                order.setCanceled(true);
                orderTableModel.replaceOrder(order);
                orderTableModel.refreshOrders();
            } else if (message.getChar(OrdStatus.FIELD) == '8') {
                Order order = orderTableModel.getOrder(message.getString(ClOrdID.FIELD));
                order.setOpenQuantity(0);
                order.setRejected(true);
                orderTableModel.replaceOrder(order);
                orderTableModel.refreshOrders();
            } else if (message.getChar(OrdStatus.FIELD) == '5') {
                Order order = orderTableModel.getOrder(message.getString(OrigClOrdID.FIELD));
                order.setExecutedQuantity((long) message.getDouble(CumQty.FIELD));
                order.setOpenQuantity((long) message.getDouble(LeavesQty.FIELD));
                order.setAvgExecutedPrice(message.getDouble(AvgPx.FIELD));
                orderTableModel.replaceOrder(order);
                orderTableModel.refreshOrders();
            } else if (message.getChar(OrdStatus.FIELD) == '2' || message.getChar(OrdStatus.FIELD) == '1') {
                Order order = orderTableModel.getOrder(message.getString(ClOrdID.FIELD));
                order.setOpenQuantity(Long.parseLong(message.getString(LeavesQty.FIELD)));
                order.setExecutedQuantity(Long.parseLong(message.getString(CumQty.FIELD)));
                order.setAvgExecutedPrice(Double.parseDouble(message.getString(AvgPx.FIELD)));
                OrdStatus ordStatus = (OrdStatus) message.getField(new OrdStatus());
                if (ordStatus.valueEquals(OrdStatus.PARTIALLY_FILLED)) {
                    executedOrdersTableModel.addOrder(order);
                } else if (ordStatus.valueEquals(OrdStatus.FILLED)) {
                    executedOrdersTableModel.addOrder(order);
                    orderTableModel.removeOrder(order.getClOrdID());
                } else if (ordStatus.valueEquals(OrdStatus.REJECTED)) {
                    order.setRejected(true);
                    order.setOpenQuantity(0);
                } else if (ordStatus.valueEquals(OrdStatus.CANCELED) || ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY)) {
                    order.setCanceled(true);
                    order.setOpenQuantity(0);
                } else if (ordStatus.valueEquals(OrdStatus.NEW)) {
                    if (order.isNew()) {
                        order.setNew(false);
                    }
                }

                orderTableModel.replaceOrder(order);
                orderTableModel.refreshOrders();
            }
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an Order object from an execution report.");
        }
    }

    private void marketSnapshot(Message message) {
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        int numEntries = 0;
        try {
            numEntries = message.getInt(NoMDEntries.FIELD);
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting NoMDEntries field from message.");
        }
        for (int i = 1; i <= numEntries; i++) {
            Order order = new Order();
            try {
                if (message.getGroup(i, noMDEntries) != null) {
                    order = orderFromNoMDEntries(message.getGroup(i, noMDEntries));
                }
            } catch (FieldNotFound e) {
                ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting noMDEntries group from message.");
            }
            try {
                order.setSymbol(message.getString(Symbol.FIELD));
            } catch (FieldNotFound e) {
                ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting Symbol field from message.");
            }
            if (order.getExecutedQuantity() == order.getQuantity()) {
                executedOrdersTableModel.addOrder(order);
            } else {
                orderTableModel.addOrder(order);
            }
        }
    }

    private void securityStatus(Message message) {
        Market market = null;
        try {
            market = new Market(
                    message.getString(Symbol.FIELD),
                    message.getDouble(LastPx.FIELD),
                    message.getDouble(HighPx.FIELD),
                    message.getDouble(LowPx.FIELD),
                    message.getDouble(BuyVolume.FIELD),
                    message.getDouble(SellVolume.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to create a new Market object from message for a security status.");
        }
        try {
            if (message.getInt(SecurityTradingStatus.FIELD) == 0) {
                marketTableModel.addMarket(market);
                assert market != null;
                sendMarketDataRequest(market);
            } else if (message.getInt(SecurityTradingStatus.FIELD) == 1) {
                marketTableModel.replaceMarket(market);
            } else {
                assert market != null;
                marketTableModel.removeMarket(market.getSymbol());
            }
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting SecurityTradingStatus field from message.");
        }
    }

    private boolean alreadyProcessed(ExecID execID, SessionID sessionID) {
        HashSet<ExecID> set = execIDs.get(sessionID);
        if (set == null) {
            set = new HashSet<>();
            set.add(execID);
            execIDs.put(sessionID, set);
            return false;
        } else {
            if (set.contains(execID)) {
                return true;
            } else {
                set.add(execID);
                return false;
            }
        }
    }

    public void addLogonObserver(Observer observer) {
        observableLogon.addObserver(observer);
    }

    public void deleteLogonObserver(Observer observer) {
        observableLogon.deleteObserver(observer);
    }

    public void addOrderObserver(Observer observer) {
        observableOrder.addObserver(observer);
    }

    public void deleteOrderObserver(Observer observer) {
        observableOrder.deleteObserver(observer);
    }

    private static class ObservableOrder extends Observable {
        public void update(Order order) {
            setChanged();
            notifyObservers(order);
            clearChanged();
        }
    }

    private static class ObservableLogon extends Observable {
        public void logon(SessionID sessionID) {
            setChanged();
            notifyObservers(new LogonEvent(sessionID, true));
            clearChanged();
        }

        public void logoff(SessionID sessionID) {
            setChanged();
            notifyObservers(new LogonEvent(sessionID, false));
            clearChanged();
        }
    }

    static {
        sideMap.put(OrderSide.BUY, new Side(Side.BUY));
        sideMap.put(OrderSide.SELL, new Side(Side.SELL));

        typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));

        tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
        tifMap.put(OrderTIF.GTC, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        tifMap.put(OrderTIF.GTD, new TimeInForce(TimeInForce.GOOD_TILL_DATE));
    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });

    }

    public Order orderFromNoMDEntries(Group noMDEntries) {
        Order order = new Order();
        try {
            order.setQuantity((long) noMDEntries.getDouble(MDEntrySize.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MDEntrySize field from noMDEntries.");
        }
        try {
            order.setOpenQuantity((long) noMDEntries.getDouble(LeavesQty.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting LeavesQty field from noMDEntries.");
        }
        try {
            order.setExecutedQuantity((long) noMDEntries.getDouble(CumQty.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting CumQty field from noMDEntries.");
        }
        try {
            order.setSide(noMDEntries.getChar(MDEntryType.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MDEntryType field from noMDEntries.");
        }
        try {
            order.setType(noMDEntries.getChar(OrdType.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting OrdType field from noMDEntries.");
        }
        if (order.getType() == '2') {
            try {
                order.setLimit(noMDEntries.getDouble(MDEntryPx.FIELD));
            } catch (FieldNotFound e) {
                ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MDEntryPx field from noMDEntries.");
            }
        }
        try {
            order.setAvgExecutedPrice(noMDEntries.getDouble(AvgPx.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting AvgPx field from noMDEntries.");
        }
        try {
            order.setEntryDate(noMDEntries.getUtcDateOnly(MDEntryDate.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MDEntryDate field from noMDEntries.");
        }
        try {
            order.setGoodTillDate(noMDEntries.getUtcDateOnly(ExpireDate.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting ExpireDate field from noMDEntries.");
        }
        try {
            order.setClOrdID(noMDEntries.getString(OrderID.FIELD));
        } catch (FieldNotFound e) {
            ordEntAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting OrderID field from noMDEntries.");
        }
        return order;
    }

    public IDGenerator getIdGenerator() {
        return idGenerator;
    }
}