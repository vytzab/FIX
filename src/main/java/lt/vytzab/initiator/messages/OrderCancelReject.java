package lt.vytzab.initiator.messages;


import quickfix.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class OrderCancelReject extends Message {
    private OrderID orderID;
    private ClOrdID clOrdID;
    private OrigClOrdID origClOrdID;
    private OrdStatus ordStatus;
    private CxlRejResponseTo cxlRejResponseTo;
    private CxlRejReason cxlRejReason;
    private Text text;
    private Account account;
    private TransactTime transactTime;

    // Constructor without parameters
    public OrderCancelReject() {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_CANCEL_REJECT));
    }

    // Constructor with mandatory field parameters
    public OrderCancelReject(String orderID, String clOrdID, String origClOrdID, char ordStatus, char cxlRejResponseTo) {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_CANCEL_REJECT));
        this.orderID = new OrderID(orderID);
        this.clOrdID = new ClOrdID(clOrdID);
        this.origClOrdID = new OrigClOrdID(origClOrdID);
        this.ordStatus = new OrdStatus(ordStatus);
        this.cxlRejResponseTo = new CxlRejResponseTo(cxlRejResponseTo);
    }

    // Setters for mandatory fields
    public void setOrderID(String orderID) {
        setField(new OrderID(orderID));
    }

    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
    }

    public void setOrigClOrdID(String origClOrdID) {
        setField(new OrigClOrdID(origClOrdID));
    }

    public void setOrdStatus(char ordStatus) {
        setField(new OrdStatus(ordStatus));
    }

    public void setCxlRejResponseTo(char cxlRejResponseTo) {
        setField(new CxlRejResponseTo(cxlRejResponseTo));
    }

    // Setters for optional fields
    public void setCxlRejReason(int cxlRejReason) {
        setField(new CxlRejReason(cxlRejReason));
    }

    public void setText(String text) {
        setField(new Text(text));
    }

    public void setAccount(String account) {
        setField(new Account(account));
    }

    public void setTransactTime(LocalDateTime transactTime) {
        setField(new TransactTime(transactTime));
    }

    // Getters
    public OrderID getOrderID() {
        return orderID;
    }

    public ClOrdID getClOrdID() {
        return clOrdID;
    }

    public OrigClOrdID getOrigClOrdID() {
        return origClOrdID;
    }

    public OrdStatus getOrdStatus() {
        return ordStatus;
    }

    public CxlRejResponseTo getCxlRejResponseTo() {
        return cxlRejResponseTo;
    }

    public CxlRejReason getCxlRejReason() {
        return cxlRejReason;
    }

    public Text getText() {
        return text;
    }

    public Account getAccount() {
        return account;
    }

    public TransactTime getTransactTime() {
        return transactTime;
    }
}