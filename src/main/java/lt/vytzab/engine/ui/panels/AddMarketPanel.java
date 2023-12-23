package lt.vytzab.engine.ui.panels;

import lt.vytzab.engine.*;
import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.helpers.DoubleNumberTextField;
import lt.vytzab.engine.helpers.IntegerNumberTextField;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.SessionNotFound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class AddMarketPanel extends JPanel implements Observer {
    private boolean symbolEntered = false;
    private boolean lastPriceEntered = false;
    private boolean dayHighEntered = false;
    private boolean dayLowEntered = false;
    private boolean buyVolumeEntered = false;
    private boolean sellVolumeEntered = false;
    private final JTextField symbolTextField = new JTextField();
    private final DoubleNumberTextField lastPriceField = new DoubleNumberTextField();
    private final DoubleNumberTextField dayHighField = new DoubleNumberTextField();
    private final DoubleNumberTextField dayLowField = new DoubleNumberTextField();
    private final DoubleNumberTextField buyVolumeField = new DoubleNumberTextField();
    private final DoubleNumberTextField sellVolumeField = new DoubleNumberTextField();

    private final JLabel messageLabel = new JLabel(" ");
    private final JButton submitButton = new JButton("Submit");

    private MarketTableModel marketTableModel = null;
    private transient EngineApplication application = null;

    private final GridBagConstraints constraints = new GridBagConstraints();

    public AddMarketPanel(final MarketTableModel marketTableModel, final EngineApplication application) {
        setName("MarketEntryPanel");
        this.marketTableModel = marketTableModel;
        this.application = application;

        SubmitActivator activator = new SubmitActivator();
        symbolTextField.addKeyListener(activator);
        lastPriceField.addKeyListener(activator);
        dayHighField.addKeyListener(activator);
        dayLowField.addKeyListener(activator);
        buyVolumeField.addKeyListener(activator);
        sellVolumeField.addKeyListener(activator);

        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        setLayout(new GridBagLayout());
        createComponents();
    }

    public void addActionListener(ActionListener listener) {
        submitButton.addActionListener(listener);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
        if (message == null || message.equals("")) messageLabel.setText(" ");
    }

    public void clearMessage() {
        setMessage(null);
    }

    private void createComponents() {
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        int x = 0;
        int y = 0;

        add(new JLabel("Symbol"), x, y);
        add(new JLabel("Last Price"), ++x, y);
        add(new JLabel("Day High"), ++x, y);
        add(new JLabel("Day Low"), ++x, y);
        add(new JLabel("Buy Volume"), ++x, y);
        add(new JLabel("Sell Volume"), ++x, y);
        constraints.ipadx = 30;

        symbolTextField.setName("SymbolTextField");
        add(symbolTextField, x = 0, ++y);
        constraints.ipadx = 0;
        lastPriceField.setName("LastPriceField");
        add(lastPriceField, ++x, y);
        dayHighField.setName("DayHighField");
        add(dayHighField, ++x, y);
        dayLowField.setName("DayLowField");
        add(dayLowField, ++x, y);
        buyVolumeField.setName("BuyVolumeField");
        add(buyVolumeField, ++x, y);
        sellVolumeField.setName("SellVolumeField");
        add(sellVolumeField, ++x, y);

        constraints.insets = new Insets(3, 5, 3, 5);
        submitButton.setName("SubmitButton");
        add(submitButton, ++x, y);
        constraints.gridwidth = 0;
        add(messageLabel, 0, ++y);

        Font font = new Font(messageLabel.getFont().getFontName(), Font.BOLD, 12);
        messageLabel.setFont(font);
        messageLabel.setForeground(Color.red);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        submitButton.setEnabled(false);
        submitButton.addActionListener(new SubmitListener());
        activateSubmit();
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
    }

    private void activateSubmit() {
        boolean activate = symbolEntered && lastPriceEntered && dayHighEntered && dayLowEntered && buyVolumeEntered && sellVolumeEntered;
        submitButton.setEnabled(activate); // Set the state of the submit button
    }

    @Override
    public void update(Observable o, Object arg) {

    }

    private class SubmitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Market market = new Market();
            market.setSymbol(symbolTextField.getText());
            market.setLastPrice(Double.parseDouble(lastPriceField.getText()));
            market.setDayHigh(Double.parseDouble(dayHighField.getText()));
            market.setDayLow(Double.parseDouble(dayLowField.getText()));
            market.setBuyVolume(Integer.parseInt(buyVolumeField.getText()));
            market.setSellVolume(Integer.parseInt(sellVolumeField.getText()));

            MarketDataDAO.createMarket(market);
            marketTableModel.addMarket(market);
            try {
                application.sendSecurityStatusFromMarket(market, 0);
            } catch (FieldNotFound | SessionNotFound ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class SubmitActivator implements KeyListener, ItemListener {
        public void keyReleased(KeyEvent e) {
            Object obj = e.getSource();
            if (obj == symbolTextField) {
                symbolEntered = testField(obj);
            } else if (obj == lastPriceField) {
                lastPriceEntered = testField(obj);
            } else if (obj == dayHighField) {
                dayHighEntered = testField(obj);
            } else if (obj == dayLowField) {
                dayLowEntered = testField(obj);
            } else if (obj == buyVolumeField) {
                buyVolumeEntered = testField(obj);
            } else if (obj == sellVolumeField) {
                sellVolumeEntered = testField(obj);
            }
            activateSubmit();
        }

        private boolean testField(Object o) {
            String value = ((JTextField) o).getText();
            value = value.trim();
            return value.length() > 0;
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {

        }
    }
}