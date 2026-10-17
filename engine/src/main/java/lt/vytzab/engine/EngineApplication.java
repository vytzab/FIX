package lt.vytzab.engine;

import lt.vytzab.engine.market.Market;
import lt.vytzab.engine.market.MarketTableModel;
import lt.vytzab.engine.order.OrderController;
import lt.vytzab.engine.helpers.IDGenerator;
import lt.vytzab.engine.market.MarketController;
import lt.vytzab.engine.order.Order;
import lt.vytzab.engine.order.OrderTableModel;
import lt.vytzab.engine.ui.panels.LogPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lt.vytzab.engine.helpers.CustomFixMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.*;
import quickfix.fix42.Message;
import quickfix.MessageCracker;
import quickfix.fix42.SecurityStatus;

public class EngineApplication extends MessageCracker implements quickfix.Application {
    private static final Logger engAppLogger = LoggerFactory.getLogger(EngineApplication.class);
    private final OrderTableModel openOrderTableModel;
    private final OrderTableModel allOrderTableModel;
    private final MarketTableModel marketTableModel;
    private final MarketController marketController = new MarketController();
    private final OrderController orderController = new OrderController();
    private final LogPanel logPanel;
    private final List<SessionID> sessionIDs = new ArrayList<>();
    private final IDGenerator idGenerator;

    public EngineApplication(MarketTableModel marketTableModel, OrderTableModel openOrderTableModel, OrderTableModel allOrderTableModel, LogPanel logPanel, IDGenerator idGenerator) {
        this.marketTableModel = marketTableModel;
        this.openOrderTableModel = openOrderTableModel;
        this.allOrderTableModel = allOrderTableModel;
        this.logPanel = logPanel;
        this.idGenerator = idGenerator;
    }

    public void onCreate(SessionID sessionId) {
    }

    public void onLogon(SessionID sessionId) {
        this.idGenerator.setSenderCompID(sessionId.getSenderCompID());
        sessionIDs.add(sessionId);
        Session session = Session.lookupSession(sessionId);
        if (session != null && session.isLoggedOn()) {
            try {
                sendSecurityStatusOnLogon(session);
            } catch (FieldNotFound | UnsupportedMessageType | IncorrectTagValue e) {
                engAppLogger.debug(e.getClass().getName() + e.getMessage() + " was caught while trying to send security status on Logon to " + session.getSessionID().getTargetCompID());
            }
        }
    }

    public void onLogout(SessionID sessionId) {
    }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("toAdmin:" + message.toString());
    }

    public void toApp(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("toApp:" + message.toString());
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("fromAdmin:" + message.toString());
    }

    public void fromApp(quickfix.Message message, SessionID sessionID) {
        displayFixMessageInLogs("fromApp:" + message.toString());
        try {
            crack(message, sessionID);
        } catch (UnsupportedMessageType e) {
            engAppLogger.error("UnsupportedMessageType " + e.getMessage() + " was caught while cracking a message from App.");
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while cracking a message from App.");
        } catch (IncorrectTagValue e) {
            engAppLogger.error("IncorrectTagValue " + e.getMessage() + " was caught while cracking a message from App.");
        }
    }

    //                          /\
    // System messages          ||
    // Messages from OrderEntry ||
    //                          \/

    public void onMessage(quickfix.fix42.NewOrderSingle message, SessionID sessionID) throws FieldNotFound {
        engAppLogger.info("Received a new order single message from " + message.getHeader().getString(SenderCompID.FIELD));
        engAppLogger.info("Message : " + message);
        Order order = orderFromNewOrderSingle(message);
        try {
            processNewOrder(order);
        } catch (Exception e) {
            order.reject();
            allOrderTableModel.addOrder(order);
            try {
                messageExecutionReport(message, OrdStatus.REJECTED);
            } catch (FieldNotFound ex) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send a message execution report for a rejected order.");
            }
            engAppLogger.error("Exception " + e + " was caught while processing new order single.");
        }
    }

    public void onMessage(quickfix.fix42.OrderCancelRequest message, SessionID sessionID) throws FieldNotFound {
        engAppLogger.info("Received an order cancel request message from " + message.getHeader().getString(SenderCompID.FIELD));
        engAppLogger.info("Message : " + message);
        Order order = null;
        try {
            order = orderController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to get order by ClOrdID using OrigClOrdID from order cancel request.");
        }
        if (order != null) {
            order.cancel();
            orderController.updateOrder(order);
            openOrderTableModel.removeOrder(order.getClOrdID());
            allOrderTableModel.replaceOrder(order);
            try {
                messageExecutionReport(message, OrdStatus.CANCELED);
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send a message execution report for a canceled order.");
            }
        } else {
            OrderCancelReject orderCancelReject = null;
            try {
                orderCancelReject = new OrderCancelReject(
                        new OrderID(idGenerator.genOrderID()),
                        new ClOrdID(message.getString(ClOrdID.FIELD)),
                        new OrigClOrdID(message.getString(OrigClOrdID.FIELD)),
                        new OrdStatus(OrdStatus.REJECTED),
                        new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an order cancel reject from order cancel request.");
            }
            try {
                try {
                    assert orderCancelReject != null;
                    Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting TargetCompID and SenderCompID from order cancel request.");
                }
            } catch (SessionNotFound e) {
                engAppLogger.error("Session not found while trying to send order cancel reject to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
            }
        }
    }

    public void onMessage(quickfix.fix42.OrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound {
        engAppLogger.info("Received an order cancel replace request message from " + message.getHeader().getString(SenderCompID.FIELD));
        engAppLogger.info("Message : " + message);
        Order order = null;
        try {
            order = orderController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to get order by ClOrdID using OrigClOrdID from order cancel replace request.");
        }
        if (order != null) {
            try {
                order.setQuantity((long)(message.getDouble(OrderQty.FIELD)));
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to set order Quantity from order cancel replace request OrderQty.");
            }
            try {
                order.setOpenQuantity((long)(message.getDouble(OrderQty.FIELD)));
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to set order OpenQuantity from order cancel replace request OrderQty.");
            }
            orderController.updateOrder(order);
            openOrderTableModel.replaceOrder(order);
            try {
                messageExecutionReport(message, OrdStatus.REPLACED);
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send a message execution report for a replaced order.");
            }
        } else {
            OrderCancelReject orderCancelReject = null;
            try {
                orderCancelReject = new OrderCancelReject(
                        new OrderID(idGenerator.genOrderID()),
                        new ClOrdID(message.getString(ClOrdID.FIELD)),
                        new OrigClOrdID(message.getString(OrigClOrdID.FIELD)),
                        new OrdStatus(OrdStatus.REJECTED),
                        new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST));
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an order cancel reject from order cancel replace request.");
            }
            try {
                try {
                    assert orderCancelReject != null;
                    Session.sendToTarget(orderCancelReject, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting TargetCompID and SenderCompID from order cancel replace request.");
                }
            } catch (SessionNotFound e) {
                engAppLogger.error("Session not found while trying to send order cancel reject to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
            }
        }
    }

    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound{
        engAppLogger.info("Received a market data request message from " + message.getHeader().getString(SenderCompID.FIELD));
        engAppLogger.info("Message : " + message);
        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();

        lt.vytzab.engine.messages.MarketDataSnapshotFullRefresh fixMD = new lt.vytzab.engine.messages.MarketDataSnapshotFullRefresh();
        try {
            fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting MDReqID from market data request.");
        }
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries;
        String senderCompId = null;
        try {
            senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting SenderCompID from market data request.");
        }
        String targetCompId = null;
        try {
            targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting TargetCompID from market data request.");
        }
        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);

        try {
            message.getGroup(1, noRelatedSyms);
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting NoRelatedSym from market data request.");
        }
        String symbol = null;
        try {
            symbol = noRelatedSyms.getString(Symbol.FIELD);
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting Symbol from market data request.");
        }
        fixMD.setString(Symbol.FIELD, symbol);
        List<Order> symbolOrders = orderController.getAllOrdersBySymbolAndSender(symbol, senderCompId);
        if (!symbolOrders.isEmpty()) {
            for (Order order : symbolOrders) {
                if (!order.getCanceled()){
                    noMDEntries = noMDEntriesFromOrder(order);
                    fixMD.addGroup(noMDEntries);
                }
            }
            try {
                Session.sendToTarget(fixMD, targetCompId, senderCompId);
            } catch (SessionNotFound e) {
                engAppLogger.error("Session not found while trying to send market data snapshot full refresh to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
            }
        }
    }

    public void onMessage(SecurityStatusRequest message, SessionID sessionID) throws FieldNotFound{
        engAppLogger.info("Received a security status request message from " + message.getHeader().getString(SenderCompID.FIELD));
        engAppLogger.info("Message : " + message);
        SecurityStatus securityStatus = null;
        try {
            securityStatus = securityStatusFromMarket(marketController.getMarket(message.getSymbol().toString()), 0);
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating a security status from market.");
        }
        try {
            try {
                assert securityStatus != null;
                Session.sendToTarget(securityStatus, message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getString(SenderCompID.FIELD));
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send a security status message in response to a security status request.");
            }
        } catch (SessionNotFound e) {
            engAppLogger.error("Session not found while trying to send security status to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
        }
    }

    private void processNewOrder(Order order) {
        if (marketController.checkIfMarketExists(order.getSymbol()) && !orderController.checkIfOrderExists(order.getClOrdID())) {
            if (orderController.createOrder(order)) {
                try {
                    orderExecutionReport(order, OrdStatus.NEW);
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send an execution report for a new order.");
                }

                openOrderTableModel.addOrder(order);
                allOrderTableModel.addOrder(order);

                ArrayList<Order> orders = new ArrayList<>();
                marketController.matchMarketOrders(marketController.getMarket(order.getSymbol()), orders);
                while (!orders.isEmpty()) {
                    openOrderTableModel.replaceOrder(orders.get(0));
                    allOrderTableModel.replaceOrder(orders.get(0));
                    try {
                        orderExecutionReport(orders.get(0), orders.get(0).isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
                    } catch (FieldNotFound e) {
                        engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send an execution report for a filled or partially filled order.");
                    }
                    orders.remove(0);
                }
                openOrderTableModel.removeFullyExecutedOrders();
                marketTableModel.addMarket(marketController.getMarket(order.getSymbol()));
                sendSecurityStatusFromMarket(marketController.getMarket(order.getSymbol()), 1);
            } else {
                order.reject();
                allOrderTableModel.addOrder(order);
                try {
                    orderExecutionReport(order, OrdStatus.REJECTED);
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send an execution report for a rejected order.");
                }
            }
        } else {
            order.reject();
            allOrderTableModel.addOrder(order);
            orderController.createOrder(order);
            try {
                orderExecutionReport(order, OrdStatus.REJECTED);
            } catch (FieldNotFound e) {
                engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to send an execution report for a rejected order.");
            }
        }
    }

    public void sendSecurityStatusOnLogon(Session session) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        for (Market market : marketController.getMarkets()) {
            SecurityStatus securityStatus = securityStatusFromMarket(market, 0);
            try {
                Session.sendToTarget(securityStatus, session.getSessionID().getSenderCompID(), session.getSessionID().getTargetCompID());
            } catch (SessionNotFound e) {
                engAppLogger.error("Session not found while trying to send security status to " + session.getSessionID().getTargetCompID() + " from " + session.getSessionID().getSenderCompID());
            }
        }
    }

    private void messageExecutionReport(Message message, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport();

        switch (message.getClass().getSimpleName()) {
            case "NewOrderSingle":
                executionReport = NewOrderSingleER((NewOrderSingle) message, ordStatus);
                break;
            case "OrderCancelRequest":
                executionReport = OrderCancelRequestER((OrderCancelRequest) message, ordStatus);
                break;
            case "OrderCancelReplaceRequest":
                executionReport = OrderCancelReplaceRequestER((OrderCancelReplaceRequest) message, ordStatus);
                break;
        }
        try {
            Session.sendToTarget(executionReport, message.getHeader().getString(quickfix.field.TargetCompID.FIELD), message.getHeader().getString(quickfix.field.SenderCompID.FIELD));
            engAppLogger.info("Execution report for clOrdID " + executionReport.getString(ClOrdID.FIELD) + "status = " + executionReport.getOrdStatus());
        } catch (SessionNotFound e) {
            engAppLogger.error("Session not found while trying to send execution report to " + message.getHeader().getString(TargetCompID.FIELD) + " from " + message.getHeader().getString(SenderCompID.FIELD));
        }
    }

    private void orderExecutionReport(Order order, char ordStatus) throws FieldNotFound {
        ExecutionReport executionReport = new ExecutionReport(
                new OrderID(idGenerator.genOrderID()),
                new ExecID(idGenerator.genExecutionID()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ordStatus),
                new OrdStatus(ordStatus),
                new Symbol(order.getSymbol()),
                new Side(order.getSide()),
                new LeavesQty(order.getOpenQuantity()),
                new CumQty(order.getExecutedQuantity()),
                new AvgPx(order.getAvgExecutedPrice()));
        executionReport.setString(ClOrdID.FIELD, order.getClOrdID());
        executionReport.setDouble(OrderQty.FIELD, order.getQuantity());
        order.setEntryDate(LocalDate.now());
        if (ordStatus == OrdStatus.CANCELED) {
            order.setCanceled(true);
        } else if (ordStatus == OrdStatus.REJECTED) {
            order.setRejected(true);
        }
        if (ordStatus == OrdStatus.FILLED || ordStatus == OrdStatus.PARTIALLY_FILLED) {
            executionReport.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            executionReport.setDouble(LastPx.FIELD, order.getPrice());
        }
        try {
            Session.sendToTarget(executionReport, order.getTargetCompID(), order.getSenderCompID());
            engAppLogger.info("Execution report for clOrdID " + executionReport.getString(ClOrdID.FIELD) + "status = " + executionReport.getOrdStatus());
        } catch (SessionNotFound e) {
            engAppLogger.error("Session not found while trying to send execution report to " + order.getTargetCompID() + " from " + order.getSenderCompID());
        }
    }

    private ExecutionReport NewOrderSingleER(NewOrderSingle message, char ordStatus) {
        ExecutionReport executionReport = null;
        try {
            executionReport = new ExecutionReport(
                    new OrderID(idGenerator.genOrderID()),
                    new ExecID(idGenerator.genExecutionID()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.REJECTED),
                    new OrdStatus(OrdStatus.REJECTED),
                    new Symbol(message.getString(Symbol.FIELD)),
                    new Side(message.getChar(Side.FIELD)),
                    new LeavesQty(0),
                    new CumQty(0),
                    new AvgPx(0));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an execution report for a new order single.");
        }
        try {
            assert executionReport != null;
            executionReport.set(message.getClOrdID());
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while setting execution report ClOrdID for a new order single execution report.");
        }
        switch (ordStatus) {
            case '8':
                break;
            case '0':
                executionReport.set(new ExecType(ExecType.NEW));
                executionReport.set(new OrdStatus(OrdStatus.NEW));
                try {
                    executionReport.set(new LeavesQty(message.getDouble(OrderQty.FIELD)));
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while setting execution report LeavesQty for a new order single execution report.");
                }
                executionReport.set(new CumQty(0));
                executionReport.set(new AvgPx(0));
                break;
        }
        return executionReport;
    }

    private ExecutionReport OrderCancelRequestER(OrderCancelRequest message, char ordStatus) {
        ExecutionReport executionReport = null;
        try {
            executionReport = new ExecutionReport(
                    new OrderID(idGenerator.genOrderID()),
                    new ExecID(idGenerator.genExecutionID()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.CANCELED),
                    new OrdStatus(OrdStatus.CANCELED),
                    new Symbol(message.getString(Symbol.FIELD)),
                    new Side(message.getChar(Side.FIELD)),
                    new LeavesQty(0),
                    new CumQty(0),
                    new AvgPx(0));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an execution report for an order cancel request.");
        }
        try {
            assert executionReport != null;
            executionReport.set(message.getClOrdID());
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while setting execution report ClOrdID for an order cancel request execution report.");
        }
        try {
            executionReport.set(message.getOrigClOrdID());
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while setting execution report OrigClOrdID for an order cancel request execution report.");
        }

        return executionReport;
    }

    private ExecutionReport OrderCancelReplaceRequestER(OrderCancelReplaceRequest message, char ordStatus){
        ExecutionReport executionReport = null;
        try {
            executionReport = new ExecutionReport(
                    new OrderID(idGenerator.genOrderID()),
                    new ExecID(idGenerator.genExecutionID()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.REJECTED),
                    new OrdStatus(OrdStatus.REJECTED),
                    new Symbol(message.getString(Symbol.FIELD)),
                    new Side(message.getChar(Side.FIELD)),
                    new LeavesQty(message.getDouble(OrderQty.FIELD)),
                    new CumQty(0),
                    new AvgPx(0));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an execution report for an order cancel replace request.");
        }
        try {
            assert executionReport != null;
            executionReport.set(message.getClOrdID());
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while setting execution report ClOrdID for an order cancel replace execution report.");
        }
        switch (ordStatus) {
            case '8':
                break;
            case '5':
                Order order = null;
                try {
                    order = orderController.getOrderByClOrdID(message.getString(OrigClOrdID.FIELD));
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while trying to get order by ClOrdID from order cancel replace request.");
                }
                assert order != null;
                executionReport.set(new ExecType(ExecType.REPLACED));
                executionReport.set(new OrdStatus(OrdStatus.REPLACED));
                executionReport.set(new LeavesQty(order.getOpenQuantity()));
                executionReport.set(new CumQty(order.getExecutedQuantity()));
                executionReport.set(new AvgPx(order.getAvgExecutedPrice()));
                try {
                    executionReport.set(new OrigClOrdID(message.getString(OrigClOrdID.FIELD)));
                } catch (FieldNotFound e) {
                    engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while setting execution report OrigClOrdID for an order cancel replace execution report.");
                }
                break;
        }
        return executionReport;
    }

    private SecurityStatus securityStatusFromMarket(Market market, int status) {
        SecurityStatus securityStatus = new SecurityStatus();
        securityStatus.set(new Symbol(market.getSymbol()));
        securityStatus.set(new HighPx(market.getDayHigh()));
        securityStatus.set(new LowPx(market.getDayLow()));
        securityStatus.set(new LastPx(market.getLastPrice()));
        securityStatus.set(new BuyVolume(market.getBuyVolume()));
        securityStatus.set(new SellVolume(market.getSellVolume()));
        securityStatus.set(new SecurityTradingStatus(status));
        return securityStatus;
    }

    public void displayFixMessageInLogs(String fixMessage) {
        SwingUtilities.invokeLater(() -> {
            logPanel.getLogModel().addElement(CustomFixMessageParser.parse(fixMessage));
            logPanel.revalidate();
            logPanel.repaint();
        });
    }

    public Order orderFromNewOrderSingle(quickfix.fix42.NewOrderSingle newOrderSingle) {
        double price = 0;
        try {
            if (newOrderSingle.getChar(OrdType.FIELD) == OrdType.LIMIT) {
                price = newOrderSingle.getDouble(Price.FIELD);
            } else if (newOrderSingle.getChar(OrdType.FIELD) == OrdType.MARKET) {
                if (marketController.checkIfMarketExists(newOrderSingle.getString(Symbol.FIELD))) {
                    price = marketController.getMarket(newOrderSingle.getString(Symbol.FIELD)).getLastPrice();
                } else {
                    price = 0;
                }
            }
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting Price from new order single.");
        }
        Order order = null;
        try {
            order = new Order(
                    System.currentTimeMillis(),
                    newOrderSingle.getString(ClOrdID.FIELD),
                    newOrderSingle.getString(Symbol.FIELD),
                    newOrderSingle.getHeader().getString(SenderCompID.FIELD),
                    newOrderSingle.getHeader().getString(TargetCompID.FIELD),
                    newOrderSingle.getChar(Side.FIELD),
                    newOrderSingle.getChar(OrdType.FIELD),
                    price,
                    (long) newOrderSingle.getDouble(OrderQty.FIELD),
                    (long) newOrderSingle.getDouble(OrderQty.FIELD),
                    0,
                    0,
                    0,
                    0,
                    false,
                    false,
                    LocalDate.now(),
                    LocalDate.now(),
                    newOrderSingle.getChar(TimeInForce.FIELD));
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while creating an order from new order single.");
        }
        String goodTillDateString = null;
        try {
            goodTillDateString = newOrderSingle.getString(ExpireDate.FIELD);
        } catch (FieldNotFound e) {
            engAppLogger.error("FieldNotFound " + e.getMessage() + " was caught while getting ExpireDate field from new order single.");
        }

        assert order != null;
        assert goodTillDateString != null;
        order.setGoodTillDate(LocalDate.parse(goodTillDateString));

        return order;
    }

    public MarketDataSnapshotFullRefresh.NoMDEntries noMDEntriesFromOrder(Order order) {
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        noMDEntries.setChar(MDEntryType.FIELD, order.getSide());
        noMDEntries.setDouble(MDEntryPx.FIELD, order.getPrice());
        noMDEntries.setDouble(MDEntrySize.FIELD, order.getQuantity());
        noMDEntries.setUtcDateOnly(MDEntryDate.FIELD, order.getEntryDate());
        noMDEntries.setUtcTimeOnly(MDEntryTime.FIELD, LocalTime.now());
        noMDEntries.setDouble(CumQty.FIELD, order.getExecutedQuantity());
        noMDEntries.setChar(OrdType.FIELD, order.getType());
        noMDEntries.setDouble(AvgPx.FIELD, order.getAvgExecutedPrice());
        noMDEntries.setDouble(LeavesQty.FIELD, order.getOpenQuantity());
        noMDEntries.setUtcDateOnly(ExpireDate.FIELD, order.getGoodTillDate());
        noMDEntries.setString(OrderID.FIELD, order.getClOrdID());
        noMDEntries.setString(Text.FIELD, "MDEntry for " + order.getClOrdID());

        return noMDEntries;
    }

    public void sendSecurityStatusFromMarket(Market market, int status) {
        for (SessionID sessionID : sessionIDs) {
            Session session = Session.lookupSession(sessionID);
            if (session != null && session.isLoggedOn()) {
                try {
                    Session.sendToTarget(securityStatusFromMarket(market, status), sessionID.getSenderCompID(), sessionID.getTargetCompID());
                } catch (SessionNotFound e) {
                    engAppLogger.error("Session not found while trying to send execution report to " + session.getSessionID().getTargetCompID() + " from " + session.getSessionID().getSenderCompID());
                }
            }
        }
    }
}
