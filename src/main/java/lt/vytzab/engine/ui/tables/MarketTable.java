package lt.vytzab.engine.ui.tables;

import lt.vytzab.engine.*;
import lt.vytzab.engine.market.MarketTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MarketTable extends JTable implements MouseListener {

    public MarketTable(MarketTableModel marketTableModel) {
        super(marketTableModel);
        addMouseListener(this);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);

        DefaultTableCellRenderer r = (DefaultTableCellRenderer) component;
        r.setForeground(Color.black);

        return component;
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