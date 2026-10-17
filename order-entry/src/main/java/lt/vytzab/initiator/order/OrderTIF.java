package lt.vytzab.initiator.order;

import java.util.HashMap;
import java.util.Map;

public class OrderTIF {
    static private final Map<String, OrderTIF> known = new HashMap<>();
    static public final OrderTIF DAY = new OrderTIF("Day", '0');
    static public final OrderTIF GTC = new OrderTIF("GTC", '1');
    static public final OrderTIF GTD = new OrderTIF("GTD", '6');
    static private final OrderTIF[] array = {DAY, GTC, GTD};
    private final String name;
    private final char charValue;

    private OrderTIF(String name, char charValue) {
        this.name = name;
        this.charValue = charValue;
        synchronized (OrderTIF.class) {
            known.put(name, this);
        }
    }

    public String getName() {
        return name;
    }

    public char getCharValue() {
        return charValue;
    }

    public String toString() {
        return name;
    }

    static public Object[] toArray() {
        return array;
    }

    public static OrderTIF parse(String type) throws IllegalArgumentException {
        OrderTIF result = known.get(type);
        if (result == null) {
            throw new IllegalArgumentException("OrderTIF: " + type + " is unknown.");
        }
        return result;
    }
}