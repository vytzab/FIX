package lt.vytzab.initiator;

import lt.vytzab.initiator.messages.NewOrderSingle;
import lt.vytzab.initiator.ui.LogPanel;
import lt.vytzab.utils.CustomFixMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.field.*;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class OrderEntryApplication implements Application {
    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private OrderTableModel orderTableModel = null;
    private ExecutionTableModel executionTableModel = null;
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

    public OrderEntryApplication(OrderTableModel orderTableModel, ExecutionTableModel executionTableModel, LogPanel logPanel) {
        this.orderTableModel = orderTableModel;
        this.executionTableModel = executionTableModel;
        this.logPanel = logPanel;
    }

    public void onCreate(SessionID sessionID) {
    }

    public void onLogon(SessionID sessionID) {
        observableLogon.logon(sessionID);
    }

    public void onLogout(SessionID sessionID) {
        observableLogon.logoff(sessionID);
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

    private void executionReport(Message message, SessionID sessionID) throws FieldNotFound {
        //Patikrinti ar jau apdirbta
        ExecID execID = (ExecID) message.getField(new ExecID());
        if (alreadyProcessed(execID, sessionID)) return;
        //Jeigu toks uzsakymas neegzistuoja sistemoje > ignore
        Order order = orderTableModel.getOrder(message.getString(ClOrdID.FIELD));
        if (order == null) {
            return;
        }

        double fillSize;

        if (message.isSetField(LastShares.FIELD)) {
            LastShares lastShares = new LastShares();
            message.getField(lastShares);
            fillSize = lastShares.getValue();
        } else {
            LeavesQty leavesQty = new LeavesQty();
            message.getField(leavesQty);
            fillSize = order.getQuantity() - leavesQty.getValue();
        }

        //Jeigu ivyko matchinimas, update order table
        if (fillSize > 0) {
            order.setOpen(order.getOpen() - (int) fillSize);
            order.setExecuted(Integer.parseInt(message.getString(CumQty.FIELD)));
            order.setAvgPx(Double.parseDouble(message.getString(AvgPx.FIELD)));
        }

        OrdStatus ordStatus = (OrdStatus) message.getField(new OrdStatus());

        if (ordStatus.valueEquals(OrdStatus.REJECTED)) {
            order.setRejected(true);
            order.setOpen(0);
        } else if (ordStatus.valueEquals(OrdStatus.CANCELED) || ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY)) {
            order.setCanceled(true);
            order.setOpen(0);
        } else if (ordStatus.valueEquals(OrdStatus.NEW)) {
            if (order.isNew()) {
                order.setNew(false);
            }
        }

        try {
            order.setMessage(message.getField(new Text()).getValue());
        } catch (FieldNotFound e) {
            System.out.println("Text field not found.");
            //TODO implement better logging
        }

        orderTableModel.updateOrder(order, message.getString(ClOrdID.FIELD));
        observableOrder.update(order);

        //Jeigu ivyko matchinimas, update execution table
        if (fillSize > 0) {
            Execution execution = new Execution();
            execution.setExchangeID(sessionID + message.getString(ExecID.FIELD));
            execution.setSymbol(message.getString(Symbol.FIELD));
            execution.setQuantity((int) (fillSize));
            if (message.isSetField(LastPx.FIELD)) {
                execution.setPrice(Double.parseDouble(message.getString(LastPx.FIELD)));
            }
            Side side = (Side) message.getField(new Side());
            execution.setSide(FIXSideToSide(side));
            executionTableModel.addExecution(execution);
        }
    }

    private void cancelReject(Message message, SessionID sessionID) throws FieldNotFound {
        String id = message.getString(ClOrdID.FIELD);
        Order order = orderTableModel.getOrder(id);
        if (order == null) return;
        if (order.getOriginalID() != null) order = orderTableModel.getOrder(order.getOriginalID());

        try {
            order.setMessage(message.getField(new Text()).getValue());
        } catch (FieldNotFound e) {
            throw new RuntimeException(e);
        }
        orderTableModel.updateOrder(order, id);
    }

    private boolean alreadyProcessed(ExecID execID, SessionID sessionID) {
        HashSet<ExecID> set = execIDs.get(sessionID);
        if (set == null) {
            set = new HashSet<>();
            set.add(execID);
            execIDs.put(sessionID, set);
            return false;
        } else {
            if (set.contains(execID)) return true;
            set.add(execID);
            return false;
        }
    }

    public void sendNewOrderSingle(Order order) throws SessionNotFound {
        NewOrderSingle newOrderSingle = new NewOrderSingle(new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()), sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));
        newOrderSingle.setOrderQty(order.getQuantity());
//        newOrderSingle.setField(new OrigClOrdID(order.generateID()));

        if (order.getType() == OrderType.LIMIT) {
            newOrderSingle.setField(new Price(order.getLimit()));
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
        sideMap.put(OrderSide.SHORT_SELL, new Side(Side.SELL_SHORT));
        sideMap.put(OrderSide.SHORT_SELL_EXEMPT, new Side(Side.SELL_SHORT_EXEMPT));
        sideMap.put(OrderSide.CROSS, new Side(Side.CROSS));
        sideMap.put(OrderSide.CROSS_SHORT, new Side(Side.CROSS_SHORT));

        typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));
        typeMap.put(OrderType.STOP, new OrdType(OrdType.STOP_STOP_LOSS));
        typeMap.put(OrderType.STOP_LIMIT, new OrdType(OrdType.STOP_LIMIT));

        tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
        tifMap.put(OrderTIF.IOC, new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL));
        tifMap.put(OrderTIF.OPG, new TimeInForce(TimeInForce.AT_THE_OPENING));
        tifMap.put(OrderTIF.GTC, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        tifMap.put(OrderTIF.GTX, new TimeInForce(TimeInForce.GOOD_TILL_CROSSING));
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
}