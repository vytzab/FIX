package lt.vytzab.initiator.messages;

import quickfix.FieldNotFound;
import quickfix.fix42.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class NewOrderSingle extends Message {
    public static final String MSGTYPE = "D";
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
        this.getHeader().setField(new MsgType("D"));
    }

    // Constructor with mandatory field parameters
    public NewOrderSingle(String clOrdID, char handlInst, String symbol, char side, LocalDateTime transactTime, char ordType) {
        this();
        this.setField(new ClOrdID(clOrdID));
        this.setField(new HandlInst(handlInst));
        this.setField(new Symbol(symbol));
        this.setField(new Side(side));
        this.setField(new TransactTime(transactTime));
        this.setField(new OrdType(ordType));

//        getHeader().setField(new MsgType("D"));
//        this.clOrdID = new ClOrdID(clOrdID);
//        this.handlInst = new HandlInst(handlInst);
//        this.symbol = new Symbol(symbol);
//        this.side = new Side(side);
//        this.transactTime = new TransactTime(transactTime);
//        this.ordType = new OrdType(ordType);
    }
    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
    }

    public void set(ClOrdID clOrdID) {
        this.setField(clOrdID);
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

    public void setHandlInst(char handlInst) {
        setField(new HandlInst(handlInst));
    }

    public void set(HandlInst handlInst) {
        this.setField(handlInst);
    }

    public HandlInst get(HandlInst value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public HandlInst getHandlInst() throws FieldNotFound {
        return this.get(new HandlInst());
    }

    public boolean isSet(HandlInst field) {
        return this.isSetField(field);
    }

    public boolean isSetHandlInst() {
        return this.isSetField(21);
    }


    public void setSymbol(String symbol) {
        setField(new Symbol(symbol));
    }

    public void set(Symbol symbol) {
        this.setField(symbol);
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

    public void set(Side side) {
        this.setField(side);
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

    public void setTransactTime(LocalDateTime transactTime) {
        setField(new TransactTime(transactTime));
    }

    public void set(TransactTime transactTime) {
        this.setField(transactTime);
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

    public void setOrdType(char ordType) {
        setField(new OrdType(ordType));
    }

    public void set(OrdType ordType) {
        this.setField(ordType);
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

    // Setters for optional fields
    public void setOrderQty(double orderQty) {
        setField(new OrderQty(orderQty));
    }

    public void set(OrderQty orderQty) {
        this.setField(orderQty);
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

    public void setPrice(double price) {
        setField(new Price(price));
    }

    public void set(Price price) {
        this.setField(price);
    }

    public Price get(Price value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public Price getPrice() throws FieldNotFound {
        return this.get(new Price());
    }

    public boolean isSet(Price field) {
        return this.isSetField(field);
    }

    public boolean isSetPrice() {
        return this.isSetField(44);
    }

    public void setStopPx(double stopPx) {
        setField(new StopPx(stopPx));
    }

    public void set(StopPx stopPx) {
        this.setField(stopPx);
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

    public void set(TimeInForce timeInForce) {
        this.setField(timeInForce);
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

    public void setText(String text) {
        setField(new Text(text));
    }

    public void set(Text text) {
        this.setField(text);
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

    public void setLocateReqd(boolean locateReqd) {
        setField(new LocateReqd(locateReqd));
    }

    public void set(LocateReqd locateReqd) {
        this.setField(locateReqd);
    }

    public LocateReqd get(LocateReqd value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public LocateReqd getLocateReqd() throws FieldNotFound {
        return this.get(new LocateReqd());
    }

    public boolean isSet(LocateReqd field) {
        return this.isSetField(field);
    }

    public boolean isSetLocateReqd() {
        return this.isSetField(114);
    }


}