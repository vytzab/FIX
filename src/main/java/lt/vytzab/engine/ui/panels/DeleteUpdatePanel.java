package lt.vytzab.engine.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import lt.vytzab.engine.*;
import lt.vytzab.engine.dao.MarketDataDAO;
import lt.vytzab.engine.helpers.DoubleNumberTextField;
import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;
import quickfix.FieldNotFound;
import quickfix.SessionNotFound;

public class DeleteUpdatePanel extends JPanel {
    private final JLabel lastPriceLabel = new JLabel("Last Price");
    private final JLabel dayHighLabel = new JLabel("Day High");
    private final JLabel dayLowLabel = new JLabel("Day Low");
    private final JLabel buyVolumeLabel = new JLabel("Buy Volume");
    private final JLabel sellVolumeLabel = new JLabel("Sell Volume");
    private final DoubleNumberTextField lastPriceField = new DoubleNumberTextField();
    private final DoubleNumberTextField dayHighField = new DoubleNumberTextField();
    private final DoubleNumberTextField dayLowField = new DoubleNumberTextField();
    private final DoubleNumberTextField buyVolumeField = new DoubleNumberTextField();
    private final DoubleNumberTextField sellVolumeField = new DoubleNumberTextField();
    private final JButton cancelButton = new JButton("Delete");
    private final JButton replaceButton = new JButton("Update");
    private Market market = null;

    private MarketTableModel marketTableModel = null;
    private final GridBagConstraints constraints = new GridBagConstraints();

    private final EngineApplication application;

    public DeleteUpdatePanel(final MarketTableModel marketTableModel, final EngineApplication application) {
        this.application = application;
        this.marketTableModel = marketTableModel;
        cancelButton.addActionListener(new DeleteListener());
        replaceButton.addActionListener(new UpdateListener());

        setLayout(new GridBagLayout());
        createComponents();
    }

    public void addActionListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
        replaceButton.addActionListener(listener);
    }

    private void createComponents() {
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        int x = 0;
        int y = 0;

        constraints.insets = new Insets(0, 0, 5, 5);
        add(cancelButton, x, y);
        add(replaceButton, ++x, y);
        constraints.weightx = 0;
        add(lastPriceLabel, ++x, y);
        constraints.weightx = 5;
        add(lastPriceField, ++x, y);
        constraints.weightx = 5;
        add(dayHighLabel, ++x, y);
        constraints.weightx = 5;
        add(dayHighField, ++x, y);
        constraints.weightx = 0;
        add(dayLowLabel, ++x, y);
        constraints.weightx = 5;
        add(dayLowField, ++x, y);
        constraints.weightx = 5;
        add(buyVolumeLabel, ++x, y);
        constraints.weightx = 5;
        add(buyVolumeField, ++x, y);
        constraints.weightx = 5;
        add(sellVolumeLabel, ++x, y);
        constraints.weightx = 5;
        add(sellVolumeField, ++x, y);
    }

    public void setEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
        replaceButton.setEnabled(enabled);
        lastPriceField.setEnabled(enabled);
        dayHighField.setEnabled(enabled);
        dayLowField.setEnabled(enabled);
        buyVolumeField.setEnabled(enabled);
        sellVolumeField.setEnabled(enabled);

        Color labelColor = enabled ? Color.black : Color.gray;
        Color bgColor = enabled ? Color.white : Color.gray;
        lastPriceField.setBackground(bgColor);
        lastPriceLabel.setBackground(bgColor);
        dayHighField.setForeground(labelColor);
        dayHighLabel.setForeground(labelColor);
        dayLowField.setBackground(bgColor);
        dayLowLabel.setBackground(bgColor);
        buyVolumeField.setBackground(bgColor);
        buyVolumeLabel.setBackground(bgColor);
        sellVolumeField.setBackground(bgColor);
        sellVolumeLabel.setBackground(bgColor);
    }

    public void update() {
        setMarket(this.market);
    }

    public void setMarket(Market market) {
        if (market != null) {
            this.market = market;
            lastPriceField.setText(Double.toString(market.getLastPrice()));
            dayHighField.setText(Double.toString(market.getDayHigh()));
            dayLowField.setText(Double.toString(market.getDayLow()));
            buyVolumeField.setText(Double.toString(market.getBuyVolume()));
            sellVolumeField.setText(Double.toString(market.getLastPrice()));
            setEnabled(true);
        }
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
    }

    private class DeleteListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MarketDataDAO.deleteMarket(market.getSymbol());
            marketTableModel.removeMarket(market.getSymbol());
            try {
                application.sendSecurityStatusFromMarket(market, 2);
            } catch (FieldNotFound | SessionNotFound ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class UpdateListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Market newMarket = new Market();
            newMarket.setSymbol(market.getSymbol());
            newMarket.setLastPrice(Double.parseDouble(lastPriceField.getText()));
            newMarket.setDayHigh(Double.parseDouble(dayHighField.getText()));
            newMarket.setDayLow(Double.parseDouble(dayLowField.getText()));
            newMarket.setBuyVolume(Double.parseDouble(buyVolumeField.getText()));
            newMarket.setSellVolume(Double.parseDouble(sellVolumeField.getText()));

            MarketDataDAO.updateMarket(newMarket);
            marketTableModel.replaceMarket(newMarket, newMarket.getSymbol());
            try {
                application.sendSecurityStatusFromMarket(newMarket, 1);
            } catch (FieldNotFound | SessionNotFound ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}