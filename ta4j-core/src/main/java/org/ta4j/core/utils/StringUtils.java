package org.ta4j.core.utils;

/**
 * @author VKozlov
 */
public abstract class StringUtils {

    public StringUtils() {
    }

    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    private static int toInt(String value) {
        return Integer.valueOf(value);
    }
}
