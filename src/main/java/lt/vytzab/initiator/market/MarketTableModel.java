package lt.vytzab.initiator.market;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MarketTableModel extends AbstractTableModel {
    private List<Market> markets = new ArrayList<>();
    private final static int SYMBOL = 0;
    private final static int LASTPRICE = 1;
    private final static int DAYHIGH = 2;
    private final static int DAYLOW = 3;
    private final static int BUYVOLUME = 4;
    private final static int SELLVOLUME = 5;
    private boolean filtered = false;

    private HashMap<Integer, Market> originalRowToMarket;
    private HashMap<String, Integer> originalSymbolToRow;
    private HashMap<Integer, Market> rowToMarket;
    private HashMap<String, Integer> symbolToRow;

    private final String[] headers;

    private List<Market> displayedMarkets;

    public MarketTableModel() {
        originalRowToMarket = new HashMap<>();
        originalSymbolToRow = new HashMap<>();
        markets = new ArrayList<>();
        rowToMarket = new HashMap<>();
        symbolToRow = new HashMap<>();

        headers = new String[]{"Symbol", "Last Price", "Day High", "Day Low", "Buy Volume", "Sell Volume"};
    }

    public void setOriginalMarkets(List<Market> markets) {
        originalRowToMarket.clear();
        originalSymbolToRow.clear();
        int row = 0;
        for (Market market : markets) {
            originalRowToMarket.put(row, market);
            originalSymbolToRow.put(market.getSymbol(), row);
            row++;
        }
    }

    public void setDisplayedMarkets(List<Market> markets) {
        displayedMarkets = new ArrayList<>(markets);
        fireTableDataChanged();
    }

    public void filterByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            // No filtering, show all orders
            rowToMarket = new HashMap<>(originalRowToMarket);
            symbolToRow = new HashMap<>(originalSymbolToRow);
            filtered = false;
        } else if (filtered) {
            //recreate original before filtering
            rowToMarket = new HashMap<>(originalRowToMarket);
            symbolToRow = new HashMap<>(originalSymbolToRow);
            originalRowToMarket = new HashMap<>(rowToMarket);
            originalSymbolToRow = new HashMap<>(symbolToRow);
            // Filter orders based on the keyword
            List<Market> filteredMarkets = rowToMarket.values().stream()
                    .filter(market -> marketMatchesKeyword(market, keyword))
                    .toList();
            rowToMarket = new HashMap<>();
            symbolToRow = new HashMap<>();
            int row = 0;
            for (Market market : filteredMarkets) {
                rowToMarket.put(row, market);
                symbolToRow.put(market.getSymbol(), row);
                row++;
            }
        } else if (!filtered) {
            originalRowToMarket = new HashMap<>(rowToMarket);
            originalSymbolToRow = new HashMap<>(symbolToRow);
            // Filter orders based on the keyword
            List<Market> filteredOrders = rowToMarket.values().stream()
                    .filter(market -> marketMatchesKeyword(market, keyword))
                    .toList();
            rowToMarket = new HashMap<>();
            symbolToRow = new HashMap<>();
            int row = 0;
            for (Market market : filteredOrders) {
                rowToMarket.put(row, market);
                symbolToRow.put(market.getSymbol(), row);
                row++;
            }
            filtered = true;
        }
        // Notify the table model about the data change
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
            symbolToRow.put(market.getSymbol(), row);
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

        rowToMarket.remove(row);
        symbolToRow.remove(symbol);

        fireTableRowsDeleted(row, row);
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

    public void cleanUp() {
        for (Market market : rowToMarket.values()) {
            removeMarket(market.getSymbol());
        }
    }
}