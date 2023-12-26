package lt.vytzab.initiator.order;

import lt.vytzab.initiator.market.Market;
import quickfix.field.TimeInForce;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class OrderTableModel extends AbstractTableModel {
    private List<Order> orders = new ArrayList<>();
    private final static int SYMBOL = 0;
    private final static int QUANTITY = 1;
    private final static int OPEN = 2;
    private final static int EXECUTED = 3;
    private final static int SIDE = 4;
    private final static int TYPE = 5;
    private final static int LIMITPRICE = 6;
    private final static int STOPPRICE = 7;
    private final static int AVGPX = 8;
    private final static int ENTRYDATE = 9;
    private final static int GOODTILLDATE = 10;
    private boolean filtered = false;

    private HashMap<Integer, Order> originalRowToOrder;
    private HashMap<String, Integer> originalIdToRow;
    private HashMap<Integer, Order> rowToOrder;
    private HashMap<String, Integer> idToRow;

    private final String[] headers;

    private List<Order> displayedOrders;

    public OrderTableModel() {
        originalRowToOrder = new HashMap<>();
        originalIdToRow = new HashMap<>();
        orders = new ArrayList<>();;
        rowToOrder = new HashMap<>();
        idToRow = new HashMap<>();

        headers = new String[]{"Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Limit", "Stop", "AvgPx", "Entry Date", "Good Till Date"};
    }

    public void setOriginalOrders(List<Order> orders) {
        originalRowToOrder.clear();
        originalIdToRow.clear();
        int row = 0;
        for (Order order : orders) {
            originalRowToOrder.put(row, order);
            originalIdToRow.put(order.getOrderID(), row);
            row++;
        }
    }

    public void filterByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            // No filtering, show all orders
            rowToOrder = new HashMap<>(originalRowToOrder);
            idToRow = new HashMap<>(originalIdToRow);
            filtered = false;
        } else if (filtered) {
            rowToOrder = new HashMap<>(originalRowToOrder);
            idToRow = new HashMap<>(originalIdToRow);
            originalRowToOrder = new HashMap<>(rowToOrder);
            originalIdToRow = new HashMap<>(idToRow);
            // Filter orders based on the keyword
            List<Order> filteredOrders = rowToOrder.values().stream()
                    .filter(order -> orderMatchesKeyword(order, keyword))
                    .toList();
            rowToOrder = new HashMap<>();
            idToRow = new HashMap<>();
            int row = 0;
            for (Order order : filteredOrders) {
                rowToOrder.put(row, order);
                idToRow.put(order.getOrderID(), row);
                row++;
            }
            filtered = true;
        } else if (!filtered) {
            originalRowToOrder = new HashMap<>(rowToOrder);
            originalIdToRow = new HashMap<>(idToRow);
            // Filter orders based on the keyword
            List<Order> filteredOrders = rowToOrder.values().stream()
                    .filter(order -> orderMatchesKeyword(order, keyword))
                    .toList();
            rowToOrder = new HashMap<>();
            idToRow = new HashMap<>();
            int row = 0;
            for (Order order : filteredOrders) {
                rowToOrder.put(row, order);
                idToRow.put(order.getOrderID(), row);
                row++;
            }
            filtered = true;
            }
        // Notify the table model about the data change
        fireTableDataChanged();
        }


    private boolean orderMatchesKeyword(Order order, String keyword) {
        return order.getSymbol().toLowerCase().contains(keyword.toLowerCase())
                ||String.valueOf(order.getQuantity()).toLowerCase().contains(keyword.toLowerCase())
                ||String.valueOf(order.getOpenQuantity()).toLowerCase().contains(keyword.toLowerCase())
                ||String.valueOf(order.getExecutedQuantity()).toLowerCase().contains(keyword.toLowerCase())
                ||(order.getSide().toString()).toLowerCase().contains(keyword.toLowerCase())
                ||(order.getType().toString()).toLowerCase().contains(keyword.toLowerCase())
                ||String.valueOf(order.getLimit()).contains(keyword)
                ||String.valueOf(order.getStop()).contains(keyword)
                ||String.valueOf(order.getAvgPx()).contains(keyword)
                ||String.valueOf(order.getEntryDate()).contains(keyword)
                ||String.valueOf(order.getGoodTillDate()).contains(keyword);
    }

    public void addOrder(Order order) {
        if (getOrder(order.getClOrdID()) == null) {
            int row = rowToOrder.size();
            orders.add(order);
            rowToOrder.put(row, order);
            idToRow.put(order.getClOrdID(), row);

            fireTableRowsInserted(row, row);
            System.out.println("Order added!");
            System.out.println("Orders size() = " + orders.size());
        } else {
            replaceOrder(order);
        }
    }

    public void replaceOrder(Order order) {
        Integer row = idToRow.get(order.getClOrdID());
        if (row == null) {
            return;
        } else {
            rowToOrder.put(row, order);
            idToRow.put(order.getClOrdID(), row);
            orders.set(row, order);
        }

        System.out.println("Order replaced!");
        fireTableRowsUpdated(row, row);
    }

    public Order getOrder(String clOrdID) {
        Integer row = idToRow.get(clOrdID);
        return (row != null) ? rowToOrder.get(row) : null;
    }

    public Order getOrder(int row) {
        return rowToOrder.get(row);
    }

    public void removeOrder(String clOrdID) {
        Integer row = idToRow.get(clOrdID);
        if (row == null) return;

        orders.remove(row.intValue());
        rowToOrder.remove(row);
        idToRow.remove(clOrdID);

        // Update row indices in symbolToRow and rowToMarket maps
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
                idToRow.put(clOrdID, row - 1);

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
                    return order.getType();
                case LIMITPRICE:
                    return order.getLimit();
                case STOPPRICE:
                    return order.getStop();
                case AVGPX:
                    return order.getAvgPx();
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

    public Class<String> getColumnClass(int columnIndex) {
        return String.class;
    }

    public void clearOrders() {
        System.out.println("Gets to clearOrders for " + getClass().getName());
        System.out.println("orders.size() = " + orders.size());
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
            idToRow.put(order.getSymbol(), row);

        }
        fireTableRowsDeleted(start, end);
    }

    public void setOrders(List<Order> updatedOrders) {
        orders = updatedOrders;
        fillOrders();
    }
}