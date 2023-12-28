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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderEntryApplication implements Application {
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final OrderTableModel orderTableModel;
    private final OrderTableModel executedOrdersTableModel;
    private final MarketTableModel marketTableModel;
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
    private SessionID sessionID = null;

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
        this.sessionID = sessionID;
    }

    public void onLogout(SessionID sessionID) {
        observableLogon.logoff(sessionID);
        orderTableModel.clearOrders();
        executedOrdersTableModel.clearOrders();
        marketTableModel.clearMarkets();
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
                    //Execution Report
                    if (message.getHeader().getField(msgType).valueEquals("8")) {
                        executionReport(message, sessionID);
                        //SECURITY_STATUS
                    } else if (message.getHeader().getField(msgType).valueEquals("f")) {
                        securityStatus(message, sessionID);
                        //MARKET_DATA_SNAPSHOT
                    } else if (message.getHeader().getField(msgType).valueEquals("W")) {
                        marketSnapshot(message, sessionID);
                    } else {
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

    public void sendNewOrderSingle(Order order, SessionID sessionID) throws SessionNotFound {
        NewOrderSingle newOrderSingle = new NewOrderSingle(new ClOrdID(order.getClOrdID()), new HandlInst('1'), new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.setOrderQty(order.getQuantity());

        if (order.getType() == OrderType.LIMIT) {
            newOrderSingle.setField(new Price(order.getLimit()));
        }
        if (order.getTIF() == OrderTIF.DAY) {
            newOrderSingle.setField(new TimeInForce('0'));
        } else if (order.getTIF() == OrderTIF.GTD) {
            newOrderSingle.setField(new TimeInForce('6'));
            newOrderSingle.setField(new ExpireDate(order.getGoodTillDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        } else if (order.getTIF() == OrderTIF.GTC) {
            newOrderSingle.setField(new TimeInForce('1'));
        }
        System.out.println(sessionID);
        Session.sendToTarget(newOrderSingle, sessionID);
    }

    public void sendOrderCancelRequest(Order order) throws SessionNotFound {
        OrderCancelRequest orderCancelRequest = new OrderCancelRequest(
                new OrigClOrdID(order.getClOrdID()),
                new ClOrdID(IDGenerator.genOrderID()),
                new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()),
                new TransactTime());
        orderCancelRequest.setField(new OrderQty(order.getQuantity()));

        Session.sendToTarget(orderCancelRequest, sessionID);
    }

    public void sendOrderCancelReplaceRequest(Order order, Order newOrder) throws SessionNotFound {
        OrderCancelReplaceRequest orderCancelReplaceRequest = new OrderCancelReplaceRequest(
                new OrigClOrdID(order.getClOrdID()),
                new ClOrdID(newOrder.getClOrdID()),
                new HandlInst('1'),
                new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()),
                new TransactTime(),
                typeToFIXType(order.getType()));
                orderCancelReplaceRequest.set(new OrderQty(newOrder.getQuantity()));
        Session.sendToTarget(orderCancelReplaceRequest, sessionID);
    }

    public void sendMarketDataRequest(Market market, SessionID sessionID) throws SessionNotFound {
        MarketDataRequest marketDataRequest = new MarketDataRequest(new MDReqID(IDGenerator.genMarketRequestID()), new SubscriptionRequestType('1'), new MarketDepth(1));
        MarketDataRequest.NoMDEntryTypes noMDEntryTypes = new MarketDataRequest.NoMDEntryTypes();
        noMDEntryTypes.set(new MDEntryType('0'));
        marketDataRequest.addGroup(noMDEntryTypes);
        noMDEntryTypes.set(new MDEntryType('1'));
        marketDataRequest.addGroup(noMDEntryTypes);
        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol(market.getSymbol()));
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
        if (message.getChar(OrdStatus.FIELD) == '4') {
            orderTableModel.removeOrder(message.getString(OrigClOrdID.FIELD));
            orderTableModel.refreshOrders();

        } else if (message.getChar(OrdStatus.FIELD) == '5') {
            Order order = orderTableModel.getOrder(message.getString(OrigClOrdID.FIELD));
            order.setExecutedQuantity(message.getDouble(CumQty.FIELD));
            order.setOpenQuantity(message.getDouble(LeavesQty.FIELD));
            order.setAvgPx(message.getDouble(AvgPx.FIELD));
            orderTableModel.replaceOrder(order);
            orderTableModel.refreshOrders();
        } else if (message.getChar(OrdStatus.FIELD) == '2' || message.getChar(OrdStatus.FIELD) == '1') {

            Order order = orderTableModel.getOrder(message.getString(ClOrdID.FIELD));
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

            orderTableModel.replaceOrder(order);
            observableOrder.update(order);
        }
    }

    private void marketSnapshot(Message message, SessionID sessionID) throws FieldNotFound {
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        int numEntries = message.getInt(NoMDEntries.FIELD);
        for (int i = 1; i <= numEntries; i++) {
            Order order = new Order();
            if (message.getGroup(i, noMDEntries) != null) {
                order = orderFromNoMDEntries(message.getGroup(i, noMDEntries));
            }
            order.setSymbol(message.getString(Symbol.FIELD));
            if (order.getExecutedQuantity() == order.getQuantity()) {
                executedOrdersTableModel.addOrder(order);
            } else {
                orderTableModel.addOrder(order);
            }
        }
    }

    private void securityStatus(Message message, SessionID sessionID) throws FieldNotFound, SessionNotFound {
        Market market = new Market(message.getString(Symbol.FIELD), message.getDouble(LastPx.FIELD), message.getDouble(HighPx.FIELD), message.getDouble(LowPx.FIELD), message.getDouble(BuyVolume.FIELD), message.getDouble(SellVolume.FIELD));
        if (message.getInt(SecurityTradingStatus.FIELD) == 0) {
            marketTableModel.addMarket(market);
            sendMarketDataRequest(market, sessionID);
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
        if (order.getType() == OrderType.LIMIT) {
            order.setLimit(noMDEntries.getDouble(MDEntryPx.FIELD));
        }
        order.setAvgPx(noMDEntries.getDouble(AvgPx.FIELD));
        order.setEntryDate(noMDEntries.getUtcDateOnly(MDEntryDate.FIELD));
        order.setGoodTillDate(noMDEntries.getUtcDateOnly(ExpireDate.FIELD));
        order.setClOrdID(noMDEntries.getString(OrderID.FIELD));
//TODO implement stop and limit
        return order;
    }
}