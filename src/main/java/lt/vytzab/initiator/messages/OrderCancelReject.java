package lt.vytzab.initiator.messages;


import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class OrderCancelReject extends Message {
    public static final String MSGTYPE = "9";

    public OrderCancelReject() {
        this.getHeader().setField(new MsgType("9"));
    }

    public OrderCancelReject(OrderID orderID, ClOrdID clOrdID, OrigClOrdID origClOrdID, OrdStatus ordStatus, CxlRejResponseTo cxlRejResponseTo) {
        this();
        this.setField(orderID);
        this.setField(clOrdID);
        this.setField(origClOrdID);
        this.setField(ordStatus);
        this.setField(cxlRejResponseTo);
    }

    // Setters for mandatory fields
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

    public void setCxlRejResponseTo(char cxlRejResponseTo) {
        setField(new CxlRejResponseTo(cxlRejResponseTo));
    }

    public void set(CxlRejResponseTo value) {
        this.setField(value);
    }

    public CxlRejResponseTo get(CxlRejResponseTo value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public CxlRejResponseTo getCxlRejResponseTo() throws FieldNotFound {
        return this.get(new CxlRejResponseTo());
    }

    public boolean isSet(CxlRejResponseTo field) {
        return this.isSetField(field);
    }

    public boolean isSetCxlRejResponseTo() {
        return this.isSetField(434);
    }

    // Setters for optional fields
    public void setCxlRejReason(int cxlRejReason) {
        setField(new CxlRejReason(cxlRejReason));
    }

    public void set(CxlRejReason value) {
        this.setField(value);
    }

    public CxlRejReason get(CxlRejReason value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public CxlRejReason getCxlRejReason() throws FieldNotFound {
        return this.get(new CxlRejReason());
    }

    public boolean isSet(CxlRejReason field) {
        return this.isSetField(field);
    }

    public boolean isSetCxlRejReason() {
        return this.isSetField(102);
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

    public void setAccount(String account) {
        setField(new Account(account));
    }

    public void set(Account value) {
        this.setField(value);
    }

    public Account get(Account value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public Account getAccount() throws FieldNotFound {
        return this.get(new Account());
    }

    public boolean isSet(Account field) {
        return this.isSetField(field);
    }

    public boolean isSetAccount() {
        return this.isSetField(1);
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
}