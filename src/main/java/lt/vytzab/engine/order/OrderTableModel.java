package lt.vytzab.engine.order;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OrderTableModel extends AbstractTableModel {
    private final List<Order> orders = new ArrayList<>();

    private final static int SENDERCOMPID = 0;
    private final static int SYMBOL = 1;
    private final static int QUANTITY = 2;
    private final static int OPEN = 3;
    private final static int EXECUTED = 4;
    private final static int SIDE = 5;
    private final static int TYPE = 6;
    private final static int PRICE = 7;
    private final static int AVGPX = 8;

    private final HashMap<Integer, Order> rowToOrder;
    private final HashMap<String, Integer> idToRow;
    private final HashMap<String, Order> idToOrder;

    private final String[] headers;

    public OrderTableModel() {
        rowToOrder = new HashMap<>();
        idToRow = new HashMap<>();
        idToOrder = new HashMap<>();

        headers = new String[]{"Sender", "Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Price", "AvgPx"};
    }

    public void addOrder(Order order) {
        int row = rowToOrder.size();
        orders.add(order);

        rowToOrder.put(row, order);
        idToRow.put(order.getClOrdID(), row);
        idToOrder.put(order.getClOrdID(), order);

        fireTableRowsInserted(row, row);
    }

    public void updateOrder(Order order, String id) {
        if (!id.equals(order.getClOrdID())) {
            String originalID = order.getClOrdID();
            order.setClOrdID(id);
            replaceOrder(order, originalID);
            return;
        }

        Integer row = idToRow.get(order.getClOrdID());
        if (row == null) {
            return;
        } else {
            orders.set(row, order);
        }
        fireTableRowsUpdated(row, row);
    }

    public void replaceOrder(Order order, String ClOrdID) {
        Integer row = idToRow.get(ClOrdID);
        if (row == null) return;
        orders.set(row, order);

        rowToOrder.put(row, order);
        idToRow.put(order.getClOrdID(), row);
        idToOrder.put(order.getClOrdID(), order);

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
                idToOrder.remove(order.getClOrdID());

                iterator.remove();
            }
        }

        fireTableDataChanged();
    }

    public void addID(Order order, String newID) {
        idToOrder.put(newID, order);
    }

    public Order getOrder(String ClOrdIDid) {
        return idToOrder.get(ClOrdIDid);
    }

    public Order getOrder(int row) {
        return rowToOrder.get(row);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
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
                case PRICE:
                    return order.getPrice();
                case AVGPX:
                    return order.getAvgExecutedPrice();
            }
        }
        return "";
    }
}