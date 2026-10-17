package lt.vytzab.engine.ui;

import javax.swing.*;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.ui.panels.LogPanel;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.panels.EnginePanel;

import java.awt.*;

public class EngineFrame extends JFrame {

    public EngineFrame(MarketTableModel marketTableModel, OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel, final EngineApplication application, JMenuBar menuBar) {
        super();
        setTitle("Engine Frame");
        setSize(1000, 600);

        getContentPane().add(new EnginePanel(marketTableModel, openOrderTableModel, allOrderTableModel, logPanel, application), BorderLayout.CENTER);
        setJMenuBar(menuBar);
        setVisible(true);
    }
    public static void centerFrameOnScreen(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();

        int x = (screenWidth - frameWidth) / 2;
        int y = (screenHeight - frameHeight) / 2;

        frame.setLocation(x, y);
    }
}