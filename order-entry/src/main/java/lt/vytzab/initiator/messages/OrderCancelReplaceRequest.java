package lt.vytzab.initiator.messages;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class OrderCancelReplaceRequest extends Message {
    public static final String MSGTYPE = "G";

    public OrderCancelReplaceRequest() {
        this.getHeader().setField(new MsgType("G"));
    }

    public OrderCancelReplaceRequest(OrigClOrdID origClOrdID, ClOrdID clOrdID, HandlInst handlInst, Symbol symbol, Side side, TransactTime transactTime, OrdType ordType) {
        this();
        this.setField(origClOrdID);
        this.setField(clOrdID);
        this.setField(handlInst);
        this.setField(symbol);
        this.setField(side);
        this.setField(transactTime);
        this.setField(ordType);
    }

    // Setters for mandatory fields
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

    public void setHandlInst(char handlInst) {
        setField(new HandlInst(handlInst));
    }

    public void set(HandlInst value) {
        this.setField(value);
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
}