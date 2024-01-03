package lt.vytzab.initiator.order;

import lt.vytzab.initiator.helpers.IDGenerator;
import quickfix.SessionID;

import java.time.LocalDate;

public class Order {
    private SessionID sessionID = null;
    private String symbol = null;
    private double quantity = 0;
    private double openQuantity = 0;
    private double executedQuantity = 0;
    private OrderSide side = OrderSide.BUY;
    private OrderType type = OrderType.MARKET;
    private OrderTIF tif = OrderTIF.DAY;
    private Double limit = null;
    private double avgPx = 0.0;
    private boolean rejected = false;
    private boolean canceled = false;
    private boolean isNew = true;
    private String message = null;
    private String OrderID = null;
    private String ClOrdID = null;
    private LocalDate entryDate = null;
    private LocalDate goodTillDate = null;

    public Order(String id) {
        ClOrdID = id;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getOpenQuantity() {
        return openQuantity;
    }

    public void setOpenQuantity(double openQuantity) {
        this.openQuantity = openQuantity;
    }

    public double getExecutedQuantity() {
        return executedQuantity;
    }

    public void setExecutedQuantity(double executedQuantity) {
        this.executedQuantity = executedQuantity;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public OrderTIF getTIF() {
        return tif;
    }

    public void setTIF(OrderTIF tif) {
        this.tif = tif;
    }

    public Double getLimit() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public void setLimit(String limit) {
        if (limit == null || limit.equals("")) {
            this.limit = null;
        } else {
            this.limit = Double.parseDouble(limit);
        }
    }

    public void setAvgPx(double avgPx) {
        this.avgPx = avgPx;
    }

    public double getAvgPx() {
        return avgPx;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public boolean getRejected() {
        return rejected;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean getCanceled() {
        return canceled;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setOrderID(String ID) {
        this.OrderID = ID;
    }

    public String getOrderID() {
        return OrderID;
    }

    public void setClOrdID(String clOrdID) {
        this.ClOrdID = clOrdID;
    }

    public String getClOrdID() {
        return ClOrdID;
    }

    public boolean isRejected() {
        return rejected;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getGoodTillDate() {
        return goodTillDate;
    }

    public void setGoodTillDate(LocalDate goodTillDate) {
        this.goodTillDate = goodTillDate;
    }
}