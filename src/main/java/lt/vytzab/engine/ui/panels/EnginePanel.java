package lt.vytzab.engine.ui.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.OrderTableModel;

public class EnginePanel extends JPanel implements Observer, ActionListener {
    private final AddMarketPanel addMarketPanel;
    private final MarketPanel marketPanel;
    private final OrderPanel openOrderPanel;
    private final OrderPanel allOrderPanel;
    private final DeleteUpdatePanel deleteUpdatePanel;
    private final MarketTableModel marketTableModel;

    public EnginePanel(MarketTableModel marketTableModel, OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel, EngineApplication application) {
        setName("Engine Panel");
        this.marketTableModel = marketTableModel;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        addMarketPanel = new AddMarketPanel(marketTableModel, application);
        constraints.insets = new Insets(0, 0, 5, 0);
        add(addMarketPanel, constraints);

        constraints.gridx++;
        constraints.weighty = 10;

        JTabbedPane tabbedPane = new JTabbedPane();
        openOrderPanel = new OrderPanel(openOrderTableModel, application);
        allOrderPanel = new OrderPanel(allOrderTableModel, application);
        marketPanel = new MarketPanel(marketTableModel);

        tabbedPane.add("Markets", marketPanel);
        tabbedPane.add("Open Orders", openOrderPanel);
        tabbedPane.add("All Orders", allOrderPanel);
        tabbedPane.add("Logs", logPanel);
        add(tabbedPane, constraints);

        deleteUpdatePanel = new DeleteUpdatePanel(marketTableModel, application);
        constraints.weighty = 0;
        add(deleteUpdatePanel, constraints);
        deleteUpdatePanel.setEnabled(false);

        addMarketPanel.addActionListener(this);
        marketPanel.marketTable().getSelectionModel().addListSelectionListener(new MarketSelection());
        deleteUpdatePanel.addActionListener(this);
    }

    public void update(Observable o, Object arg) {
        deleteUpdatePanel.update();
    }

    public void actionPerformed(ActionEvent e) {
        ListSelectionModel selection = marketPanel.marketTable().getSelectionModel();
        selection.clearSelection();
    }

    private class MarketSelection implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel selection = marketPanel.marketTable().getSelectionModel();
            if (selection.isSelectionEmpty()) {
                addMarketPanel.clearMessage();
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

            if (numSelected > 1) addMarketPanel.clearMessage();
            else {
                Market market = marketTableModel.getMarket(selectedRow);
                if (market != null) {
                    addMarketPanel.setMessage("Market selected");
                    deleteUpdatePanel.setMarket(market);
                }
            }
        }
    }
}