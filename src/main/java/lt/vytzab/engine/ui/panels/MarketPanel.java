package lt.vytzab.engine.ui.panels;

import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.ui.tables.MarketTable;

import javax.swing.*;
import java.awt.*;

public class MarketPanel extends JPanel {
    private final JTable marketTable;

    public MarketPanel(MarketTableModel marketTableModel) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        marketTable = new MarketTable(marketTableModel);
        add(new JScrollPane(marketTable), BorderLayout.CENTER);
        marketTable.setAutoCreateRowSorter(false);

        FilterPanel filterPanel = new FilterPanel(marketTableModel);
        add(filterPanel, BorderLayout.NORTH);
    }

    public JTable marketTable() {
        return marketTable;
    }
    public static class FilterPanel extends JPanel {
        private final MarketTableModel marketTableModel;
        private final JTextField keywordTextField;

        public FilterPanel(MarketTableModel marketTableModel) {
            this.marketTableModel = marketTableModel;

            setLayout(new FlowLayout(FlowLayout.LEFT));

            JLabel keywordLabel = new JLabel("Keyword:");
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
            marketTableModel.filterByKeyword(keyword);
        }

        private void generateReport() {
            JFileChooser fileChooser = new JFileChooser();
            int userSelection = fileChooser.showSaveDialog(this);

            if (!marketTableModel.isMarketsEmpty()) {
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!fileName.toLowerCase().endsWith(".csv")) {
                        fileName += ".csv";
                    }
                    marketTableModel.generateReport(fileName);
                    System.out.println("Generating Report...");
                }
            }
        }
    }
}