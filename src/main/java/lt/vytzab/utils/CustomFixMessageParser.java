package lt.vytzab.utils;

public class CustomFixMessageParser {
    public static String parse(String fixMessage) {
        return fixMessage.replace('\u0001', '|');
    }
}