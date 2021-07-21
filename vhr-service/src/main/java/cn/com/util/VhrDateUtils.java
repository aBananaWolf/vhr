package cn.com.util;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author wyl
 * @create 2020-08-08 10:24
 */
public class VhrDateUtils {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String date2String(Date date) {
        LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
        return dateTimeFormatter.format(localDate);
    }

    public static String[] date2String(Date[] dateArray) {
        if (dateArray == null) {
            return null;
        }
        String[] strings = new String[dateArray.length];
        for (int i = 0; i < dateArray.length; i++) {
            strings[i] = date2String(dateArray[i]);
        }
        return strings;
    }

    public static String localDate2String(LocalDate localDate) {
        return dateTimeFormatter.format(localDate);
    }
}
