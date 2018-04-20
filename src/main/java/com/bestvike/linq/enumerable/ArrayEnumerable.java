package com.bestvike.linq.enumerable;

import com.bestvike.OutRef;
import com.bestvike.collections.generic.Array;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.partition.IPartition;
import com.bestvike.linq.enumerator.ArrayEnumerator;
import com.bestvike.out;

import java.util.List;

/**
 * Created by 许崇雷 on 2017/7/17.
 */
public final class ArrayEnumerable<TElement> implements IPartition<TElement> {
    private final Array<TElement> source;

    public ArrayEnumerable(Array<TElement> source) {
        this.source = source;
    }

    public Array<TElement> getSource() {
        return this.source;
    }

    @Override
    public IEnumerator<TElement> enumerator() {
        return new ArrayEnumerator<>(this.source);
    }


    //region IIListProvider

    @Override
    public int _getCount(boolean onlyIfCheap) {
        return this.source.length();
    }

    @Override
    public boolean _contains(TElement value) {
        return this.source.contains(value);
    }

    @Override
    public Array<TElement> _toArray() {
        return this.source.clone();
    }

    @Override
    public TElement[] _toArray(Class<TElement> clazz) {
        return this.source.toArray(clazz);
    }

    @Override
    public List<TElement> _toList() {
        return this.source.toList();
    }

    //endregion


    //region IPartition

    @Override
    public IPartition<TElement> _skip(int count) {
        return null;
    }

    @Override
    public IPartition<TElement> _take(int count) {
        return null;
    }

    @Override
    public TElement _tryGetElementAt(int index, out<Boolean> found) {
        return found.setValue(index < this.source.length()) ? this.source.get(index) : null;
    }

    @Override
    public TElement _tryGetFirst(out<Boolean> found) {
        return found.setValue(this.source.length() > 0) ? this.source.get(0) : null;
    }

    @Override
    public TElement _tryGetLast(out<Boolean> found) {
        return found.setValue(this.source.length() > 0) ? this.source.get(this.source.length() - 1) : null;
    }

    //endregion
}
