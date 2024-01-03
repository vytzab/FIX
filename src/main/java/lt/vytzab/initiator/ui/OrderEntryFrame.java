package lt.vytzab.initiator.ui;

import java.awt.*;

import javax.swing.*;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.helpers.IDGenerator;
import lt.vytzab.initiator.market.MarketTableModel;
import lt.vytzab.initiator.order.OrderTableModel;
import lt.vytzab.initiator.ui.panels.LogPanel;
import lt.vytzab.initiator.ui.panels.OrderEntryPanel;

public class OrderEntryFrame extends JFrame {

    public OrderEntryFrame(MarketTableModel marketTableModel, OrderTableModel orderTableModel, OrderTableModel executedOrdersTableModel, LogPanel logPanel, final OrderEntryApplication application, JMenuBar menuBar, IDGenerator idGenerator) {
        super();
        setTitle("Order Entry Frame");
        setSize(900, 600);

        getContentPane().add(new OrderEntryPanel(marketTableModel, orderTableModel, executedOrdersTableModel, logPanel, application, idGenerator), BorderLayout.CENTER);
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