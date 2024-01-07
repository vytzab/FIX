package lt.vytzab.engine.order.workers;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.order.Order;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OpenOrderFillWorker extends SwingWorker<Void, List<Order>> {
    private final OrderTableModel tableModel;

    public OpenOrderFillWorker(OrderTableModel tableModel) {
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
        List<Order> orderList = MarketOrderDAO.readAllMarketOrders();
        List<Order> openOrderList = new ArrayList<>();
        for (Order order : orderList) {
            if (LocalDate.now().isAfter(order.getGoodTillDate()) && !order.isFullyExecuted() && !order.isClosed() && !order.isFilled()){
                order.setOpenQuantity(0);
                order.setCanceled(true);
            }
            if (!order.isClosed() && !order.isFullyExecuted() && !order.isFilled() && !order.getCanceled()) {
                openOrderList.add(order);
            }
        }
        return openOrderList;
    }
}
