package lt.vytzab.engine.ui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.tables.OrderTable;

public class OrderPanel extends JPanel {
    private JTable orderTable = null;
    private OrderTableModel orderTableModel = null;
    private JTextField filterTextField;

    public OrderPanel(OrderTableModel orderTableModel, EngineApplication application) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        FilterPanel filterPanel = new FilterPanel(orderTableModel);
        add(filterPanel, BorderLayout.NORTH);
        orderTable = new OrderTable(orderTableModel);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);
    }

    public JTable orderTable() {
        return orderTable;
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