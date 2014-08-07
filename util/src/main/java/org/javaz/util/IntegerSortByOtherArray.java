package org.javaz.util;

import java.util.Comparator;

/**
 * Created by user on 08.07.14.
 * Comparator used for sorting one array by other 's order
 */
public class IntegerSortByOtherArray implements Comparator<Integer> {
    private long[] values;
    private boolean asc;


    public IntegerSortByOtherArray(long[] values, boolean asc) {
        this.values = values;
        this.asc = asc;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        long compare = asc ? values[o2] - values[o1] : values[o1] - values[o2];

        if(compare > 0) {
            return -1;
        }
        
        if(compare < 0) {
            return 1;
        }

        return 0;
    }
}
