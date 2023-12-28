package lt.vytzab.initiator.ui.tables;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.order.Order;
import lt.vytzab.initiator.order.OrderTableModel;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OrderTable extends JTable implements MouseListener {
    SortOrder currentSortOrder = null;

    public OrderTable(OrderTableModel orderTableModel) {
        super(orderTableModel);
        addMouseListener(this);

        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = getColumnModel().getColumnIndexAtX(e.getX());
                orderTableModel.setSortedOrders(columnIndex, toggleSortOrder(currentSortOrder));
            }
        });
    }

    public void setCurrentSortOrder(SortOrder currentSortOrder) {
        this.currentSortOrder = currentSortOrder;
    }

    // Helper method to toggle sort order between ASCENDING and DESCENDING
    private SortOrder toggleSortOrder(SortOrder currentSortOrder) {
        if (currentSortOrder == null || currentSortOrder == SortOrder.DESCENDING) {
            setCurrentSortOrder(SortOrder.ASCENDING);
            return SortOrder.ASCENDING;
        } else {
            setCurrentSortOrder(SortOrder.DESCENDING);
            return SortOrder.DESCENDING;
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Order order = ((OrderTableModel) dataModel).getOrder(row);

        if (order != null) {
            int open = (int) order.getOpenQuantity();
            double executed = order.getExecutedQuantity();
            boolean rejected = order.getRejected();
            boolean canceled = order.getCanceled();

            DefaultTableCellRenderer r = (DefaultTableCellRenderer) renderer;
            r.setForeground(Color.black);

            if (rejected)
                r.setBackground(Color.red);
            else if (canceled)
                r.setBackground(Color.gray);
            else if (open == 0 && executed == 0.0)
                r.setBackground(Color.yellow);
            else if (open > 0)
                r.setBackground(Color.green);
            else if (open == 0)
                r.setBackground(Color.white);
        }

        return super.prepareRenderer(renderer, row, column);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Handle mouse click events as needed
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Handle mouse entered events as needed
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Handle mouse exited events as needed
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Handle mouse pressed events as needed
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Handle mouse released events as needed
    }
}