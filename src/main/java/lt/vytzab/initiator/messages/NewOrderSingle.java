package lt.vytzab.initiator.messages;

import quickfix.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class NewOrderSingle extends Message {
    private ClOrdID clOrdID;
    private HandlInst handlInst;
    private Symbol symbol;
    private Side side;
    private TransactTime transactTime;
    private OrdType ordType;
    private OrderQty orderQty;
    private Price price;
    private StopPx stopPx;
    private TimeInForce timeInForce;
    private Text text;
    private LocateReqd locateReqd;

    // Constructor without parameters
    public NewOrderSingle() {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_SINGLE));
    }

    // Constructor with mandatory field parameters
    public NewOrderSingle(String clOrdID, char handlInst, String symbol, char side, LocalDateTime transactTime, char ordType) {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_SINGLE));
        this.clOrdID = new ClOrdID(clOrdID);
        this.handlInst = new HandlInst(handlInst);
        this.symbol = new Symbol(symbol);
        this.side = new Side(side);
        this.transactTime = new TransactTime(transactTime);
        this.ordType = new OrdType(ordType);
    }
    // Setters for mandatory fields
    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
    }

    public void setHandlInst(char handlInst) {
        setField(new HandlInst(handlInst));
    }

    public void setSymbol(String symbol) {
        setField(new Symbol(symbol));
    }

    public void setSide(char side) {
        setField(new Side(side));
    }

    public void setTransactTime(LocalDateTime transactTime) {
        setField(new TransactTime(transactTime));
    }

    public void setOrdType(char ordType) {
        setField(new OrdType(ordType));
    }

    // Setters for optional fields
    public void setOrderQty(double orderQty) {
        setField(new OrderQty(orderQty));
    }

    public void setPrice(double price) {
        setField(new Price(price));
    }

    public void setStopPx(double stopPx) {
        setField(new StopPx(stopPx));
    }

    public void setTimeInForce(char timeInForce) {
        setField(new TimeInForce(timeInForce));
    }

    public void setText(String text) {
        setField(new Text(text));
    }

    public void setLocateReqd(boolean locateReqd) {
        setField(new LocateReqd(locateReqd));
    }

    // Getters
    public ClOrdID getClOrdID() {
        return clOrdID;
    }

    public HandlInst getHandlInst() {
        return handlInst;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public TransactTime getTransactTime() {
        return transactTime;
    }

    public OrdType getOrdType() {
        return ordType;
    }

    public OrderQty getOrderQty() {
        return orderQty;
    }

    public Price getPrice() {
        return price;
    }

    public StopPx getStopPx() {
        return stopPx;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public Text getText() {
        return text;
    }

    public LocateReqd getLocateReqd() {
        return locateReqd;
    }
}