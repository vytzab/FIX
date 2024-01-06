package lt.vytzab.initiator.order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OrderType implements Comparable<OrderType> {
    static private final Map<String, OrderType> known = new HashMap<>();
    static public final OrderType MARKET = new OrderType("Market", '1');
    static public final OrderType LIMIT = new OrderType("Limit", '2');
    private final String name;
    private final char charValue;
    static private final OrderType[] array = {MARKET, LIMIT};

    private OrderType(String name, char charValue) {
        this.name = name;
        this.charValue = charValue;
        synchronized (OrderType.class) {
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

    public static OrderType parse(String type) throws IllegalArgumentException {
        OrderType result = known.get(type);
        if (result == null) {
            throw new IllegalArgumentException("OrderType: " + type + " is unknown.");
        }
        return result;
    }
    @Override
    public int compareTo(OrderType other) {
        // Compare based on the order in the array
        return Integer.compare(Arrays.asList(array).indexOf(this), Arrays.asList(array).indexOf(other));
    }
}