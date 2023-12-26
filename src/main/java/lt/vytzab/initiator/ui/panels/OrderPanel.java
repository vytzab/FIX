package lt.vytzab.initiator.ui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.order.OrderTableModel;
import lt.vytzab.initiator.ui.tables.OrderTable;

/**
 * Contains the Order table.
 */
public class OrderPanel extends JPanel {
    private JTable orderTable = null;
    private OrderTableModel orderTableModel = null;
    private JTextField filterTextField;
    private FilterPanel filterPanel = null;

    public OrderPanel(OrderTableModel orderTableModel, OrderEntryApplication application) {
        this.orderTableModel = orderTableModel;
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

        FilterPanel filterPanel = new FilterPanel(orderTableModel);

        add(filterPanel, BorderLayout.NORTH);

        orderTable = new OrderTable(orderTableModel, application);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);
    }

    public JTable orderTable() {
        return orderTable;
    }

    private void applyFilter() {
        orderTableModel.filterByKeyword(filterTextField.getText().trim().toLowerCase());
    }
    public class FilterPanel extends JPanel {
        private final OrderTableModel orderTableModel;
        private final JTextField keywordTextField;
        private final JLabel keywordLabel;

        public FilterPanel(OrderTableModel orderTableModel) {
            this.orderTableModel = orderTableModel;

            setLayout(new FlowLayout(FlowLayout.LEFT));

            keywordLabel = new JLabel("Keyword:");
            keywordTextField = new JTextField(20);

            JButton filterButton = new JButton("Filter");
            filterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    filterOrders();
                }
            });

            add(keywordLabel);
            add(keywordTextField);
            add(filterButton);
        }

        private void filterOrders() {
            String keyword = keywordTextField.getText();
            orderTableModel.filterByKeyword(keyword);
        }
    }
}