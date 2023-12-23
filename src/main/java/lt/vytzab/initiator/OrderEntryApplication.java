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
import quickfix.field.*;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.MarketDataSnapshotFullRefresh;
import quickfix.fix42.SecurityStatusRequest;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class OrderEntryApplication implements Application {
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private OrderTableModel orderTableModel = null;
    private OrderTableModel executedOrdersTableModel = null;
    private MarketTableModel marketTableModel = null;
    private final ObservableOrder observableOrder = new ObservableOrder();
    private final ObservableLogon observableLogon = new ObservableLogon();
    private final LogPanel logPanel;
    private boolean isAvailable = true;
    private boolean isMissingField;
    //Pirkimo / pardavimo puses konvertavimui is FIX i object
    static private final TwoWayMap sideMap = new TwoWayMap();
    //Tipo konvertavimui is FIX i object
    static private final TwoWayMap typeMap = new TwoWayMap();
    //TIF konvertavimui is FIX i object
    static private final TwoWayMap tifMap = new TwoWayMap();
    //Tikrinimui ar zinute su tokiu ExecutionID jau processed.
    static private final HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(OrderEntryApplication.class);

    public OrderEntryApplication(MarketTableModel marketTableModel, OrderTableModel orderTableModel, OrderTableModel executedOrdersTableModel, LogPanel logPanel) {
        this.marketTableModel = marketTableModel;
        this.orderTableModel = orderTableModel;
        this.executedOrdersTableModel = executedOrdersTableModel;
        this.logPanel = logPanel;
    }

    public void onCreate(SessionID sessionID) {
    }

    public void onLogon(SessionID sessionID) {
        observableLogon.logon(sessionID);
        try {
            sendSecurityStatusRequest(sessionID);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

    public void onLogout(SessionID sessionID) {
        observableLogon.logoff(sessionID);
        marketTableModel.cleanUp();
    }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("toAdmin:" + message.toString());
        logger.info("toAdmin:" + message);
    }

    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
        displayFixMessageInLogs("toApp:" + message.toString());
        logger.info("toApp:" + message);
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        displayFixMessageInLogs("fromAdmin:" + message.toString());
        logger.info("fromAdmin:" + message);
    }

    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        displayFixMessageInLogs("fromApp:" + message.toString());
        logger.info("fromApp:" + message);
        try {
            SwingUtilities.invokeLater(new MessageProcessor(message, sessionID));
        } catch (Exception e) {
        }
    }

    //Zinuciu gautu is variklio apdirbimui
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
                if (isAvailable) {
                    //Jeigu gautas execution report msgType = 8
                    if (message.getHeader().getField(msgType).valueEquals("8")) {
                        executionReport(message, sessionID);
                        //Jeigu gautas cancel reject report msgType = 8
                    } else if (message.getHeader().getField(msgType).valueEquals("9")) {
                        cancelReject(message, sessionID);
                    } else if (message.getHeader().getField(msgType).valueEquals("W")) {
                        marketSnapshot(message, sessionID);
                    } else if (message.getHeader().getField(msgType).valueEquals("f")) {
                        securityStatus(message, sessionID);
                    }  else {
                        sendBusinessReject(message, BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE, "Unsupported Message Type");
                    }
                } else {
                    sendBusinessReject(message, BusinessRejectReason.APPLICATION_NOT_AVAILABLE, "Application not available");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSessionReject(Message message, int rejectReason) throws FieldNotFound, SessionNotFound {
        Message reply = (messageFactory.create(message.getHeader().getString(BeginString.FIELD), MsgType.REJECT));
        reply.getHeader().setString(SenderCompID.FIELD, message.getHeader().getString(TargetCompID.FIELD));
        reply.getHeader().setString(TargetCompID.FIELD, message.getHeader().getString(SenderCompID.FIELD));
        String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        reply.setInt(SessionRejectReason.FIELD, rejectReason);
        Session.sendToTarget(reply);
    }

    private void sendBusinessReject(Message message, int rejectReason, String rejectText) throws FieldNotFound, SessionNotFound {
        Message reply = (messageFactory.create(message.getHeader().getString(BeginString.FIELD), MsgType.BUSINESS_MESSAGE_REJECT));
        reply.getHeader().setString(SenderCompID.FIELD, message.getHeader().getString(TargetCompID.FIELD));
        reply.getHeader().setString(TargetCompID.FIELD, message.getHeader().getString(SenderCompID.FIELD));
        String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        reply.setInt(BusinessRejectReason.FIELD, rejectReason);
        reply.setString(Text.FIELD, rejectText);
        Session.sendToTarget(reply);
    }

    public void sendNewOrderSingle(Order order) throws SessionNotFound {
        NewOrderSingle newOrderSingle = new NewOrderSingle(new ClOrdID(order.getOrderID()), new HandlInst('1'), new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.setOrderQty(order.getQuantity());

        if (order.getType() == OrderType.LIMIT) {
            newOrderSingle.setField(new Price(order.getLimit()));
        }
        if (order.getTIF() == OrderTIF.DAY) {
            newOrderSingle.setField(new TimeInForce('0'));
        } else if (order.getTIF() == OrderTIF.GTD) {
            newOrderSingle.setField(new TimeInForce('6'));
            newOrderSingle.setField(new ExpireDate(order.getGoodTillDate().toString()));
        }else if (order.getTIF() == OrderTIF.GTC) {
            newOrderSingle.setField(new TimeInForce('1'));
        }
        Session.sendToTarget(newOrderSingle, order.getSessionID());
    }

    public void sendOrderCancelRequest(Order order) throws SessionNotFound {
//        String id = order.generateID();
//        OrderCancelRequest orderCancelRequest = new OrderCancelRequest(new OrigClOrdID(order.getID()), new ClOrdID(id), new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), new TransactTime());
//        orderCancelRequest.setField(new OrderQty(order.getQuantity()));
//
//        orderTableModel.addID(order, id);
//        Session.sendToTarget(orderCancelRequest, order.getSessionID());
    }

    public void sendOrderCancelReplaceRequest(Order order, Order newOrder) throws SessionNotFound {
//        OrderCancelReplaceRequest orderCancelReplaceRequest = new OrderCancelReplaceRequest(new OrigClOrdID(order.getID()), new ClOrdID(newOrder.getID()), new HandlInst('1'), new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
//
//        orderTableModel.addID(order, newOrder.getID());
//        if (order.getQuantity() != newOrder.getQuantity())
//            orderCancelReplaceRequest.setField(new OrderQty(newOrder.getQuantity()));
//        if (!order.getLimit().equals(newOrder.getLimit()))
//            orderCancelReplaceRequest.setField(new Price(newOrder.getLimit()));
//        Session.sendToTarget(orderCancelReplaceRequest, order.getSessionID());
    }

    public void sendMarketDataRequest(SessionID sessionID) throws SessionNotFound {
        MarketDataRequest marketDataRequest = new MarketDataRequest(new MDReqID(IDGenerator.genMarketRequestID()), new SubscriptionRequestType('1'), new MarketDepth(1));
        MarketDataRequest.NoMDEntryTypes noMDEntryTypes = new MarketDataRequest.NoMDEntryTypes();
        noMDEntryTypes.set(new MDEntryType('0'));
        marketDataRequest.addGroup(noMDEntryTypes);
        noMDEntryTypes.set(new MDEntryType('1'));
        marketDataRequest.addGroup(noMDEntryTypes);
        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol("AAPL"));
        marketDataRequest.addGroup(noRelatedSym);

        Session.sendToTarget(marketDataRequest, sessionID);
    }

    public void sendSecurityStatusRequest(SessionID sessionID) throws SessionNotFound {
        SecurityStatusRequest securityStatusRequest = new SecurityStatusRequest(new SecurityStatusReqID(IDGenerator.genOrderID()), new Symbol("AAPL"), new SubscriptionRequestType('1'));

        Session.sendToTarget(securityStatusRequest, sessionID);
    }

    private void executionReport(Message message, SessionID sessionID) throws FieldNotFound {
        //Patikrinti ar jau apdirbta
        ExecID execID = (ExecID) message.getField(new ExecID());
        if (alreadyProcessed(execID, sessionID)) {
            return;
        }
        //Jeigu toks uzsakymas neegzistuoja sistemoje > ignore
        Order order = orderTableModel.getOrder(message.getString(ClOrdID.FIELD));
        if (order == null) {
            return;
        } else {
            double fillSize;

            // Ar buvo matchinta?
            LeavesQty leavesQty = new LeavesQty();
            message.getField(leavesQty);
            fillSize = order.getQuantity() - leavesQty.getValue();

            //Jeigu ivyko matchinimas, update order table
            if (fillSize > 0) {
                order.setOpenQuantity(order.getOpenQuantity() - (int) fillSize);
                order.setExecutedQuantity(Integer.parseInt(message.getString(CumQty.FIELD)));
                order.setAvgPx(Double.parseDouble(message.getString(AvgPx.FIELD)));
                executedOrdersTableModel.addOrder(order);
            }

            OrdStatus ordStatus = (OrdStatus) message.getField(new OrdStatus());

            if (ordStatus.valueEquals(OrdStatus.REJECTED)) {
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

            orderTableModel.updateOrder(order, message.getString(ClOrdID.FIELD));
            observableOrder.update(order);
        }
    }

    private void cancelReject(Message message, SessionID sessionID) throws FieldNotFound {
        String id = message.getString(ClOrdID.FIELD);
        Order order = orderTableModel.getOrder(id);
        if (order == null) return;
        if (order.getClOrdID() != null) order = orderTableModel.getOrder(order.getClOrdID());

        try {
            order.setMessage(message.getField(new Text()).getValue());
        } catch (FieldNotFound e) {
            throw new RuntimeException(e);
        }
        orderTableModel.updateOrder(order, message.getField(new OrigClOrdID()).getValue());
    }

    private void marketSnapshot(Message message, SessionID sessionID) throws FieldNotFound {
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        Order order = orderFromNoMDEntries(message.getGroup(1, noMDEntries));
        order.setSymbol(message.getString(Symbol.FIELD));
        orderTableModel.addOrder(order);
    }

    private void securityStatus(Message message, SessionID sessionID) throws FieldNotFound {
        Market market = new Market(message.getString(Symbol.FIELD), message.getDouble(LastPx.FIELD), message.getDouble(HighPx.FIELD), message.getDouble(LowPx.FIELD), message.getDouble(BuyVolume.FIELD), message.getDouble(SellVolume.FIELD));
        if (message.getInt(SecurityTradingStatus.FIELD) == 0) {
            marketTableModel.addMarket(market);
        } else if (message.getInt(SecurityTradingStatus.FIELD) == 1) {
            marketTableModel.replaceMarket(market, market.getSymbol());
        } else {
            marketTableModel.removeMarket(market.getSymbol());
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

    public Side sideToFIXSide(OrderSide side) {
        return (Side) sideMap.getFirst(side);
    }

    public OrderSide FIXSideToSide(Side side) {
        return (OrderSide) sideMap.getSecond(side);
    }

    public OrdType typeToFIXType(OrderType type) {
        return (OrdType) typeMap.getFirst(type);
    }

    public OrderType FIXTypeToType(OrdType type) {
        return (OrderType) typeMap.getSecond(type);
    }

    public TimeInForce tifToFIXTif(OrderTIF tif) {
        return (TimeInForce) tifMap.getFirst(tif);
    }

    public OrderTIF FIXTifToTif(TimeInForce tif) {
        return (OrderTIF) typeMap.getSecond(tif);
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

    public boolean isMissingField() {
        return isMissingField;
    }

    public void setMissingField(boolean isMissingField) {
        this.isMissingField = isMissingField;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });

    }
    public Order orderFromNoMDEntries(Group noMDEntries) throws FieldNotFound {
        Order order = new Order();
        order.setQuantity(noMDEntries.getDouble(MDEntrySize.FIELD));
        order.setOpenQuantity(noMDEntries.getDouble(LeavesQty.FIELD));
        order.setExecutedQuantity(noMDEntries.getDouble(CumQty.FIELD));
        order.setSide(FIXSideToSide(new Side(noMDEntries.getChar(MDEntryType.FIELD))));
        order.setType(FIXTypeToType(new OrdType(noMDEntries.getChar(OrdType.FIELD))));
        order.setLimit(noMDEntries.getDouble(MDEntryPx.FIELD));
        order.setStop(noMDEntries.getDouble(MDEntryPx.FIELD));
        order.setAvgPx(noMDEntries.getDouble(AvgPx.FIELD));
        order.setEntryDate(noMDEntries.getUtcDateOnly(MDEntryDate.FIELD));
        order.setGoodTillDate(noMDEntries.getUtcDateOnly(ExpireDate.FIELD));
        order.setOrderID(noMDEntries.getString(OrderID.FIELD));
//TODO implement stop and limit
        return order;
    }
}