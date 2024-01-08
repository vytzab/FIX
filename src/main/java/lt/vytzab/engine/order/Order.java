package lt.vytzab.engine.order;

import quickfix.field.Side;

import java.time.LocalDate;
import java.util.Objects;

public class Order {
    private final long entryTime;
    private String clOrdID;
    private final String symbol;
    private final String senderCompID;
    private final String targetCompID;
    private final char side;
    private final char tif;
    private final char ordType;
    private final double price;
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

    // Constructor with all parameters
    public Order(long entryTime, String clOrdID, String symbol, String senderCompID, String targetCompID, char side, char ordType, double price, long quantity, long openQuantity, long executedQuantity, double avgExecutedPrice, double lastExecutedPrice, long lastExecutedQuantity, boolean rejected, boolean canceled, LocalDate entryDate, LocalDate goodTillDate, char tif) {
        this.entryTime = entryTime;
        this.clOrdID = clOrdID;
        this.symbol = symbol;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.side = side;
        this.tif = tif;
        this.ordType = ordType;
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

    public boolean getRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public double getAvgExecutedPrice() {
        return avgExecutedPrice;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public long getExecutedQuantity() {
        return executedQuantity;
    }

    public long getLastExecutedQuantity() {
        return lastExecutedQuantity;
    }

    public long getOpenQuantity() {
        return openQuantity;
    }

    public String getSenderCompID() {
        return senderCompID;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public char getSide() {
        return side;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTargetCompID() {
        return targetCompID;
    }

    public char getType() {
        return ordType;
    }

    public void setClOrdID(String clOrdID) {
        this.clOrdID = clOrdID;
    }

    //Returns true if order filled
    public boolean isFilled() {
        return quantity == executedQuantity;
    }

    public void cancel() {
        openQuantity = 0;
        this.canceled = true;
    }

    public void reject() {
        openQuantity = 0;
        this.rejected = true;
    }

    public boolean isClosed() {
        return openQuantity == 0;
    }

    public void execute(double price, long quantity) {
        avgExecutedPrice = ((quantity * price) + (avgExecutedPrice * executedQuantity)) / (quantity + executedQuantity);

        openQuantity = openQuantity - quantity;
        executedQuantity = executedQuantity + quantity;
        lastExecutedPrice = price;
        lastExecutedQuantity = quantity;
    }

    public String toString() {
        return (side == Side.BUY ? "BUY" : "SELL") + " " + quantity + "@$" + price + " (" + openQuantity + ")";
    }

    public long getEntryTime() {
        return entryTime;
    }

    public double getLastExecutedPrice() {
        return lastExecutedPrice;
    }

    public boolean isFullyExecuted() {
        return getOpenQuantity() == 0;
    }

    public char getTif() {
        return tif;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void setOpenQuantity(long openQuantity) {
        this.openQuantity = openQuantity;
    }

    public void setExecutedQuantity(long executedQuantity) {
        this.executedQuantity = executedQuantity;
    }

    public void setAvgExecutedPrice(double avgExecutedPrice) {
        this.avgExecutedPrice = avgExecutedPrice;
    }

    public void setLastExecutedPrice(double lastExecutedPrice) {
        this.lastExecutedPrice = lastExecutedPrice;
    }

    public void setLastExecutedQuantity(long lastExecutedQuantity) {
        this.lastExecutedQuantity = lastExecutedQuantity;
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