package lt.vytzab.engine.dao;

import lt.vytzab.engine.Variables;
import lt.vytzab.engine.order.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static lt.vytzab.engine.Variables.*;

public class MarketOrderDAO {
    public static boolean createMarketOrder(Order order) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "INSERT INTO market_orders (clOrdID, symbol, senderCompID, targetCompID, side, ordType, price, quantity, " +
                    "openQuantity, executedQuantity, avgExecutedPrice, lastExecutedPrice, lastExecutedQuantity, entryTime, rejected, canceled, entryDate, goodTillDate, tif) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, order.getClOrdID());
                statement.setString(2, order.getSymbol());
                statement.setString(3, order.getSenderCompID());
                statement.setString(4, order.getTargetCompID());
                statement.setString(5, String.valueOf(order.getSide()));
                statement.setString(6, String.valueOf(order.getType()));
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
                statement.setString(19, String.valueOf(order.getTif()));

                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Order getOrderByClOrdID(String clOrdID) {
        Order order = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "SELECT * FROM market_orders WHERE clOrdID = ?";

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
                                resultSet.getDate("goodTillDate").toLocalDate(),
                                resultSet.getString("tif").charAt(0)
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    public static boolean updateMarketOrder(Order order) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "UPDATE market_orders SET symbol = ?, senderCompID = ?, targetCompID = ?, side = ?," +
                    " ordType = ?, price = ?, quantity = ?, openQuantity = ?, executedQuantity = ?, avgExecutedPrice = ?," +
                    " lastExecutedPrice = ?, lastExecutedQuantity = ?, entryTime = ?, rejected = ?, canceled = ?, goodTillDate = ?, tif = ?  WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, order.getSymbol());
                statement.setString(2, order.getSenderCompID());
                statement.setString(3, order.getTargetCompID());
                statement.setString(4, String.valueOf(order.getSide()));
                statement.setString(5, String.valueOf(order.getType()));
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
                statement.setString(17, String.valueOf(order.getTif()));
                statement.setString(18, order.getClOrdID());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteOrderByClOrdID(String clOrdID) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "DELETE FROM market_orders WHERE clOrdID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clOrdID);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Order> readAllMarketOrders() {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "SELECT * FROM market_orders";

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
                                resultSet.getDate("goodTillDate").toLocalDate(),
                                resultSet.getString("tif").charAt(0)
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<Order> readAllMarketOrdersBySenderCompID(String senderCompID) {
        List<Order> orders = new ArrayList<>();
        Order order = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "SELECT * FROM market_orders WHERE senderCompID = ?";

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
                                resultSet.getDate("goodTillDate").toLocalDate(),
                                resultSet.getString("tif").charAt(0)
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (order == null) {
            System.out.println("Orders not found.");
        }
        return orders;
    }

    public static List<Order> readAllMarketOrdersByField(String column, String value) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "SELECT * FROM market_orders WHERE " + column + " = ?";

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
                                resultSet.getDate("goodTillDate").toLocalDate(),
                                resultSet.getString("tif").charAt(0)
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<Order> readAllMarketOrdersBySymbol(String symbol) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "SELECT * FROM market_orders WHERE symbol = ?";

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
                                resultSet.getDate("goodTillDate").toLocalDate(),
                                resultSet.getString("tif").charAt(0)
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<Order> readAllMarketOrdersBySymbolAndSender(String symbol, String senderCompID) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, Variables.getUsername(), Variables.getPassword())) {
            String sql = "SELECT * FROM market_orders WHERE symbol = ? AND senderCompID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, symbol);
                statement.setString(2, senderCompID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Order order = new Order(
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
                                resultSet.getLong("lastExecutedQuantity"),
                                resultSet.getBoolean("rejected"),
                                resultSet.getBoolean("canceled"),
                                resultSet.getDate("entryDate").toLocalDate(),
                                resultSet.getDate("goodTillDate").toLocalDate(),
                                resultSet.getString("tif").charAt(0)
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}
