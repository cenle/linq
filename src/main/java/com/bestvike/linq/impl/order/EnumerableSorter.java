package com.bestvike.linq.impl.order;

import com.bestvike.collections.generic.Array;
import com.bestvike.linq.function.Func1;

import java.util.Comparator;

/**
 * Created by 许崇雷 on 2017/7/11.
 */
public final class EnumerableSorter<TElement, TKey> extends AbstractEnumerableSorter<TElement> {
    private final Func1<TElement, TKey> _keySelector;
    private final Comparator<TKey> _comparer;
    private final boolean _descending;
    private final AbstractEnumerableSorter<TElement> _next;
    private Array<TKey> _keys;

    protected EnumerableSorter(Func1<TElement, TKey> keySelector, Comparator<TKey> comparer, boolean descending, AbstractEnumerableSorter<TElement> next) {
        this._keySelector = keySelector;
        this._comparer = comparer;
        this._descending = descending;
        this._next = next;
    }

    protected void computeKeys(Array<TElement> elements, int count) {
        this._keys = Array.create(count);
        for (int i = 0; i < count; i++)
            this._keys.set(i, this._keySelector.apply(elements.get(i)));
        if (this._next == null)
            return;
        this._next.computeKeys(elements, count);
    }

    protected int compareAnyKeys(int index1, int index2) {
        int c = this._comparer.compare(this._keys.get(index1), this._keys.get(index2));
        if (c == 0) {
            if (this._next == null)
                return index1 - index2;
            return this._next.compareAnyKeys(index1, index2);
        }

        // -c will result in a negative value for int.MinValue (-int.MinValue == int.MinValue).
        // Flipping keys earlier is more likely to trigger something strange in a comparer,
        // particularly as it comes to the sort being stable.
        return (this._descending != (c > 0)) ? 1 : -1;
    }
}