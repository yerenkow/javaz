package org.javaz.util;

import java.util.Comparator;

/**
 * Created by user on 26.07.14.
 * Comparator used for sorting one array by other 's order with
 * ability to take third value if needed. (Override value)
 */
public class IntegerSortByTwoArrays implements Comparator<Integer> {
    private long[] values1;
    private long[] values2;
    private boolean asc;
    private long noValue = 0L;


    public IntegerSortByTwoArrays(long[] values1, long[] values2, boolean asc) {
        this.values1 = values1;
        this.values2 = values2;
        this.asc = asc;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        long v1 = values2[o1] != noValue ? values2[o1] : values1[o1];
        long v2 = values2[o2] != noValue ? values2[o2] : values1[o2];
        long compare = asc ? v1 - v2 : v2 - v1;

        if(compare > 0) {
            return -1;
        }
        
        if(compare < 0) {
            return 1;
        }

        return 0;
    }

    public long getNoValue() {
        return noValue;
    }

    public void setNoValue(long noValue) {
        this.noValue = noValue;
    }
}
