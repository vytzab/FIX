package lt.vytzab.engine.order;

import quickfix.field.Side;

import java.time.LocalDate;

public class Order {
    private final long entryTime;
    private String clOrdID;
    private final String symbol;
    private final String senderCompID;
    private final String targetCompID;
    private final char side;
    private final char ordType;
    private final double price;
    private final long quantity;
    private long openQuantity;
    private long executedQuantity;
    private double avgExecutedPrice;
    private double lastExecutedPrice;
    private long lastExecutedQuantity;
    private boolean rejected;
    private boolean canceled;
    private LocalDate entryDate;
    private LocalDate GoodTillDate;


    // Constructor without openQuantity parameters
    public Order(long entryTime, String clOrdID, String symbol, String senderCompID, String targetCompID, char side, char ordType, double price, long quantity, long executedQuantity, double avgExecutedPrice, double lastExecutedPrice, long lastExecutedQuantity, boolean rejected, boolean canceled, LocalDate entryDate, LocalDate goodTillDate) {
        this.entryTime = entryTime;
        this.clOrdID = clOrdID;
        this.symbol = symbol;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.side = side;
        this.ordType = ordType;
        this.price = price;
        this.quantity = quantity;
        this.openQuantity = quantity;
        this.executedQuantity = executedQuantity;
        this.avgExecutedPrice = avgExecutedPrice;
        this.lastExecutedPrice = lastExecutedPrice;
        this.lastExecutedQuantity = lastExecutedQuantity;
        this.rejected = rejected;
        this.canceled = canceled;
        this.entryDate = entryDate;
        GoodTillDate = goodTillDate;
    }
    // Constructor with all parameters
    public Order(long entryTime, String clOrdID, String symbol, String senderCompID, String targetCompID, char side, char ordType, double price, long quantity, long openQuantity, long executedQuantity, double avgExecutedPrice, double lastExecutedPrice, long lastExecutedQuantity, boolean rejected, boolean canceled, LocalDate entryDate, LocalDate goodTillDate) {
        this.entryTime = entryTime;
        this.clOrdID = clOrdID;
        this.symbol = symbol;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.side = side;
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
        GoodTillDate = goodTillDate;
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

    public char getOrdType() {
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
    }

    public boolean isClosed() {
        return openQuantity == 0;
    }


    // Execute the order with a given price and quantity, update fields based on execution
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
}