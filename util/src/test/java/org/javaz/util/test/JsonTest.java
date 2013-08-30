package org.javaz.util.test;

import junit.framework.Assert;
import org.javaz.util.JsonUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class JsonTest
{
    @Test
    public void testJson()
    {
        new JsonUtil();

        HashMap test = new HashMap();
        ArrayList list1 = new ArrayList();
        list1.add("a\nb");
        list1.add("C");
        HashMap map = new HashMap();
        list1.add(map);
        list1.add(map);
        Object[] arr2 = new Object[]{"a", "b", "c", "escape'", "\" ' \n \r"};

        map.put("array", arr2);
        test.put("list", list1);
        String s = JsonUtil.convertToJS(test);
        Assert.assertEquals("{\"list\":[\"a\\nb\",\"C\",{\"array\":[\"a\",\"b\",\"c\",\"escape'\",\"\\\" ' \\n \\r\"]},{\"array\":[\"a\",\"b\",\"c\",\"escape'\",\"\\\" ' \\n \\r\"]}]}", s);

        String s2 = JsonUtil.convertToJS(test, true);
        Assert.assertTrue(s2.contains("\r\n"));
    }

}
