package lt.vytzab.engine.messages;

import quickfix.Message;
import quickfix.field.*;

public class BusinessMessageReject extends Message {
    private RefMsgType refMsgType;
    private BusinessRejectReason businessRejectReason;
    private Text text;
    private RefSeqNum refSeqNum;
    private BusinessRejectRefID businessRejectRefID;

    // Constructor without parameters
    public BusinessMessageReject() {
        super();
        getHeader().setField(new MsgType(MsgType.BUSINESS_MESSAGE_REJECT));
    }

    // Constructor with mandatory field parameters
    public BusinessMessageReject(String refMsgType, int businessRejectReason) {
        super();
        getHeader().setField(new MsgType(MsgType.BUSINESS_MESSAGE_REJECT));
        this.refMsgType = new RefMsgType(refMsgType);
        this.businessRejectReason = new BusinessRejectReason(businessRejectReason);
    }

    // Setters for mandatory fields
    public void setRefMsgType(String refMsgType) {
        setField(new RefMsgType(refMsgType));
    }
    public void setBusinessRejectReason(int businessRejectReason) {
        setField(new BusinessRejectReason(businessRejectReason));
    }

    // Setters for optional fields
    public void setText(String text) {
        setField(new Text(text));
    }
    public void setRefSeqNum(int refSeqNum) {
        setField(new RefSeqNum(refSeqNum));
    }
    public void setBusinessRejectRefID(String businessRejectRefID) {
        setField(new BusinessRejectRefID(businessRejectRefID));
    }

    // Getters
    public RefMsgType getRefMsgType() {
        return refMsgType;
    }

    public BusinessRejectReason getBusinessRejectReason() {
        return businessRejectReason;
    }

    public Text getText() {
        return text;
    }

    public RefSeqNum getRefSeqNum() {
        return refSeqNum;
    }

    public BusinessRejectRefID getBusinessRejectRefID() {
        return businessRejectRefID;
    }
}
