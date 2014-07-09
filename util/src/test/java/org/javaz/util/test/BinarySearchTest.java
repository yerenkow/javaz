package org.javaz.util.test;

import junit.framework.Assert;
import org.javaz.util.BinarySearch;
import org.javaz.util.IndexesUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class BinarySearchTest {

    @Ignore
    @Test
    public void testComplexSearch() {
        int total = 5;
        ArrayList objs = new ArrayList();
        ArrayList objs2 = new ArrayList();
        for(int i =0 ; i < total; i++) {
            Object[] objects = genDataLong(8, 3, 100000, 1, 15000);
//            Object[] objects = genDataInt(8, 4, 125000, 3, 5000);
            objs.add(objects);
            objs2.add(convertToHashSets(objects));
        }
        //warm-up
        for(int x =0; x < 10; x++) {
            for (Iterator iterator = objs.iterator(); iterator.hasNext(); ) {
                Object[] objects = (Object[]) iterator.next();
                long[] result = BinarySearch.complexMultiSearch((long[][][]) objects[0], (long[][]) objects[1]);
            }
            for (Iterator iterator = objs2.iterator(); iterator.hasNext(); ) {
                Object[] objects = (Object[]) iterator.next();
                List result = simpleHashSetChecks((HashSet[][]) objects[0], (HashSet) objects[1]);
            }
        }

        int totalFound = 0;
        long nanoTime = System.nanoTime();
        for (Iterator iterator = objs.iterator(); iterator.hasNext(); ) {
            Object[] objects = (Object[]) iterator.next();
            long[] result = BinarySearch.complexMultiSearch((long[][][]) objects[0], (long[][]) objects[1]);
            totalFound += result.length;
        }
        long nanoTime2 = System.nanoTime();
        System.out.println("Method: fast array scan.");
        System.out.println("totalFound = " + totalFound);
        long totalTime = nanoTime2 - nanoTime;
        System.out.println("spent for 1 array, per 1 ms = " + totalTime /1000000.0/total);
        System.out.println("total spent ms = " + totalTime /1000000.0);

        int totalFound2 = 0;
        nanoTime = System.nanoTime();
        for (Iterator iterator = objs2.iterator(); iterator.hasNext(); ) {
            Object[] objects = (Object[]) iterator.next();
            List result = simpleHashSetChecks((HashSet[][]) objects[0], (HashSet) objects[1]);
            totalFound2 += result.size();
        }
        nanoTime2 = System.nanoTime();
        System.out.println("===");
        System.out.println("Method: thorough set checks.");
        System.out.println("totalFound = " + totalFound2);
        long totalTime2 = nanoTime2 - nanoTime;
        System.out.println("spent for 1 array, ms = " + totalTime2/1000000.0/total);
        System.out.println("total spent ms = " + totalTime2 /1000000.0);
        Assert.assertEquals(totalFound, totalFound2);
        System.out.println("Performance ratio: " + (totalTime2*1.0 / totalTime));
    }

    private List simpleHashSetChecks(HashSet[][] data, HashSet negative) {
        List<Long> retValue = new LinkedList<Long>();
        HashSet first = data[0][0];
        for (Iterator iterator = first.iterator(); iterator.hasNext(); ) {
            Long value = (Long) iterator.next();
            if(negative.contains(value)) {
                continue;
            }

            boolean anyFailed = false;
            for (int i = 1; !anyFailed && i < data.length; i++) {
                HashSet[] hashSets = data[i];
                boolean anyFound = false;
                for (int j = 0; !anyFound && j < hashSets.length; j++) {
                    anyFound = hashSets[j].contains(value);
                }
                if(!anyFound) {
                    anyFailed = true;
                }
            }
            if(!anyFailed) {
                retValue.add(value);
            }
        }
        return retValue;
    }

    private Object convertToHashSets(Object[] objects) {
        long[][][] data = (long[][][]) objects[0];
        long[][] negative = (long[][]) objects[1];
        HashSet[][] sets = new HashSet[data.length][];
        HashSet negset = new HashSet();
        for(int i = 0; i < data.length; i++) {
            sets[i] = new HashSet[data[i].length];
            for(int j = 0; j < data[i].length; j++) {
                sets[i][j] = new HashSet();
                for(int k = 0; k < data[i][j].length; k++) {
                    sets[i][j].add(data[i][j][k]);
                }
            }
        }

        for(int i = 0; i < negative.length; i++) {
            for(int j = 0; j < negative[i].length; j++) {
                negset.add(negative[i][j]);
            }
        }
        return new Object[] {sets, negset} ;
    }

    private Object[] genDataInt(int dims, int arrs, int items, int negs, int negsCount) {
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

    private Object[] genDataLong(int dims, int arrs, int items, int negs, int negsCount) {
        int from = 1;
        int to =   3*items;
        Random random = new Random();
        long[][][] data = new long[dims][][];
        for(int i = 0; i < data.length; i++) {
            to =   3*items;
            int size = (i ==0 ? 1 : arrs);
            if(i == data.length - 1) {
                to = items;
            }
            data[i] = new long[size][];
            for(int j = 0; j < data[i].length; j++) {

                HashSet set = new HashSet();
                for(int k = 0; k < items; k++) {
                    set.add(random.nextInt(to) + from);
                }
                data[i][j] = new long[set.size()];
                ArrayList list = new ArrayList(set);
                Collections.sort(list);
                for (int k = 0; k < data[i][j].length; k++) {
                    data[i][j][k] = ((Integer) list.get(k)).longValue();
                }
            }
        }
        long[][] negative = new long[negs][];
        for(int i = 0; i < negs; i++) {

            HashSet set = new HashSet();
            for(int k = 0; k < items; k++) {
                set.add(random.nextInt(to) + from);
            }
            negative[i] = new long[set.size()];
            ArrayList list = new ArrayList(set);
            Collections.sort(list);
            for (int k = 0; k < negative[i].length; k++) {
                negative[i][k] = ((Integer) list.get(k)).longValue();
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

    @Test
    public void testInclusivePerformance() {
        int howMany = 5000;
        int range = 100000;

        int[][] ints = genData(howMany, range);

        for(int j = 0; j < 1000; j++) {
        for (int i = 0; i < ints.length; i++) {
            int[] anInt = ints[i];
            int x1 = anInt[300];
            int x2 = anInt[700];
            BinarySearch.binaryRangeSearchInclusive(anInt, x1, x2);
        }

        for (int i = 0; i < ints.length; i++) {
            int[] anInt = ints[i];
            int x1 = anInt[300];
            int x2 = anInt[700];
            int i1 = Arrays.binarySearch(anInt, x1);
            int i2 = Arrays.binarySearch(anInt, x2);

        }
        }



        long nanoTime = System.nanoTime();
        for (int i = 0; i < ints.length; i++) {
            int[] anInt = ints[i];
            int x1 = anInt[300];
            int x2 = anInt[700];
            int[] ints1 = BinarySearch.binaryRangeSearchInclusive(anInt, x1, x2);
        }
        long nanoTime2 = System.nanoTime();
        System.out.println("per 1, ns = " + (nanoTime2 - nanoTime)/howMany);
        System.out.println("total spent ms = " + (nanoTime2 - nanoTime)/1000000.0);


        nanoTime = System.nanoTime();
        for (int i = 0; i < ints.length; i++) {
            int[] anInt = ints[i];
            int x1 = anInt[300];
            int x2 = anInt[700];
            int i1 = Arrays.binarySearch(anInt, x1);
            int i2 = Arrays.binarySearch(anInt, x2);
        }
        nanoTime2 = System.nanoTime();
        System.out.println("per 1, ns = " + (nanoTime2 - nanoTime)/howMany);
        System.out.println("total spent ms = " + (nanoTime2 - nanoTime)/1000000.0);

    }

    private int[][] genData(int howMany, int range) {
        Random random = new Random();
        int xx = 1;
        int[][] ints = new int[howMany][];
        for(int i = 0; i<howMany; i++) {
            ints[i] = new int[range];
            for(int j = 0; j < range; j++) {
                ints[i][j] += xx;
                xx += random.nextInt(100);
            }
        }
        return ints;
    }

    @Test
    public void testGetValuesFromKeysByIds() {
        long[] keys = {1L, 2L, 3L, 4L, 5L, 6L};
        long[] values = {100L,200L,300L, -50L, -70L, -90L};
        long[] ids = {1L, 2L, 5L, 6L};
        long[] valuesFromKeysByIds = IndexesUtil.getValuesFromKeysByIds(ids, keys, values);
        Assert.assertEquals(valuesFromKeysByIds.length, 4);
        Assert.assertEquals(valuesFromKeysByIds[0], 100L);
        Assert.assertEquals(valuesFromKeysByIds[1], 200L);
        Assert.assertEquals(valuesFromKeysByIds[2], -70L);
        Assert.assertEquals(valuesFromKeysByIds[3], -90L);
    }

    @Test
    public void testArraySort() {
        long[] values = {100L,200L,300L, -50L, -70L, -90L};

        Integer[] ord = IndexesUtil.getOrderOfArrays(values, false);
        Assert.assertTrue( values[ord[ord.length -1]] > values[ord[0]]);

        ord = IndexesUtil.getOrderOfArrays(values, true);
        Assert.assertTrue( values[ord[ord.length -1]] < values[ord[0]]);
    }

    @Test
    public void testArrayIndexesSize() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(16);
        ArrayList tasks = new ArrayList();
        final StringBuffer sb = new StringBuffer();
        for(int i =0; i < 1000;i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    Random random = new Random();
                    int i = random.nextInt(1048576 / 20);
                    Integer[] emptyIndexes = IndexesUtil.getEmptyIndexes(i);
                    Assert.assertEquals(emptyIndexes.length, i);
                    int start = 0;
                    for (int j = 0; j < emptyIndexes.length; j++) {
                        Integer emptyIndex = emptyIndexes[j];
                        Assert.assertEquals(emptyIndex.intValue(), start++);
                    }
                    sb.append(i + " OK\n");
                    return null;
                }
            });
        }
        long nano = System.nanoTime();
        service.invokeAll(tasks);
        long nano2 = System.nanoTime();
//        System.out.println(" Nanos - " + (nano2 - nano));
    }
}
