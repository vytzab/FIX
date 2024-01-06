package lt.vytzab.engine.order.workers;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;

import javax.swing.*;
import java.util.List;

public class AllOrderFillWorker extends SwingWorker<Void, List<Order>> {
    private final OrderTableModel tableModel;

    public AllOrderFillWorker(OrderTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    protected Void doInBackground(){
        List<Order> updatedMarkets = fetchData();

        publish(updatedMarkets);

        return null;
    }

    @Override
    protected void process(List<List<Order>> chunks) {
        if (!isCancelled()) {
            tableModel.setOrders(chunks.get(chunks.size() - 1));
        }
    }

    private List<Order> fetchData() {
        return MarketOrderDAO.readAllMarketOrders();
    }
}
