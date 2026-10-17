package lt.vytzab.initiator.order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OrderSide implements Comparable<OrderSide>{
    static private final Map<String, OrderSide> known = new HashMap<>();
    static public final OrderSide BUY = new OrderSide("Buy", '1');
    static public final OrderSide SELL = new OrderSide("Sell", '2');
    static private final OrderSide[] array = {BUY, SELL};
    private final String name;
    private final char charValue;

    private OrderSide(String name, char charValue) {
        this.name = name;
        this.charValue = charValue;
        synchronized (OrderSide.class) {
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

    public static OrderSide parse(String type) throws IllegalArgumentException {
        OrderSide result = known.get(type);
        if (result == null) {
            throw new IllegalArgumentException("OrderSide: " + type + " is unknown.");
        }
        return result;
    }

    @Override
    public int compareTo(OrderSide other) {
        return Integer.compare(Arrays.asList(array).indexOf(this), Arrays.asList(array).indexOf(other));
    }
}