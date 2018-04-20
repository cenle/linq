package com.bestvike.linq.impl.cache;

import com.bestvike.OutRef;
import com.bestvike.collections.generic.Array;
import com.bestvike.collections.generic.EnumerableHelpers;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.partition.IIListProvider;
import com.bestvike.linq.util.ArrayUtils;

/**
 * Created by 许崇雷 on 2017/7/17.
 */
public final class Buffer<TElement> {
    private final Array<TElement> items;
    private final int count;

    public Buffer(IEnumerable<TElement> source) {
        if (source instanceof IIListProvider) {
            IIListProvider<TElement> iterator = (IIListProvider<TElement>) source;
            this.items = iterator._toArray();
            this.count = this.items.length();
        } else {
            OutRef<Integer> countRef = OutRef.empty();
            this.items = EnumerableHelpers.toArray(source, countRef);
            this.count = countRef.getValue();
        }
    }

    public Array<TElement> getItems() {
        return this.items;
    }

    public int getCount() {
        return this.count;
    }

    public TElement[] toArray(Class<TElement> clazz) {
        TElement[] array = ArrayUtils.newInstance(clazz, this.count);
        if (this.count > 0)
            Array.copy(this.items, 0, array, 0, this.count);
        return array;
    }
}
