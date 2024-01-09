package lt.vytzab.engine.order.workers;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;

import javax.swing.*;
import java.time.LocalDate;
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
        List<Order> orderList = MarketOrderDAO.getAllMarketOrders();
        for (Order order : orderList) {
            if (LocalDate.now().isAfter(order.getGoodTillDate()) && !order.isFullyExecuted() && !order.isClosed() && !order.isFilled()){
                order.setOpenQuantity(0);
                order.setCanceled(true);
            }
        }
        return orderList;
    }
}
