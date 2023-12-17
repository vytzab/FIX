package lt.vytzab.engine.order;

import java.util.HashMap;
import java.util.Map;

public class OrderTIF {
    static private final Map<String, OrderTIF> known = new HashMap<>();
    static public final OrderTIF DAY = new OrderTIF("Day");
    static public final OrderTIF GTC = new OrderTIF("GTC");
    static public final OrderTIF GTX = new OrderTIF("GTD");

    static private final OrderTIF[] array = {DAY, GTC, GTX};

    private final String name;

    private OrderTIF(String name) {
        this.name = name;
        synchronized (OrderTIF.class) {
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

    public static OrderTIF parse(String type) throws IllegalArgumentException {
        OrderTIF result = known.get(type);
        if (result == null) {
            throw new IllegalArgumentException("OrderTIF: " + type + " is unknown.");
        }
        return result;
    }
}