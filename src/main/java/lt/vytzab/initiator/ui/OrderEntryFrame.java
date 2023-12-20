package lt.vytzab.initiator.ui;

import java.awt.*;

import javax.swing.JFrame;

import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.execution.ExecutionTableModel;
import lt.vytzab.initiator.order.OrderTableModel;
import lt.vytzab.initiator.ui.panels.LogPanel;
import lt.vytzab.initiator.ui.panels.OrderEntryPanel;

public class OrderEntryFrame extends JFrame {

    public OrderEntryFrame(OrderTableModel orderTableModel, ExecutionTableModel executionTableModel, LogPanel logPanel, final OrderEntryApplication application) {
        super();
        setTitle("Order Entry");
        setSize(600, 400);
        getContentPane().add(new OrderEntryPanel(orderTableModel, executionTableModel, logPanel, application), BorderLayout.CENTER);
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