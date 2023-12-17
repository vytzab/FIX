package lt.vytzab.engine.market;

import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.Order;
import quickfix.field.OrdType;

import java.util.List;

public class MarketController {
    private final List<Market> markets = MarketDataDAO.readAllMarkets();

    public Market getMarket(String symbol) {
        for (Market market : markets) {
            if (market.getSymbol().equals(symbol)){
                return market;
            }
        }
        return null;
    }

    public boolean checkIfMarketExists(String symbol){
        Market market = MarketDataDAO.readMarket(symbol);
        return market != null;
    }

    public boolean insert(Order order) {
        return MarketOrderDAO.createMarketOrder(order);
//        return getMarket(order.getSymbol()).insert(order);
    }

//    public void match(String symbol, ArrayList<Order> orders) {
//        getMarket(symbol).match(symbol, orders);
//    }

    public boolean match(Market market) {
        getBidAskOrders(market);
        while (true) {
            if (market.getBidOrders().isEmpty() || market.getAskOrders().isEmpty()) {
                return false;
            }
            Order bidOrder = market.getBidOrders().get(0);
            Order askOrder = market.getAskOrders().get(0);
            if (bidOrder.getOrdType() == OrdType.MARKET || askOrder.getOrdType() == OrdType.MARKET || (bidOrder.getPrice() >= askOrder.getPrice())) {
                matchOrders(bidOrder, askOrder);
                market.setLastPrice(bidOrder.getAvgExecutedPrice());
                if (bidOrder.isClosed()) {
                    market.getBidOrders().remove(bidOrder);
                }
                if (askOrder.isClosed()) {
                    market.getAskOrders().remove(askOrder);
                }
            } else return false;
        }
    }
    public void getBidAskOrders(Market market) {
        List<Order> allOrders = MarketOrderDAO.readAllMarketOrdersBySymbol(market.getSymbol());
        List<Order> bidOrders = null;
        List<Order> askOrders = null;;
        for (Order order : allOrders) {
            if (order.getSide()=='1') {
                assert false;
                bidOrders.add(order);
            } else if (order.getSide()=='2') {
                assert false;
                askOrders.add(order);
            }
        }
    }

    private void matchOrders(Order bid, Order ask) {
        double price = ask.getOrdType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = Math.min(bid.getOpenQuantity(), ask.getOpenQuantity());

        bid.execute(price, quantity);
        ask.execute(price, quantity);
        MarketOrderDAO.updateMarketOrder(bid);
        MarketOrderDAO.updateMarketOrder(ask);
    }

    public Order getOrderByClOrdID(String clOrdID) {
        return MarketOrderDAO.getOrderByClOrdID(clOrdID);
    }

    public void deleteOrderByClOrdID(String clOrdID) {
        MarketOrderDAO.deleteOrderByClOrdID(clOrdID);
    }

    public void display() {
    }

}