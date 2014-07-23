package org.javaz.util;

import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by user on 08.07.14.
 * Utils to sort arrays by other arrays order
 */
public class IndexesUtil {

   private static Logger logger = LogManager.getLogger(IndexesUtil.class);

    /**
     *
     * @param ids - thee keys MUST be sorted and all of them exists in keys
     * @param keys - sorted array of keys
     * @param values - values.
     * @return array of requested values
     */
    public static long[] getValuesFromKeysByIds(long[] ids, long[] keys, long[] values) {
        final int idsLength = ids.length;
        final long[] vals = new long[idsLength];
        final int keysLength = keys.length;
        int pos = 0;

        for (int i = 0; pos < keysLength && i < idsLength; i++) {
            final long id = ids[i];
            while(pos < keysLength && id > keys[pos]) {
                // iterate until we find it
                pos++;
            }
            vals[i] = values[pos];
        }

        return vals;
    }


    public static Integer[] getOrderOfArrays(long[] values, boolean ascValues) {
        Integer[] indexes = getEmptyIndexes(values.length);
        Arrays.sort(indexes, new IntegerSortByOtherArray(values, ascValues));

        return indexes;
    }

    //1048576 - 4
    public static final String DEFAULT_MAX_TEMPLATE_ARRAY_SIZE = "1048572";

    // that's means that our array should fit exactly in 4Mb of RAM. I hope :D
    public static final int MAX_TEMPLATE_ARRAY_SIZE =
            Integer.valueOf(System.getProperty("org.javaz.util.IndexesUtil.MAX_TEMPLATE_ARRAY_SIZE",
                    DEFAULT_MAX_TEMPLATE_ARRAY_SIZE));

    // that's means that our array should fit exactly in 256Kb of RAM. I hope :D
    public static final int ORIGINAL_TEMPLATE_ARRAY_SIZE = 65536 - 4;
    /**
     * 64k of values, template array, must be a lot faster copy it, than fill new one.
     */
    private static Integer[] basicIndexArray = new Integer[ORIGINAL_TEMPLATE_ARRAY_SIZE];

    static
    {
        for(int i = 0; i < basicIndexArray.length; i++) {
            basicIndexArray[i] = i;
        }
    }

    /**
     *
     * @param length - must be positive, no checks for speedup
     * @return copy of indexes.
     */
    public static Integer[] getEmptyIndexes(int length) {
        final int originalLength = basicIndexArray.length;
        Integer[] integers = Arrays.copyOf(basicIndexArray, length);
        if(originalLength < integers.length) {
            for(int i = originalLength; i < integers.length; i++) {
                integers[i] = i;
            }

            if(length > MAX_TEMPLATE_ARRAY_SIZE) {
                logger.info("Requested " + length/1024 + "k, while max is " + MAX_TEMPLATE_ARRAY_SIZE/1024
                    + "k, consider increasing MAX_TEMPLATE_ARRAY_SIZE");
            }

            // we grow until MAX
            if(originalLength < MAX_TEMPLATE_ARRAY_SIZE &&
                    //in case less than half MAX we prefer grow only at least doubling size
                    ((originalLength < MAX_TEMPLATE_ARRAY_SIZE/2 && integers.length / originalLength >= 2 )
                            //in case more than half MAX we grow until we fit
                            || (originalLength >= MAX_TEMPLATE_ARRAY_SIZE/2))) {
                final int newSize =
                        2*originalLength + 4 > MAX_TEMPLATE_ARRAY_SIZE ? MAX_TEMPLATE_ARRAY_SIZE : 2*originalLength + 4;

                // this is not very thread-safe, but it should work in this specific case -
                // array only grow, and it's old one is sub-array of new one, so should be no harm for logic.
                logger.info("Growing template index array from " + originalLength/1024 + "k to " + newSize/1024 + "k");
                basicIndexArray = Arrays.copyOf(integers, newSize);
            }
        }
        return integers;
    }
}
