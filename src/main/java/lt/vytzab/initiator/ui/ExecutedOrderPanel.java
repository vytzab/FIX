package lt.vytzab.initiator.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import lt.vytzab.initiator.ExecutionTableModel;

public class ExecutedOrderPanel extends JPanel {

    public ExecutedOrderPanel(ExecutionTableModel executionTableModel) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;

        JTable table = new ExecutionTable(executionTableModel);
        add(new JScrollPane(table), constraints);
    }

}