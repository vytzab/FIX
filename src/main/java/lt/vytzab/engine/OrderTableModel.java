package lt.vytzab.engine;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OrderTableModel extends AbstractTableModel {
    private List<MarketOrder> orders = new ArrayList<>();

    private final static int SYMBOL = 0;
    private final static int QUANTITY = 1;
    private final static int OPEN = 2;
    private final static int EXECUTED = 3;
    private final static int SIDE = 4;
    private final static int TYPE = 5;
    private final static int LIMITPRICE = 6;
    private final static int STOPPRICE = 7;
    private final static int AVGPX = 8;
    private final static int TARGET = 9;

    private final HashMap<Integer, MarketOrder> rowToOrder;
    private final HashMap<String, Integer> idToRow;
    private final HashMap<String, MarketOrder> idToOrder;

    private final String[] headers;

    public OrderTableModel() {
        rowToOrder = new HashMap<>();
        idToRow = new HashMap<>();
        idToOrder = new HashMap<>();

        headers = new String[] {"Symbol", "Quantity", "Open", "Executed", "Side",
                "Type", "Limit", "Stop", "AvgPx", "Target"};
    }

    public void addOrder(MarketOrder order) {
        int row = rowToOrder.size();
        orders.add(order);

        rowToOrder.put(row, order);
        idToRow.put(order.getClOrdID(), row);
        idToOrder.put(order.getClOrdID(), order);

        fireTableRowsInserted(row, row);
    }

    public void updateOrder(MarketOrder order, String id) {

        if (!id.equals(order.getClOrdID())) {
            String originalID = order.getClOrdID();
            order.setClOrdID(id);
            replaceOrder(order, originalID);
            return;
        }

        Integer row = idToRow.get(order.getClOrdID());
        if (row == null)
            return;
        fireTableRowsUpdated(row, row);
    }

    public void replaceOrder(MarketOrder order, String originalID) {

        Integer row = idToRow.get(originalID);
        if (row == null)
            return;

        rowToOrder.put(row, order);
        idToRow.put(order.getClOrdID(), row);
        idToOrder.put(order.getClOrdID(), order);

        fireTableRowsUpdated(row, row);
    }

    public void removeFullyExecutedOrders() {
        Iterator<MarketOrder> iterator = orders.iterator();
        while (iterator.hasNext()) {
            MarketOrder order = iterator.next();
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

    public void addID(MarketOrder order, String newID) {
        idToOrder.put(newID, order);
    }

    public MarketOrder getOrder(String id) {
        return idToOrder.get(id);
    }

    public MarketOrder getOrder(int row) {
        return rowToOrder.get(row);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) { }

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
        MarketOrder order = rowToOrder.get(rowIndex);
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
                return order.getOrdType();
            case LIMITPRICE:
                return order.getPrice();
            case STOPPRICE:
                return order.getPrice();
            case AVGPX:
                return order.getAvgExecutedPrice();
        }
        return "";
    }
}