package lt.vytzab.initiator.ui.tables;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import lt.vytzab.initiator.execution.ExecutionTableModel;

public class ExecutionTable extends JTable {
    public ExecutionTable(ExecutionTableModel executionTableModel) {
        super(executionTableModel);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        DefaultTableCellRenderer r = (DefaultTableCellRenderer) renderer;
        r.setForeground(Color.black);
        r.setBackground(row % 2 == 0 ? Color.white : Color.lightGray);

        return super.prepareRenderer(renderer, row, column);
    }
}