package com.bestvike.linq.partition;

import com.bestvike.linq.impl.order.AbstractOrderedEnumerable;
import com.bestvike.linq.iterator.AbstractIterator;
import com.bestvike.linq.iterator.Iterator;

/**
 * Created by 许崇雷 on 2018-04-18.
 */
final class OrderedPartition <TElement> extends Iterator<TElement> implements IPartition<TElement> {
    private final AbstractOrderedEnumerable<TElement> source;
    private final  int minIndexInclusive;
    private  final  int maxIndexInclusive;

    OrderedPartition(AbstractOrderedEnumerable<TElement> source, int minIndexInclusive, int maxIndexInclusive) {
        this.source = source;
        this.minIndexInclusive = minIndexInclusive;
        this.maxIndexInclusive = maxIndexInclusive;
    }

    @Override
    public AbstractIterator<TElement> clone() {
        return null;
    }

    @Override
    public boolean moveNext() {
        return false;
    }
}
