package lt.vytzab.engine.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IDGenerator {
    private Long orderIdCounter = 0L;
    private Long executionIdCounter = 0L;
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

    public String genExecutionID() {
        String id = senderCompID + "ER" + getCurrentDateTimeAsString() + executionIdCounter.toString();
        executionIdCounter++;
        return id;
    }

    public Long getOrderIdCounter() {
        return orderIdCounter;
    }

    public void setOrderIdCounter(Long orderIdCounter) {
        this.orderIdCounter = orderIdCounter;
    }

    public Long getExecutionIdCounter() {
        return executionIdCounter;
    }

    public void setExecutionIdCounter(Long executionIdCounter) {
        this.executionIdCounter = executionIdCounter;
    }

    public String getSenderCompID() {
        return senderCompID;
    }

    public void setSenderCompID(String senderCompID) {
        this.senderCompID = senderCompID;
    }
}
