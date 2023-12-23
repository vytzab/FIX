package lt.vytzab.initiator.ui.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.ZoneId;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;
import lt.vytzab.initiator.OrderEntryApplication;
import lt.vytzab.initiator.helpers.DoubleNumberTextField;
import lt.vytzab.initiator.helpers.IntegerNumberTextField;
import lt.vytzab.initiator.helpers.LogonEvent;
import lt.vytzab.initiator.order.Order;
import lt.vytzab.initiator.order.OrderSide;
import lt.vytzab.initiator.order.OrderTIF;
import lt.vytzab.initiator.order.OrderTableModel;
import lt.vytzab.initiator.order.OrderType;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.TimeInForce;

public class AddOrderPanel extends JPanel implements Observer {
    private boolean symbolEntered = false;
    private boolean quantityEntered = false;
    private boolean limitEntered = false;
    private boolean sessionEntered = false;

    private final JTextField symbolTextField = new JTextField();
    private final IntegerNumberTextField quantityTextField = new IntegerNumberTextField();

    private final JComboBox sideComboBox = new JComboBox(OrderSide.toArray());
    private final JComboBox typeComboBox = new JComboBox(OrderType.toArray());
    private final JComboBox tifComboBox = new JComboBox(OrderTIF.toArray());

    private final DoubleNumberTextField limitPriceTextField = new DoubleNumberTextField();

    private final JDateChooser dateChooser = new JDateChooser();
    private final JComboBox sessionComboBox = new JComboBox();

    private final JLabel limitPriceLabel = new JLabel("Limit");
    private final JLabel dateLabel = new JLabel("Good till Date");

    private final JLabel messageLabel = new JLabel(" ");
    private final JButton submitButton = new JButton("Submit");

    private OrderTableModel orderTableModel = null;
    private transient OrderEntryApplication application = null;

    private final GridBagConstraints constraints = new GridBagConstraints();

    public AddOrderPanel(final OrderTableModel orderTableModel, final OrderEntryApplication application) {
        setName("OrderEntryPanel");
        this.orderTableModel = orderTableModel;
        this.application = application;

        application.addLogonObserver(this);

        SubmitActivator activator = new SubmitActivator();
        symbolTextField.addKeyListener(activator);
        quantityTextField.addKeyListener(activator);
        limitPriceTextField.addKeyListener(activator);
        dateChooser.addKeyListener(activator);
        sessionComboBox.addItemListener(activator);

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
        add(new JLabel("Quantity"), ++x, y);
        add(new JLabel("Side"), ++x, y);
        add(new JLabel("Type"), ++x, y);
        constraints.ipadx = 30;
        add(limitPriceLabel, ++x, y);
        add(dateLabel, ++x, y);
        constraints.ipadx = 0;
        add(new JLabel("TIF"), ++x, y);
        constraints.ipadx = 30;

        symbolTextField.setName("SymbolTextField");
        add(symbolTextField, x = 0, ++y);
        constraints.ipadx = 0;
        quantityTextField.setName("QuantityTextField");
        add(quantityTextField, ++x, y);
        sideComboBox.setName("SideComboBox");
        add(sideComboBox, ++x, y);
        typeComboBox.setName("TypeComboBox");
        add(typeComboBox, ++x, y);
        limitPriceTextField.setName("LimitPriceTextField");
        add(limitPriceTextField, ++x, y);
        dateChooser.setName("DateChooser");
        add(dateChooser, ++x, y);
        tifComboBox.setName("TifComboBox");
        add(tifComboBox, ++x, y);

        constraints.insets = new Insets(3, 0, 0, 0);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        sessionComboBox.setName("SessionComboBox");
        add(sessionComboBox, 0, ++y);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        submitButton.setName("SubmitButton");
        add(submitButton, x, y);
        constraints.gridwidth = 0;
        add(messageLabel, 0, ++y);

        typeComboBox.addItemListener(new PriceListener());
        typeComboBox.setSelectedItem(OrderType.MARKET);

        tifComboBox.addItemListener(new TIFListener());
        tifComboBox.setSelectedItem(OrderTIF.DAY);

        limitPriceTextField.setEnabled(false);
        dateChooser.setEnabled(false);

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
        OrderType type = (OrderType) typeComboBox.getSelectedItem();
        boolean activate = symbolEntered && quantityEntered && sessionEntered;

        if (type == OrderType.MARKET) {
            submitButton.setEnabled(activate);
        } else if (type == OrderType.LIMIT) {
            submitButton.setEnabled(activate && limitEntered);
        }

    }

    private class PriceListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            OrderType item = (OrderType) typeComboBox.getSelectedItem();
            if (item == OrderType.MARKET) {
                enableLimitPrice(false);
            } else if (item == OrderType.LIMIT) {
                enableLimitPrice(true);
            } else {
                enableLimitPrice(true);
            }
            activateSubmit();
        }

        private void enableLimitPrice(boolean enabled) {
            Color labelColor = enabled ? Color.black : Color.gray;
            Color bgColor = enabled ? Color.white : Color.gray;
            limitPriceTextField.setEnabled(enabled);
            limitPriceTextField.setBackground(bgColor);
            limitPriceLabel.setForeground(labelColor);
        }
    }

    private class TIFListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            OrderTIF item = (OrderTIF) tifComboBox.getSelectedItem();
            enableDate(item == OrderTIF.GTD);
            activateSubmit();
        }

        private void enableDate(boolean enabled) {
            dateChooser.setEnabled(enabled);
        }
    }

    public void update(Observable o, Object arg) {
        LogonEvent logonEvent = (LogonEvent) arg;
        if (logonEvent.isLoggedOn()) sessionComboBox.addItem(logonEvent.getSessionID());
        else sessionComboBox.removeItem(logonEvent.getSessionID());
    }

    private class SubmitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Order order = new Order();
            order.setSide((OrderSide) sideComboBox.getSelectedItem());
            order.setType((OrderType) typeComboBox.getSelectedItem());
            order.setTIF((OrderTIF) tifComboBox.getSelectedItem());

            order.setSymbol(symbolTextField.getText());
            order.setQuantity(Integer.parseInt(quantityTextField.getText()));
            order.setOpenQuantity(order.getQuantity());

            order.setGoodTillDate(dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            OrderType type = order.getType();
            if (type == OrderType.LIMIT) {
                order.setLimit(limitPriceTextField.getText());
            }
            orderTableModel.addOrder(order);
            try {
                application.sendNewOrderSingle(order, (SessionID) sessionComboBox.getSelectedItem());
            } catch (SessionNotFound ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class SubmitActivator implements KeyListener, ItemListener {
        public void keyReleased(KeyEvent e) {
            Object obj = e.getSource();
            if (obj == symbolTextField) {
                symbolEntered = testField(obj);
            } else if (obj == quantityTextField) {
                quantityEntered = testField(obj);
            } else if (obj == limitPriceTextField) {
                limitEntered = testField(obj);
            }
            activateSubmit();
        }

        public void itemStateChanged(ItemEvent e) {
            sessionEntered = sessionComboBox.getSelectedItem() != null;
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
    }
}