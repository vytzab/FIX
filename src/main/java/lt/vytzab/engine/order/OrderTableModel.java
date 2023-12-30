package lt.vytzab.engine.order;

import lt.vytzab.engine.market.Market;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;

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
    private boolean filtered = false;

    private final String[] headers;

    private List<Order> displayedOrders;

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
            switch (columnIndex) {
                case SENDERCOMPID:
                    return order.getSenderCompID();
                case SYMBOL:
                    return order.getSymbol();
                case QUANTITY:
                    return order.getQuantity();
                case OPEN:
                    return order.getOpenQuantity();
                case EXECUTED:
                    return order.getExecutedQuantity();
                case SIDE:
                    return order.getSide();
                case TYPE:
                    return order.getOrdType();
                case LIMITPRICE:
                    return order.getLimit();
                case AVGPX:
                    return order.getAvgExecutedPrice();
                case ENTRYDATE:
                    return order.getEntryDate();
                case GOODTILLDATE:
                    return order.getGoodTillDate();
                default:
                    return "";
            }
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

    public void fillOrders() {
        orders.addAll(originalOrders);
    }

    public void updateOrders(List<Order> updatedOrders) {
        originalOrders = new ArrayList<>(orders);
        orders = new ArrayList<>(updatedOrders);

        fireTableDataChanged();
    }

    public void setOrders(List<Order> updatedOrders) {
        orders = new ArrayList<>(updatedOrders);

        // Initialize or clear the originalMarkets list
        if (originalOrders == null) {
            originalOrders = new ArrayList<>(updatedOrders);
        } else {
            originalOrders.clear();
            originalOrders.addAll(updatedOrders);
        }

        fireTableDataChanged();
    }

    public void refreshOrders() {
        clearOrders();
        fillOrders();
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

    private static class OrderComparator implements Comparator<Order> {
        private int columnIndex;

        public OrderComparator(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        public int compare(Order order1, Order order2) {
            // Implement comparison logic based on the specified column index
            switch (columnIndex) {
                case SENDERCOMPID:
                    return order1.getSenderCompID().compareTo(order2.getSenderCompID());
                case SYMBOL:
                    return order1.getSymbol().compareTo(order2.getSymbol());
                case QUANTITY:
                    return Long.compare(order1.getQuantity(), order2.getQuantity());
                case OPEN:
                    return Long.compare(order1.getOpenQuantity(), order2.getOpenQuantity());
                case EXECUTED:
                    return Long.compare(order1.getExecutedQuantity(), order2.getExecutedQuantity());
                case SIDE:
                    return Character.compare(order1.getSide(), order2.getSide());
                case TYPE:
                    return Character.compare(order1.getOrdType(), order2.getOrdType());
                case LIMITPRICE:
                    return Double.compare(order1.getLimit(), order2.getLimit());
                case AVGPX:
                    return Double.compare(order1.getAvgExecutedPrice(), order2.getAvgExecutedPrice());
                case ENTRYDATE:
                    return order1.getEntryDate().compareTo(order2.getEntryDate());
                case GOODTILLDATE:
                    return order1.getGoodTillDate().compareTo(order2.getGoodTillDate());
                default:
                    return 0; // Default to no sorting
            }
        }
    }
}