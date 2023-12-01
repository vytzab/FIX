package lt.vytzab.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeString {
    public static String getCurrentDateTimeAsString() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return currentDateTime.format(formatter);
    }
}