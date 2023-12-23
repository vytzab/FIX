package lt.vytzab.initiator.ui.panels;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.market.MarketTableModel;
import lt.vytzab.initiator.ui.tables.MarketTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MarketPanel extends JPanel {
    private JTable marketTable = null;
    private MarketTableModel marketTableModel = null;
    private JTextField filterTextField;

    public MarketPanel(MarketTableModel marketTableModel, OrderEntryApplication application) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        FilterPanel filterPanel = new FilterPanel(marketTableModel);
        add(filterPanel, BorderLayout.NORTH);
        marketTable = new MarketTable(marketTableModel, application);
        add(new JScrollPane(marketTable), BorderLayout.CENTER);
    }

    public JTable marketTable() {
        return marketTable;
    }
    private void applyFilter() {
        marketTableModel.filterByKeyword(filterTextField.getText().trim().toLowerCase());
    }
    public class FilterPanel extends JPanel {
        private final MarketTableModel marketTableModel;
        private final JTextField keywordTextField;
        private final JLabel keywordLabel;

        public FilterPanel(MarketTableModel marketTableModel) {
            this.marketTableModel = marketTableModel;

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
            marketTableModel.filterByKeyword(keyword);
        }
    }
}