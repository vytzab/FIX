package lt.vytzab.initiator.ui;

import java.awt.BorderLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import lt.vytzab.initiator.OrderEntry;
import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.ExecutionTableModel;
import lt.vytzab.initiator.OrderTableModel;

public class OrderEntryFrame extends JFrame {

    public OrderEntryFrame(OrderTableModel orderTableModel, ExecutionTableModel executionTableModel, LogPanel logPanel, final OrderEntryApplication application) {
        super();
        setTitle("Banzai!");
        setSize(600, 400);

//        if (System.getProperties().containsKey("openfix")) {
//            createMenuBar(application);
//        }
        getContentPane().add(new OrderEntryPanel(orderTableModel, executionTableModel, logPanel, application),
                BorderLayout.CENTER);
        setVisible(true);
    }
//
//    private void createMenuBar(final OrderEntryApplication application) {
//        JMenuBar menubar = new JMenuBar();
//
//        JMenu sessionMenu = new JMenu("Session");
//        menubar.add(sessionMenu);
//
//        JMenuItem logonItem = new JMenuItem("Logon");
//        logonItem.addActionListener(e -> OrderEntry.get().logon());
//        sessionMenu.add(logonItem);
//
//        JMenuItem logoffItem = new JMenuItem("Logoff");
//        logoffItem.addActionListener(e -> OrderEntry.get().logout());
//        sessionMenu.add(logoffItem);
//
//        JMenu appMenu = new JMenu("Application");
//        menubar.add(appMenu);
//
//        JMenuItem appAvailableItem = new JCheckBoxMenuItem("Available");
//        appAvailableItem.setSelected(application.isAvailable());
//        appAvailableItem.addActionListener(e -> application.setAvailable(((JCheckBoxMenuItem) e.getSource()).isSelected()));
//        appMenu.add(appAvailableItem);
//
//        JMenuItem sendMissingFieldRejectItem = new JCheckBoxMenuItem("Send Missing Field Reject");
//        sendMissingFieldRejectItem.setSelected(application.isMissingField());
//        sendMissingFieldRejectItem.addActionListener(e -> application.setMissingField(((JCheckBoxMenuItem) e.getSource()).isSelected()));
//        appMenu.add(sendMissingFieldRejectItem);
//
//        setJMenuBar(menubar);
//    }
}