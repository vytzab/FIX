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

    // Check if a market with the given symbol exists
    public boolean checkIfMarketExists(String symbol){
        Market market = MarketDAO.readMarket(symbol);
        return market != null;
    }
    // Match orders within a market
    public boolean matchMarketOrders(Market market, ArrayList<Order> orders) {
        // Fetch bid and ask orders for the given market
        getBidAskOrders(market);
        // Continue matching until there are no bid or ask orders left
        while (true) {
            // Check if either bid or ask orders are empty
            if (market.getBidOrders().isEmpty() || market.getAskOrders().isEmpty()) {
                // Return false if there matched orders
                return !orders.isEmpty();
            }
            // Get the top bid and ask orders
            Order bidOrder = market.getBidOrders().get(0);
            Order askOrder = market.getAskOrders().get(0);
            // if bid order type MARKET or ask order type MARKET or bidPrice > askPrice
            if (bidOrder.getOrdType() == OrdType.MARKET || askOrder.getOrdType() == OrdType.MARKET || (bidOrder.getPrice() >= askOrder.getPrice())) {
                // Match bid and ask orders
                matchOrders(bidOrder, askOrder);
                // Update the last price in the market
//                market.setLastPrice(bidOrder.getAvgExecutedPrice());
                // Add matched orders to the orders list (if not already present)
                if (!orders.contains(bidOrder)) {
                    orders.add(0, bidOrder);
                }
                if (!orders.contains(askOrder)) {
                    orders.add(0, askOrder);
                }
                // Remove closed orders from the market
                if (bidOrder.isClosed()) {
                    market.getBidOrders().remove(0);
                }
                if (askOrder.isClosed()) {
                    market.getAskOrders().remove(0);
                }
            } else return !orders.isEmpty();
        }
    }

    // Retrieve bid and ask orders for a market
    public void getBidAskOrders(Market market) {
        // Retrieve all orders for the specified market symbol
        List<Order> allOrders = MarketOrderDAO.readAllMarketOrdersBySymbol(market.getSymbol());
        // Separate bid and ask orders based on order side
        List<Order> bidOrders = new ArrayList<>();
        List<Order> askOrders = new ArrayList<>();
        // Iterate through all orders to categorize them as bid or ask orders
        for (Order order : allOrders) {
            // Check if the order is not fully executed
            if (!order.isClosed()) {
                // Categorize orders based on side
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
        // Check if the ask order type is LIMIT, otherwise use bid price
        double price = ask.getOrdType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        // Determine the quantity to be executed (minimum of bid and ask open quantities)
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