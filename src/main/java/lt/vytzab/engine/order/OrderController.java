package lt.vytzab.engine.order;

import lt.vytzab.engine.dao.MarketDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.market.Market;

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
        return MarketOrderDAO.getAllMarketOrders();
    }

    public List<Order> getAllOrdersBySymbolAndSender(String symbol, String senderCompId) {
        return MarketOrderDAO.readAllMarketOrdersBySymbolAndSender(symbol, senderCompId);
    }

    public boolean checkIfOrderExists(String clOrdID){
        Order order = getOrderByClOrdID(clOrdID);
        return order != null;
    }
}
