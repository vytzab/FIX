package lt.vytzab.engine.order;

import lt.vytzab.engine.dao.MarketOrderDAO;

import java.util.List;

public class OrderController {

    public boolean createOrder(Order order) {
        return MarketOrderDAO.createMarketOrder(order);
    }

    public Order getOrderByClOrdID(String clOrdID) {
        return MarketOrderDAO.getOrderByClOrdID(clOrdID);
    }

    public boolean updateOrder(Order order) {
        return MarketOrderDAO.updateMarketOrder(order);
    }

    public boolean deleteOrderByClOrdID(String clOrdID) {
        return MarketOrderDAO.deleteOrderByClOrdID(clOrdID);
    }

    public List<Order> getAllOrders() {
        return MarketOrderDAO.readAllMarketOrders();
    }

    public List<Order> getAllOrdersBySymbolAndSender(String symbol, String senderCompId) {
        return MarketOrderDAO.readAllMarketOrdersBySymbolAndSender(symbol, senderCompId);
    }
}
