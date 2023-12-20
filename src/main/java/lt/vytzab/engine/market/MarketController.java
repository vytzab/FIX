package lt.vytzab.engine.market;

import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.order.Order;
import quickfix.field.OrdType;

import java.util.ArrayList;
import java.util.List;

import static lt.vytzab.engine.Variables.FILLED_ORDERS_DB;
import static lt.vytzab.engine.Variables.MARKET_ORDERS_DB;

public class MarketController {
    // List of markets loaded from MarketDataDAO
    private final List<Market> markets = MarketDataDAO.readAllMarkets();

    // Get a specific market by symbol
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
        Market market = MarketDataDAO.readMarket(symbol);
        return market != null;
    }


    // Insert an order into the market
    public boolean insertOrder(Order order) {
        return MarketOrderDAO.createMarketOrder(order, MARKET_ORDERS_DB);
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
                market.setLastPrice(bidOrder.getAvgExecutedPrice());
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
        List<Order> allOrders = MarketOrderDAO.readAllMarketOrdersBySymbol(market.getSymbol(), MARKET_ORDERS_DB);
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

        // Execute the bid and ask orders at the determined price and quantity
        bid.execute(price, quantity);
        ask.execute(price, quantity);

        // Update the bid and ask orders in the market order DAO (Data Access Object)
        MarketOrderDAO.updateMarketOrder(bid, MARKET_ORDERS_DB);
        MarketOrderDAO.updateMarketOrder(ask, MARKET_ORDERS_DB);
    }

    // Get an order by ClOrdID
    public Order getOrderByClOrdID(String clOrdID, String tableName) {
        return MarketOrderDAO.getOrderByClOrdID(clOrdID, tableName);
    }

    // Delete an order by ClOrdID
    public void deleteOrderByClOrdID(String clOrdID, String tableName) {
        MarketOrderDAO.deleteOrderByClOrdID(clOrdID, tableName);
    }

    // Move filled order to different table
    public void moveFilledOrder(Order order) {
        MarketOrderDAO.createMarketOrder(order, FILLED_ORDERS_DB);
        deleteOrderByClOrdID(order.getClOrdID(), MARKET_ORDERS_DB);
    }

    public List<Market> getMarkets() {
        return markets;
    }

    // Placeholder method for displaying market information
    public void display() {
        // TODO: Implement logic for displaying market information
    }
}