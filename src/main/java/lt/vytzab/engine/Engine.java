package lt.vytzab.engine;

import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.market.workers.MarketFillWorker;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.order.workers.AllOrderFillWorker;
import lt.vytzab.engine.order.workers.OpenOrderFillWorker;
import lt.vytzab.engine.ui.EngineFrame;
import lt.vytzab.engine.ui.panels.LogPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.field.*;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

import javax.management.JMException;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalTime;

import static lt.vytzab.engine.Variables.MARKET_ORDERS_DB;
import static lt.vytzab.engine.ui.EngineFrame.centerFrameOnScreen;

public class Engine {
    private final static Logger log = LoggerFactory.getLogger(Engine.class);
    private static SocketAcceptor acceptor = null;
    private static final MarketTableModel marketTableModel = new MarketTableModel();
    private static final OrderTableModel openOrderTableModel = new OrderTableModel();
    private static final OrderTableModel allOrderTableModel = new OrderTableModel();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        try {
            // bitu stream arba is failo arba default
            InputStream inputStream = getSettingsInputStream(args);
            // settings is bitu streamo
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();

            // sukuriamas Engine objektas
            Engine engine = new Engine(settings);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
//        Order order = MarketOrderDAO.readAllMarketOrders(MARKET_ORDERS_DB).get(0);
//        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
//
//        MarketDataSnapshotFullRefresh fixMD = new MarketDataSnapshotFullRefresh();
//        fixMD.setString(MDReqID.FIELD, "MdReqID");
//        System.out.println(fixMD);
//        fixMD.getHeader().setString(SenderCompID.FIELD, "SenderCompID");
//        fixMD.getHeader().setString(TargetCompID.FIELD, "TargetCompID");
//        System.out.println(fixMD);
//
//        fixMD.setString(Symbol.FIELD, order.getSymbol());
//        noMDEntries.setChar(MDEntryType.FIELD, order.getSide());
//        noMDEntries.setDouble(MDEntryPx.FIELD, order.getPrice());
//        noMDEntries.setDouble(CumQty.FIELD, order.getExecutedQuantity());
//        noMDEntries.setChar(OrdType.FIELD, order.getOrdType());
//        noMDEntries.setDouble(MDEntrySize.FIELD, order.getQuantity());
//        noMDEntries.setDouble(LeavesQty.FIELD, order.getOpenQuantity());
//        noMDEntries.setUtcDateOnly(MDEntryDate.FIELD, order.getEntryDate());
//        noMDEntries.setUtcTimeOnly(MDEntryTime.FIELD, LocalTime.now());
//        noMDEntries.setUtcDateOnly(ExpireDate.FIELD, order.getGoodTillDate());
//        noMDEntries.setString(OrderID.FIELD, order.getClOrdID());
//        noMDEntries.setString(Text.FIELD, "");
//
//        fixMD.addGroup(noMDEntries);
//        fixMD.addGroup(noMDEntries);
//        fixMD.addGroup(noMDEntries);
//
//        System.out.println(fixMD);
    }

    public Engine(SessionSettings settings) throws ConfigError, FieldConvertError, JMException {
        // Ivykiu registro atvaizdavimas
        LogPanel logPanel = new LogPanel();
        // Sukuriama Engine aplikacija
        EngineApplication application = new EngineApplication(openOrderTableModel, allOrderTableModel, logPanel);
        // Kaupia FIX zinutes
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        // Ivykiu registravimas
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        // Kuria FIX zinutes
        MessageFactory messageFactory = new DefaultMessageFactory();

        // Sukuriamas acceptorius priimantis connections is iniciatoriu
        acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory, messageFactory);
        JMenuBar menuBar = createMenu();
        JFrame frame = new EngineFrame(marketTableModel, openOrderTableModel, allOrderTableModel, logPanel, application, menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        centerFrameOnScreen(frame);
    }

    private static void start() throws RuntimeError, ConfigError {
        MarketFillWorker mfWorker = new MarketFillWorker(marketTableModel);
        mfWorker.execute();
        OpenOrderFillWorker oofWorker = new OpenOrderFillWorker(openOrderTableModel);
        oofWorker.execute();
        AllOrderFillWorker aofWorker = new AllOrderFillWorker(allOrderTableModel);
        aofWorker.execute();
        acceptor.start();
    }

    private static void stop() {
        marketTableModel.clearMarkets();
        openOrderTableModel.clearOrders();
        allOrderTableModel.clearOrders();
        acceptor.stop();
    }

    // Grazina bitu streama
    // Jeigu pateiktas argumentas, nuskaito faila
    // Jeigu nepateiktas, nuskaito default
    // Jeigu null, ismeta pranesima kaip naudoti
    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = Engine.class.getResourceAsStream("/engine.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + Engine.class.getName() + " [configFile].");
            System.exit(1);
        }
        return inputStream;
    }
    private static JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu sessionMenu = new JMenu("Session");
        JMenuItem startItem = new JMenuItem("Start");
        JMenuItem stopItem = new JMenuItem("Stop");

        startItem.addActionListener(e -> {
            try {
                start();
            } catch (ConfigError ex) {
                throw new RuntimeException(ex);
            }
        });

        stopItem.addActionListener(e -> {
            stop();
        });

        sessionMenu.add(startItem);
        sessionMenu.addSeparator();
        sessionMenu.add(stopItem);

        // Add menus to the menu bar
        menuBar.add(sessionMenu);

        return menuBar;
    }
}