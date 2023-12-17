package lt.vytzab.engine.messages;

import quickfix.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class OrderCancelReplaceRequest extends Message {
    private OrigClOrdID origClOrdID;
    private ClOrdID clOrdID;
    private HandlInst handlInst;
    private Symbol symbol;
    private Side side;
    private TransactTime transactTime;
    private OrdType ordType;

    // Constructor without parameters
    public OrderCancelReplaceRequest() {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_CANCEL_REPLACE_REQUEST));
    }

    // Constructor with mandatory field parameters
    public OrderCancelReplaceRequest(String origClOrdID, String clOrdID, char handlInst, String symbol, char side, LocalDateTime transactTime, char ordType) {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_CANCEL_REPLACE_REQUEST));
        this.origClOrdID = new OrigClOrdID(origClOrdID);
        this.clOrdID = new ClOrdID(clOrdID);
        this.handlInst = new HandlInst(handlInst);
        this.symbol = new Symbol(symbol);
        this.side = new Side(side);
        this.transactTime = new TransactTime(transactTime);
        this.origClOrdID = new OrigClOrdID(origClOrdID);
    }

    // Setters for mandatory fields
    public void setOrigClOrdID(String origClOrdID) {
        setField(new OrigClOrdID(origClOrdID));
    }

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

    // Getters
    public OrigClOrdID getOrigClOrdID() {
        return origClOrdID;
    }

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
}