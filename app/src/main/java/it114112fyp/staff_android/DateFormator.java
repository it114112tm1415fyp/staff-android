package it114112fyp.staff_android;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateFormator {
    final static SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public static String getDate(String date) {
        try {
            return dateFormat.format(serverFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTime(String date) {
        try {
            return timeFormat.format(serverFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateTime(String date) {
        return getDate(date) + " " + getTime(date);
    }

}