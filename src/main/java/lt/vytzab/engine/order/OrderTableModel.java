package lt.vytzab.engine.order;

import lt.vytzab.engine.market.Market;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OrderTableModel extends AbstractTableModel {
    private List<Order> orders = new ArrayList<>();
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

    private ConcurrentHashMap<Integer, Order> originalRowToOrder;
    private ConcurrentHashMap<String, Integer> originalClOrdIDToRow;
    private ConcurrentHashMap<Integer, Order> rowToOrder;
    private ConcurrentHashMap<String, Integer> clOrdIDToRow;

    private final String[] headers;

    private List<Order> displayedOrders;

    public OrderTableModel() {
        originalRowToOrder = new ConcurrentHashMap<>();
        originalClOrdIDToRow = new ConcurrentHashMap<>();
        orders = new ArrayList<>();
        rowToOrder = new ConcurrentHashMap<>();
        clOrdIDToRow = new ConcurrentHashMap<>();

        headers = new String[]{"Sender", "Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Limit", "Average Price", "Entry Date", "Good Till Date"};
    }

    public void filterByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            rowToOrder = new ConcurrentHashMap<>(originalRowToOrder);
            clOrdIDToRow = new ConcurrentHashMap<>(originalClOrdIDToRow);
            filtered = false;
        } else if (filtered) {
            rowToOrder = new ConcurrentHashMap<>(originalRowToOrder);
            clOrdIDToRow = new ConcurrentHashMap<>(originalClOrdIDToRow);
            originalRowToOrder = new ConcurrentHashMap<>(rowToOrder);
            originalClOrdIDToRow = new ConcurrentHashMap<>(clOrdIDToRow);
            List<Order> filteredOrders = rowToOrder.values().stream()
                    .filter(order -> orderMatchesKeyword(order, keyword))
                    .toList();
            rowToOrder = new ConcurrentHashMap<>();
            clOrdIDToRow = new ConcurrentHashMap<>();
            int row = 0;
            for (Order order : filteredOrders) {
                rowToOrder.put(row, order);
                clOrdIDToRow.put(order.getClOrdID(), row);
                row++;
            }
            filtered = true;
        } else {
            originalRowToOrder = new ConcurrentHashMap<>(rowToOrder);
            originalClOrdIDToRow = new ConcurrentHashMap<>(clOrdIDToRow);
            List<Order> filteredOrders = rowToOrder.values().stream()
                    .filter(order -> orderMatchesKeyword(order, keyword))
                    .toList();
            rowToOrder = new ConcurrentHashMap<>();
            clOrdIDToRow = new ConcurrentHashMap<>();
            int row = 0;
            for (Order order : filteredOrders) {
                rowToOrder.put(row, order);
                clOrdIDToRow.put(order.getClOrdID(), row);
                row++;
            }
            filtered = true;
        }
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
                || String.valueOf(order.getGoodTillDate()).contains(keyword);
    }

    public void addOrder(Order order) {
        if (getOrder(order.getClOrdID()) == null) {
            int row = rowToOrder.size();
            orders.add(order);
            rowToOrder.put(row, order);
            clOrdIDToRow.put(order.getClOrdID(), row);

            fireTableRowsInserted(row, row);
        } else {
            replaceOrder(order, order.getClOrdID());
        }
    }

    public void replaceOrder(Order order, String clOrdID) {
        Integer row = clOrdIDToRow.get(clOrdID);
        if (row == null) {
            return;
        } else {
            rowToOrder.put(row, order);
            clOrdIDToRow.put(clOrdID, row);
            orders.set(row, order);
        }

        fireTableRowsUpdated(row, row);
    }

    public Order getOrder(String clOrdID) {
        Integer row = clOrdIDToRow.get(clOrdID);
        return (row != null) ? rowToOrder.get(row) : null;
    }

    public Order getOrder(int row) {
        return rowToOrder.get(row);
    }

    public void removeOrder(String clOrdID) {
        Integer row = clOrdIDToRow.get(clOrdID);
        if (row == null) return;

        orders.remove(row.intValue());
        rowToOrder.remove(row);
        clOrdIDToRow.remove(clOrdID);

        updateRowIndices(row);

        fireTableRowsDeleted(row, row);
    }

    private void updateRowIndices(int removedRow) {
        // Create a copy of the entry set to avoid ConcurrentModificationException
        Set<Map.Entry<Integer, Order>> entrySetCopy = new HashSet<>(rowToOrder.entrySet());

        for (Map.Entry<Integer, Order> entry : entrySetCopy) {
            Integer row = entry.getKey();
            if (row > removedRow) {
                Order order = entry.getValue();
                String clOrdID = order.getClOrdID();

                // Update symbolToRow map
                clOrdIDToRow.put(clOrdID, row - 1);

                // Update rowToMarket map
                rowToOrder.put(row - 1, order);
            }
        }
    }

    public int getRowCount() {
        return rowToOrder.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Order order = rowToOrder.get(rowIndex);

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
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.isFullyExecuted()) {
                removeOrder(order.getClOrdID());

                iterator.remove();
            }
        }
        fireTableDataChanged();
    }

    public void clearOrders() {
        int start = 0;
        int end = orders.size();
        for (Order order : orders) {
            rowToOrder.values().remove(order);
        }
        fireTableRowsDeleted(start, end);
    }

    public void fillOrders() {
        int start = 0;
        int end = orders.size();
        for (Order order : orders) {
            int row = rowToOrder.size();
            rowToOrder.put(row, order);
            clOrdIDToRow.put(order.getClOrdID(), row);
        }
        fireTableRowsDeleted(start, end);
    }

    public void setOrders(List<Order> updatedOrders) {
        orders = updatedOrders;
        fillOrders();
    }
}