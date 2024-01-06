package lt.vytzab.engine.market;

import lt.vytzab.engine.dao.MarketDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.Order;
import quickfix.field.OrdType;

import java.util.ArrayList;
import java.util.List;

public class MarketController {
    private List<Market> markets = MarketDAO.readAllMarkets();

    public Market getMarket(String symbol) {
        for (Market market : markets) {
            if (market.getSymbol().equals(symbol)){
                return market;
            }
        }
        return null;
    }

    public boolean checkIfMarketExists(String symbol){
        Market market = MarketDAO.readMarket(symbol);
        return market != null;
    }
    public boolean matchMarketOrders(Market market, ArrayList<Order> orders) {
        getBidAskOrders(market);
        while (true) {
            if (market.getBidOrders().isEmpty() || market.getAskOrders().isEmpty()) {
                return !orders.isEmpty();
            }
            Order bidOrder = market.getBidOrders().get(0);
            Order askOrder = market.getAskOrders().get(0);
            if (bidOrder.getType() == OrdType.MARKET || askOrder.getType() == OrdType.MARKET || (bidOrder.getPrice() >= askOrder.getPrice())) {
                matchOrders(bidOrder, askOrder);
                if (!orders.contains(bidOrder)) {
                    orders.add(0, bidOrder);
                }
                if (!orders.contains(askOrder)) {
                    orders.add(0, askOrder);
                }
                if (bidOrder.isClosed()) {
                    market.getBidOrders().remove(0);
                }
                if (askOrder.isClosed()) {
                    market.getAskOrders().remove(0);
                }
            } else return !orders.isEmpty();
        }
    }

    public void getBidAskOrders(Market market) {
        List<Order> allOrders = MarketOrderDAO.readAllMarketOrdersBySymbol(market.getSymbol());
        List<Order> bidOrders = new ArrayList<>();
        List<Order> askOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (!order.isClosed()) {
                if (order.getSide() == '1') {
                    bidOrders.add(order);
                } else if (order.getSide() == '2') {
                    askOrders.add(order);
                }
            }
        }
        market.setAskOrders(askOrders);
        market.setBidOrders(bidOrders);
    }

    // Execute matching orders
    private void matchOrders(Order bid, Order ask) {
        double price = ask.getType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = Math.min(bid.getOpenQuantity(), ask.getOpenQuantity());

        bid.execute(price, quantity);
        ask.execute(price, quantity);

        Market market = getMarket(bid.getSymbol());
        market.setLastPrice(bid.getAvgExecutedPrice());
        market.setBuyVolume(market.getBuyVolume() + bid.getLastExecutedQuantity());
        market.setSellVolume(market.getSellVolume() + ask.getLastExecutedQuantity());
        if (market.getDayLow() > ask.getAvgExecutedPrice()) {
            market.setDayLow(ask.getAvgExecutedPrice());
        }
        if (market.getDayHigh() < ask.getAvgExecutedPrice()) {
            market.setDayHigh(ask.getAvgExecutedPrice());
        }

        updateMarket(market);

        MarketOrderDAO.updateMarketOrder(bid);
        MarketOrderDAO.updateMarketOrder(ask);
    }
    public List<Market> getMarkets() {
        return markets;
    }
    public void refreshMarkets() {
        markets = MarketDAO.readAllMarkets();
    }
    public void updateMarket(Market market) {
        MarketDAO.updateMarket(market);
    }
}