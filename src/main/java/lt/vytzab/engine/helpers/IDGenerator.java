package lt.vytzab.engine.helpers;

public class IDGenerator {
    private static Long orderIdCounter = 0L;
    private Long executionIdCounter = 0L;


    public String genOrderID() {
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