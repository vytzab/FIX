package lt.vytzab.engine.dao;

import lt.vytzab.engine.market.Market;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static lt.vytzab.engine.Variables.*;

public class MarketDataDAO {
    public static void createMarket(Market market) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "INSERT INTO market_data (symbol, last_price, day_high, day_low, buy_volume, sell_volume) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, market.getSymbol());
                statement.setDouble(2, market.getLastPrice());
                statement.setDouble(3, market.getDayHigh());
                statement.setDouble(4, market.getDayLow());
                statement.setDouble(5, market.getBuyVolume());
                statement.setDouble(5, market.getSellVolume());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Market readMarket(String symbol) {
        Market market = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM market_data WHERE symbol = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, symbol);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        market = new Market(
                                resultSet.getString("symbol"),
                                resultSet.getDouble("last_price"),
                                resultSet.getDouble("day_high"),
                                resultSet.getDouble("day_low"),
                                resultSet.getDouble("buy_volume"),
                                resultSet.getDouble("sell_volume")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (market == null) {
            System.out.println("Market not found.");
        }
        return market;
    }

    public static List<Market> readAllMarkets() {
        List<Market> markets = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM market_data";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Market market = new Market(
                                resultSet.getString("symbol"),
                                resultSet.getDouble("last_price"),
                                resultSet.getDouble("day_high"),
                                resultSet.getDouble("day_low"),
                                resultSet.getDouble("buy_volume"),
                                resultSet.getDouble("sell_volume")
                        );
                        markets.add(market);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (markets.isEmpty()) {
            System.out.println("Markets not found.");
        }
        return markets;
    }

    public static void updateMarket(Market market) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "UPDATE market_data SET last_price = ?, day_high = ?, day_low = ?, buy_volume = ?, sell_volume = ? WHERE symbol = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, market.getLastPrice());
                statement.setDouble(2, market.getDayHigh());
                statement.setDouble(3, market.getDayLow());
                statement.setDouble(4, market.getBuyVolume());
                statement.setDouble(4, market.getSellVolume());
                statement.setString(5, market.getSymbol());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Market Data Entry updated successfully.");
                } else {
                    System.out.println("No rows were updated. Market Data Entry not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMarket(String symbol) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "DELETE FROM market_data WHERE symbol = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, symbol);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Market Data Entry deleted successfully.");
                } else {
                    System.out.println("No rows were deleted. Market Data Entry not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
