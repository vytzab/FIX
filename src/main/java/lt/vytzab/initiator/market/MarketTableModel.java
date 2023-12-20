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
    private final static int VOLUME = 4;

    private final HashMap<Integer, Market> rowToMarket;
    private final HashMap<String, Integer> symbolToRow;

    private final String[] headers;

    public MarketTableModel() {
        markets = new ArrayList<>();
        rowToMarket = new HashMap<>();
        symbolToRow = new HashMap<>();

        headers = new String[]{"Symbol", "Last Price", "Day High", "Day Low", "Volume"};
    }

    public void addMarket(Market market) {
        if (getMarket(market.getSymbol()) == null) {
            int row = rowToMarket.size();
            markets.add(market);

            rowToMarket.put(row, market);
            symbolToRow.put(market.getSymbol(), row);

            fireTableRowsInserted(row, row);
        } else {
            replaceMarket(market, market.getSymbol());
        }
    }

    public void replaceMarket(Market market, String symbol) {
        Integer row = symbolToRow.get(symbol);
        if (row == null) {
            return;
        } else {
            rowToMarket.put(row, market);
            symbolToRow.put(market.getSymbol(), row);
        }
        fireTableRowsUpdated(row, row);
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
}