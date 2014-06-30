package org.javaz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple util, to quickly find array indices for interested elements in sorted arrays,
 */
public class BinarySearch {
    
    public static int[] binaryRangeSearchInclusive(int[] a, int keyMin, int keyMax) {
        int low1 = 0;
        int high1 = a.length - 1;

        int low2 = low1;
        int high2 = high1;

        int start = -1;
        int end = -1;

        while ((start < 0 && low1 <= high1) || (end < 0 && low2 <= high2)) {
            if(start < 0 && low1 <= high1) {
                int mid = (low1 + high1) >>> 1;
                int midVal = a[mid];

                if (midVal < keyMin)
                    low1 = mid + 1;
                else if (midVal > keyMin)
                    high1 = mid - 1;
                else
                    start = mid;
            }

            if(end < 0 && low2 <= high2) {
                int mid = (low2 + high2) >>> 1;
                int midVal = a[mid];

                if (midVal < keyMax)
                    low2 = mid + 1;
                else if (midVal > keyMax)
                    high2 = mid - 1;
                else
                    end = mid;
            }
        }

        if(start < 0) {
            if(a[low1 - 1] >= keyMin) {
                start = low1 - 1;
            } else if(a[low1] >= keyMin) {
                start = low1;
            } else if(a[low1 + 1] >= keyMin) {
                start = low1 + 1;
            }
        }

        if(end < 0) {
            if(a[high2 + 1] <= keyMax) {
                end = high2 + 1;
            } else if(a[high2] <= keyMax) {
                end = high2;
            } else if(a[high2 - 1] <= keyMax) {
                end = high2 - 1;
            }
        }

        if(start > end) {
            return new int[]{-1, -1};
        }
        return new int[]{start, end};
    }

    public static int[] binaryRangeSearchInclusive(long[] a, long keyMin, long keyMax) {
        int low1 = 0;
        int high1 = a.length - 1;

        int low2 = low1;
        int high2 = high1;

        int start = -1;
        int end = -1;

        while ((start < 0 && low1 <= high1) || (end < 0 && low2 <= high2)) {
            if(start < 0 && low1 <= high1) {
                int mid = (low1 + high1) >>> 1;
                long midVal = a[mid];

                if (midVal < keyMin)
                    low1 = mid + 1;
                else if (midVal > keyMin)
                    high1 = mid - 1;
                else
                    start = mid;
            }

            if(end < 0 && low2 <= high2) {
                int mid = (low2 + high2) >>> 1;
                long midVal = a[mid];

                if (midVal < keyMax)
                    low2 = mid + 1;
                else if (midVal > keyMax)
                    high2 = mid - 1;
                else
                    end = mid;
            }
        }

        if(start < 0) {
            if(a[low1 - 1] >= keyMin) {
                start = low1 - 1;
            } else if(a[low1] >= keyMin) {
                start = low1;
            } else if(a[low1 + 1] >= keyMin) {
                start = low1 + 1;
            }
        }

        if(end < 0) {
            if(a[high2 + 1] <= keyMax) {
                end = high2 + 1;
            } else if(a[high2] <= keyMax) {
                end = high2;
            } else if(a[high2 - 1] <= keyMax) {
                end = high2 - 1;
            }
        }

        if(start > end) {
            return new int[]{-1, -1};
        }
        return new int[]{start, end};
    }

    public static int[] binaryRangeSearchExclusive(int[] a, int keyMin, int keyMax) {
        return binaryRangeSearchInclusive(a, keyMin + 1, keyMax - 1);
    }

    public static int[] binaryRangeSearchExclusive(long[] a, long keyMin, long keyMax) {
        return binaryRangeSearchInclusive(a, keyMin + 1, keyMax - 1);
    }

    public static List<Integer> complexMultiSearch(final int[][][] data, final int[][] negative) {
        // this is the list we'll return
        List<Integer> retValue = new LinkedList<Integer>();
        // this first array must be single-element array.
        final int[][] firstArrayArray = data[0];
        //this is array, on which we'll iterate.
        final int[] firstArray = firstArrayArray[0];

        //this is indices for each array-array.
        final int[][] indices = new int[data.length][];
        for (int i = 0; i < data.length; i++) {
            indices[i] = new int[data[i].length];
        }
        final int[] negIndices = new int[negative.length];

        for (indices[0][0] = 0; indices[0][0] < firstArray.length; indices[0][0]++) {
            // main loop based on first array.
            // what we do is step by step checking other arrays (or sets of arrays)
            // for same value present there.
            boolean anyFailed = false;
            final int val = firstArray[indices[0][0]];

            //simple checks for negative
            for (int i = 0; !anyFailed && i < negative.length; i++) {
                while(negIndices[i] < negative[i].length && negative[i][negIndices[i]] <= val) {
                    anyFailed = val == negative[i][negIndices[i]++];
                }
            }

            // note, that we starting from 1, not 0!
            for (int i = 1; !anyFailed && i < data.length; i++) {
                // scanned variant array
                final int[][] sva = data[i];
                boolean anyFound = false;
                for (int j = 0; !anyFound && j < sva.length; j++) {
                    while(indices[i][j] < sva[j].length && sva[j][indices[i][j]] <= val) {
                        anyFound = val == sva[j][indices[i][j]++];
                    }
                }

                if(!anyFound) {
                    anyFailed = true;
                }
            }

            if(!anyFailed) {
                retValue.add(val);
            }
        }
        return retValue;
    }

    public static List<Long> complexMultiSearch(final long[][][] data, final long[][] negative) {
        // this is the list we'll return
        List<Long> retValue = new LinkedList<Long>();
        // this first array must be single-element array.
        final long[][] firstArrayArray = data[0];
        //this is array, on which we'll iterate.
        final long[] firstArray = firstArrayArray[0];

        //this is indices for each array-array.
        final int[][] indices = new int[data.length][];
        for (int i = 0; i < data.length; i++) {
            indices[i] = new int[data[i].length];
        }
        final int[] negIndices = new int[negative.length];

        for (indices[0][0] = 0; indices[0][0] < firstArray.length; indices[0][0]++) {
            // main loop based on first array.
            // what we do is step by step checking other arrays (or sets of arrays)
            // for same value present there.
            boolean anyFailed = false;
            final long val = firstArray[indices[0][0]];

            //simple checks for negative
            for (int i = 0; !anyFailed && i < negative.length; i++) {
                while(negIndices[i] < negative[i].length && negative[i][negIndices[i]] <= val) {
                    anyFailed = val == negative[i][negIndices[i]++];
                }
            }

            // note, that we starting from 1, not 0!
            for (int i = 1; !anyFailed && i < data.length; i++) {
                // scanned variant array
                final long[][] sva = data[i];
                boolean anyFound = false;
                for (int j = 0; !anyFound && j < sva.length; j++) {
                    while(indices[i][j] < sva[j].length && sva[j][indices[i][j]] <= val) {
                        anyFound = val == sva[j][indices[i][j]++];
                    }
                }

                if(!anyFound) {
                    anyFailed = true;
                }
            }

            if(!anyFailed) {
                retValue.add(val);
            }
        }
        return retValue;
    }
}
