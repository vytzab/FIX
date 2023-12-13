package lt.vytzab.engine.db;

import lt.vytzab.engine.Market;
import lt.vytzab.engine.MarketCreator;

import java.sql.*;
import java.util.HashMap;

import static lt.vytzab.engine.Variables.*;

public class MarketDataDAO {
    public static void createMarket(Market market) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "INSERT INTO market_data (symbol, last_price, day_high, day_low, volume) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, market.getSymbol());
                statement.setDouble(2, market.getLastPrice());
                statement.setDouble(3, market.getDayHigh());
                statement.setDouble(4, market.getDayLow());
                statement.setInt(5, market.getVolume());
                // Set other parameters using market data
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
                                resultSet.getInt("volume")
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

    public static HashMap<String, Market> readAllMarkets() {
        HashMap<String, Market> markets = new HashMap<>();
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
                                resultSet.getInt("volume")
                        );
                        markets.put(market.getSymbol(), market);
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

    public static void updateMarket(String symbol, double lastPrice, double dayHigh, double dayLow, int volume) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "UPDATE market_data SET last_price = ?, day_high = ?, day_low = ?, volume = ? WHERE symbol = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, lastPrice);
                statement.setDouble(2, dayHigh);
                statement.setDouble(3, dayLow);
                statement.setInt(4, volume);
                statement.setString(5, symbol);

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
