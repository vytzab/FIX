package lt.vytzab.engine.messages;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.field.*;
import quickfix.fix42.Message;

public class MarketDataSnapshotFullRefresh extends Message {
    public static final String MSGTYPE = "W";

    public MarketDataSnapshotFullRefresh() {
        this.getHeader().setField(new MsgType("W"));
    }

    public MarketDataSnapshotFullRefresh(Symbol symbol) {
        this();
        this.setField(symbol);
    }

    public void set(MDReqID value) {
        this.setField(value);
    }

    public MDReqID get(MDReqID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public MDReqID getMDReqID() throws FieldNotFound {
        return this.get(new MDReqID());
    }

    public boolean isSet(MDReqID field) {
        return this.isSetField(field);
    }

    public boolean isSetMDReqID() {
        return this.isSetField(262);
    }

    public void set(Symbol value) {
        this.setField(value);
    }

    public Symbol get(Symbol value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public Symbol getSymbol() throws FieldNotFound {
        return this.get(new Symbol());
    }

    public boolean isSet(Symbol field) {
        return this.isSetField(field);
    }

    public boolean isSetSymbol() {
        return this.isSetField(55);
    }

    public void set(TotalVolumeTraded value) {
        this.setField(value);
    }

    public TotalVolumeTraded get(TotalVolumeTraded value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public TotalVolumeTraded getTotalVolumeTraded() throws FieldNotFound {
        return this.get(new TotalVolumeTraded());
    }

    public boolean isSet(TotalVolumeTraded field) {
        return this.isSetField(field);
    }

    public boolean isSetTotalVolumeTraded() {
        return this.isSetField(387);
    }

    public void set(quickfix.field.NoMDEntries value) {
        this.setField(value);
    }

    public quickfix.field.NoMDEntries get(quickfix.field.NoMDEntries value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public quickfix.field.NoMDEntries getNoMDEntries() throws FieldNotFound {
        return this.get(new quickfix.field.NoMDEntries());
    }

    public boolean isSet(quickfix.field.NoMDEntries field) {
        return this.isSetField(field);
    }

    public boolean isSetNoMDEntries() {
        return this.isSetField(268);
    }

    public static class NoMDEntries extends Group {
        private static final int[] ORDER = new int[]{269, 270, 271, 272, 273, 14, 346, 40, 151, 432, 37, 58};

        public NoMDEntries() {
            super(268, 269, ORDER);
        }

        public void set(CumQty value) {
            this.setField(value);
        }

        public CumQty get(CumQty value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public CumQty getCumQty() throws FieldNotFound {
            return this.get(new CumQty());
        }

        public boolean isSet(CumQty field) {
            return this.isSetField(field);
        }

        public boolean isSetCumQty() {
            return this.isSetField(14);
        }

        public void set(MDEntryType value) {
            this.setField(value);
        }

        public MDEntryType get(MDEntryType value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public MDEntryType getMDEntryType() throws FieldNotFound {
            return this.get(new MDEntryType());
        }

        public boolean isSet(MDEntryType field) {
            return this.isSetField(field);
        }

        public boolean isSetMDEntryType() {
            return this.isSetField(269);
        }

        public void set(MDEntryPx value) {
            this.setField(value);
        }

        public MDEntryPx get(MDEntryPx value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public MDEntryPx getMDEntryPx() throws FieldNotFound {
            return this.get(new MDEntryPx());
        }

        public boolean isSet(MDEntryPx field) {
            return this.isSetField(field);
        }

        public boolean isSetMDEntryPx() {
            return this.isSetField(270);
        }

        public void set(MDEntrySize value) {
            this.setField(value);
        }

        public MDEntrySize get(MDEntrySize value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public MDEntrySize getMDEntrySize() throws FieldNotFound {
            return this.get(new MDEntrySize());
        }

        public boolean isSet(MDEntrySize field) {
            return this.isSetField(field);
        }

        public boolean isSetMDEntrySize() {
            return this.isSetField(271);
        }

        public void set(NumberOfOrders value) {
            this.setField(value);
        }

        public NumberOfOrders get(NumberOfOrders value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public NumberOfOrders getNumberOfOrders() throws FieldNotFound {
            return this.get(new NumberOfOrders());
        }

        public boolean isSet(NumberOfOrders field) {
            return this.isSetField(field);
        }

        public boolean isSetNumberOfOrders() {
            return this.isSetField(346);
        }

        public void set(OrdType value) {
            this.setField(value);
        }

        public OrdType get(OrdType value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public OrdType getOrdType() throws FieldNotFound {
            return this.get(new OrdType());
        }

        public boolean isSet(OrdType field) {
            return this.isSetField(field);
        }

        public boolean isSetOrdType() {
            return this.isSetField(40);
        }

        public void set(LeavesQty value) {
            this.setField(value);
        }

        public LeavesQty get(LeavesQty value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public LeavesQty getLeavesQty() throws FieldNotFound {
            return this.get(new LeavesQty());
        }

        public boolean isSet(LeavesQty field) {
            return this.isSetField(field);
        }

        public boolean isSetLeavesQty() {
            return this.isSetField(151);
        }

        public void set(MDEntryDate value) {
            this.setField(value);
        }

        public MDEntryDate get(MDEntryDate value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public MDEntryDate getMDEntryDate() throws FieldNotFound {
            return this.get(new MDEntryDate());
        }

        public boolean isSet(MDEntryDate field) {
            return this.isSetField(field);
        }

        public boolean isSetMDEntryDate() {
            return this.isSetField(272);
        }

        public void set(MDEntryTime value) {
            this.setField(value);
        }

        public MDEntryTime get(MDEntryTime value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public MDEntryTime getMDEntryTime() throws FieldNotFound {
            return this.get(new MDEntryTime());
        }

        public boolean isSet(MDEntryTime field) {
            return this.isSetField(field);
        }

        public boolean isSetMDEntryTime() {
            return this.isSetField(273);
        }

        public void set(ExpireDate value) {
            this.setField(value);
        }

        public ExpireDate get(ExpireDate value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public ExpireDate getExpireDate() throws FieldNotFound {
            return this.get(new ExpireDate());
        }

        public boolean isSet(ExpireDate field) {
            return this.isSetField(field);
        }

        public boolean isSetExpireDate() {
            return this.isSetField(432);
        }

        public void set(OrderID value) {
            this.setField(value);
        }

        public OrderID get(OrderID value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public OrderID getOrderID() throws FieldNotFound {
            return this.get(new OrderID());
        }

        public boolean isSet(OrderID field) {
            return this.isSetField(field);
        }

        public boolean isSetOrderID() {
            return this.isSetField(37);
        }

        public void set(Text value) {
            this.setField(value);
        }

        public Text get(Text value) throws FieldNotFound {
            this.getField(value);
            return value;
        }

        public Text getText() throws FieldNotFound {
            return this.get(new Text());
        }

        public boolean isSet(Text field) {
            return this.isSetField(field);
        }

        public boolean isSetText() {
            return this.isSetField(58);
        }

    }
}