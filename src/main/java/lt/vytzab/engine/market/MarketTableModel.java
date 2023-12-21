package lt.vytzab.engine.market;
import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.market.Market;

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

    private final HashMap<Integer, Market> rowToMarket;
    private final HashMap<String, Integer> symbolToRow;

    private final String[] headers;

    public MarketTableModel() {
        markets = new ArrayList<>();
        rowToMarket = new HashMap<>();
        symbolToRow = new HashMap<>();

        headers = new String[]{"Symbol", "Last Price", "Day High", "Day Low", "Buy Volume", "Sell Volume"};
    }

    public void addMarket(Market market) {
        int row = rowToMarket.size();
        markets.add(market);

        rowToMarket.put(row, market);
        symbolToRow.put(market.getSymbol(), row);

        fireTableRowsInserted(row, row);
    }

    public void updateMarket(Market market, String symbol) {
        if (!symbol.equals(market.getSymbol())) {
            String originalSymbol = market.getSymbol();
            market.setSymbol(symbol);
            replaceMarket(market, originalSymbol);
            return;
        }

        Integer row = symbolToRow.get(market.getSymbol());
        if (row == null) return;
        fireTableRowsUpdated(row, row);
    }

    public void replaceMarket(Market market, String symbol) {
        Integer row = symbolToRow.get(symbol);
        if (row == null) return;

        rowToMarket.put(row, market);
        symbolToRow.put(market.getSymbol(), row);

        fireTableRowsUpdated(row, row);
    }

    public Market getMarket(int row) {
        return rowToMarket.get(row);
    }

    public Market getMarket(String symbol) {
        Integer row = symbolToRow.get(symbol);
        return (row != null) ? rowToMarket.get(row) : null;
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