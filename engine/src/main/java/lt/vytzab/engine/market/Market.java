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
        return Objects.equals(getSymbol(), market.getSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSymbol());
    }
}