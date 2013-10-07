package org.javaz.util;

import java.util.*;

/**
 * This tools calculates difference between Object(s) A and B, produced difference of same
 * interface that have A and B.
 * Supported classes - Map, List.
 */
public class ObjectDifference
{
    public static final int TYPE_IGNORE = 0;
    public static final int TYPE_EQUALS = 1;
    public static final int TYPE_NOT_EQUALS = 2;

    public static HashMap packHashesByKey(List<Map> records, Object keyField)
    {
        HashMap hash = new HashMap();
        for (Iterator<Map> iterator = records.iterator(); iterator.hasNext(); )
        {
            Map map = iterator.next();
            hash.put(map.get(keyField), map);
        }
        return hash;
    }

    public static HashMap getInANotInB(Map a, Map b)
    {
        HashMap result = new HashMap();
        result.putAll(a);
        Set set = b.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext(); )
        {
            Object o = iterator.next();
            result.remove(o);
        }
        return result;
    }

    public static HashMap getInAAndInBEquals(Map a, Map b)
    {
        return getInAAndInB(a, b, TYPE_EQUALS);
    }

    public static HashMap getInAAndInBNotEquals(Map a, Map b)
    {
        return getInAAndInB(a, b, TYPE_NOT_EQUALS);
    }

    public static HashMap getInAAndInB(Map a, Map b)
    {
        return getInAAndInB(a, b, TYPE_IGNORE);
    }

    private static HashMap getInAAndInB(Map a, Map b, int equals)
    {
        HashMap result = new HashMap();
        Set set = a.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext(); )
        {
            Object o = iterator.next();
            if (a.containsKey(o) && b.containsKey(o))
            {
                //we are producing values only in case we don't care about equality,
                // OR we care and objects are equals
                if (equals == TYPE_IGNORE)
                {
                    result.put(o, a.get(o));
                }
                else
                {
                    boolean areTheyEquals = deepEquals(a.get(o), b.get(o));

                    if ((areTheyEquals && equals == TYPE_EQUALS) || (!areTheyEquals && equals == TYPE_NOT_EQUALS))
                        result.put(o, a.get(o));
                }
            }
        }

        return result;
    }

    private static boolean deepEquals(Object a, Object b)
    {
        //temporary hack, but working, if there's no self-nesting complex cases.
        return JsonUtil.convertToJS(a, false, true).equals(JsonUtil.convertToJS(b, false, true));
    }

}
