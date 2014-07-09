package org.javaz.util;

import java.util.Comparator;

/**
 * Created by user on 08.07.14.
 */
public class IntegerSortByOtherArray implements Comparator {
    private long[] values;
    private boolean asc;


    public IntegerSortByOtherArray(long[] values, boolean asc) {
        this.values = values;
        this.asc = asc;
    }

    @Override
    public int compare(Object o1, Object o2) {
        long compare = asc ? values[(Integer) o1] - values[(Integer) o2] : values[(Integer) o2] - values[(Integer) o1];

        if(compare > 0) {
            return -1;
        }
        
        if(compare < 0) {
            return 1;
        }

        return 0;
    }
}
