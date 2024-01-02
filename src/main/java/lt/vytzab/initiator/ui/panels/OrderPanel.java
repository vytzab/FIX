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

public class OrderPanel extends JPanel {
    private JTable orderTable;
    private OrderTableModel orderTableModel;

    public OrderPanel(OrderTableModel orderTableModel, OrderEntryApplication application) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        this.orderTableModel = orderTableModel;
        orderTable = new OrderTable(orderTableModel);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);
        orderTable.setAutoCreateRowSorter(false);

        FilterPanel filterPanel = new FilterPanel(orderTableModel);
        add(filterPanel, BorderLayout.NORTH);
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
            JButton generateReportButton = new JButton("Generate Report");

            filterButton.addActionListener(e -> filterOrders());
            generateReportButton.addActionListener(e -> generateReport());

            add(keywordLabel);
            add(keywordTextField);
            add(filterButton);
            add(generateReportButton);
        }

        private void filterOrders() {
            String keyword = keywordTextField.getText();
            orderTableModel.filterByKeyword(keyword);
        }

        private void generateReport() {
            JFileChooser fileChooser = new JFileChooser();
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String fileName = fileChooser.getSelectedFile().getAbsolutePath();// Append .csv extension if not already present
                if (!fileName.toLowerCase().endsWith(".csv")) {
                    fileName += ".csv";
                }
                orderTableModel.generateReport(fileName);
                System.out.println("Generating Report...");
            }
        }
    }
}