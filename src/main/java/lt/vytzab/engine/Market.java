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
            if (bidOrders.size() == 0 || askOrders.size() == 0) {
                return orders.size() != 0;
            }
            MarketOrder bidOrder = bidOrders.get(0);
            MarketOrder askOrder = askOrders.get(0);
            if (bidOrder.getType() == OrdType.MARKET || askOrder.getType() == OrdType.MARKET
                    || (bidOrder.getPrice() >= askOrder.getPrice())) {
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
            } else
                return orders.size() != 0;
        }
    }

    private void match(MarketOrder bid, MarketOrder ask) {
        double price = ask.getType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = bid.getOpenQuantity() >= ask.getOpenQuantity() ? ask.getOpenQuantity() : bid.getOpenQuantity();

        bid.execute(price, quantity);
        ask.execute(price, quantity);
    }

    public boolean insert(MarketOrder order) {
        return order.getSide() == Side.BUY ? insert(order, true, bidOrders) : insert(order, false, askOrders);
    }

    private boolean insert(MarketOrder order, boolean descending, List<MarketOrder> orders) {
        if (orders.size() == 0) {
            orders.add(order);
        } else if (order.getType() == OrdType.MARKET) {
            orders.add(0, order);
        } else {
            for (int i = 0; i < orders.size(); i++) {
                MarketOrder o = orders.get(i);
                if ((descending ? order.getPrice() > o.getPrice() : order.getPrice() < o.getPrice())
                        && order.getEntryTime() < o.getEntryTime()) {
                    orders.add(i, order);
                }
            }
            orders.add(order);
        }
        return true;
    }

    public void erase(MarketOrder order) {
        if (order.getSide() == Side.BUY) {
            bidOrders.remove(find(bidOrders, order.getClientOrderId()));
        } else {
            askOrders.remove(find(askOrders, order.getClientOrderId()));
        }
    }

    public MarketOrder find(String symbol, char side, String id) {
        return find(side == Side.BUY ? bidOrders : askOrders, id);
    }

    private MarketOrder find(List<MarketOrder> orders, String clientOrderId) {
        for (MarketOrder order : orders) {
            if (order.getClientOrderId().equals(clientOrderId)) {
                return order;
            }
        }
        return null;
    }

    public void display() {
        displaySide(bidOrders, "BIDS");
        displaySide(askOrders, "ASKS");
    }

    private void displaySide(List<MarketOrder> orders, String title) {
        DecimalFormat priceFormat = new DecimalFormat("#.00");
        DecimalFormat qtyFormat = new DecimalFormat("######");
        System.out.println(title + ":\n----");
        for (MarketOrder order : orders) {
            System.out.println(qtyFormat.format(order.getOpenQuantity()) + " x " + priceFormat.format(order.getPrice()) + "$  |"
                    +  " - entered by " + order.getOwner() + " at " + new Date(order.getEntryTime()));
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