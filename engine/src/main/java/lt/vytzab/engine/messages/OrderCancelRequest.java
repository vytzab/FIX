package lt.vytzab.engine.messages;

import quickfix.fix42.Message;
import quickfix.field.*;

import java.time.LocalDateTime;

public class OrderCancelRequest extends Message {
    public static final String MSGTYPE = "F";
    private OrigClOrdID origClOrdID;
    private ClOrdID clOrdID;
    private Symbol symbol;
    private Side side;
    private TransactTime transactTime;
    private OrderQty orderQty;
    private OrderID orderID;

    // Constructor without parameters
    public OrderCancelRequest() {
        super();
        getHeader().setField(new MsgType("F"));
    }

    // Constructor with mandatory field parameters
    public OrderCancelRequest(String origClOrdID, String orderID, String clOrdID, String symbol, char side, LocalDateTime transactTime) {
        super();
        getHeader().setField(new MsgType("F"));
        this.origClOrdID = new OrigClOrdID(origClOrdID);
        this.clOrdID = new ClOrdID(clOrdID);
        this.symbol = new Symbol(symbol);
        this.side = new Side(side);
        this.transactTime = new TransactTime(transactTime);
    }

    // Setters for mandatory fields
    public void setOrigClOrdID(String origClOrdID) {
        setField(new OrigClOrdID(origClOrdID));
    }

    public void setClOrdID(String clOrdID) {
        setField(new ClOrdID(clOrdID));
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

    // Setters for optional fields
    public void setOrderQty(double orderQty) {
        setField(new OrderQty(orderQty));
    }

    public void setOrderID(String orderID) {
        setField(new OrderID(orderID));
    }

    // Getters
    public OrigClOrdID getOrigClOrdID() {
        return origClOrdID;
    }

    public OrderID getOrderID() {
        return orderID;
    }

    public ClOrdID getClOrdID() {
        return clOrdID;
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

    public OrderQty getOrderQty() {
        return orderQty;
    }
}