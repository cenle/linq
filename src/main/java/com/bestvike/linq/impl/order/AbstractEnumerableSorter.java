package com.bestvike.linq.impl.order;

import com.bestvike.collections.generic.Array;

/**
 * Created by 许崇雷 on 2017/7/11.
 */
public abstract class AbstractEnumerableSorter<TElement> {
    protected abstract void computeKeys(Array<TElement> elements, int count);

    protected abstract int compareAnyKeys(int index1, int index2);

    private int[] computeMap(Array<TElement> elements, int count) {
        this.computeKeys(elements, count);
        int[] map = new int[count];
        for (int i = 0; i < map.length; i++) {
            map[i] = i;
        }

        return map;
    }

    protected int[] sort(Array<TElement> elements, int count) {
        int[] map = this.computeMap(elements, count);
        this.quickSort(map, 0, count - 1);
        return map;
    }

    protected int[] sort(Array<TElement> elements, int count, int minIdx, int maxIdx) {
        int[] map = this.computeMap(elements, count);
        this.partialQuickSort(map, 0, count - 1, minIdx, maxIdx);
        return map;
    }

    protected TElement elementAt(Array<TElement> elements, int count, int idx) {
        return elements.get(this.quickSelect(this.computeMap(elements, count), count - 1, idx));
    }

    private int compareKeys(int index1, int index2) {
        return index1 == index2 ? 0 : this.compareAnyKeys(index1, index2);
    }

    @SuppressWarnings("Duplicates")
    private void quickSort(int[] map, int left, int right) {
        do {
            int i = left;
            int j = right;
            int x = map[i + ((j - i) >> 1)];
            do {
                while (i < map.length && this.compareKeys(x, map[i]) > 0) {
                    i++;
                }

                while (j >= 0 && this.compareKeys(x, map[j]) < 0) {
                    j--;
                }

                if (i > j) {
                    break;
                }

                if (i < j) {
                    int temp = map[i];
                    map[i] = map[j];
                    map[j] = temp;
                }

                i++;
                j--;
            }
            while (i <= j);

            if (j - left <= right - i) {
                if (left < j) {
                    this.quickSort(map, left, j);
                }

                left = i;
            } else {
                if (i < right) {
                    this.quickSort(map, i, right);
                }

                right = j;
            }
        }
        while (left < right);
    }

    // Sorts the k elements between minIdx and maxIdx without sorting all elements
    // Time complexity: O(n + k log k) best and average case. O(n^2) worse case.
    @SuppressWarnings("Duplicates")
    private void partialQuickSort(int[] map, int left, int right, int minIdx, int maxIdx) {
        do {
            int i = left;
            int j = right;
            int x = map[i + ((j - i) >> 1)];
            do {
                while (i < map.length && this.compareKeys(x, map[i]) > 0) {
                    i++;
                }

                while (j >= 0 && this.compareKeys(x, map[j]) < 0) {
                    j--;
                }

                if (i > j) {
                    break;
                }

                if (i < j) {
                    int temp = map[i];
                    map[i] = map[j];
                    map[j] = temp;
                }

                i++;
                j--;
            }
            while (i <= j);

            if (minIdx >= i) {
                left = i + 1;
            } else if (maxIdx <= j) {
                right = j - 1;
            }

            if (j - left <= right - i) {
                if (left < j) {
                    this.partialQuickSort(map, left, j, minIdx, maxIdx);
                }

                left = i;
            } else {
                if (i < right) {
                    this.partialQuickSort(map, i, right, minIdx, maxIdx);
                }

                right = j;
            }
        }
        while (left < right);
    }

    // Finds the element that would be at idx if the collection was sorted.
    // Time complexity: O(n) best and average case. O(n^2) worse case.
    @SuppressWarnings("Duplicates")
    private int quickSelect(int[] map, int right, int idx) {
        int left = 0;
        do {
            int i = left;
            int j = right;
            int x = map[i + ((j - i) >> 1)];
            do {
                while (i < map.length && this.compareKeys(x, map[i]) > 0) {
                    i++;
                }

                while (j >= 0 && this.compareKeys(x, map[j]) < 0) {
                    j--;
                }

                if (i > j) {
                    break;
                }

                if (i < j) {
                    int temp = map[i];
                    map[i] = map[j];
                    map[j] = temp;
                }

                i++;
                j--;
            }
            while (i <= j);

            if (i <= idx) {
                left = i + 1;
            } else {
                right = j - 1;
            }

            if (j - left <= right - i) {
                if (left < j) {
                    right = j;
                }

                left = i;
            } else {
                if (i < right) {
                    left = i;
                }

                right = j;
            }
        }
        while (left < right);

        return map[idx];
    }
}