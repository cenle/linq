package com.bestvike.linq.util;

import com.bestvike.collections.generic.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by 许崇雷 on 2017/7/19.
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> Array<T> toArray(Collection<T> source) {
        int length = source.size();
        Array<T> array = Array.create(length);
        Array.copy(source, array);
        return array;
    }

    public static <T> T[] toArray(Collection<T> source, Class<T> clazz) {
        int length = source.size();
        T[] array = ArrayUtils.newInstance(clazz, length);
        return source.toArray(array);
    }

    public static <T> List<T> toList(Collection<T> source) {
        return new ArrayList<>(source);
    }

    public static <T> void addAll(Collection<T> source, Iterable<? extends T> iterable) {
        for (T item : iterable)
            source.add(item);
    }
}
