package lt.vytzab.initiator.ui.panels;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.market.MarketTableModel;
import lt.vytzab.initiator.ui.tables.MarketTable;

import javax.swing.*;
import java.awt.*;

public class MarketPanel extends JPanel {
    private JTable marketTable = null;

    public MarketPanel(MarketTableModel marketTableModel, OrderEntryApplication application) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;

        marketTable = new MarketTable(marketTableModel, application);
        add(new JScrollPane(marketTable), constraints);
    }

    public JTable getMarketTable() {
        return marketTable;
    }
}