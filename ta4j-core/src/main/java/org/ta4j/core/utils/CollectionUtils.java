package org.ta4j.core.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author VKozlov
 */
public abstract class CollectionUtils {

    public CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
