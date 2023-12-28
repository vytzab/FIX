package lt.vytzab.engine;

import lt.vytzab.engine.market.workers.MarketFillWorker;
import lt.vytzab.engine.market.MarketTableModel;
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

import javax.management.JMException;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static lt.vytzab.engine.ui.EngineFrame.centerFrameOnScreen;

public class Engine {
    private final static Logger log = LoggerFactory.getLogger(Engine.class);
    private static SocketAcceptor acceptor = null;
    private static final MarketTableModel marketTableModel = new MarketTableModel();
    private static final OrderTableModel openOrderTableModel = new OrderTableModel();
    private static final OrderTableModel allOrderTableModel = new OrderTableModel();
    private static boolean started = false;

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
            //TODO add credentials for DB

            // sukuriamas Engine objektas
            Engine engine = new Engine(settings);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
        if (!started) {
            acceptor.start();
            started = true;
        }
    }

    private static void stop() {
        marketTableModel.clearMarkets();
        openOrderTableModel.clearOrders();
        allOrderTableModel.clearOrders();
        if (started) {
            acceptor.stop();
            started = false;
        }
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

        menuBar.add(sessionMenu);

        return menuBar;
    }
}