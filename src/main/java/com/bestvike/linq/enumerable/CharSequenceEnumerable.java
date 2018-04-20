package com.bestvike.linq.enumerable;

import com.bestvike.OutRef;
import com.bestvike.collections.generic.Array;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.IListEnumerable;
import com.bestvike.linq.partition.IPartition;
import com.bestvike.linq.enumerator.CharSequenceEnumerator;
import com.bestvike.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017/7/25.
 */
public final class CharSequenceEnumerable implements IPartition<Character> {
    private final CharSequence source;

    public CharSequenceEnumerable(CharSequence source) {
        this.source = source;
    }

    public CharSequence getSource() {
        return this.source;
    }

    @Override
    public IEnumerator<Character> enumerator() {
        return new CharSequenceEnumerator(this.source);
    }

    @Override
    public int _getCount(boolean onlyIfCheap) {
        return this.source.length();
    }

    @Override
    public boolean _contains(Character value) {
        int length = this.source.length();
        for (int i = 0; i < length; i++) {
            if (Objects.equals(this.source.charAt(i), value))
                return true;
        }
        return false;
    }

    @Override
    public Array<Character> _toArray() {
        int length = this.source.length();
        Array<Character> array = Array.create(length);
        for (int i = 0; i < length; i++)
            array.set(i, this.source.charAt(i));
        return array;
    }

    @Override
    public Character[] _toArray(Class<Character> clazz) {
        int length = this.source.length();
        Character[] array = new Character[length];
        for (int i = 0; i < length; i++)
            array[i] = this.source.charAt(i);
        return array;
    }

    @Override
    public List<Character> _toList() {
        int length = this.source.length();
        List<Character> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            list.add(this.source.charAt(i));
        return list;
    }

    @Override
    public IPartition<Character> _skip(int count) {
        return null;
    }

    @Override
    public IPartition<Character> _take(int count) {
        return null;
    }

    @Override
    public Character _tryGetElementAt(int index, out<Boolean> found) {
        return null;
    }

    @Override
    public Character _tryGetFirst(out<Boolean> found) {
        return null;
    }

    @Override
    public Character _tryGetLast(out<Boolean> found) {
        return null;
    }
}
