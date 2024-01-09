package lt.vytzab.engine.market;

import lt.vytzab.engine.dao.MarketDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.Order;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.List;

public class MarketController {
    private List<Market> markets = MarketDAO.readAllMarkets();

    public Market getMarket(String symbol) {
        return MarketDAO.getMarket(symbol);
    }

    public boolean checkIfMarketExists(String symbol){
        Market market = MarketDAO.getMarket(symbol);
        return market != null;
    }

    public List<Market> getMarkets() {
        return MarketDAO.readAllMarkets();
    }
    public void refreshMarkets() {
        markets = MarketDAO.readAllMarkets();
    }
    public static void updateMarket(Market market) {
        MarketDAO.updateMarket(market);
    }

    public void matchMarketOrders(Market market, ArrayList<Order> orders) {
        getBidAskOrders(market);
        while (true) {
            if (market.getBidOrders().isEmpty() || market.getAskOrders().isEmpty()) {
                return;
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
            } else return;
        }
    }

    public void getBidAskOrders(Market market) {
        List<Order> allOrders = MarketOrderDAO.readAllMarketOrdersBySymbol(market.getSymbol());
        List<Order> bidOrders = new ArrayList<>();
        List<Order> askOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (!order.isClosed() && !order.getCanceled() && !order.getRejected()) {
                insert(order, bidOrders, askOrders);
            }
        }
        market.setAskOrders(askOrders);
        market.setBidOrders(bidOrders);
    }

    public void insert(Order order, List<Order> bidOrders, List<Order> askOrders) {
        if (order.getSide() == Side.BUY) {
            insert(order, true, bidOrders);
        } else {
            insert(order, false, askOrders);
        }
    }

    private void insert(Order newOrder, boolean descending, List<Order> activeOrders) {
        if (activeOrders.isEmpty()) {
            activeOrders.add(newOrder);
        } else if (newOrder.getType() == OrdType.MARKET) {
            activeOrders.add(0, newOrder);
        } else {
            for (int i = 0; i < activeOrders.size(); i++) {
                Order activeOrder = activeOrders.get(i);
                if ((descending ? newOrder.getPrice() > activeOrder.getPrice() : newOrder.getPrice() < activeOrder.getPrice()) && newOrder.getEntryTime() < activeOrder.getEntryTime()) {
                    activeOrders.add(i, newOrder);
                }
            }
            activeOrders.add(newOrder);
        }
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
}