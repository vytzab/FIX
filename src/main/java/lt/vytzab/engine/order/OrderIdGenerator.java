package lt.vytzab.engine.order;

public class OrderIdGenerator {
    private int orderIdCounter = 0;
    private int executionIdCounter = 0;

    public String genExecutionID() {
        return Integer.toString(executionIdCounter++);
    }

    public String genOrderID() {
        return Integer.toString(orderIdCounter++);
    }
}
