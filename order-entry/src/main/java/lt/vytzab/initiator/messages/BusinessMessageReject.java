package lt.vytzab.initiator.messages;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.*;

public class BusinessMessageReject extends Message {
    public static final String MSGTYPE = "j";

    // Constructor without parameters
    public BusinessMessageReject() {
        this.getHeader().setField(new MsgType("j"));
    }

    // Constructor with mandatory field parameters
    public BusinessMessageReject(RefMsgType refMsgType, BusinessRejectReason businessRejectReason) {
        this();
        this.setField(refMsgType);
        this.setField(businessRejectReason);
    }

    // Mandatory
    public void setRefMsgType(String refMsgType) {
        setField(new RefMsgType(refMsgType));
    }

    public void set(RefMsgType refMsgType) {
        this.setField(refMsgType);
    }

    public RefMsgType get(RefMsgType value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public RefMsgType getRefMsgType() throws FieldNotFound {
        return this.get(new RefMsgType());
    }

    public boolean isSet(RefMsgType field) {
        return this.isSetField(field);
    }

    public boolean isSetRefMsgType() {
        return this.isSetField(372);
    }

    public void setBusinessRejectReason(int businessRejectReason) {
        setField(new BusinessRejectReason(businessRejectReason));
    }

    public void set(BusinessRejectReason businessRejectReason) {
        this.setField(businessRejectReason);
    }

    public BusinessRejectReason get(BusinessRejectReason value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public BusinessRejectReason getBusinessRejectReason() throws FieldNotFound {
        return this.get(new BusinessRejectReason());
    }

    public boolean isSet(BusinessRejectReason field) {
        return this.isSetField(field);
    }

    public boolean isSetBusinessRejectReason() {
        return this.isSetField(380);
    }

    // Optional
    public void setText(String text) {
        setField(new Text(text));
    }

    public void set(Text text) {
        this.setField(text);
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

    public void setRefSeqNum(int refSeqNum) {
        setField(new RefSeqNum(refSeqNum));
    }

    public void set(RefSeqNum refSeqNum) {
        this.setField(refSeqNum);
    }

    public RefSeqNum get(RefSeqNum value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public RefSeqNum getRefSeqNum() throws FieldNotFound {
        return this.get(new RefSeqNum());
    }

    public boolean isSet(RefSeqNum field) {
        return this.isSetField(field);
    }

    public boolean isSetRefSeqNum() {
        return this.isSetField(45);
    }

    public void setBusinessRejectRefID(String businessRejectRefID) {
        setField(new BusinessRejectRefID(businessRejectRefID));
    }

    public void set(BusinessRejectRefID businessRejectRefID) {
        this.setField(businessRejectRefID);
    }

    public BusinessRejectRefID get(BusinessRejectRefID value) throws FieldNotFound {
        this.getField(value);
        return value;
    }

    public BusinessRejectRefID getBusinessRejectRefID() throws FieldNotFound {
        return this.get(new BusinessRejectRefID());
    }

    public boolean isSet(BusinessRejectRefID field) {
        return this.isSetField(field);
    }

    public boolean isSetBusinessRejectRefID() {
        return this.isSetField(379);
    }
}