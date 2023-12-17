package lt.vytzab.engine.order;

import java.util.HashMap;
import java.util.Map;

public class OrderSide {
    static private final Map<String, OrderSide> known = new HashMap<>();
    static public final OrderSide BUY = new OrderSide("Buy");
    static public final OrderSide SELL = new OrderSide("Sell");

    static private final OrderSide[] array = {BUY, SELL};

    private final String name;

    private OrderSide(String name) {
        this.name = name;
        synchronized (OrderSide.class) {
            known.put(name, this);
        }
    }

    public String getName() {
        return name;
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
}