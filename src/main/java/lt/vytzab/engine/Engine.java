package lt.vytzab.engine;

import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.dao.MarketOrderDAO;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderIdGenerator;
import lt.vytzab.engine.order.OrderTableModel;
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
import quickfix.field.OrdType;
import quickfix.field.Side;

import javax.management.JMException;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;

import static lt.vytzab.engine.helpers.DateTimeString.getCurrentDateTimeAsString;

public class Engine {
    private final static Logger log = LoggerFactory.getLogger(Engine.class);
    private static SocketAcceptor acceptor = null;
    private static final MarketTableModel marketTableModel = new MarketTableModel();

    public static void main(String[] args) {
//        OrderIdGenerator gen = new OrderIdGenerator();
        // public Market(String symbol, Double lastPrice, Double dayHigh, Double dayLow, int volume)
//        INSERT INTO market_data (symbol, last_price, day_high, day_low, volume)
//        Market market = new Market("TESTSYMBOL", 69.2, 99.9, 65.8, 10000);
//        MarketDataDAO.createMarket(market);
//        MarketDataDAO.updateMarket(market);
//        System.out.println(MarketDataDAO.readAllMarkets());
//        MarketDataDAO.deleteMarket("TESTSYMBOL");
//        System.out.println(MarketDataDAO.readMarket("TESTSYMBOL"));

        // public Order(long entryTime, String clOrdID, String symbol,
        // String senderCompID, String targetCompID, char side, char ordType, double price, long quantity, long openQuantity,
        // long executedQuantity, double avgExecutedPrice, double lastExecutedPrice, long lastExecutedQuantity, boolean rejected,
        // boolean canceled, LocalDate entryDate, LocalDate goodTillDate) {
        //INSERT INTO market_orders (clOrdID, symbol, senderCompID, targetCompID, side, ordType, price, quantity, openQuantity, executedQuantity,
        // avgExecutedPrice, lastExecutedPrice, lastExecutedQuantity, entryTime, rejected, canceled, entryDate, goodTillDate
//        Order order = new Order(System.currentTimeMillis(), "12", "TESTSYMBOL3", "TESTSENDERCOMPID1", "TESTTARGETCOMPID", '1', '1',
//                70.1, 1, 1000, 678, 69.9, 70.0, 78, false, false, LocalDate.now(), LocalDate.now().plusDays(7));
//        Order orderx = new Order(System.currentTimeMillis(), "234", "TESTSYMBOL3", "TESTSENDERCOMPID1", "TESTTARGETCOMPID", '1', '1',
//                70.1, 1, 1000, 678, 69.9, 70.0, 78, false, false, LocalDate.now(), LocalDate.now().plusDays(7));
//        Order orderz = new Order(System.currentTimeMillis(), "55", "TESTSYMBOL3", "TESTSENDERCOMPID2", "TESTTARGETCOMPID", '1', '1',
//                70.1, 2, 1000, 678, 69.9, 70.0, 78, false, false, LocalDate.now(), LocalDate.now().plusDays(7));
//        Order ordert = new Order(System.currentTimeMillis(), "678", "TESTSYMBOL3", "TESTSENDERCOMPID3", "TESTTARGETCOMPID", '1', '1',
//                70.1, 3, 1000, 678, 69.9, 70.0, 78, false, false, LocalDate.now(), LocalDate.now().plusDays(7));
//        Order order2 = new Order(System.currentTimeMillis(), "888", "TESTSYMBOL4", "TESTSENDERCOMPID3", "TESTTARGETCOMPID", '1', '1',
//                70.1, 3, 1000, 678, 69.9, 70.0, 78, false, false, LocalDate.now(), LocalDate.now().plusDays(7));
//        Order order3 = new Order(System.currentTimeMillis(), "964", "TESTSYMBOL5", "TESTSENDERCOMPID2", "TESTTARGETCOMPID", '1', '1',
//                70.1, 2, 1000, 678, 69.9, 70.0, 78, false, false, LocalDate.now(), LocalDate.now().plusDays(7));
//        MarketOrderDAO.createMarketOrder(order);
//        MarketOrderDAO.createMarketOrder(order2);
//        MarketOrderDAO.createMarketOrder(order3);
//        System.out.println(MarketOrderDAO.readAllMarketOrdersByField("price", "70.1"));
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
    }

    public Engine(SessionSettings settings) throws ConfigError, FieldConvertError, JMException {
        // Uzfiksuojamas dabartinis laikas logams
        String currentTime = getCurrentDateTimeAsString();
        // Ivykiu registro atvaizdavimas
        LogPanel logPanel = new LogPanel();
        // Galiojantiems uzsakymams atvaizduoti
        OrderTableModel openOrderTableModel = new OrderTableModel();
        // Visiems gautiems uzsakymams atvaizduoti
        OrderTableModel allOrderTableModel = new OrderTableModel();
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
    }

    private static void start() throws RuntimeError, ConfigError {
        marketTableModel.getMarketsFromDB();
        acceptor.start();
    }

    private static void stop() {
        marketTableModel.saveMarketsToDB();
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