package lt.vytzab.engine.market;
import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.order.Order;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarketTableModel extends AbstractTableModel {
    private List<Market> markets = new ArrayList<>();
    private final static int SYMBOL = 0;
    private final static int LASTPRICE = 1;
    private final static int DAYHIGH = 2;
    private final static int DAYLOW = 3;
    private final static int BUYVOLUME = 4;
    private final static int SELLVOLUME = 5;
    private boolean filtered = false;

    private ConcurrentHashMap<Integer, Market> originalRowToMarket;
    private ConcurrentHashMap<String, Integer> originalSymbolToRow;
    private ConcurrentHashMap<Integer, Market> rowToMarket;
    private ConcurrentHashMap<String, Integer> symbolToRow;

    private final String[] headers;

    private List<Market> displayedMarkets;

    public MarketTableModel() {
        originalRowToMarket = new ConcurrentHashMap<>();
        originalSymbolToRow = new ConcurrentHashMap<>();
        markets = new ArrayList<>();
        rowToMarket = new ConcurrentHashMap<>();
        symbolToRow = new ConcurrentHashMap<>();

        headers = new String[]{"Symbol", "Last Price", "Day High", "Day Low", "Buy Volume", "Sell Volume"};
    }

    public void filterByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            rowToMarket = new ConcurrentHashMap<>(originalRowToMarket);
            symbolToRow = new ConcurrentHashMap<>(originalSymbolToRow);
            filtered = false;
        } else if (filtered) {
            rowToMarket = new ConcurrentHashMap<>(originalRowToMarket);
            symbolToRow = new ConcurrentHashMap<>(originalSymbolToRow);
            originalRowToMarket = new ConcurrentHashMap<>(rowToMarket);
            originalSymbolToRow = new ConcurrentHashMap<>(symbolToRow);
            List<Market> filteredMarkets = rowToMarket.values().stream()
                    .filter(market -> marketMatchesKeyword(market, keyword))
                    .toList();
            rowToMarket = new ConcurrentHashMap<>();
            symbolToRow = new ConcurrentHashMap<>();
            int row = 0;
            for (Market market : filteredMarkets) {
                rowToMarket.put(row, market);
                symbolToRow.put(market.getSymbol(), row);
                row++;
            }
        } else {
            originalRowToMarket = new ConcurrentHashMap<>(rowToMarket);
            originalSymbolToRow = new ConcurrentHashMap<>(symbolToRow);
            List<Market> filteredOrders = rowToMarket.values().stream()
                    .filter(market -> marketMatchesKeyword(market, keyword))
                    .toList();
            rowToMarket = new ConcurrentHashMap<>();
            symbolToRow = new ConcurrentHashMap<>();
            int row = 0;
            for (Market market : filteredOrders) {
                rowToMarket.put(row, market);
                symbolToRow.put(market.getSymbol(), row);
                row++;
            }
            filtered = true;
        }
        fireTableDataChanged();
    }

    private boolean marketMatchesKeyword(Market market, String keyword) {
        return market.getSymbol().toLowerCase().contains(keyword.toLowerCase())
                || market.getLastPrice().toString().toLowerCase().contains(keyword.toLowerCase())
                || market.getDayHigh().toString().toLowerCase().contains(keyword.toLowerCase())
                || market.getDayLow().toString().toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(market.getBuyVolume()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(market.getSellVolume()).toLowerCase().contains(keyword.toLowerCase());
    }

    public void addMarket(Market market) {
        if (getMarket(market.getSymbol()) == null) {
            int row = rowToMarket.size();
            markets.add(market);
            rowToMarket.put(row, market);
            symbolToRow.put(market.getSymbol(), row);

            fireTableRowsInserted(row, row);
        }
        replaceMarket(market, market.getSymbol());
    }

    public void replaceMarket(Market market, String symbol) {
        Integer row = symbolToRow.get(symbol);
        if (row == null) {
            return;
        } else {
            rowToMarket.put(row, market);
            symbolToRow.put(symbol, row);
            markets.set(row, market);
        }

        fireTableRowsUpdated(row, row);
    }

    public Market getMarket(String symbol) {
        Integer row = symbolToRow.get(symbol);
        return (row != null) ? rowToMarket.get(row) : null;
    }

    public Market getMarket(int row) {
        return rowToMarket.get(row);
    }

    public void removeMarket(String symbol) {
        Integer row = symbolToRow.get(symbol);
        if (row == null) return;

        markets.remove(row.intValue());
        rowToMarket.remove(row);
        symbolToRow.remove(symbol);

        updateRowIndices(row);

        fireTableRowsDeleted(row, row);
    }

    private void updateRowIndices(int removedRow) {
        // Create a copy of the entry set to avoid ConcurrentModificationException
        Set<Map.Entry<Integer, Market>> entrySetCopy = new HashSet<>(rowToMarket.entrySet());

        for (Map.Entry<Integer, Market> entry : entrySetCopy) {
            Integer row = entry.getKey();
            if (row > removedRow) {
                Market market = entry.getValue();
                String symbol = market.getSymbol();

                // Update symbolToRow map
                symbolToRow.put(symbol, row - 1);

                // Update rowToMarket map
                rowToMarket.put(row - 1, market);
            }
        }
    }

    public int getRowCount() {
        return rowToMarket.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Market market = rowToMarket.get(rowIndex);

        if (market != null) {
            switch (columnIndex) {
                case SYMBOL:
                    return market.getSymbol();
                case LASTPRICE:
                    return market.getLastPrice();
                case DAYHIGH:
                    return market.getDayHigh();
                case DAYLOW:
                    return market.getDayLow();
                case BUYVOLUME:
                    return market.getBuyVolume();
                case SELLVOLUME:
                    return market.getSellVolume();
                default:
                    return "";
            }
        }
        return "";
    }

    public void clearMarkets() {
        int start = 0;
        int end = markets.size();
        for (Market market : markets) {
            rowToMarket.values().remove(market);

        }
        fireTableRowsDeleted(start, end);
    }

    public void fillMarkets() {
        int start = 0;
        int end = markets.size();
        for (Market market : markets) {
            int row = rowToMarket.size();
            rowToMarket.put(row, market);
            symbolToRow.put(market.getSymbol(), row);

        }
        fireTableRowsDeleted(start, end);
    }

    public void setMarkets(List<Market> updatedMarkets) {
        markets = updatedMarkets;
        fillMarkets();
    }
}