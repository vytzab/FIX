package lt.vytzab.initiator.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IDGenerator {
    private static Long orderIdCounter = 0L;
    private Long executionIdCounter = 0L;
    private Long marketRequestIdCounter = 0L;
    public static String getCurrentDateTimeAsString() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return currentDateTime.format(formatter);
    }

    public static String genOrderID() {
        String id = getCurrentDateTimeAsString() + orderIdCounter.toString();
        orderIdCounter++;
        return id;
    }
    public String genExecutionID() {
        String id = getCurrentDateTimeAsString() + executionIdCounter.toString();
        executionIdCounter++;
        return id;
    }

    public static String genMarketRequestID() {
        String id = getCurrentDateTimeAsString() + orderIdCounter.toString();
        orderIdCounter++;
        return id;
    }
}