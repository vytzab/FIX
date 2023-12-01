package lt.vytzab.initiator.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.ExecutionTableModel;
import lt.vytzab.initiator.Order;
import lt.vytzab.initiator.OrderTableModel;

/**
 * Main content panel
 */
public class OrderEntryPanel extends JPanel implements Observer, ActionListener {

    private final AddOrderPanel orderEntryPanel;
    private final OrderPanel orderPanel;
    private final CancelReplacePanel cancelReplacePanel;
    private final OrderTableModel orderTableModel;
    private final LogPanel logPanel;

    public OrderEntryPanel(OrderTableModel orderTableModel, ExecutionTableModel executionTableModel, LogPanel logPanel, OrderEntryApplication application) {
        setName("BanzaiPanel");
        this.orderTableModel = orderTableModel;
        this.logPanel = logPanel;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        orderEntryPanel = new AddOrderPanel(orderTableModel, application);
        constraints.insets = new Insets(0, 0, 5, 0);
        add(orderEntryPanel, constraints);

        constraints.gridx++;
        constraints.weighty = 10;

        JTabbedPane tabbedPane = new JTabbedPane();
        orderPanel = new OrderPanel(orderTableModel, application);
        ExecutedOrderPanel executionPanel = new ExecutedOrderPanel(executionTableModel);

        tabbedPane.add("Orders", orderPanel);
        tabbedPane.add("Executions", executionPanel);
        tabbedPane.add("Logs", logPanel);
        add(tabbedPane, constraints);

        cancelReplacePanel = new CancelReplacePanel(application);
        constraints.weighty = 0;
        add(cancelReplacePanel, constraints);
        cancelReplacePanel.setEnabled(false);

        orderEntryPanel.addActionListener(this);
        orderPanel.orderTable().getSelectionModel().addListSelectionListener(new OrderSelection());
        cancelReplacePanel.addActionListener(this);
        application.addOrderObserver(this);
    }

    public void update(Observable o, Object arg) {
        cancelReplacePanel.update();
    }

    public void actionPerformed(ActionEvent e) {
        ListSelectionModel selection = orderPanel.orderTable().getSelectionModel();
        selection.clearSelection();
    }

    private class OrderSelection implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel selection = orderPanel.orderTable().getSelectionModel();
            if (selection.isSelectionEmpty()) {
                orderEntryPanel.clearMessage();
                return;
            }

            int firstIndex = e.getFirstIndex();
            int lastIndex = e.getLastIndex();
            int selectedRow = 0;
            int numSelected = 0;

            for (int i = firstIndex; i <= lastIndex; ++i) {
                if (selection.isSelectedIndex(i)) {
                    selectedRow = i;
                    numSelected++;
                }
            }

            if (numSelected > 1)
                orderEntryPanel.clearMessage();
            else {
                Order order = orderTableModel.getOrder(selectedRow);
                if (order != null) {
                    orderEntryPanel.setMessage(order.getMessage());
                    cancelReplacePanel.setOrder(order);
                }
            }
        }
    }
}