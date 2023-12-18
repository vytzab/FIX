package lt.vytzab.initiator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.UIManager;

import lt.vytzab.initiator.messages.NewOrderSingle;
import lt.vytzab.initiator.ui.LogPanel;
import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import lt.vytzab.initiator.ui.OrderEntryFrame;
import quickfix.field.*;

public class OrderEntry {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final Logger log = LoggerFactory.getLogger(OrderEntry.class);
    private static OrderEntry banzai;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;
    private JFrame frame = null;

    public OrderEntry(String[] args) throws Exception {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = OrderEntry.class.getResourceAsStream("/orderEntry.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + OrderEntry.class.getName() + " [configFile].");
            return;
        }
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));

        OrderTableModel orderTableModel = new OrderTableModel();
        ExecutionTableModel executionTableModel = new ExecutionTableModel();
        LogPanel logPanel = new LogPanel();
        OrderEntryApplication application = new OrderEntryApplication(orderTableModel, executionTableModel, logPanel);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);

        JmxExporter exporter = new JmxExporter();
        exporter.register(initiator);

        frame = new OrderEntryFrame(orderTableModel, executionTableModel, logPanel, application);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                log.error("Logon failed", e);
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
    }

    public void stop() {
        shutdownLatch.countDown();
    }

    public JFrame getFrame() {
        return frame;
    }

    public static OrderEntry get() {
        return banzai;
    }

    public static void main(String[] args) throws Exception {
//        Order order = new Order();
//        NewOrderSingle newOrderSingle = new NewOrderSingle(new ClOrdID(order.getID()), new HandlInst('1'), new Symbol("AAPL"), new Side('1'), new TransactTime(), new OrdType('1'));
//        newOrderSingle.setOrderQty(100);
//
//        if (order.getType() == OrderType.LIMIT) {
//            newOrderSingle.setField(new Price(order.getLimit()));
//        }
//        newOrderSingle.setField(new OrigClOrdID(order.generateID()));
//        System.out.println(new OrigClOrdID(order.generateID()));
//
//        System.out.println(order.getID());
//        System.out.println(new ClOrdID(order.getID()));
//        System.out.println(newOrderSingle);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        log.info("initiating Banzai!");
        banzai = new OrderEntry(args);
        log.info("Banzai initiated.");
        if (!System.getProperties().containsKey("openfix")) {
            banzai.logon();
        }
        shutdownLatch.await();
    }

}