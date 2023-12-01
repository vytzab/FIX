package lt.vytzab.utils;

import quickfix.LogFactory;
import quickfix.SessionID;

public class CustomLogFactory implements LogFactory {
    @Override
    public CustomLog create(SessionID sessionID) {
        return null;
    }
}
