package com.bestvike.linq.impl.order;

import com.bestvike.linq.function.Func1;

import java.util.Comparator;

/**
 * Created by 许崇雷 on 2018-04-18.
 */
public class CachingComparer<TElement, TKey> extends AbstractCachingComparer<TElement> {
    protected final Func1<TElement, TKey> _keySelector;
    protected final Comparator<TKey> _comparer;
    protected final boolean _descending;
    protected TKey _lastKey;

    public CachingComparer(Func1<TElement, TKey> keySelector, Comparator<TKey> comparer, boolean descending) {
        this._keySelector = keySelector;
        this._comparer = comparer;
        this._descending = descending;
    }

    int compare(TElement element, boolean cacheLower) {
        TKey newKey = this._keySelector.apply(element);
        int cmp = this._descending ? this._comparer.compare(this._lastKey, newKey) : this._comparer.compare(newKey, this._lastKey);
        if (cacheLower == cmp < 0) {
            this._lastKey = newKey;
        }

        return cmp;
    }

    void setElement(TElement element) {
        this._lastKey = this._keySelector.apply(element);
    }
}
