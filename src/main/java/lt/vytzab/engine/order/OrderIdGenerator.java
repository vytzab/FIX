package lt.vytzab.engine.order;

import lt.vytzab.engine.dao.MarketOrderDAO;

import java.util.ArrayList;
import java.util.List;

public class OrderIdGenerator {
    private int orderIdCounter = 0;
    private int executionIdCounter = 0;
    List<Order> orders = new ArrayList<>();

    public String genExecutionID() {
        return Integer.toString(executionIdCounter++);
    }

    public String genOrderID() {
        orders = MarketOrderDAO.readAllMarketOrders();
        for (Order order : orders) {
            if (Integer.parseInt(order.getClOrdID()) > orderIdCounter) {
                orderIdCounter = Integer.parseInt(order.getClOrdID());
            }
        }
        return Integer.toString(orderIdCounter+1);
    }
}
