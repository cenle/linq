package com.bestvike.linq.iterator;

import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.function.Func1;

/**
 * Created by 许崇雷 on 2017/7/10.
 */
public abstract class Iterator<TSource> extends AbstractIterator<TSource> {
    public abstract <TResult> IEnumerable<TResult> _select(Func1<TSource, TResult> selector);

    public abstract IEnumerable<TSource> _where(Func1<TSource, Boolean> predicate);
}
