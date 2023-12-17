package lt.vytzab.engine.ui.panels;

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

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.OrderTableModel;

/**
 * Main content panel
 */
public class EnginePanel extends JPanel implements Observer, ActionListener {
    private final AddMarketPanel addMarketPanel;
    private final MarketPanel marketPanel;
    private final OrderPanel openOrderPanel;
    private final OrderPanel allOrderPanel;
    private final LogPanel logPanel;
    private final OrderTableModel openOrderTableModel;
    private final OrderTableModel allOrderTableModel;
    private final MarketTableModel marketTableModel;

    public EnginePanel(MarketTableModel marketTableModel, OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel, EngineApplication application) {
        setName("Engine Panel");
        this.openOrderTableModel = openOrderTableModel;
        this.allOrderTableModel = allOrderTableModel;
        this.marketTableModel = marketTableModel;
        this.logPanel = logPanel;

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
        marketPanel = new MarketPanel(marketTableModel, application);

        tabbedPane.add("Markets", marketPanel);
        tabbedPane.add("Open Orders", openOrderPanel);
        tabbedPane.add("All Orders", allOrderPanel);
        tabbedPane.add("Logs", logPanel);
        add(tabbedPane, constraints);

        constraints.weighty = 0;

        addMarketPanel.addActionListener(this);
    }

    public void update(Observable o, Object arg) {
    }

    public void actionPerformed(ActionEvent e) {
    }
}