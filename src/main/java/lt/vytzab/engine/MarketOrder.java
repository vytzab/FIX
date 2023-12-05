package lt.vytzab.engine;

import quickfix.field.Side;

public class MarketOrder {
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

    public MarketOrder(String clOrdID, String symbol, String senderCompID, String targetCompID, char side, char ordType,
                       double price, long quantity) {
        super();
        this.clOrdID = clOrdID;
        this.symbol = symbol;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
        this.side = side;
        this.ordType = ordType;
        this.price = price;
        this.quantity = quantity;
        openQuantity = quantity;
        entryTime = System.currentTimeMillis();
    }

    //Getters

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

    //Setters

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

    public void execute(double price, long quantity) {
        avgExecutedPrice = ((quantity * price) + (avgExecutedPrice * executedQuantity)) / (quantity + executedQuantity);

        openQuantity -= quantity;
        executedQuantity += quantity;
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