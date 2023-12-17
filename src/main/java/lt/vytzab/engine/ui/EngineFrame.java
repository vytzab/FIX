package lt.vytzab.engine.ui;

import javax.swing.*;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.ui.panels.LogPanel;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.panels.EnginePanel;

import java.awt.BorderLayout;

public class EngineFrame extends JFrame {

    public EngineFrame(MarketTableModel marketTableModel, OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel, final EngineApplication application, JMenuBar menuBar) {
        super();
        setTitle("Engine Frame");
        setSize(800, 600);

        getContentPane().add(new EnginePanel(marketTableModel, openOrderTableModel, allOrderTableModel, logPanel, application), BorderLayout.CENTER);
        setJMenuBar(menuBar);
        setVisible(true);
    }
}