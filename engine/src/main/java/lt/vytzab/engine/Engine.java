package lt.vytzab.engine;

import lt.vytzab.engine.helpers.IDGenerator;
import lt.vytzab.engine.dao.MarketDAO;
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
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Scanner;

import static lt.vytzab.engine.ui.EngineFrame.centerFrameOnScreen;

public class Engine {
    private static final Logger engLogger = LoggerFactory.getLogger(Engine.class);
    private static SocketAcceptor acceptor = null;
    private static final MarketTableModel marketTableModel = new MarketTableModel();
    private static final OrderTableModel openOrderTableModel = new OrderTableModel();
    private static final OrderTableModel allOrderTableModel = new OrderTableModel();
    private static boolean started = false;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            engLogger.debug("Exception " + e.getMessage() + " was caught while setting Engine look and feel.");
        }
        try {
            InputStream inputStream = getSettingsInputStream(args);
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();
            setDBCredentials();

            Engine engine = new Engine(settings);
        } catch (ConfigError e) {
            engLogger.debug("ConfigError " + e.getMessage() + " was caught while initializing Session settings or Engine .");
        } catch (FileNotFoundException e) {
            engLogger.debug("FileNotFoundException " + e.getMessage() + " was caught while creating input stream.");
        } catch (IOException e) {
            engLogger.debug("IOException " + e.getMessage() + " was caught while closing input stream.");
        }
    }

    public Engine(SessionSettings settings) throws ConfigError {
        LogPanel logPanel = new LogPanel();
        IDGenerator idGenerator = new IDGenerator();
        EngineApplication application = new EngineApplication(marketTableModel, openOrderTableModel, allOrderTableModel, logPanel, idGenerator);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();

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
        engLogger.info("Engine has started successfully.");
    }

    private static void stop() throws SQLException {
        marketTableModel.clearMarkets();
        openOrderTableModel.clearOrders();
        allOrderTableModel.clearOrders();
        if (started) {
            acceptor.stop();
            started = false;
        }
        engLogger.info("Engine has stopped successfully.");
    }

    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = Engine.class.getResourceAsStream("/engine.cfg");
            engLogger.info("Engine configuration input stream was taken from resources file.");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
            engLogger.info("Engine configuration input stream was taken from user provided file.");
        }
        if (inputStream == null) {
            System.out.println("usage: " + Engine.class.getName() + " [configFile].");
            System.exit(1);
            engLogger.info("Engine configuration input stream is null.");
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
                engLogger.debug("ConfigError " + ex.getMessage() + " was caught while starting Engine.");
            }
        });

        stopItem.addActionListener(e -> {
            try {
                stop();
            } catch (SQLException ex) {
                engLogger.debug("SQLException " + ex.getMessage() + " was caught while stopping Engine.");
            }
        });

        sessionMenu.add(startItem);
        sessionMenu.addSeparator();
        sessionMenu.add(stopItem);

        menuBar.add(sessionMenu);

        return menuBar;
    }

    private static void setDBCredentials() {
        boolean connected = false;
        Scanner scanner = new Scanner(System.in);
        while (!connected) {
            System.out.print("Please enter the database username: ");
            Variables.setUsername(scanner.nextLine());

            System.out.print("Please enter the database password: ");
            Variables.setPassword(scanner.nextLine());

            connected = MarketDAO.checkDatabaseConnectivity();
        }
    }
}