package com.bestvike.linq.impl.order;

import com.bestvike.linq.function.Func1;

import java.util.Comparator;

/**
 * Created by 许崇雷 on 2018-04-18.
 */
public class CachingComparerWithChild<TElement, TKey> extends CachingComparer<TElement, TKey> {
    private final AbstractCachingComparer<TElement> _child;

    public CachingComparerWithChild(Func1<TElement, TKey> keySelector, Comparator<TKey> comparer, boolean descending, AbstractCachingComparer<TElement> child) {
        super(keySelector, comparer, descending);
        this._child = child;
    }

    protected int compare(TElement element, boolean cacheLower) {
        TKey newKey = this._keySelector.apply(element);
        int cmp = this._descending ? this._comparer.compare(this._lastKey, newKey) : this._comparer.compare(newKey, this._lastKey);
        if (cmp == 0)
            return this._child.compare(element, cacheLower);
        if (cacheLower == cmp < 0) {
            this._lastKey = newKey;
            this._child.setElement(element);
        }
        return cmp;
    }

    protected void setElement(TElement element) {
        super.setElement(element);
        this._child.setElement(element);
    }
}
