package lt.vytzab.initiator.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import lt.vytzab.initiator.*;
import lt.vytzab.initiator.helpers.DoubleNumberTextField;
import lt.vytzab.initiator.helpers.IDGenerator;
import lt.vytzab.initiator.helpers.IntegerNumberTextField;
import lt.vytzab.initiator.order.Order;
import quickfix.SessionNotFound;

public class CancelReplacePanel extends JPanel {
    private final JLabel quantityLabel = new JLabel("Quantity");
    private final JLabel limitPriceLabel = new JLabel("Limit");
    private final IntegerNumberTextField quantityTextField = new IntegerNumberTextField();
    private final DoubleNumberTextField limitPriceTextField = new DoubleNumberTextField();
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
        add(limitPriceLabel, ++x, y);
        constraints.weightx = 5;
        add(limitPriceTextField, ++x, y);
    }

    public void setEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
        replaceButton.setEnabled(enabled);
        quantityTextField.setEnabled(enabled);
        limitPriceTextField.setEnabled(enabled);

        Color labelColor = enabled ? Color.black : Color.gray;
        Color bgColor = enabled ? Color.white : Color.gray;
        quantityTextField.setBackground(bgColor);
        limitPriceTextField.setBackground(bgColor);
        quantityLabel.setForeground(labelColor);
        limitPriceLabel.setForeground(labelColor);
    }

    public void update() {
        setOrder(this.order);
    }

    public void setOrder(Order order) {
        if (order == null) return;
        this.order = order;
        quantityTextField.setText(Double.toString(order.getOpenQuantity()));

        Double limit = order.getLimit();
        if (limit != null) limitPriceTextField.setText(order.getLimit().toString());
        setEnabled(order.getOpenQuantity() > 0);
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
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
            Order newOrder = (Order) order.clone();
            newOrder.setClOrdID(IDGenerator.genOrderID());
            newOrder.setQuantity(Double.parseDouble(quantityTextField.getText()));
            newOrder.setLimit(Double.parseDouble(limitPriceTextField.getText()));
            newOrder.setRejected(false);
            newOrder.setCanceled(false);

            try {
                application.sendOrderCancelReplaceRequest(order, newOrder);
            } catch (SessionNotFound ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}