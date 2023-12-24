package lt.vytzab.engine.ui.tables;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OrderTable extends JTable implements MouseListener {
    private final transient EngineApplication application;

    public OrderTable(OrderTableModel orderTableModel, EngineApplication application) {
        super(orderTableModel);
        this.application = application;
        addMouseListener(this);
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