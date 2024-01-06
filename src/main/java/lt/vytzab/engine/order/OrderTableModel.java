package lt.vytzab.engine.order;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class OrderTableModel extends AbstractTableModel {
    private List<Order> orders;
    private List<Order> originalOrders;
    private List<Order> sortedOrders;
    private final static int SENDERCOMPID = 0;
    private final static int SYMBOL = 1;
    private final static int QUANTITY = 2;
    private final static int OPEN = 3;
    private final static int EXECUTED = 4;
    private final static int SIDE = 5;
    private final static int TYPE = 6;
    private final static int LIMITPRICE = 7;
    private final static int AVGPX = 8;
    private final static int ENTRYDATE = 9;
    private final static int GOODTILLDATE = 10;

    private final String[] headers;

    public OrderTableModel() {
        orders = new ArrayList<>();
        originalOrders = new ArrayList<>();
        sortedOrders = new ArrayList<>();
        headers = new String[]{"Sender", "Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Limit", "Average Price", "Entry Date", "Good Till Date"};
    }

    public void filterByKeyword(String keyword) {
        List<Order> filteredOrders = originalOrders.stream()
                .filter(order -> orderMatchesKeyword(order, keyword))
                .toList();

        orders = new ArrayList<>(filteredOrders);

        fireTableDataChanged();
    }

    private boolean orderMatchesKeyword(Order order, String keyword) {
        return order.getSymbol().toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getSenderCompID()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getQuantity()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getOpenQuantity()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getExecutedQuantity()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getSide()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getOrdType()).toLowerCase().contains(keyword.toLowerCase())
                || String.valueOf(order.getLimit()).contains(keyword)
                || String.valueOf(order.getAvgExecutedPrice()).contains(keyword)
                || String.valueOf(order.getEntryDate()).contains(keyword)
                || String.valueOf(order.getGoodTillDate()).contains(keyword)
                || String.valueOf(order.getSenderCompID()).toLowerCase().contains(keyword);
    }

    public void addOrder(Order order) {
        if (!orders.contains(order)) {
            orders.add(order);
            originalOrders.add(order);
            fireTableRowsInserted(orders.indexOf(order), orders.indexOf(order));
        }
        replaceOrder(order);
    }

    public void replaceOrder(Order order) {
        int index = orders.indexOf(order);

        if (index != -1) {
            orders.set(index, order);
            originalOrders.set(index, order);
            fireTableRowsUpdated(index, index);
        } else {
            addOrder(order);
        }
    }

    public Order getOrder(String clOrdID) {
        return orders.stream()
                .filter(order -> order.getClOrdID().equals(clOrdID))
                .findFirst()
                .orElse(null);
    }

    public Order getOrder(int row) {
        return row < orders.size() ? orders.get(row) : null;
    }

    public void removeOrder(String clOrdID) {
        Order orderToRemove = getOrder(clOrdID);
        if (orderToRemove != null) {
            int index = orders.indexOf(orderToRemove);
            orders.remove(index);
            originalOrders.remove(index);

            fireTableRowsDeleted(index, index);
        }
    }

    public int getRowCount() {
        return orders.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Order order = getOrder(rowIndex);

        if (order != null) {
            return switch (columnIndex) {
                case SENDERCOMPID -> order.getSenderCompID();
                case SYMBOL -> order.getSymbol();
                case QUANTITY -> order.getQuantity();
                case OPEN -> order.getOpenQuantity();
                case EXECUTED -> order.getExecutedQuantity();
                case SIDE -> order.getSide();
                case TYPE -> order.getOrdType();
                case LIMITPRICE -> order.getPrice();
                case AVGPX -> order.getAvgExecutedPrice();
                case ENTRYDATE -> order.getEntryDate();
                case GOODTILLDATE -> order.getGoodTillDate();
                default -> "";
            };
        }
        return "";
    }

    public void removeFullyExecutedOrders() {
        ListIterator<Order> iterator = orders.listIterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.isFullyExecuted()) {
                int index = iterator.nextIndex() - 1;
                iterator.remove();

                fireTableRowsDeleted(index, index);
            }
        }
    }

    public void clearOrders() {
        int end = orders.size();
        orders.clear();
        fireTableRowsDeleted(0, end);
    }

    public void setOrders(List<Order> updatedOrders) {
        orders = new ArrayList<>(updatedOrders);

        if (originalOrders == null) {
            originalOrders = new ArrayList<>(updatedOrders);
        } else {
            originalOrders.clear();
            originalOrders.addAll(updatedOrders);
        }

        fireTableDataChanged();
    }

    public boolean isOrdersEmpty() {
        return orders.isEmpty();
    }

    public void setSortedOrders(int columnIndex, SortOrder sortOrder) {
        orders = sortOrders(columnIndex, sortOrder);

        fireTableDataChanged();
    }

    private List<Order> sortOrders(int columnIndex, SortOrder sortOrder) {
        sortedOrders = new ArrayList<>(orders);

        OrderTableModel.OrderComparator comparator = new OrderTableModel.OrderComparator(columnIndex);

        // Sort the list based on the comparator and sortOrder
        sortedOrders.sort((market1, market2) -> {
            int result = comparator.compare(market1, market2);
            return (sortOrder == SortOrder.DESCENDING) ? -result : result;
        });
        return sortedOrders;
    }

    private String orderToCSVString(Order order) {
        return String.format("%s,%s,%d,%d,%d,%c,%c,%.2f,%.2f,%s,%s",
                order.getSenderCompID(),
                order.getSymbol(),
                order.getQuantity(),
                order.getOpenQuantity(),
                order.getExecutedQuantity(),
                order.getSide(),
                order.getOrdType(),
                order.getLimit(),
                order.getAvgExecutedPrice(),
                order.getEntryDate(),
                order.getGoodTillDate());
    }

    public void generateReport(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write data
            for (Order order : orders) {
                writer.write(orderToCSVString(order));
                writer.newLine();
            }

            System.out.println("CSV file exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private record OrderComparator(int columnIndex) implements Comparator<Order> {
        @Override
            public int compare(Order order1, Order order2) {
                // Implement comparison logic based on the specified column index
                return switch (columnIndex) {
                    case SENDERCOMPID -> order1.getSenderCompID().compareTo(order2.getSenderCompID());
                    case SYMBOL -> order1.getSymbol().compareTo(order2.getSymbol());
                    case QUANTITY -> Long.compare(order1.getQuantity(), order2.getQuantity());
                    case OPEN -> Long.compare(order1.getOpenQuantity(), order2.getOpenQuantity());
                    case EXECUTED -> Long.compare(order1.getExecutedQuantity(), order2.getExecutedQuantity());
                    case SIDE -> Character.compare(order1.getSide(), order2.getSide());
                    case TYPE -> Character.compare(order1.getOrdType(), order2.getOrdType());
                    case LIMITPRICE -> Double.compare(order1.getLimit(), order2.getLimit());
                    case AVGPX -> Double.compare(order1.getAvgExecutedPrice(), order2.getAvgExecutedPrice());
                    case ENTRYDATE -> order1.getEntryDate().compareTo(order2.getEntryDate());
                    case GOODTILLDATE -> order1.getGoodTillDate().compareTo(order2.getGoodTillDate());
                    default -> 0; // Default to no sorting
                };
            }
        }
}