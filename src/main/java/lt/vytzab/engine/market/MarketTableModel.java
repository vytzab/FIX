package lt.vytzab.engine.market;

import lt.vytzab.engine.dao.MarketDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

public class MarketTableModel extends AbstractTableModel {
    private List<Market> markets;
    private List<Market> originalMarkets;
    private List<Market> sortedMarkets;
    private final static int SYMBOL = 0;
    private final static int LASTPRICE = 1;
    private final static int DAYHIGH = 2;
    private final static int DAYLOW = 3;
    private final static int BUYVOLUME = 4;
    private final static int SELLVOLUME = 5;

    private final String[] headers;

    public MarketTableModel() {
        markets = new ArrayList<>();
        originalMarkets = new ArrayList<>();
        sortedMarkets = new ArrayList<>();
        headers = new String[]{"Symbol", "Last Price", "Day High", "Day Low", "Buy Volume", "Sell Volume"};
    }

    public void filterByKeyword(String keyword) {
        List<Market> filteredMarkets = originalMarkets.stream()
                .filter(market -> marketMatchesKeyword(market, keyword))
                .toList();

        markets = new ArrayList<>(filteredMarkets);

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
        if (!markets.contains(market)) {
            markets.add(market);
            originalMarkets.add(market); // Add to originalMarkets as well
            fireTableRowsInserted(markets.indexOf(market), markets.indexOf(market));
        }
        replaceMarket(market);
    }

    public void replaceMarket(Market market) {
        int index = markets.indexOf(market);

        if (index != -1) {
            markets.set(index, market);
            originalMarkets.set(index, market);
            fireTableRowsUpdated(index, index);
        } else {
            addMarket(market);
        }
    }

    public Market getMarket(String symbol) {
        return markets.stream()
                .filter(market -> market.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }

    public Market getMarket(int row) {
        return row < markets.size() ? markets.get(row) : null;
    }

    public void removeMarket(String symbol) {
        Market marketToRemove = getMarket(symbol);
        if (marketToRemove != null) {
            int index = markets.indexOf(marketToRemove);
            markets.remove(index);
            originalMarkets.remove(index);

            fireTableRowsDeleted(index, index);
        }
    }

    public int getRowCount() {
        return markets.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Market market = getMarket(rowIndex);

        if (market != null) {
            return switch (columnIndex) {
                case SYMBOL -> market.getSymbol();
                case LASTPRICE -> formatDouble(market.getLastPrice());
                case DAYHIGH -> formatDouble(market.getDayHigh());
                case DAYLOW -> formatDouble(market.getDayLow());
                case BUYVOLUME -> formatDouble(market.getBuyVolume());
                case SELLVOLUME -> formatDouble(market.getSellVolume());
                default -> "";
            };
        }
        return "";
    }

    private String formatDouble(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(value);
    }

    public void clearMarkets() throws SQLException {
        for (Market market : originalMarkets) {
            MarketDAO.createHistoricMarketDataEntry(market);
            market.setSellVolume(0);
            market.setBuyVolume(0);
            MarketDAO.updateMarket(market);
        }
        markets.clear();
    }

    public void setMarkets(List<Market> updatedMarkets) {
        markets = new ArrayList<>(updatedMarkets);

        if (originalMarkets == null) {
            originalMarkets = new ArrayList<>(updatedMarkets);
        } else {
            originalMarkets.clear();
            originalMarkets.addAll(updatedMarkets);
        }

        fireTableDataChanged();
    }

    public void setSortedMarkets(int columnIndex, SortOrder sortOrder) {
        markets = sortMarkets(columnIndex, sortOrder);

        fireTableDataChanged();
    }

    private List<Market> sortMarkets(int columnIndex, SortOrder sortOrder) {
        sortedMarkets = new ArrayList<>(markets);

        MarketComparator comparator = new MarketComparator(columnIndex);

        sortedMarkets.sort((market1, market2) -> {
            int result = comparator.compare(market1, market2);
            return (sortOrder == SortOrder.DESCENDING) ? -result : result;
        });
        return sortedMarkets;
    }

    private String marketToCSVString(Market market) {
        DecimalFormat volumeFormat = new DecimalFormat("###.##");

        return String.format("%s,%.2f,%.2f,%.2f,%s,%s,%d,%d",
                market.getSymbol(),
                market.getLastPrice(),
                market.getDayHigh(),
                market.getDayLow(),
                volumeFormat.format(market.getBuyVolume()),
                volumeFormat.format(market.getSellVolume()),
                market.getBidOrders().size(),
                market.getAskOrders().size());
    }

    public void generateReport(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Symbol,Last Price,Day High,Day Low,Buy Volume,Sell Volume");
            writer.newLine();

            for (Market market : markets) {
                writer.write(marketToCSVString(market));
                writer.newLine();
            }

            System.out.println("CSV file exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isMarketsEmpty() {
        return markets.isEmpty();
    }

    private record MarketComparator(int columnIndex) implements Comparator<Market> {
        @Override
            public int compare(Market market1, Market market2) {
            return switch (columnIndex) {
                case SYMBOL -> market1.getSymbol().compareTo(market2.getSymbol());
                case LASTPRICE -> Double.compare(market1.getLastPrice(), market2.getLastPrice());
                case DAYHIGH -> Double.compare(market1.getDayHigh(), market2.getDayHigh());
                case DAYLOW -> Double.compare(market1.getDayLow(), market2.getDayLow());
                case BUYVOLUME -> Double.compare(market1.getBuyVolume(), market2.getBuyVolume());
                case SELLVOLUME -> Double.compare(market1.getSellVolume(), market2.getSellVolume());
                default -> 0;
            };
            }
        }
}