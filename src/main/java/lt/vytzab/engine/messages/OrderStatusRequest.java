package lt.vytzab.engine.messages;

import quickfix.Message;
import quickfix.field.*;

public class OrderStatusRequest extends Message {
    private ClOrdID clOrdID;
    private Symbol symbol;
    private Side side;
    private OrderID orderID;
    private ClientID clientID;
    private Account account;
    private ExecBroker execBroker;

    // Constructor without parameters
    public OrderStatusRequest() {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_STATUS_REQUEST));
    }

    // Constructor with mandatory field parameters
    public OrderStatusRequest(String clOrdID, String symbol, char side) {
        super();
        getHeader().setField(new MsgType(MsgType.ORDER_STATUS_REQUEST));
        this.clOrdID = new ClOrdID(clOrdID);
        this.symbol = new Symbol(symbol);
        this.side = new Side(side);
    }

    // Setters for mandatory fields
    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
    }
    public void setSymbol(String symbol) {
        setField(new Symbol(symbol));
    }
    public void setSide(char side) {
        setField(new Side(side));
    }

    // Setters for optional fields
    public void setOrderID(String orderID) {
        setField(new OrderID(orderID));
    }
    public void setClientID(String clientID) {
        setField(new ClientID(clientID));
    }
    public void setAccount(String account) {
        setField(new Account(account));
    }
    public void setExecBroker(String execBroker) {
        setField(new ExecBroker(execBroker));
    }

    // Getters
    public ClOrdID getClOrdID() {
        return clOrdID;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public OrderID getOrderID() {
        return orderID;
    }

    public ClientID getClientID() {
        return clientID;
    }

    public Account getAccount() {
        return account;
    }

    public ExecBroker getExecBroker() {
        return execBroker;
    }
}