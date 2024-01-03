package lt.vytzab.initiator.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IDGenerator {
    private Long orderIdCounter = 0L;
    private Long marketRequestIDCounter = 0L;
    private String senderCompID;

    public IDGenerator() {
    }
    public String getCurrentDateTimeAsString() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return currentDateTime.format(formatter);
    }

    public String genOrderID() {
        String id = senderCompID + "OR" + getCurrentDateTimeAsString() + orderIdCounter.toString();
        orderIdCounter++;
        return id;
    }

    public String genMarketRequestID() {
        String id = senderCompID + "MR" + getCurrentDateTimeAsString() + marketRequestIDCounter.toString();
        marketRequestIDCounter++;
        return id;
    }

    public Long getOrderIdCounter() {
        return orderIdCounter;
    }

    public void setOrderIdCounter(Long orderIdCounter) {
        this.orderIdCounter = orderIdCounter;
    }

    public Long getMarketRequestIDCounter() {
        return marketRequestIDCounter;
    }

    public void setMarketRequestIDCounter(Long marketRequestIDCounter) {
        this.marketRequestIDCounter = marketRequestIDCounter;
    }

    public String getSenderCompID() {
        return senderCompID;
    }

    public void setSenderCompID(String senderCompID) {
        this.senderCompID = senderCompID;
    }
}