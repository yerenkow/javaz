package org.javaz.util;

import java.util.*;

/**
 * Simple util, to quickly find array indices for interested elements in sorted arrays,
 */
public class BinarySearch {
    
    public static int DEFAULT_MAX_SIZE = 16*1024;

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
            if(low1 > 0 && a[low1 - 1] >= keyMin) {
                start = low1 - 1;
            } else if(low1 < a.length && a[low1] >= keyMin) {
                start = low1;
            } else if(low1 + 1 < a.length && a[low1 + 1] >= keyMin) {
                start = low1 + 1;
            }
            if(start < 0) {
                return new int[]{-1, -1};
            }
        }

        if(end < 0) {
            if(high2 + 1 < a.length && a[high2 + 1] <= keyMax) {
                end = high2 + 1;
            } else if(high2 < a.length && a[high2] <= keyMax) {
                end = high2;
            } else if(high2 > 0 && a[high2 - 1] <= keyMax) {
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
            if(low1 > 0 && a[low1 - 1] >= keyMin) {
                start = low1 - 1;
            } else if(low1 < a.length && a[low1] >= keyMin) {
                start = low1;
            } else if(low1 + 1 < a.length && a[low1 + 1] >= keyMin) {
                start = low1 + 1;
            }
            if(start < 0) {
                return new int[]{-1, -1};
            }
        }

        if(end < 0) {
            if(high2 + 1 < a.length && a[high2 + 1] <= keyMax) {
                end = high2 + 1;
            } else if(high2 < a.length && a[high2] <= keyMax) {
                end = high2;
            } else if(high2 > 0 && a[high2 - 1] <= keyMax) {
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

    public static int[] complexMultiSearch(final int[][][] data, final int[][] negative) {
        return complexMultiSearch(data, negative, DEFAULT_MAX_SIZE);
    }

    public static int[] complexMultiSearch(final int[][][] data, final int[][] negative, final int maxSize) {

        //this is indices for each array-array.
        final int[][] indices = new int[data.length][];

        int guessArrayIndex = 0;
        int minRecords = Integer.MAX_VALUE;

        final int[] arraysNotCompleted = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            indices[i] = new int[data[i].length];
            //let's find least filled array in single top-array
            if(data[i].length == 1 && minRecords > data[i][0].length) {
                guessArrayIndex = i;
                minRecords = data[i][0].length;
            }
            arraysNotCompleted[i] = data[i].length;
        }
        final int[] negIndices = new int[negative.length];

        // this should set to false when any sub-array will be depleted, so
        // there will be no sense in continuing.
        boolean allArrayHaveMoreElements = true;

        final int iterationIndex = guessArrayIndex;

        // this first array must be single-element array.
        final int[][] firstArrayArray = data[iterationIndex];
        //this is array, on which we'll iterate.
        final int[] firstArray = firstArrayArray[0];

        final int exactMaxSize = firstArray.length > maxSize ? maxSize : firstArray.length;
        final int[] retValue = new int[exactMaxSize];

        // this is the list we'll return
        int position = 0;

        for (indices[iterationIndex][0] = 0; allArrayHaveMoreElements
                && indices[iterationIndex][0] < firstArray.length; indices[iterationIndex][0]++) {
            // main loop based on first array.
            // what we do is step by step checking other arrays (or sets of arrays)
            // for same value present there.
            boolean anyFailed = false;
            final int val = firstArray[indices[iterationIndex][0]];

            //simple checks for negative
            for (int i = 0; !anyFailed && i < negative.length; i++) {
                while(negIndices[i] < negative[i].length && negative[i][negIndices[i]] <= val) {
                    anyFailed = val == negative[i][negIndices[i]++];
                }
            }

            // note, that we starting from 0, but we skip index by which we are iterating.
            for (int i = 0; !anyFailed && i < data.length; i++) {
                if(i == iterationIndex) {
                    continue;
                }
                // scanned variant array
                final int[][] sva = data[i];
                boolean anyFound = false;
                for (int j = 0; !anyFound && j < sva.length; j++) {
                    while(indices[i][j] < sva[j].length && sva[j][indices[i][j]] <= val) {
                        anyFound = val == sva[j][indices[i][j]++];
                        //this means we fully iterated through this array
                        if(indices[i][j] == sva[j].length) {
                            // this set to true, if and only if ALL of arrays in level `i` is completed
                            // this means that there's no point in iterating, since there will be no values at all.
                            allArrayHaveMoreElements = --arraysNotCompleted[i] > 0;
                        }
                    }
                }

                if(!anyFound) {
                    anyFailed = true;
                }
            }

            if(!anyFailed) {
                retValue[position++] = val;
                if(position >= maxSize) {
                    return retValue;
                }
            }
        }
        return Arrays.copyOf(retValue, position);
    }

    public static long[] complexMultiSearch(final long[][][] data, final long[][] negative) {
        return complexMultiSearch(data, negative, DEFAULT_MAX_SIZE);
    }

    public static long[] complexMultiSearch(final long[][][] data, final long[][] negative, final int maxSize) {

        final int dataLength = data.length;
        final int negativeLength = negative.length;

        //this is indices for each array-array.
        final int[][] indices = new int[dataLength][];

        int iterationIndex = 0;
        int minRecords = Integer.MAX_VALUE;

        final int[] arraysNotCompleted = new int[dataLength];
        for (int i = 0; i < dataLength; i++) {
            indices[i] = new int[data[i].length];
            //let's find least filled array in single top-array
            if(data[i].length == 1 && minRecords > data[i][0].length) {
                iterationIndex = i;
                minRecords = data[i][0].length;
            }
            arraysNotCompleted[i] = data[i].length;
        }
        final int[] negIndices = new int[negativeLength];

        // this should set to false when any sub-array will be depleted, so
        // there will be no sense in continuing.
        boolean allArrayHaveMoreElements = true;

        // this first array must be single-element array.
        final long[][] firstArrayOfArray = data[iterationIndex];
        //this is array, on which we'll iterate.
        final long[] iterationArray = firstArrayOfArray[0];

        final int iterationArrayLength = iterationArray.length;
        final int exactMaxSize = iterationArrayLength > maxSize ? maxSize : iterationArrayLength;

        // this is the list we'll return
        final long[] retValue = new long[exactMaxSize];
        // next position in retValue for found id
        int position = 0;

        boolean anyFailed = false;
        for (int mainIndex = 0; mainIndex < iterationArrayLength; mainIndex++) {
            // main loop based on first array.
            // what we do is step by step checking other arrays (or sets of arrays)
            // for same value present there.
            final long val = iterationArray[mainIndex];
            anyFailed = false;

            //simple checks for negative
            for (int i = 0; !anyFailed && i < negativeLength; i++) {
                while(negIndices[i] < negative[i].length && negative[i][negIndices[i]] <= val) {
                    anyFailed = val == negative[i][negIndices[i]++];
                }
            }

            // note, that we starting from 0, but we skip index by which we are iterating.
            for (int i = 0; !anyFailed && i < dataLength; i++) {
                if (i == iterationIndex) {
                    continue;
                }
                // scanned variant array
                final long[][] sva = data[i];
                boolean anyFound = false;
                for (int j = 0; !anyFound && j < sva.length; j++) {
                    while (indices[i][j] < sva[j].length && sva[j][indices[i][j]] <= val) {
                        anyFound = val == sva[j][indices[i][j]++];
                        //this means we fully iterated through this array
                        if (indices[i][j] == sva[j].length) {
                            // this set to true, if and only if ALL of arrays in level `i` is completed
                            // this means that there's no point in iterating, since there will be no values at all.
                            allArrayHaveMoreElements = --arraysNotCompleted[i] > 0;
                        }
                    }
                }

                if(!anyFound) {
                    anyFailed = true;
                }
            }

            if(!anyFailed) {
                retValue[position++] = val;
                if(position == exactMaxSize) {
                    return retValue;
                }
            }

            if (!allArrayHaveMoreElements) {
                return Arrays.copyOf(retValue, position);
            }
        }
        return Arrays.copyOf(retValue, position);
    }
}
