package lt.vytzab.initiator.order;

import lt.vytzab.initiator.market.Market;
import quickfix.field.TimeInForce;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OrderTableModel extends AbstractTableModel {
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

    private final HashMap<Integer, Order> rowToOrder;
    private final HashMap<String, Integer> idToRow;
    private final HashMap<String, Order> idToOrder;

    private final String[] headers;

    private List<Order> displayedOrders;

    public OrderTableModel() {
        rowToOrder = new HashMap<>();
        idToRow = new HashMap<>();
        idToOrder = new HashMap<>();
        headers = new String[]{"Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Limit", "Stop", "AvgPx", "Entry Date", "Good Till Date"};
        displayedOrders = new ArrayList<>();
    }

    public void setDisplayedOrders(List<Order> orders) {
        displayedOrders = new ArrayList<>(orders);
        fireTableDataChanged();
    }

    public void filterByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            // No filtering, show all orders
            setDisplayedOrders(new ArrayList<>(rowToOrder.values()));
        } else {
            // Filter orders based on the keyword
            List<Order> filteredOrders = rowToOrder.values().stream()
                    .filter(order -> orderMatchesKeyword(order, keyword))
                    .collect(Collectors.toList());
            setDisplayedOrders(filteredOrders);
        }
    }

    private boolean orderMatchesKeyword(Order order, String keyword) {
        return order.getSymbol().toLowerCase().contains(keyword.toLowerCase()) || String.valueOf(order.getQuantity()).toLowerCase().contains(keyword.toLowerCase()) || String.valueOf(order.getOpenQuantity()).toLowerCase().contains(keyword.toLowerCase()) ||
                String.valueOf(order.getExecutedQuantity()).toLowerCase().contains(keyword.toLowerCase()) || (order.getSide().toString()).toLowerCase().contains(keyword.toLowerCase()) || (order.getType().toString()).toLowerCase().contains(keyword.toLowerCase()) ||
                String.valueOf(order.getLimit()).contains(keyword) || String.valueOf(order.getStop()).contains(keyword) || String.valueOf(order.getAvgPx()).contains(keyword) || String.valueOf(order.getEntryDate()).contains(keyword) || String.valueOf(order.getGoodTillDate()).contains(keyword);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void addOrder(Order order) {
        if (getOrder(order.getOrderID()) == null) {
            int row = rowToOrder.size();
            rowToOrder.put(row, order);
            idToRow.put(order.getOrderID(), row);
            displayedOrders = new ArrayList<>(rowToOrder.values());

            fireTableRowsInserted(row, row);
        } else {
            replaceOrder(order, order.getOrderID());
        }
    }

    public void updateOrder(Order order, String id) {
        if (!id.equals(order.getOrderID())) {
            String originalID = order.getOrderID();
            order.setOrderID(id);
            replaceOrder(order, originalID);
            return;
        }

        Integer row = idToRow.get(order.getOrderID());
        if (row == null) return;
        fireTableRowsUpdated(row, row);
    }

    public void replaceOrder(Order order, String clOrdID) {
        Integer row = idToRow.get(clOrdID);
        if (row == null) {
            return;
        } else {
            rowToOrder.put(row, order);
            idToRow.put(order.getOrderID(), row);
        }
        fireTableRowsUpdated(row, row);
    }

    public void addID(Order order, String newID) {
        idToOrder.put(newID, order);
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
                if (order.getType() == OrderType.LIMIT) {
                    return null;
                } else {
                    return order.getLimit();
                }
            case STOPPRICE:
                if (order.getType() == OrderType.LIMIT) {
                    return null;
                } else {
                    return order.getStop();
                }
            case AVGPX:
                return order.getAvgPx();
            case ENTRYDATE:
                return order.getEntryDate();
            case GOODTILLDATE:
                if (order.getTIF() == OrderTIF.DAY) {
                    return LocalDate.now();
                } else if (order.getTIF() == OrderTIF.GTD) {
                    return order.getGoodTillDate();
                } else {
                    return null;
                }
        }
        return "";
    }
}