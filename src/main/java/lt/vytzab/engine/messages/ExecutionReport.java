package lt.vytzab.engine.messages;

import quickfix.fix42.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class ExecutionReport extends Message {
    public static final String MSGTYPE = "8";
    private OrderID orderID;
    private ExecID execID;
    private ExecTransType execTransType;
    private ExecType execType;
    private OrdStatus ordStatus;
    private Symbol symbol;
    private Side side;
    private LeavesQty leavesQty;
    private CumQty cumQty;
    private AvgPx avgPx;
    private ClOrdID clOrdID;
    private OrigClOrdID origClOrdID;
    private OrdRejReason ordRejReason;
    private OrderQty orderQty;
    private OrdType ordType;
    private StopPx stopPx;
    private TimeInForce timeInForce;
    private ExpireDate expireDate;
    private ExpireTime expireTime;
    private LastShares lastShares;
    private LastPx lastPx;
    private TradingSessionID tradingSessionID;
    private TransactTime transactTime;
    private Text text;

    // Constructor without parameters
    public ExecutionReport() {
        super();
        getHeader().setField(new MsgType("8"));
    }

    // Constructor with mandatory field parameters
    public ExecutionReport(String orderID, String execID, char execTransType, char execType, char ordStatus, String symbol, char side, double leavesQty, double cumQty, double avgPx) {
        super();
        getHeader().setField(new MsgType("8"));
        this.orderID = new OrderID(orderID);
        this.execID = new ExecID(execID);
        this.execTransType = new ExecTransType(execTransType);
        this.execType = new ExecType(execType);
        this.ordStatus = new OrdStatus(ordStatus);
        this.symbol = new Symbol(symbol);
        this.side = new Side(side);
        this.leavesQty = new LeavesQty(leavesQty);
        this.cumQty = new CumQty(cumQty);
        this.avgPx = new AvgPx(avgPx);
    }
    // Setters for mandatory fields
    public void setOrderID(String orderID) {
        setField(new OrderID(orderID));
    }

    public void setExecID(String execID) {
        setField(new ExecID(execID));
    }

    public void setExecTransType(char execTransType) {
        setField(new ExecTransType(execTransType));
    }

    public void setExecType(char execType) {
        setField(new ExecType(execType));
    }

    public void setOrdStatus(char ordStatus) {
        setField(new OrdStatus(ordStatus));
    }

    public void setSymbol(String symbol) {
        setField(new Symbol(symbol));
    }

    public void setSide(char side) {
        setField(new Side(side));
    }

    public void setLeavesQty(double leavesQty) {
        setField(new LeavesQty(leavesQty));
    }

    public void setCumQty(double cumQty) {
        setField(new CumQty(cumQty));
    }

    public void setAvgPx(double avgPx) {
        setField(new AvgPx(avgPx));
    }

    // Setters for optional fields
    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
    }

    public void setOrigClOrdID(String origClOrdID) {
        setField(new OrigClOrdID(origClOrdID));
    }

    public void setOrdRejReason(int ordRejReason) {
        setField(new OrdRejReason(ordRejReason));
    }

    public void setOrderQty(double orderQty) {
        setField(new OrderQty(orderQty));
    }

    public void setOrdType(char ordType) {
        setField(new OrdType(ordType));
    }

    public void setStopPx(double stopPx) {
        setField(new StopPx(stopPx));
    }

    public void setTimeInForce(char timeInForce) {
        setField(new TimeInForce(timeInForce));
    }

    public void setExpireDate(String expireDate) {
        setField(new ExpireDate(expireDate));
    }

    public void setExpireTime(LocalDateTime expireTime) {
        setField(new ExpireTime(expireTime));
    }

    public void setLastShares(double lastShares) {
        setField(new LastShares(lastShares));
    }

    public void setLastPx(double lastPx) {
        setField(new LastPx(lastPx));
    }

    public void setTradingSessionID(String tradingSessionID) {
        setField(new TradingSessionID(tradingSessionID));
    }

    public void setTransactTime(LocalDateTime transactTime) {
        setField(new TransactTime(transactTime));
    }

    public void setText(String text) {
        setField(new Text(text));
    }

    // Getters
    public OrderID getOrderID() {
        return orderID;
    }

    public ExecID getExecID() {
        return execID;
    }

    public ExecTransType getExecTransType() {
        return execTransType;
    }

    public ExecType getExecType() {
        return execType;
    }

    public OrdStatus getOrdStatus() {
        return ordStatus;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public LeavesQty getLeavesQty() {
        return leavesQty;
    }

    public CumQty getCumQty() {
        return cumQty;
    }

    public AvgPx getAvgPx() {
        return avgPx;
    }

    public ClOrdID getClOrdID() {
        return clOrdID;
    }

    public OrigClOrdID getOrigClOrdID() {
        return origClOrdID;
    }

    public OrdRejReason getOrdRejReason() {
        return ordRejReason;
    }

    public OrderQty getOrderQty() {
        return orderQty;
    }

    public OrdType getOrdType() {
        return ordType;
    }

    public StopPx getStopPx() {
        return stopPx;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public ExpireDate getExpireDate() {
        return expireDate;
    }

    public ExpireTime getExpireTime() {
        return expireTime;
    }

    public LastShares getLastShares() {
        return lastShares;
    }

    public LastPx getLastPx() {
        return lastPx;
    }

    public TradingSessionID getTradingSessionID() {
        return tradingSessionID;
    }

    public TransactTime getTransactTime() {
        return transactTime;
    }

    public Text getText() {
        return text;
    }
}