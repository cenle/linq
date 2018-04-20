package com.bestvike.linq.impl.order;

import com.bestvike.collections.generic.Comparer;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.exception.Errors;
import com.bestvike.linq.function.Func1;

import java.util.Comparator;

/**
 * Created by 许崇雷 on 2017/7/17.
 */
public final class OrderedEnumerable<TElement, TKey> extends AbstractOrderedEnumerable<TElement> {
    private final AbstractOrderedEnumerable<TElement> _parent;
    private final Func1<TElement, TKey> _keySelector;
    private final Comparator<TKey> _comparer;
    private final boolean _descending;

    OrderedEnumerable(IEnumerable<TElement> source, Func1<TElement, TKey> keySelector, Comparator<TKey> comparer, boolean descending, AbstractOrderedEnumerable<TElement> parent) {
        if (source == null)
            throw Errors.argumentNull("source");
        if (keySelector == null)
            throw Errors.argumentNull("keySelector");

        _source = source;
        _parent = parent;
        _keySelector = keySelector;
        _comparer = comparer == null ? Comparer.Default() : comparer;
        _descending = descending;
    }

    protected AbstractEnumerableSorter<TElement> getEnumerableSorter(AbstractEnumerableSorter<TElement> next) {
        AbstractEnumerableSorter<TElement> sorter = new EnumerableSorter<>(_keySelector, _comparer, _descending, next);
        if (_parent != null) {
            sorter = _parent.getEnumerableSorter(sorter);
        }

        return sorter;
    }

    protected AbstractCachingComparer<TElement> getComparer(AbstractCachingComparer<TElement> childComparer) {
        AbstractCachingComparer<TElement> cmp = childComparer == null
                ? new CachingComparer<TElement, TKey>(_keySelector, _comparer, _descending)
                : new CachingComparerWithChild<TElement, TKey>(_keySelector, _comparer, _descending, childComparer);
        return _parent != null ? _parent.getComparer(cmp) : cmp;
    }
}
