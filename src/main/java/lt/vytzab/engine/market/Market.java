package lt.vytzab.engine.market;

import lt.vytzab.engine.order.Order;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Market {
    private List<Order> bidOrders = new ArrayList<>();
    private List<Order> askOrders = new ArrayList<>();
    private String symbol;
    private Double lastPrice;
    private Double dayHigh;
    private Double dayLow;
    private Double buyVolume = 0.0;
    private Double sellVolume = 0.0;

    public Market(String symbol, Double lastPrice, Double dayHigh, Double dayLow, Double buyVolume, Double sellVolume) {
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.dayHigh = dayHigh;
        this.dayLow = dayLow;
        this.buyVolume = buyVolume;
        this.sellVolume = sellVolume;
    }

    public Market() {
    }

    public boolean match(String symbol, List<Order> orders) {
        while (true) {
            if (bidOrders.isEmpty() || askOrders.isEmpty()) {
                return !orders.isEmpty();
            }
            Order bidOrder = bidOrders.get(0);
            Order askOrder = askOrders.get(0);
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

    private void match(Order bid, Order ask) {
        double price = ask.getOrdType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = Math.min(bid.getOpenQuantity(), ask.getOpenQuantity());

        bid.execute(price, quantity);
        ask.execute(price, quantity);
        lastPrice = bid.getAvgExecutedPrice();
        buyVolume += bid.getQuantity();
        sellVolume += ask.getQuantity();
    }

    public boolean insert(Order order) {
        return order.getSide() == Side.BUY ? insert(order, true, bidOrders) : insert(order, false, askOrders);
    }

    private boolean insert(Order newOrder, boolean descending, List<Order> activeOrders) {
        if (activeOrders.isEmpty()) {
            activeOrders.add(newOrder);
        } else if (newOrder.getOrdType() == OrdType.MARKET) {
            activeOrders.add(0, newOrder);
        } else {
            for (int i = 0; i < activeOrders.size(); i++) {
                Order activeOrder = activeOrders.get(i);
                //If newOrder.side = BUY and price is higher than an active selling orders -
                if ((descending ? newOrder.getPrice() > activeOrder.getPrice() : newOrder.getPrice() < activeOrder.getPrice()) && newOrder.getEntryTime() < activeOrder.getEntryTime()) {
                    activeOrders.add(i, newOrder);
                }
            }
            activeOrders.add(newOrder);
        }
        return true;
    }

    public void erase(Order order) {
        if (order.getSide() == Side.BUY) {
            bidOrders.remove(find(bidOrders, order.getClOrdID()));
        } else {
            askOrders.remove(find(askOrders, order.getClOrdID()));
        }
    }

    public Order find(String symbol, char side, String id) {
        return find(side == Side.BUY ? bidOrders : askOrders, id);
    }

    private Order find(List<Order> orders, String clientOrderId) {
        for (Order order : orders) {
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

    private void displaySide(List<Order> orders, String title) {
        DecimalFormat priceFormat = new DecimalFormat("#.00");
        DecimalFormat qtyFormat = new DecimalFormat("######");
        System.out.println(title + ":\n----");
        for (Order order : orders) {
            System.out.println(qtyFormat.format(order.getOpenQuantity()) + " x " + priceFormat.format(order.getPrice()) + "$  |" + " - entered by " + order.getSenderCompID() + " at " + new Date(order.getEntryTime()));
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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

    public double getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(double buyVolume) {
        this.buyVolume = buyVolume;
    }
    public double getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(double sellVolume) {
        this.sellVolume = sellVolume;
    }

    public List<Order> getBidOrders() {
        return bidOrders;
    }

    public List<Order> getAskOrders() {
        return askOrders;
    }

    public void setBidOrders(List<Order> bidOrders) {
        this.bidOrders = bidOrders;
    }

    public void setAskOrders(List<Order> askOrders) {
        this.askOrders = askOrders;
    }

    @Override
    public String toString() {
        return "Market{" +
                "bidOrders=" + bidOrders +
                ", askOrders=" + askOrders +
                ", symbol='" + symbol + '\'' +
                ", lastPrice=" + lastPrice +
                ", dayHigh=" + dayHigh +
                ", dayLow=" + dayLow +
                ", buyVolume=" + buyVolume +
                ", sellVolume=" + sellVolume +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Market market)) return false;
        return Objects.equals(getBidOrders(), market.getBidOrders()) && Objects.equals(getAskOrders(), market.getAskOrders()) && Objects.equals(getSymbol(), market.getSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBidOrders(), getAskOrders(), getSymbol(), getLastPrice(), getDayHigh(), getDayLow(), getBuyVolume(), getSellVolume());
    }
}