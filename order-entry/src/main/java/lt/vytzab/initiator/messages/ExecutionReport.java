package lt.vytzab.initiator.messages;


import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class ExecutionReport extends Message {
    public static final String MSGTYPE = "8";

    // Constructor without parameters
    public ExecutionReport() {
        this.getHeader().setField(new MsgType("8"));
    }

    // Constructor with mandatory field parameters
    public ExecutionReport(OrderID orderID, ExecID execID, ExecTransType execTransType, ExecType execType, OrdStatus ordStatus, Symbol symbol, Side side, LeavesQty leavesQty, CumQty cumQty, AvgPx avgPx) {
        this();
        this.setField(orderID);
        this.setField(execID);
        this.setField(execTransType);
        this.setField(execType);
        this.setField(ordStatus);
        this.setField(symbol);
        this.setField(side);
        this.setField(leavesQty);
        this.setField(cumQty);
        this.setField(avgPx);
    }
    // Mandatory
    public void setOrderID(String orderID) {
        setField(new OrderID(orderID));
    }

    public void set(OrderID value) {
        this.setField(value);
    }

    public OrderID get(OrderID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public OrderID getOrderID() throws FieldNotFound {
        return this.get(new OrderID());
    }

    public boolean isSet(OrderID field) {
        return this.isSetField(field);
    }

    public boolean isSetOrderID() {
        return this.isSetField(37);
    }

    public void setExecID(String execID) {
        setField(new ExecID(execID));
    }

    public void set(ExecID value) {
        this.setField(value);
    }

    public ExecID get(ExecID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ExecID getExecID() throws FieldNotFound {
        return this.get(new ExecID());
    }

    public boolean isSet(ExecID field) {
        return this.isSetField(field);
    }

    public boolean isSetExecID() {
        return this.isSetField(17);
    }

    public void setExecTransType(char execTransType) {
        setField(new ExecTransType(execTransType));
    }

    public void set(ExecTransType value) {
        this.setField(value);
    }

    public ExecTransType get(ExecTransType value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ExecTransType getExecTransType() throws FieldNotFound {
        return this.get(new ExecTransType());
    }

    public boolean isSet(ExecTransType field) {
        return this.isSetField(field);
    }

    public boolean isSetExecTransType() {
        return this.isSetField(20);
    }

    public void setExecType(char execType) {
        setField(new ExecType(execType));
    }

    public void set(ExecType value) {
        this.setField(value);
    }

    public ExecType get(ExecType value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ExecType getExecType() throws FieldNotFound {
        return this.get(new ExecType());
    }

    public boolean isSet(ExecType field) {
        return this.isSetField(field);
    }

    public boolean isSetExecType() {
        return this.isSetField(150);
    }

    public void setOrdStatus(char ordStatus) {
        setField(new OrdStatus(ordStatus));
    }

    public void set(OrdStatus value) {
        this.setField(value);
    }

    public OrdStatus get(OrdStatus value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public OrdStatus getOrdStatus() throws FieldNotFound {
        return this.get(new OrdStatus());
    }

    public boolean isSet(OrdStatus field) {
        return this.isSetField(field);
    }

    public boolean isSetOrdStatus() {
        return this.isSetField(39);
    }

    public void setSymbol(String symbol) {
        setField(new Symbol(symbol));
    }

    public void set(Symbol value) {
        this.setField(value);
    }

    public Symbol get(Symbol value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public Symbol getSymbol() throws FieldNotFound {
        return this.get(new Symbol());
    }

    public boolean isSet(Symbol field) {
        return this.isSetField(field);
    }

    public boolean isSetSymbol() {
        return this.isSetField(55);
    }

    public void setSide(char side) {
        setField(new Side(side));
    }

    public void set(Side value) {
        this.setField(value);
    }

    public Side get(Side value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public Side getSide() throws FieldNotFound {
        return this.get(new Side());
    }

    public boolean isSet(Side field) {
        return this.isSetField(field);
    }

    public boolean isSetSide() {
        return this.isSetField(54);
    }

    public void setLeavesQty(double leavesQty) {
        setField(new LeavesQty(leavesQty));
    }

    public void set(LeavesQty value) {
        this.setField(value);
    }

    public LeavesQty get(LeavesQty value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public LeavesQty getLeavesQty() throws FieldNotFound {
        return this.get(new LeavesQty());
    }

    public boolean isSet(LeavesQty field) {
        return this.isSetField(field);
    }

    public boolean isSetLeavesQty() {
        return this.isSetField(151);
    }

    public void setCumQty(double cumQty) {
        setField(new CumQty(cumQty));
    }

    public void set(CumQty value) {
        this.setField(value);
    }

    public CumQty get(CumQty value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public CumQty getCumQty() throws FieldNotFound {
        return this.get(new CumQty());
    }

    public boolean isSet(CumQty field) {
        return this.isSetField(field);
    }

    public boolean isSetCumQty() {
        return this.isSetField(14);
    }

    public void setAvgPx(double avgPx) {
        setField(new AvgPx(avgPx));
    }

    public void set(AvgPx value) {
        this.setField(value);
    }

    public AvgPx get(AvgPx value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public AvgPx getAvgPx() throws FieldNotFound {
        return this.get(new AvgPx());
    }

    public boolean isSet(AvgPx field) {
        return this.isSetField(field);
    }

    public boolean isSetAvgPx() {
        return this.isSetField(6);
    }

    // Setters for optional fields
    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
    }

    public void set(ClOrdID value) {
        this.setField(value);
    }

    public ClOrdID get(ClOrdID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ClOrdID getClOrdID() throws FieldNotFound {
        return this.get(new ClOrdID());
    }

    public boolean isSet(ClOrdID field) {
        return this.isSetField(field);
    }

    public boolean isSetClOrdID() {
        return this.isSetField(11);
    }

    public void setOrigClOrdID(String origClOrdID) {
        setField(new OrigClOrdID(origClOrdID));
    }

    public void set(OrigClOrdID value) {
        this.setField(value);
    }

    public OrigClOrdID get(OrigClOrdID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public OrigClOrdID getOrigClOrdID() throws FieldNotFound {
        return this.get(new OrigClOrdID());
    }

    public boolean isSet(OrigClOrdID field) {
        return this.isSetField(field);
    }

    public boolean isSetOrigClOrdID() {
        return this.isSetField(41);
    }

    public void setOrdRejReason(int ordRejReason) {
        setField(new OrdRejReason(ordRejReason));
    }

    public void set(OrdRejReason value) {
        this.setField(value);
    }

    public OrdRejReason get(OrdRejReason value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public OrdRejReason getOrdRejReason() throws FieldNotFound {
        return this.get(new OrdRejReason());
    }

    public boolean isSet(OrdRejReason field) {
        return this.isSetField(field);
    }

    public boolean isSetOrdRejReason() {
        return this.isSetField(103);
    }

    public void setOrderQty(double orderQty) {
        setField(new OrderQty(orderQty));
    }

    public void set(OrderQty value) {
        this.setField(value);
    }

    public OrderQty get(OrderQty value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public OrderQty getOrderQty() throws FieldNotFound {
        return this.get(new OrderQty());
    }

    public boolean isSet(OrderQty field) {
        return this.isSetField(field);
    }

    public boolean isSetOrderQty() {
        return this.isSetField(38);
    }

    public void setOrdType(char ordType) {
        setField(new OrdType(ordType));
    }

    public void set(OrdType value) {
        this.setField(value);
    }

    public OrdType get(OrdType value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public OrdType getOrdType() throws FieldNotFound {
        return this.get(new OrdType());
    }

    public boolean isSet(OrdType field) {
        return this.isSetField(field);
    }

    public boolean isSetOrdType() {
        return this.isSetField(40);
    }

    public void setStopPx(double stopPx) {
        setField(new StopPx(stopPx));
    }

    public void set(StopPx value) {
        this.setField(value);
    }

    public StopPx get(StopPx value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public StopPx getStopPx() throws FieldNotFound {
        return this.get(new StopPx());
    }

    public boolean isSet(StopPx field) {
        return this.isSetField(field);
    }

    public boolean isSetStopPx() {
        return this.isSetField(99);
    }

    public void setTimeInForce(char timeInForce) {
        setField(new TimeInForce(timeInForce));
    }

    public void set(TimeInForce value) {
        this.setField(value);
    }

    public TimeInForce get(TimeInForce value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public TimeInForce getTimeInForce() throws FieldNotFound {
        return this.get(new TimeInForce());
    }

    public boolean isSet(TimeInForce field) {
        return this.isSetField(field);
    }

    public boolean isSetTimeInForce() {
        return this.isSetField(59);
    }

    public void setExpireDate(String expireDate) {
        setField(new ExpireDate(expireDate));
    }

    public void set(ExpireDate value) {
        this.setField(value);
    }

    public ExpireDate get(ExpireDate value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ExpireDate getExpireDate() throws FieldNotFound {
        return this.get(new ExpireDate());
    }

    public boolean isSet(ExpireDate field) {
        return this.isSetField(field);
    }

    public boolean isSetExpireDate() {
        return this.isSetField(432);
    }

    public void setExpireTime(LocalDateTime expireTime) {
        setField(new ExpireTime(expireTime));
    }

    public void set(ExpireTime value) {
        this.setField(value);
    }

    public ExpireTime get(ExpireTime value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ExpireTime getExpireTime() throws FieldNotFound {
        return this.get(new ExpireTime());
    }

    public boolean isSet(ExpireTime field) {
        return this.isSetField(field);
    }

    public boolean isSetExpireTime() {
        return this.isSetField(126);
    }

    public void setLastShares(double lastShares) {
        setField(new LastShares(lastShares));
    }

    public void set(LastShares value) {
        this.setField(value);
    }

    public LastShares get(LastShares value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public LastShares getLastShares() throws FieldNotFound {
        return this.get(new LastShares());
    }

    public boolean isSet(LastShares field) {
        return this.isSetField(field);
    }

    public boolean isSetLastShares() {
        return this.isSetField(32);
    }

    public void setLastPx(double lastPx) {
        setField(new LastPx(lastPx));
    }

    public void set(LastPx value) {
        this.setField(value);
    }

    public LastPx get(LastPx value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public LastPx getLastPx() throws FieldNotFound {
        return this.get(new LastPx());
    }

    public boolean isSet(LastPx field) {
        return this.isSetField(field);
    }

    public boolean isSetLastPx() {
        return this.isSetField(31);
    }

    public void setTradingSessionID(String tradingSessionID) {
        setField(new TradingSessionID(tradingSessionID));
    }

    public void set(TradingSessionID value) {
        this.setField(value);
    }

    public TradingSessionID get(TradingSessionID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public TradingSessionID getTradingSessionID() throws FieldNotFound {
        return this.get(new TradingSessionID());
    }

    public boolean isSet(TradingSessionID field) {
        return this.isSetField(field);
    }

    public boolean isSetTradingSessionID() {
        return this.isSetField(336);
    }

    public void setTransactTime(LocalDateTime transactTime) {
        setField(new TransactTime(transactTime));
    }

    public void set(TransactTime value) {
        this.setField(value);
    }

    public TransactTime get(TransactTime value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public TransactTime getTransactTime() throws FieldNotFound {
        return this.get(new TransactTime());
    }

    public boolean isSet(TransactTime field) {
        return this.isSetField(field);
    }

    public boolean isSetTransactTime() {
        return this.isSetField(60);
    }

    public void setText(String text) {
        setField(new Text(text));
    }

    public void set(Text value) {
        this.setField(value);
    }

    public Text get(Text value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public Text getText() throws FieldNotFound {
        return this.get(new Text());
    }

    public boolean isSet(Text field) {
        return this.isSetField(field);
    }

    public boolean isSetText() {
        return this.isSetField(58);
    }
}