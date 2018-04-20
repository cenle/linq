package com.bestvike.linq.impl.order;

import com.bestvike.OutRef;
import com.bestvike.collections.generic.Array;
import com.bestvike.collections.generic.ICollection;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.IOrderedEnumerable;
import com.bestvike.linq.enumerator.AbstractEnumerator;
import com.bestvike.linq.function.Func1;
import com.bestvike.linq.impl.cache.Buffer;
import com.bestvike.linq.iterator.Enumerable;
import com.bestvike.linq.partition.IIListProvider;
import com.bestvike.linq.partition.IPartition;
import com.bestvike.linq.util.ArrayUtils;
import com.bestvike.linq.util.ListUtils;
import com.bestvike.out;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 许崇雷 on 2017/7/17.
 */
public abstract class AbstractOrderedEnumerable<TElement> implements IOrderedEnumerable<TElement>, IPartition<TElement> {
    IEnumerable<TElement> _source;

    private int[] sortedMap(Buffer<TElement> buffer) {
        return getEnumerableSorter().sort(buffer.getItems(), buffer.getCount());
    }

    private int[] sortedMap(Buffer<TElement> buffer, int minIdx, int maxIdx) {
        return getEnumerableSorter().sort(buffer.getItems(), buffer.getCount(), minIdx, maxIdx);
    }

    public IEnumerator<TElement> enumerator() {
        return new OrderedEnumerableEnumerator();
    }


    protected IEnumerator<TElement> enumerator(int minIdx, int maxIdx) {
        return new OrderedEnumerableRangeEnumerator(minIdx, maxIdx);
    }


    private AbstractEnumerableSorter<TElement> getEnumerableSorter() {
        return getEnumerableSorter(null);
    }

    abstract AbstractEnumerableSorter<TElement> getEnumerableSorter(AbstractEnumerableSorter<TElement> next);

    private AbstractCachingComparer<TElement> getComparer() {
        return getComparer(null);
    }

    abstract AbstractCachingComparer<TElement> getComparer(AbstractCachingComparer<TElement> childComparer);

    public <TKey> IOrderedEnumerable<TElement> createOrderedEnumerable(Func1<TElement, TKey> keySelector, Comparator<TKey> comparer, boolean descending) {
        return new OrderedEnumerable<>(this._source, keySelector, comparer, descending, this);
    }

    protected int _getCount(int minIdx, int maxIdx, boolean onlyIfCheap) {
        int count = _getCount(onlyIfCheap);
        if (count <= 0)
            return count;

        if (count <= minIdx)
            return 0;

        return (count <= maxIdx ? count : maxIdx + 1) - minIdx;
    }

    protected Array<TElement> _toArray(int minIdx, int maxIdx) {
        Buffer<TElement> buffer = new Buffer<>(this._source);
        int count = buffer.getCount();
        if (count <= minIdx)
            return Array.empty();

        if (count <= maxIdx)
            maxIdx = count - 1;

        if (minIdx == maxIdx)
            return Array.singleton(getEnumerableSorter().elementAt(buffer.getItems(), count, minIdx));

        int[] map = sortedMap(buffer, minIdx, maxIdx);
        Array<TElement> array = Array.create(maxIdx - minIdx + 1);
        int idx = 0;
        while (minIdx <= maxIdx) {
            array.set(idx, buffer.getItems().get(map[minIdx]));
            ++idx;
            ++minIdx;
        }

        return array;
    }

    protected List<TElement> _toList(int minIdx, int maxIdx) {
        Buffer<TElement> buffer = new Buffer<>(this._source);
        int count = buffer.getCount();
        if (count <= minIdx) {
            return ListUtils.empty();
        }

        if (count <= maxIdx) {
            maxIdx = count - 1;
        }

        if (minIdx == maxIdx) {
            return ListUtils.singleton(getEnumerableSorter().elementAt(buffer.getItems(), count, minIdx));
        }

        int[] map = sortedMap(buffer, minIdx, maxIdx);
        List<TElement> list = new ArrayList<>(maxIdx - minIdx + 1);
        while (minIdx <= maxIdx) {
            list.add(buffer.getItems().get(map[minIdx]));
            ++minIdx;
        }

        return list;
    }

    public TElement _tryGetFirst(Func1<TElement, Boolean> predicate, OutRef<Boolean> found) {
        AbstractCachingComparer<TElement> comparer = getComparer();
        try (IEnumerator<TElement> e = this._source.enumerator()) {
            TElement value;
            do {
                if (!e.moveNext()) {
                    found.setValue(false);
                    return null;
                }

                value = e.current();
            }
            while (!predicate.apply(value));

            comparer.setElement(value);
            while (e.moveNext()) {
                TElement x = e.current();
                if (predicate.apply(x) && comparer.compare(x, true) < 0) {
                    value = x;
                }
            }

            found.setValue(true);
            return value;
        }
    }

    public TElement _tryGetLast(int minIdx, int maxIdx, OutRef<Boolean> found) {
        Buffer<TElement> buffer = new Buffer<TElement>(this._source);
        int count = buffer.getCount();
        if (minIdx >= count) {
            found.setValue(false);
            return null;
        }

        found.setValue(true);
        return (maxIdx < count - 1) ? getEnumerableSorter().elementAt(buffer.getItems(), count, maxIdx) : _last(buffer);
    }

    public TElement _tryGetLast(Func1<TElement, Boolean> predicate, OutRef<Boolean> found) {
        AbstractCachingComparer<TElement> comparer = getComparer();
        try (IEnumerator<TElement> e = this._source.enumerator()) {
            TElement value;
            do {
                if (!e.moveNext()) {
                    found.setValue(false);
                    return null;
                }

                value = e.current();
            }
            while (!predicate.apply(value));

            comparer.setElement(value);
            while (e.moveNext()) {
                TElement x = e.current();
                if (predicate.apply(x) && comparer.compare(x, false) >= 0) {
                    value = x;
                }
            }

            found.setValue(true);
            return value;
        }
    }

    @Override
    public int _getCount(boolean onlyIfCheap) {
        if (this._source instanceof IIListProvider) {
            IIListProvider<TElement> listProv = (IIListProvider<TElement>) this._source;
            return listProv._getCount(onlyIfCheap);
        }

        return !onlyIfCheap || this._source instanceof ICollection ? Enumerable.count(this) : -1;
    }

    @Override
    public Array<TElement> _toArray() {
        Buffer<TElement> buffer = new Buffer<>(this._source);
        int count = buffer.getCount();
        if (count == 0)
            return buffer.getItems();

        Array<TElement> array = Array.create(count);
        int[] map = sortedMap(buffer);
        for (int i = 0; i != array.length(); i++)
            array.set(i, buffer.getItems().get(map[i]));

        return array;
    }

    @Override
    public TElement[] _toArray(Class<TElement> clazz) {
        Buffer<TElement> buffer = new Buffer<>(this._source);
        int count = buffer.getCount();
        if (count == 0)
            return buffer.toArray(clazz);

        TElement[] array = ArrayUtils.newInstance(clazz,count);
        int[] map = sortedMap(buffer);
        for (int i = 0; i != array.length; i++)
            array[i]= buffer.getItems().get(map[i]);
        return array;
    }

    @Override
    public List<TElement> _toList() {
        Buffer<TElement> buffer = new Buffer<>(this._source);
        int count = buffer.getCount();
        List<TElement> list = new ArrayList<>(count);
        if (count > 0) {
            int[] map = sortedMap(buffer);
            for (int i = 0; i != count; i++)
                list.add(buffer.getItems().get(map[i]));
        }

        return list;
    }

    public IPartition<TElement> _skip(int count) {
        return new OrderedPartition<TElement>(this, count, Integer.MAX_VALUE);
    }

    public IPartition<TElement> _take(int count) {
        return new OrderedPartition<TElement>(this, 0, count - 1);
    }

    public TElement _tryGetElementAt(int index, out<Boolean> found) {
        if (index == 0) {
            return _tryGetFirst(found);
        }

        if (index > 0) {
            Buffer<TElement> buffer = new Buffer<>(this._source);
            int count = buffer.getCount();
            if (index < count) {
                found.setValue(true);
                return getEnumerableSorter().elementAt(buffer.getItems(), count, index);
            }
        }

        found.setValue(false);
        return null;
    }

    public TElement _tryGetFirst(out<Boolean> found) {
        AbstractCachingComparer<TElement> comparer = getComparer();
        try (IEnumerator<TElement> e = this._source.enumerator()) {
            if (!e.moveNext()) {
                found.setValue(false);
                return null;
            }

            TElement value = e.current();
            comparer.setElement(value);
            while (e.moveNext()) {
                TElement x = e.current();
                if (comparer.compare(x, true) < 0) {
                    value = x;
                }
            }

            found.setValue(true);
            return value;
        }
    }

    public TElement _tryGetLast(out<Boolean> found) {
        try (IEnumerator<TElement> e = this._source.enumerator()) {
            if (!e.moveNext()) {
                found.setValue(false);
                return null;
            }

            AbstractCachingComparer<TElement> comparer = getComparer();
            TElement value = e.current();
            comparer.setElement(value);
            while (e.moveNext()) {
                TElement current = e.current();
                if (comparer.compare(current, false) >= 0) {
                    value = current;
                }
            }

            found.setValue(true);
            return value;
        }
    }

    private TElement _last(Buffer<TElement> buffer) {
        AbstractCachingComparer<TElement> comparer = getComparer();
        Array<TElement> items = buffer.getItems();
        int count = buffer.getCount();
        TElement value = items.get(0);
        comparer.setElement(value);
        for (int i = 1; i != count; ++i) {
            TElement x = items.get(i);
            if (comparer.compare(x, false) >= 0) {
                value = x;
            }
        }

        return value;
    }


    private class OrderedEnumerableEnumerator extends AbstractEnumerator<TElement> {
        private Buffer<TElement> buffer;
        private int[] map;
        private int index;

        @Override
        public boolean moveNext() {
            switch (this.state) {
                case 0:
                    this.buffer = new Buffer<>(AbstractOrderedEnumerable.this._source);
                    if (this.buffer.getCount() <= 0) {
                        this.close();
                        return false;
                    }
                    this.map = AbstractOrderedEnumerable.this.sortedMap(this.buffer);
                    this.index = -1;
                    this.state = 1;
                case 1:
                    this.index++;
                    if (this.index < this.buffer.getCount()) {
                        this.current = this.buffer.getItems().get(this.map[this.index]);
                        return true;
                    }
                    this.close();
                    return false;
                default:
                    return false;
            }
        }
    }

    private class OrderedEnumerableRangeEnumerator extends AbstractEnumerator<TElement> {
        private int minIdx;
        private int maxIdx;
        private Buffer<TElement> buffer;
        private int[] map;
        private int count;

        private OrderedEnumerableRangeEnumerator(int minIdx, int maxIdx) {
            this.minIdx = minIdx;
            this.maxIdx = maxIdx;
        }

        @Override
        public boolean moveNext() {
            switch (this.state) {
                case 0:
                    this.buffer = new Buffer<>(AbstractOrderedEnumerable.this._source);
                    this.count = this.buffer.getCount();
                    if (this.count < this.minIdx) {
                        this.close();
                        return false;
                    }
                    if (this.count <= this.maxIdx)
                        this.maxIdx = this.count - 1;
                    if (this.minIdx == this.maxIdx) {
                        this.current = AbstractOrderedEnumerable.this.getEnumerableSorter().elementAt(this.buffer.getItems(), this.count, this.minIdx);
                        this.state = 2;
                        return true;
                    }
                    this.map = AbstractOrderedEnumerable.this.sortedMap(this.buffer, this.minIdx, this.maxIdx);
                    this.state = 1;
                case 1:
                    if (this.minIdx < this.maxIdx) {
                        this.current = this.buffer.getItems().get(this.map[this.minIdx]);
                        ++this.minIdx;
                        return true;
                    }
                    this.map = null;
                    this.close();
                    return false;
                case 2:
                    this.close();
                    return false;
                default:
                    return false;
            }
        }
    }
}