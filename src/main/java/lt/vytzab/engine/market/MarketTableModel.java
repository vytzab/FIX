package lt.vytzab.engine.market;
import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.order.Order;

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
    private final static int VOLUME = 4;
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
            filtered = true;
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

    public void updateMarket(Market market, String symbol) {
        if (!symbol.equals(market.getSymbol())) {
            String originalSymbol = market.getSymbol();
            market.setSymbol(symbol);
            replaceMarket(market, originalSymbol);
            return;
        }

        Integer row = symbolToRow.get(market.getSymbol());
        if (row == null) {
            return;
        } else {
            markets.set(row, market);
        }
        fireTableRowsUpdated(row, row);
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

        markets.remove(row.intValue());
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
                case 0:
                    return market.getSymbol();
                case 1:
                    return market.getLastPrice();
                case 2:
                    return market.getDayHigh();
                case 3:
                    return market.getDayLow();
                case 4:
                    return market.getBuyVolume();
                case 5:
                    return market.getSellVolume();
                default:
                    return "";
            }
        }
        return "";
    }

    public void getMarketsFromDB() {
        List<Market> marketList = MarketDataDAO.readAllMarkets();
        for (Market market : marketList) {
            addMarket(market);
        }
    }

    public void saveMarketsToDB() {
        List<Market> databaseMarkets = MarketDataDAO.readAllMarkets(); // Assuming this method fetches markets from the database

        // Compare lists and identify changes
        List<Market> marketsToAdd = new ArrayList<>();
        List<Market> marketsToDelete = new ArrayList<>();
        List<Market> marketsToUpdate = new ArrayList<>();

        for (Market appMarket : markets) {
            Market dbMarket = findMarketInList(appMarket, databaseMarkets);

            if (dbMarket == null) {
                // Market is in the application list but not in the database
                marketsToAdd.add(appMarket);
            } else if (!appMarket.equals(dbMarket)) {
                // Market is in both lists, but attributes have changed
                marketsToUpdate.add(appMarket);
            }
        }

        for (Market dbMarket : databaseMarkets) {
            if (!markets.contains(dbMarket)) {
                // Market is in the database but not in the application list
                marketsToDelete.add(dbMarket);
            }
        }

        // Update the database
        insertMarkets(marketsToAdd);
        updateMarkets(marketsToUpdate);
        deleteMarkets(marketsToDelete);
    }

    private Market findMarketInList(Market targetMarket, List<Market> marketList) {
        for (Market market : marketList) {
            if (targetMarket==market) {
                return market;
            }
        }
        return null;
    }

    private void insertMarkets(List<Market> markets) {
        for (Market market : markets) {
            MarketDataDAO.createMarket(market); // Assuming this method inserts a market into the database
        }
    }

    private void updateMarkets(List<Market> markets) {
        for (Market market : markets) {
            MarketDataDAO.updateMarket(market); // Assuming this method updates a market in the database
        }
    }

    private void deleteMarkets(List<Market> markets) {
        for (Market market : markets) {
            MarketDataDAO.deleteMarket(market.getSymbol()); // Assuming this method deletes a market from the database based on symbol
        }
    }
}