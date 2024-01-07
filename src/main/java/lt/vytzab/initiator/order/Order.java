package lt.vytzab.initiator.order;

import lt.vytzab.initiator.helpers.IDGenerator;
import quickfix.SessionID;
import quickfix.field.Side;

import java.time.LocalDate;
import java.util.Objects;

public class Order {
    private String clOrdID;
    private String symbol;
    private char side;
    private char tif;
    private char type;
    private double price;
    private long quantity;
    private long openQuantity;
    private long executedQuantity;
    private double avgExecutedPrice;
    private double lastExecutedPrice;
    private double limit;
    private long lastExecutedQuantity;
    private boolean rejected;
    private boolean canceled;
    private LocalDate entryDate;
    private LocalDate GoodTillDate;
    private boolean isNew = true;

    public Order(String clOrdID, String symbol, char side, char type, double price, long quantity, long openQuantity, long executedQuantity, double avgExecutedPrice, double lastExecutedPrice, long lastExecutedQuantity, boolean rejected, boolean canceled, LocalDate entryDate, LocalDate goodTillDate, char tif) {
        this.clOrdID = clOrdID;
        this.symbol = symbol;
        this.side = side;
        this.tif = tif;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.openQuantity = openQuantity;
        this.executedQuantity = executedQuantity;
        this.avgExecutedPrice = avgExecutedPrice;
        this.lastExecutedPrice = lastExecutedPrice;
        this.lastExecutedQuantity = lastExecutedQuantity;
        this.rejected = rejected;
        this.canceled = canceled;
        this.entryDate = entryDate;
        this.GoodTillDate = goodTillDate;
    }
    public Order() {
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public void setClOrdID(String clOrdID) {
        this.clOrdID = clOrdID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public char getSide() {
        return side;
    }

    public void setSide(char side) {
        this.side = side;
    }

    public char getTif() {
        return tif;
    }

    public void setTif(char tif) {
        this.tif = tif;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getOpenQuantity() {
        return openQuantity;
    }

    public void setOpenQuantity(long openQuantity) {
        this.openQuantity = openQuantity;
    }

    public long getExecutedQuantity() {
        return executedQuantity;
    }

    public void setExecutedQuantity(long executedQuantity) {
        this.executedQuantity = executedQuantity;
    }

    public double getAvgExecutedPrice() {
        return avgExecutedPrice;
    }

    public void setAvgExecutedPrice(double avgExecutedPrice) {
        this.avgExecutedPrice = avgExecutedPrice;
    }

    public double getLastExecutedPrice() {
        return lastExecutedPrice;
    }

    public void setLastExecutedPrice(double lastExecutedPrice) {
        this.lastExecutedPrice = lastExecutedPrice;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public long getLastExecutedQuantity() {
        return lastExecutedQuantity;
    }

    public void setLastExecutedQuantity(long lastExecutedQuantity) {
        this.lastExecutedQuantity = lastExecutedQuantity;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getGoodTillDate() {
        return GoodTillDate;
    }

    public void setGoodTillDate(LocalDate goodTillDate) {
        GoodTillDate = goodTillDate;
    }

    public boolean isFilled() {
        return quantity == executedQuantity;
    }

    public void cancel() {
        openQuantity = 0;
    }

    public boolean isClosed() {
        return openQuantity == 0;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    public String toString() {
        return (side == Side.BUY ? "BUY" : "SELL") + " " + quantity + "@$" + price + " (" + openQuantity + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(getClOrdID(), order.getClOrdID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClOrdID());
    }
}