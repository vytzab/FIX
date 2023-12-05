package lt.vytzab.engine;

import java.util.ArrayList;
import java.util.HashMap;

public class MarketController {
    private final HashMap<String, Market> markets = MarketCreator.createMarkets();

    private Market getMarket(String symbol) {
        return markets.get(symbol);
    }

    public boolean insert(MarketOrder order) {
        return getMarket(order.getSymbol()).insert(order);
    }

    public void match(String symbol, ArrayList<MarketOrder> orders) {
        getMarket(symbol).match(symbol, orders);
    }

    public MarketOrder find(String symbol, char side, String id) {
        return getMarket(symbol).find(symbol, side, id);
    }

    public void erase(MarketOrder order) {
        getMarket(order.getSymbol()).erase(order);
    }

    public void display() {
        for (String symbol : markets.keySet()) {
                getMarket(symbol).display(symbol);
        }
    }

}