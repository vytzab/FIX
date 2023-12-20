package lt.vytzab.engine.order;

import lt.vytzab.engine.dao.MarketOrderDAO;

import java.util.ArrayList;
import java.util.List;

import static lt.vytzab.engine.Variables.MARKET_ORDERS_DB;

public class OrderIdGenerator {
    private Long orderIdCounter = 0L;
    private int executionIdCounter = 0;
    List<Order> orders = new ArrayList<>();

    public String genExecutionID() {
        return Integer.toString(executionIdCounter++);
    }

    public String genOrderID() {
        orders = MarketOrderDAO.readAllMarketOrders(MARKET_ORDERS_DB);
        for (Order order : orders) {
            if (Long.parseLong(order.getClOrdID()) > orderIdCounter) {
                orderIdCounter = Long.parseLong(order.getClOrdID());
            }
        }
        return Long.toString(orderIdCounter+1);
    }
}
