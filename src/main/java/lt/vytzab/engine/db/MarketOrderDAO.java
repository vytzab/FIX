package lt.vytzab.engine.db;

import lt.vytzab.engine.Market;
import lt.vytzab.engine.MarketOrder;

import java.sql.*;
import java.util.HashMap;

import static lt.vytzab.engine.Variables.*;

public class MarketOrderDAO {
    public static void createMarketOrder(MarketOrder order) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "INSERT INTO market_orders (clOrdID, symbol, senderCompID, targetCompID, side, ordType, price, quantity, " +
                    "openQuantity, executedQuantity, avgExecutedPrice, lastExecutedPrice, lastExecutedQuantity, entryTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, order.getClOrdID());
                statement.setString(2, order.getSymbol());
                statement.setString(3, order.getSenderCompID());
                statement.setString(4, order.getTargetCompID());
                statement.setString(5, String.valueOf(order.getSide()));
                statement.setString(6, String.valueOf(order.getOrdType()));
                statement.setDouble(7, order.getPrice());
                statement.setLong(8, order.getQuantity());
                statement.setLong(9, order.getOpenQuantity());
                statement.setLong(10, order.getExecutedQuantity());
                statement.setDouble(11, order.getAvgExecutedPrice());
                statement.setDouble(12, order.getLastExecutedPrice());
                statement.setLong(13, order.getLastExecutedQuantity());
                statement.setLong(14, order.getEntryTime());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static MarketOrder readMarketOrder(String clOrdID) {
        MarketOrder order = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM market_orders WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clOrdID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        order = new MarketOrder(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (order == null) {
            System.out.println("Order not found.");
        }
        return order;
    }

    public static HashMap<String, MarketOrder> readAllMarketOrdersByField(String key) {
        HashMap<String, MarketOrder> orders = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM market_orders WHERE " + key + " = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        MarketOrder order = new MarketOrder(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity")
                        );
                        orders.put(key, order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (orders.isEmpty()) {
            System.out.println("Orders not found.");
        }
        return orders;
    }

    public static HashMap<String, MarketOrder> readAllMarketOrders() {
        HashMap<String, MarketOrder> orders = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM market_orders";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        MarketOrder order = new MarketOrder(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity")
                        );
                        orders.put(order.getSenderCompID(), order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (orders.isEmpty()) {
            System.out.println("Orders not found.");
        }
        return orders;
    }

    public static void updateMarketOrder(long entryTime, String clOrdID, String symbol, String senderCompID, String targetCompID, char side, char ordType, double price, long quantity, long openQuantity, long executedQuantity, double avgExecutedPrice, double lastExecutedPrice, long lastExecutedQuantity) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "UPDATE market_orders SET clOrdID = ?, symbol = ?, senderCompID = ?, targetCompID = ?, side = ?," +
                    " ordType = ?, price = ?, quantity = ?, openQuantity = ?, executedQuantity = ?, avgExecutedPrice = ?," +
                    " lastExecutedPrice = ?, lastExecutedQuantity = ?, entryTime = ?  WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, entryTime);
                statement.setString(2, symbol);
                statement.setString(2, senderCompID);
                statement.setString(2, targetCompID);
                statement.setString(2, String.valueOf(side));
                statement.setString(3, String.valueOf(ordType));
                statement.setDouble(4, price);
                statement.setLong(1, quantity);
                statement.setLong(1, openQuantity);
                statement.setLong(1, executedQuantity);
                statement.setDouble(4, avgExecutedPrice);
                statement.setDouble(4, lastExecutedPrice);
                statement.setLong(1, lastExecutedQuantity);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Market Order deleted successfully.");
                } else {
                    System.out.println("No rows were updated. Order not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMarketOrder(String clOrdID) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "DELETE FROM market_orders WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clOrdID);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Market Order deleted successfully.");
                } else {
                    System.out.println("No rows were deleted. Order not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
