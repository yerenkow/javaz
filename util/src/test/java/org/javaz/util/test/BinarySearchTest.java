package org.javaz.util.test;

import junit.framework.Assert;
import org.javaz.util.BinarySearch;
import org.junit.Test;

import java.util.*;

/**
 */
public class BinarySearchTest {

    @Test
    public void testComplexSearch() {
        int total = 100;
        ArrayList objs = new ArrayList();
        for(int i =0 ; i < total; i++) {
            Object[] objects = genData(8, 4, 125000, 3, 5000);
            objs.add(objects);
        }

        int totalFound = 0;
        long nanoTime = System.nanoTime();
        for (Iterator iterator = objs.iterator(); iterator.hasNext(); ) {
            Object[] objects = (Object[]) iterator.next();
            List<Integer> integers = BinarySearch.complexMultiSearch((int[][][]) objects[0], (int[][]) objects[1]);
            totalFound += integers.size();
        }
        long nanoTime2 = System.nanoTime();
        System.out.println("totalFound = " + totalFound);
        System.out.println("per 1 ms = " + (nanoTime2 - nanoTime)/1000000.0/total);
        System.out.println("total spent ms = " + (nanoTime2 - nanoTime)/1000000.0);
    }

    private Object[] genData(int dims, int arrs, int items, int negs, int negsCount) {
        int from = 1;
        int to =   3*items;
        Random random = new Random();
        int[][][] data = new int[dims][][];
        for(int i = 0; i < data.length; i++) {
            int size = (i ==0 ? 1 : arrs);
            data[i] = new int[size][];
            for(int j = 0; j < data[i].length; j++) {

                HashSet set = new HashSet();
                for(int k = 0; k < items; k++) {
                    set.add(random.nextInt(to) + from);
                }
                data[i][j] = new int[set.size()];
                ArrayList list = new ArrayList(set);
                Collections.sort(list);
                for (int k = 0; k < data[i][j].length; k++) {
                    data[i][j][k] = (Integer) list.get(k);
                }
            }
        }
        int[][] negative = new int[negs][];
        for(int i = 0; i < negs; i++) {

            HashSet set = new HashSet();
            for(int k = 0; k < items; k++) {
                set.add(random.nextInt(to) + from);
            }
            negative[i] = new int[set.size()];
            ArrayList list = new ArrayList(set);
            Collections.sort(list);
            for (int k = 0; k < negative[i].length; k++) {
                negative[i][k] = (Integer) list.get(k);
            }
        }

        return new Object[]{data, negative};
    }

    @Test
    public void testInclusiveSearch() {
        testIncl(new int[]{1, 3, 5, 7, 9, 11}, 3, 5, 1, 2);
        testIncl(new int[]{1, 3, 5, 7, 9, 11}, 3, 3, 1, 1);
        testIncl(new int[]{1, 3, 5, 7, 9, 11}, 2, 2, -1, -1);

        testExcl(new int[]{1, 3, 5, 7, 9, 11}, 3, 5, -1, -1);
        testExcl(new int[]{1, 3, 5, 7, 9, 11}, 3, 7, 2, 2);
        testExcl(new int[]{1, 3, 5, 7, 9, 11}, 2, 4, 1, 1);
    }

    private void testIncl(int[] ints, int from, int to, int idx, int idx2) {
        int[] range = BinarySearch.binaryRangeSearchInclusive(ints, from, to);
        Assert.assertEquals(range[0], idx);
        Assert.assertEquals(range[1], idx2);
    }

    private void testExcl(int[] ints, int from, int to, int idx, int idx2) {
        int[] range = BinarySearch.binaryRangeSearchExclusive(ints, from, to);
        Assert.assertEquals(range[0], idx);
        Assert.assertEquals(range[1], idx2);
    }
}
