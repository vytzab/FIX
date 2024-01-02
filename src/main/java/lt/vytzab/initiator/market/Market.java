package lt.vytzab.initiator.market;

import lt.vytzab.initiator.order.Order;
import lt.vytzab.initiator.order.OrderType;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Market {
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

    @Override
    public String toString() {
        return "Market{" +
                ", symbol='" + symbol + '\'' +
                ", lastPrice=" + lastPrice +
                ", dayHigh=" + dayHigh +
                ", dayLow=" + dayLow +
                ", buyVolume=" + buyVolume +
                ", sellVolume=" + sellVolume +
                '}';
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

    public Double getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(Double buyVolume) {
        this.buyVolume = buyVolume;
    }

    public Double getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(Double sellVolume) {
        this.sellVolume = sellVolume;
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