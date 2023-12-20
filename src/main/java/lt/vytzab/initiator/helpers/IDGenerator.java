package lt.vytzab.initiator.helpers;

public class IDGenerator {
    private static Long orderIdCounter = 0L;
    private Long executionIdCounter = 0L;
    private Long marketRequestIdCounter = 0L;


    public static String genOrderID() {
        String id = DateTimeString.getCurrentDateTimeAsString() + orderIdCounter.toString();
        orderIdCounter++;
        return id;
    }
    public String genExecutionID() {
        String id = DateTimeString.getCurrentDateTimeAsString() + executionIdCounter.toString();
        executionIdCounter++;
        return id;
    }

    public static String genMarketRequestID() {
        String id = DateTimeString.getCurrentDateTimeAsString() + orderIdCounter.toString();
        orderIdCounter++;
        return id;
    }
}