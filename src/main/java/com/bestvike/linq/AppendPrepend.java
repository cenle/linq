package com.bestvike.linq;

import com.bestvike.collections.generic.Array;
import com.bestvike.collections.generic.SingleLinkedNode;
import com.bestvike.linq.enumerable.ArrayEnumerable;
import com.bestvike.linq.exception.Errors;
import com.bestvike.linq.iterator.AppendIterator;
import com.bestvike.linq.iterator.Iterator;
import com.bestvike.linq.util.ArrayUtils;
import com.bestvike.linq.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Created by 许崇雷 on 2017-09-11.
 */
final class AppendPrepend {
    private AppendPrepend() {
    }


    public static <TSource> IEnumerable<TSource> append(IEnumerable<TSource> source, TSource element) {
        if (source == null)
            throw Errors.argumentNull("source");
        return (source instanceof AppendPrependIterator)
        return new AppendIterator<>(source, item);


        return source is AppendPrependIterator<TSource > appendable
                ? appendable.Append(element)
                : new AppendPrepend1Iterator<TSource>(source, element, appending:true);
    }


    private static abstract class AppendPrependIterator<TSource> extends Iterator<TSource> implements IIListProvider2<TSource> {
        protected final IEnumerable<TSource> source;
        protected IEnumerator<TSource> enumerator;

        protected AppendPrependIterator(IEnumerable<TSource> source) {
            assert source != null;
            this.source = source;
        }

        protected void getSourceEnumerator() {
            assert this.enumerator == null;
            this.enumerator = this.source.enumerator();
        }

        public abstract AppendPrependIterator<TSource> appendCore(TSource item);

        public abstract AppendPrependIterator<TSource> prependCore(TSource item);

        protected boolean loadFromEnumerator() {
            if (this.enumerator.moveNext()) {
                this.current = this.enumerator.current();
                return true;
            }
            this.close();
            return false;
        }

        @Override
        public void close() {
            if (this.enumerator != null) {
                this.enumerator.close();
                this.enumerator = null;
            }
            super.close();
        }

        @Override
        public abstract TSource[] internalToArray(Class<TSource> clazz);

        @Override
        public abstract Array<TSource> internalToArray();

        @Override
        public abstract List<TSource> internalToList();

        @Override
        public abstract int internalSize(boolean onlyIfCheap);
    }


    private static final class AppendPrepend1Iterator<TSource> extends AppendPrependIterator<TSource> {
        private final TSource item;
        private final boolean appending;

        public AppendPrepend1Iterator(IEnumerable<TSource> source, TSource item, boolean appending) {
            super(source);
            this.item = item;
            this.appending = appending;
        }

        @Override
        public Iterator<TSource> clone() {
            return new AppendPrepend1Iterator<>(this.source, this.item, this.appending);
        }

        @Override
        public boolean moveNext() {
            switch (this.state) {
                case 1:
                    this.state = 2;
                    if (!this.appending) {
                        this.current = this.item;
                        return true;
                    }
                case 2:
                    this.state = 3;
                    this.getSourceEnumerator();
                case 3:
                    if (this.loadFromEnumerator())
                        return true;
                    if (this.appending) {
                        this.current = this.item;
                        return true;
                    }
                    this.close();
                    return false;
                default:
                    return false;
            }
        }

        public AppendPrependIterator<TSource> append(TSource item) {
            if (this.appending) {
                return new AppendPrependN<TSource>(source, null, new SingleLinkedNode<>(this.item).add(item), 0, 2);
            } else {
                return new AppendPrependN<TSource>(source, new SingleLinkedNode<TSource>(this.item), new SingleLinkedNode<TSource>(item), prependCount:1, appendCount:1);
            }
        }

        public AppendPrependIterator<TSource> prepend(TSource item) {
            if (this.appending) {
                return new AppendPrependN<TSource>(source, new SingleLinkedNode<TSource>(item), new SingleLinkedNode<TSource>(this.item), prependCount:1, appendCount:1);
            } else {
                return new AppendPrependN<TSource>(source, new SingleLinkedNode<TSource>(this.item).Add(item), null, prependCount:2, appendCount:0);
            }
        }

        private TSource[] LazyToArray() {
            assert (this.sizeCore(true) == -1);

            var builder = new LargeArrayBuilder<TSource>(initialize:true);

            if (!this.appending) {
                builder.SlowAdd(this.item);
            }

            builder.AddRange(source);

            if (this.appending) {
                builder.SlowAdd(this.item);
            }

            return builder.ToArray();
        }

        public TSource[] ToArray() {
            int count = this.GetCount(onlyIfCheap:true);
            if (count == -1) {
                return this.LazyToArray();
            }

            TSource[] array = new TSource[count];
            int index;
            if (this.appending) {
                index = 0;
            } else {
                array[0] = this.item;
                index = 1;
            }

            EnumerableHelpers.Copy(source, array, index, count - 1);

            if (this.appending) {
                array[array.Length - 1] = this.item;
            }

            return array;
        }

        public List<TSource> ToList() {
            int count = this.GetCount(true);
            List<TSource> list = count == -1 ? new List<TSource>() : new List<TSource>(count);
            if (!this.appending) {
                list.Add(this.item);
            }

            list.AddRange(source);
            if (this.appending) {
                list.Add(this.item);
            }

            return list;
        }

        public int GetCount(boolean onlyIfCheap) {
            if (source is IIListProvider<TSource > listProv)
            {
                int count = listProv.GetCount(onlyIfCheap);
                return count == -1 ? -1 : count + 1;
            }

            return !onlyIfCheap || source is ICollection<TSource > ? source.Count() + 1 : -1;
        }
    }


    private static class AppendPrependN<TSource> extends AppendPrependIterator<TSource> {
        private final SingleLinkedNode<TSource> _prepended;
        private final SingleLinkedNode<TSource> _appended;
        private final int _prependCount;
        private final int _appendCount;
        private SingleLinkedNode<TSource> _node;

        public AppendPrependN(IEnumerable<TSource> source, SingleLinkedNode<TSource> prepended, SingleLinkedNode<TSource> appended, int prependCount, int appendCount) {
            super(source);

            assert prepended != null || appended != null;
            assert prependCount > 0 || appendCount > 0;
            assert prependCount + appendCount >= 2;
            assert (prepended == null ? 0 : prepended.getCount()) == prependCount;
            assert (appended == null ? 0 : appended.getCount()) == appendCount;

            _prepended = prepended;
            _appended = appended;
            _prependCount = prependCount;
            _appendCount = appendCount;
        }

        public Iterator<TSource> Clone() =>new AppendPrependN<TSource>(_source,_prepended,_appended,_prependCount,_appendCount);

        public boolean MoveNext() {
            switch (this.state) {
                case 1:
                    _node = _prepended;
                    this.state = 2;
                case 2:
                    if (_node != null) {
                        _current = _node.Item;
                        _node = _node.Linked;
                        return true;
                    }

                    this.state = 3;
                    getSourceEnumerator();
                case 3:
                    if (LoadFromEnumerator()) {
                        return true;
                    }

                    if (_appended == null) {
                        return false;
                    }

                    _enumerator = _appended.GetEnumerator(_appendCount);
                    _state = 4;
                        goto case 4
                    ;
                case 4:
                    return loadFromEnumerator();
            }

            Dispose();
            return false;
        }

        public AppendPrependIterator

                <TSource> Append(TSource item) {
            var appended = _appended != null ? _appended.Add(item) : new SingleLinkedNode<TSource>(item);
            return new AppendPrependN<TSource>(_source, _prepended, appended, _prependCount, _appendCount + 1);
        }

        public AppendPrependIterator<TSource> Prepend(TSource item) {
            var prepended = _prepended != null ? _prepended.Add(item) : new SingleLinkedNode<TSource>(item);
            return new AppendPrependN<TSource>(_source, prepended, _appended, _prependCount + 1, _appendCount);
        }

        private TSource[] lazyToArray() {
            assert getCount( true) ==-1;

            var builder = new SparseArrayBuilder<TSource>(initialize:true);

            if (_prepended != null) {
                builder.Reserve(_prependCount);
            }

            builder.AddRange(_source);

            if (_appended != null) {
                builder.Reserve(_appendCount);
            }

            TSource[] array = builder.ToArray();

            int index = 0;
            for (SingleLinkedNode<TSource> node = _prepended; node != null; node = node.Linked) {
                array[index++] = node.Item;
            }

            index = array.Length - 1;
            for (SingleLinkedNode<TSource> node = _appended; node != null; node = node.Linked) {
                array[index--] = node.Item;
            }

            return array;
        }

        public TSource[] toArray(Class<TSource> clazz) {
            int count = getCount(true);
            if (count == -1) {
                return LazyToArray();
            }

            TSource[] array = ArrayUtils.newInstance(clazz, count);
            int index = 0;
            for (SingleLinkedNode<TSource> node = _prepended; node != null; node = node.getLinked()) {
                array[index] = node.getItem();
                ++index;
            }

            if (this.source instanceof ArrayEnumerable) {
                ArrayEnumerable<TSource> sourceCollection = (ArrayEnumerable<TSource>) this.source;
                Array.copy(sourceCollection.getSource(), 0, array, index, sourceCollection._getCount());
            } else {
                for (TSource item : this.source) {
                    array[index] = item;
                    ++index;
                }
            }

            index = array.length;
            for (SingleLinkedNode<TSource> node = _appended; node != null; node = node.getLinked()) {
                --index;
                array[index] = node.getItem();
            }

            return array;
        }

        public List<TSource> toList() {
            int count = getCount(true);
            List<TSource> list = count == -1 ? new ArrayList<TSource>() : new ArrayList<TSource>(count);
            for (SingleLinkedNode<TSource> node = _prepended; node != null; node = node.getLinked()) {
                list.add(node.getItem());
            }

            CollectionUtils.addAll(list, this.source);
            if (_appended != null) {
                IEnumerator<TSource> e = _appended.getEnumerator(_appendCount);
                while (e.moveNext()) {
                    list.add(e.current());
                }
            }

            return list;
        }

        public int getCount(boolean onlyIfCheap) {
            if (this.source instanceof IIListProvider2) {
                IIListProvider2<TSource> listProv = (IIListProvider2<TSource>) this.source;
                int count = listProv.internalSize(onlyIfCheap);
                return count == -1 ? -1 : count + _appendCount + _prependCount;
            }

            return !onlyIfCheap || source instanceof Collection ? source.count() + _appendCount + _prependCount : -1;
        }
    }
}
