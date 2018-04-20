package com.bestvike.linq.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017/7/19.
 */
public final class ListUtils {
    private ListUtils() {
    }

    public static <T> List<T> empty() {
        return new ArrayList<>(0);
    }

    public static <T> List<T> singleton(T element) {
        List<T> list = new ArrayList<>(1);
        list.add(element);
        return list;
    }
}
