package lt.vytzab.initiator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.management.JMException;
import javax.swing.*;

import lt.vytzab.initiator.helpers.IDGenerator;
import lt.vytzab.initiator.market.MarketTableModel;
import lt.vytzab.initiator.ui.panels.LogPanel;
import lt.vytzab.initiator.order.OrderTableModel;
import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import lt.vytzab.initiator.ui.OrderEntryFrame;

import static lt.vytzab.initiator.ui.OrderEntryFrame.centerFrameOnScreen;

public class OrderEntry {
    private static final Logger ordEntLogger = LoggerFactory.getLogger(OrderEntry.class);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static OrderEntry orderEntry;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            ordEntLogger.info(e.getMessage(), e);
        }
        orderEntry = new OrderEntry(args);
        shutdownLatch.await();
    }

    public OrderEntry(String[] args) {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = OrderEntry.class.getResourceAsStream("/orderEntry.cfg");
            ordEntLogger.info("Order Entry configuration input stream was taken from resources file.");
        } else if (args.length == 1) {
            try {
                inputStream = new FileInputStream(args[0]);
                ordEntLogger.info("Order Entry configuration input stream was taken from user provided file.");
            } catch (FileNotFoundException e) {
                ordEntLogger.error("FieldNotFound " + e.getMessage() + " Order Entry configuration file not found.");
            }
        }
        if (inputStream == null) {
            System.out.println("usage: " + OrderEntry.class.getName() + " [configFile].");
            ordEntLogger.info("Order Entry configuration input stream is null.");
            return;
        }
        SessionSettings settings = null;
        try {
            settings = new SessionSettings(inputStream);
        } catch (ConfigError e) {
            ordEntLogger.debug("ConfigError " + e.getMessage() + " was caught while initializing Session settings.");
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            ordEntLogger.debug("IOException " + e.getMessage() + " was caught while closing input stream.");
        }

        boolean logHeartbeats = Boolean.parseBoolean(System.getProperty("logHeartbeats", "true"));

        OrderTableModel orderTableModel = new OrderTableModel();
        OrderTableModel executedOrdersTableModel = new OrderTableModel();
        MarketTableModel marketTableModel = new MarketTableModel();
        LogPanel logPanel = new LogPanel();
        IDGenerator idGenerator = new IDGenerator();
        OrderEntryApplication application = new OrderEntryApplication(marketTableModel, orderTableModel, executedOrdersTableModel, logPanel, idGenerator);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        try {
            assert settings != null;
            initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);
        } catch (ConfigError e) {
            throw new RuntimeException(e);
        }
        JMenuBar menuBar = createMenu();

        JmxExporter exporter = null;
        try {
            exporter = new JmxExporter();
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
        exporter.register(initiator);

        JFrame frame = new OrderEntryFrame(marketTableModel, orderTableModel, executedOrdersTableModel, logPanel, application, menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        centerFrameOnScreen(frame);
    }

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                ordEntLogger.error("Logon failed", e);
            }
        } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    public void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
        shutdownLatch.countDown();
    }

    public static OrderEntry get() {
        return orderEntry;
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu sessionMenu = new JMenu("Session");
        JMenuItem startItem = new JMenuItem("Logon");
        JMenuItem stopItem = new JMenuItem("Logout");

        startItem.addActionListener(e -> logon());

        stopItem.addActionListener(e -> logout());

        sessionMenu.add(startItem);
        sessionMenu.addSeparator();
        sessionMenu.add(stopItem);

        menuBar.add(sessionMenu);

        return menuBar;
    }
}