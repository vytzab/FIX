package lt.vytzab.engine;

import lt.vytzab.engine.ui.EngineFrame;
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

import static lt.vytzab.utils.DateTimeString.getCurrentDateTimeAsString;

public class Engine {
    private final static Logger log = LoggerFactory.getLogger(Engine.class);
    private final SocketAcceptor acceptor;
    private JFrame frame = null;

    public static void main(String[] args) throws Exception {
        try {
            // bitu stream arba is failo arba default
            InputStream inputStream = getSettingsInputStream(args);
            // settings is bitu streamo
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();

            // sukuriamas Engine objektas
            Engine engine = new Engine(settings);

            engine.start();

            System.out.println("press <enter> to quit");
            System.in.read();

            engine.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Engine(SessionSettings settings) throws ConfigError, FieldConvertError, JMException {
        // Uzfiksuojamas dabartinis laikas logams
        String currentTime = getCurrentDateTimeAsString();
        LogPanel logPanel = new LogPanel();
        OrderTableModel orderTableModel = new OrderTableModel();
        // Sukuriama Engine aplikacija
        EngineApplication application = new EngineApplication(orderTableModel, logPanel);
        // Kaupia FIX zinutes
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        // Ivykiu registravimas
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        // Kuria FIX zinutes
        MessageFactory messageFactory = new DefaultMessageFactory();

        // Sukuriamas acceptorius priimantis connections is iniciatoriu
        acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory, messageFactory);

        frame = new EngineFrame(orderTableModel, logPanel, application);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void start() throws RuntimeError, ConfigError {
        acceptor.start();
    }

    private void stop() {
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
}