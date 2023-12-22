package lt.vytzab.engine.dao;

import lt.vytzab.engine.order.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static lt.vytzab.engine.Variables.*;

public class MarketOrderDAO {
    public static boolean createMarketOrder(Order order, String tableName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "INSERT INTO " + tableName + " (clOrdID, symbol, senderCompID, targetCompID, side, ordType, price, quantity, " +
                    "openQuantity, executedQuantity, avgExecutedPrice, lastExecutedPrice, lastExecutedQuantity, entryTime, rejected, canceled, entryDate, goodTillDate, tif) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                statement.setBoolean(15, order.getRejected());
                statement.setBoolean(16, order.getCanceled());
                statement.setObject(17, order.getEntryDate());
                statement.setObject(18, order.getGoodTillDate());
                statement.setObject(18, order.getTif());

                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Order> readAllMarketOrdersBySenderCompID(String senderCompID, String tableName) {
        List<Order> orders = new ArrayList<>();
        Order order = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM " + tableName + " WHERE senderCompID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, senderCompID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        order = new Order(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("tif").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity"),
                                resultSet.getBoolean("rejected"),
                                resultSet.getBoolean("canceled"),
                                resultSet.getDate("entryDate").toLocalDate(),
                                resultSet.getDate("goodTillDate").toLocalDate()
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (order == null) {
            System.out.println("Order not found.");
        }
        return orders;
    }

    public static List<Order> readAllMarketOrdersByField(String column, String value, String tableName) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM " + tableName + " market_orders WHERE " + column + " = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Order order = new Order(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("tif").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity"),
                                resultSet.getBoolean("rejected"),
                                resultSet.getBoolean("canceled"),
                                resultSet.getDate("entryDate").toLocalDate(),
                                resultSet.getDate("goodTillDate").toLocalDate()
                        );
                        orders.add(order);
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

    public static List<Order> readAllMarketOrders(String tableName) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM " + tableName;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Order order = new Order(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("tif").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity"),
                                resultSet.getBoolean("rejected"),
                                resultSet.getBoolean("canceled"),
                                resultSet.getDate("entryDate").toLocalDate(),
                                resultSet.getDate("goodTillDate").toLocalDate()
                        );
                        orders.add(order);
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

    public static List<Order> readAllMarketOrdersBySymbol(String symbol, String tableName) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM " + tableName + " WHERE symbol = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, symbol);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Order order = new Order(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("tif").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity"),
                                resultSet.getBoolean("rejected"),
                                resultSet.getBoolean("canceled"),
                                resultSet.getDate("entryDate").toLocalDate(),
                                resultSet.getDate("goodTillDate").toLocalDate()
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (orders.isEmpty()) {
            System.out.println("No orders found.");
        }
        return orders;
    }

    public static void updateMarketOrder(Order order, String tableName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "UPDATE " + tableName + " SET symbol = ?, senderCompID = ?, targetCompID = ?, side = ?," +
                    " ordType = ?, price = ?, quantity = ?, openQuantity = ?, executedQuantity = ?, avgExecutedPrice = ?," +
                    " lastExecutedPrice = ?, lastExecutedQuantity = ?, entryTime = ?, rejected = ?, canceled = ?, goodTillDate = ?, tif = ?  WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, order.getSymbol());
                statement.setString(2, order.getSenderCompID());
                statement.setString(3, order.getTargetCompID());
                statement.setString(4, String.valueOf(order.getSide()));
                statement.setString(5, String.valueOf(order.getOrdType()));
                statement.setDouble(6, order.getPrice());
                statement.setLong(7, order.getQuantity());
                statement.setLong(8, order.getOpenQuantity());
                statement.setLong(9, order.getExecutedQuantity());
                statement.setDouble(10, order.getAvgExecutedPrice());
                statement.setDouble(11, order.getLastExecutedPrice());
                statement.setLong(12, order.getLastExecutedQuantity());
                statement.setLong(13, order.getEntryTime());
                statement.setBoolean(14, order.getRejected());
                statement.setBoolean(15, order.getCanceled());
                statement.setObject(16, order.getGoodTillDate());
                statement.setObject(16, order.getTif());
                statement.setString(17, order.getClOrdID());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Market Order updated successfully.");
                } else {
                    System.out.println("No rows were updated. Order not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Order getOrderByClOrdID(String clOrdID, String tableName){
        Order order = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM " + tableName + " WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clOrdID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        order = new Order(
                                resultSet.getLong("entryTime"),
                                resultSet.getString("clOrdID"),
                                resultSet.getString("symbol"),
                                resultSet.getString("senderCompID"),
                                resultSet.getString("targetCompID"),
                                resultSet.getString("side").charAt(0),
                                resultSet.getString("tif").charAt(0),
                                resultSet.getString("ordType").charAt(0),
                                resultSet.getDouble("price"),
                                resultSet.getLong("quantity"),
                                resultSet.getLong("openQuantity"),
                                resultSet.getLong("executedQuantity"),
                                resultSet.getDouble("avgExecutedPrice"),
                                resultSet.getDouble("lastExecutedPrice"),
                                resultSet.getLong("lastExecutedQuantity"),
                                resultSet.getBoolean("rejected"),
                                resultSet.getBoolean("canceled"),
                                resultSet.getDate("entryDate").toLocalDate(),
                                resultSet.getDate("goodTillDate").toLocalDate()
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (order == null) {
            System.out.println("Order not found.");
        }
        return order;
    }

    public static void deleteOrderByClOrdID(String clOrdID, String tableName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String sql = "DELETE FROM " + tableName + " WHERE clOrdID = ?";

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
