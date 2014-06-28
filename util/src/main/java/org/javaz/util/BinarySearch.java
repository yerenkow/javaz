package org.javaz.util;

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
}
