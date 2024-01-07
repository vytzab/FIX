package lt.vytzab.initiator.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import lt.vytzab.initiator.*;
import lt.vytzab.initiator.helpers.IntegerNumberTextField;
import lt.vytzab.initiator.order.Order;
import quickfix.SessionNotFound;

public class CancelReplacePanel extends JPanel {
    private final JLabel quantityLabel = new JLabel("Quantity");
    private final IntegerNumberTextField quantityTextField = new IntegerNumberTextField();
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton replaceButton = new JButton("Replace");
    private Order order = null;
    private final GridBagConstraints constraints = new GridBagConstraints();
    private final OrderEntryApplication application;

    public CancelReplacePanel(final OrderEntryApplication application) {
        this.application = application;
        cancelButton.addActionListener(new CancelListener());
        replaceButton.addActionListener(new ReplaceListener());

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
        add(quantityLabel, ++x, y);
        constraints.weightx = 5;
        add(quantityTextField, ++x, y);
        constraints.weightx = 0;
    }

    public void setEnabledMarket(boolean enabled) {
        cancelButton.setEnabled(enabled);
        replaceButton.setEnabled(enabled);
        quantityTextField.setEnabled(enabled);

        Color labelColor = enabled ? Color.black : Color.gray;
        Color bgColor = enabled ? Color.white : Color.gray;
        quantityTextField.setBackground(bgColor);
        quantityLabel.setForeground(labelColor);
    }

    public void update() {
        setOrder(this.order);
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order == null) {
            return;
        }
        setEnabledMarket(true);
        quantityTextField.setText(Double.toString(order.getOpenQuantity()));
    }

    private void add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
    }

    private class CancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                application.sendOrderCancelRequest(order);
            } catch (SessionNotFound ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class ReplaceListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if (checkQuantityField()) {
                double newQuantity = Double.parseDouble(quantityTextField.getText());

                try {
                    application.sendOrderCancelReplaceRequest(order, newQuantity);
                } catch (SessionNotFound ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        private boolean checkQuantityField() {
            String quantityText = quantityTextField.getText();
            if (quantityText != null && !quantityText.trim().isEmpty()) {
                try {
                    double quantity = Double.parseDouble(quantityText);
                    return quantity > 0;
                } catch (NumberFormatException ex) {
                    showMessageDialog();
                    return false;
                }
            }
            return false;
        }

        private void showMessageDialog() {
            JOptionPane.showMessageDialog(CancelReplacePanel.this, "Please enter a valid positive quantity.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
        }
    }
}