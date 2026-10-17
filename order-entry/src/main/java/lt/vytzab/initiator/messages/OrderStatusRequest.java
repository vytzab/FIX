package lt.vytzab.initiator.messages;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

public class OrderStatusRequest extends Message {
    public static final String MSGTYPE = "H";

    public OrderStatusRequest() {
        this.getHeader().setField(new MsgType("H"));
    }

    public OrderStatusRequest(ClOrdID clOrdID, Symbol symbol, Side side) {
        this();
        this.setField(clOrdID);
        this.setField(symbol);
        this.setField(side);
    }

    // Setters for mandatory fields
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

    // Setters for optional fields
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

    public void setClientID(String clientID) {
        setField(new ClientID(clientID));
    }

    public void set(ClientID value) {
        this.setField(value);
    }

    public ClientID get(ClientID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ClientID getClientID() throws FieldNotFound {
        return this.get(new ClientID());
    }

    public boolean isSet(ClientID field) {
        return this.isSetField(field);
    }

    public boolean isSetClientID() {
        return this.isSetField(109);
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

    public void setExecBroker(String execBroker) {
        setField(new ExecBroker(execBroker));
    }

    public void set(ExecBroker value) {
        this.setField(value);
    }

    public ExecBroker get(ExecBroker value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public ExecBroker getExecBroker() throws FieldNotFound {
        return this.get(new ExecBroker());
    }

    public boolean isSet(ExecBroker field) {
        return this.isSetField(field);
    }

    public boolean isSetExecBroker() {
        return this.isSetField(76);
    }
}