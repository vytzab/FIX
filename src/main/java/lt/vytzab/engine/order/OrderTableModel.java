package lt.vytzab.engine.order;

import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.market.Market;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static lt.vytzab.engine.Variables.MARKET_ORDERS_DB;

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
    private final static int STOPPRICE = 8;
    private final static int AVGPX = 9;
    private final static int ENTRYDATE = 10;
    private final static int GOODTILLDATE = 11;
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
        rowToOrder = new HashMap<>();
        idToRow = new HashMap<>();
        displayedOrders = new ArrayList<>();

        headers = new String[]{"Sender", "Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Limit", "Stop", "AvgPx", "Entry Date", "Good Till Date"};
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
                idToRow.put(order.getClOrdID(), row);
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
                idToRow.put(order.getClOrdID(), row);
                row++;
            }
            filtered = true;
        }
        // Notify the table model about the data change
        fireTableDataChanged();
    }

    private boolean orderMatchesKeyword(Order order, String keyword) {
        return order.getSymbol().toLowerCase().contains(keyword.toLowerCase()) || String.valueOf(order.getQuantity()).toLowerCase().contains(keyword.toLowerCase()) || String.valueOf(order.getOpenQuantity()).toLowerCase().contains(keyword.toLowerCase()) ||
                String.valueOf(order.getExecutedQuantity()).toLowerCase().contains(keyword.toLowerCase()) || String.valueOf(order.getSide()).toLowerCase().contains(keyword.toLowerCase()) || String.valueOf(order.getOrdType()).toLowerCase().contains(keyword.toLowerCase()) ||
                String.valueOf(order.getLimit()).contains(keyword) || String.valueOf(order.getStop()).contains(keyword) || String.valueOf(order.getAvgExecutedPrice()).contains(keyword) || String.valueOf(order.getEntryDate()).contains(keyword) || String.valueOf(order.getGoodTillDate()).contains(keyword);
    }

    public void addOrder(Order order) {
        if (getOrder(order.getClOrdID()) == null) {
            int row = rowToOrder.size();
            orders.add(order);
            rowToOrder.put(row, order);
            idToRow.put(order.getClOrdID(), row);

            fireTableRowsInserted(row, row);
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
        fireTableRowsUpdated(row, row);
    }

    public void removeFullyExecutedOrders() {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.isFullyExecuted()) {
                // Remove the order from the mappings
                int row = idToRow.get(order.getClOrdID());
                rowToOrder.remove(row);
                idToRow.remove(order.getClOrdID());

                iterator.remove();
            }
        }

        fireTableDataChanged();
    }

    public Order getOrder(String clOrdID) {
        Integer row = idToRow.get(clOrdID);
        return (row != null) ? rowToOrder.get(row) : null;
    }

    public Order getOrder(int row) {
        return rowToOrder.get(row);
    }

    public Class<String> getColumnClass(int columnIndex) {
        return String.class;
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
                case STOPPRICE:
                    return order.getStop();
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
            idToRow.put(order.getSymbol(), row);

        }
        fireTableRowsDeleted(start, end);
    }

    public void setOrders(List<Order> updatedOrders) {
        orders = updatedOrders;
        fillOrders();
    }
}