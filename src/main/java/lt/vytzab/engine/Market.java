package lt.vytzab.engine;

import quickfix.field.OrdType;
import quickfix.field.Side;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Market {
    private final List<MarketOrder> bidOrders = new ArrayList<>();
    private final List<MarketOrder> askOrders = new ArrayList<>();
    private Double lastPrice;
    private Double dayHigh;
    private Double dayLow;
    private int volume = 0;

    public boolean match(String symbol, List<MarketOrder> orders) {
        while (true) {
            if (bidOrders.isEmpty() || askOrders.isEmpty()) {
                return !orders.isEmpty();
            }
            MarketOrder bidOrder = bidOrders.get(0);
            MarketOrder askOrder = askOrders.get(0);
            if (bidOrder.getOrdType() == OrdType.MARKET || askOrder.getOrdType() == OrdType.MARKET || (bidOrder.getPrice() >= askOrder.getPrice())) {
                match(bidOrder, askOrder);
                if (!orders.contains(bidOrder)) {
                    orders.add(0, bidOrder);
                }
                if (!orders.contains(askOrder)) {
                    orders.add(0, askOrder);
                }

                if (bidOrder.isClosed()) {
                    bidOrders.remove(bidOrder);
                }
                if (askOrder.isClosed()) {
                    askOrders.remove(askOrder);
                }
            } else return !orders.isEmpty();
        }
    }

    private void match(MarketOrder bid, MarketOrder ask) {
        double price = ask.getOrdType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = Math.min(bid.getOpenQuantity(), ask.getOpenQuantity());

        bid.execute(price, quantity);
        ask.execute(price, quantity);
    }

    public boolean insert(MarketOrder order) {
        return order.getSide() == Side.BUY ? insert(order, true, bidOrders) : insert(order, false, askOrders);
    }

    private boolean insert(MarketOrder newOrder, boolean descending, List<MarketOrder> activeOrders) {
        if (activeOrders.isEmpty()) {
            activeOrders.add(newOrder);
        } else if (newOrder.getOrdType() == OrdType.MARKET) {
            activeOrders.add(0, newOrder);
        } else {
            for (int i = 0; i < activeOrders.size(); i++) {
                MarketOrder activeOrder = activeOrders.get(i);
                //If newOrder.side = BUY and price is higher than an active selling orders -
                if ((descending ? newOrder.getPrice() > activeOrder.getPrice() : newOrder.getPrice() < activeOrder.getPrice()) && newOrder.getEntryTime() < activeOrder.getEntryTime()) {
                    activeOrders.add(i, newOrder);
                }
            }
            activeOrders.add(newOrder);
        }
        return true;
    }

    public void erase(MarketOrder order) {
        if (order.getSide() == Side.BUY) {
            bidOrders.remove(find(bidOrders, order.getClOrdID()));
        } else {
            askOrders.remove(find(askOrders, order.getClOrdID()));
        }
    }

    public MarketOrder find(String symbol, char side, String id) {
        return find(side == Side.BUY ? bidOrders : askOrders, id);
    }

    private MarketOrder find(List<MarketOrder> orders, String clientOrderId) {
        for (MarketOrder order : orders) {
            if (order.getClOrdID().equals(clientOrderId)) {
                return order;
            }
        }
        return null;
    }

    public void display(String symbol) {
        if (!bidOrders.isEmpty() || !askOrders.isEmpty()) {
            System.out.println("MARKET: " + symbol);
        }
        if (!bidOrders.isEmpty()) {
            displaySide(askOrders, "ASKS");
        }
        if (!askOrders.isEmpty()) {
            displaySide(askOrders, "ASKS");
        }
    }

    private void displaySide(List<MarketOrder> orders, String title) {
        DecimalFormat priceFormat = new DecimalFormat("#.00");
        DecimalFormat qtyFormat = new DecimalFormat("######");
        System.out.println(title + ":\n----");
        for (MarketOrder order : orders) {
            System.out.println(qtyFormat.format(order.getOpenQuantity()) + " x " + priceFormat.format(order.getPrice()) + "$  |" + " - entered by " + order.getSenderCompID() + " at " + new Date(order.getEntryTime()));
        }
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Double getDayHigh() {
        return dayHigh;
    }

    public void setDayHigh(Double dayHigh) {
        this.dayHigh = dayHigh;
    }

    public Double getDayLow() {
        return dayLow;
    }

    public void setDayLow(Double dayLow) {
        this.dayLow = dayLow;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}