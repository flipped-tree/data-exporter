package com.cx.exporter.utils;

import java.util.Collection;

public class CollUtils {
    public static <E> boolean isEmpty(Collection<E> coll) {
        if (coll == null || coll.isEmpty()) {
            return true;
        }
        return false;
    }
}
