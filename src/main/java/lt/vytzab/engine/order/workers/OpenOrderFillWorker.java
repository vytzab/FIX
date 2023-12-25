package lt.vytzab.engine.order.workers;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.order.Order;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static lt.vytzab.engine.Variables.MARKET_ORDERS_DB;

public class OpenOrderFillWorker extends SwingWorker<Void, List<Order>> {
    private OrderTableModel tableModel;

    public OpenOrderFillWorker(OrderTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // Perform background task (e.g., fetch updated market data)
        List<Order> updatedMarkets = fetchData(); // Implement this method based on your requirements

        // Publish the intermediate result to the process method
        publish(updatedMarkets);

        return null;
    }

    @Override
    protected void process(List<List<Order>> chunks) {
        // Process intermediate results on the Event Dispatch Thread
        if (!isCancelled()) {
            // Assuming you are updating the entire table
            tableModel.setOrders(chunks.get(chunks.size() - 1));
        }
    }

    @Override
    protected void done() {
        // Executed on the Event Dispatch Thread after doInBackground is finished
        // You can perform any final UI updates or cleanup here
    }

    // Implement this method to fetch updated market data
    private List<Order> fetchData() {
        List<Order> orderList = MarketOrderDAO.readAllMarketOrders();
        List<Order> openOrderList = new ArrayList<>();
        for (Order order : orderList) {
            if (!order.isClosed() || !order.isFullyExecuted() || !order.isFilled()) {
                openOrderList.add(order);
            }
        }
        return openOrderList;
    }
}
