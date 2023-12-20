package lt.vytzab.initiator.order;

import quickfix.field.TimeInForce;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.HashMap;

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

    public OrderTableModel() {
        rowToOrder = new HashMap<>();
        idToRow = new HashMap<>();
        idToOrder = new HashMap<>();

        headers = new String[]{"Symbol", "Quantity", "Open", "Executed", "Side", "Type", "Limit", "Stop", "AvgPx", "Entry Date", "Good Till Date"};
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void addOrder(Order order) {
        int row = rowToOrder.size();

        rowToOrder.put(row, order);
        idToRow.put(order.getOrderID(), row);
        idToOrder.put(order.getOrderID(), order);

        fireTableRowsInserted(row, row);
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

    public void replaceOrder(Order order, String originalID) {
        Integer row = idToRow.get(originalID);
        if (row == null) return;

        rowToOrder.put(row, order);
        idToRow.put(order.getOrderID(), row);
        idToOrder.put(order.getOrderID(), order);

        fireTableRowsUpdated(row, row);
    }

    public void addID(Order order, String newID) {
        idToOrder.put(newID, order);
    }

    public Order getOrder(String id) {
        return idToOrder.get(id);
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
                if(order.getType()==OrderType.LIMIT) {
                   return null;
                } else {
                    return order.getLimit();
                }
            case STOPPRICE:
                if(order.getType()==OrderType.LIMIT) {
                    return null;
                } else {
                    return order.getStop();
                }
            case AVGPX:
                return order.getAvgPx();
            case ENTRYDATE:
                return order.getEntryDate();
            case GOODTILLDATE:
                if(order.getTIF()==OrderTIF.DAY) {
                    LocalDate.now();
                } else if (order.getTIF()==OrderTIF.GTD){
                    return order.getGoodTillDate();
                } else {
                    return null;
                }
        }
        return "";
    }
}