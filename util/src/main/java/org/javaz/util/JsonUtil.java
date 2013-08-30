package org.javaz.util;

import java.util.*;

/**
 * Simple way to convert any set of toString()-able objects to JSON
 * Can be useful to send data to JS-frontend
 */
public class JsonUtil
{
    public static String convertToJS(Object hashObject)
    {
        return convertToJS(hashObject, false);
    }

    public static String convertToJS(Object object, boolean newLine)
    {

        StringBuffer sb = new StringBuffer();

        if (object instanceof Map)
        {
            Map hash = (Map) object;
            sb.append("{");
            Set set = hash.keySet();
            String outerZpt = "";
            for (Iterator iteratorX = set.iterator(); iteratorX.hasNext(); )
            {
                Object x = iteratorX.next();
                String s = "" + x;
                sb.append(outerZpt);
                sb.append(convertToJS(s, newLine));
                sb.append(":");
                Object o = hash.get(x);
                sb.append(convertToJS(o, newLine));
                outerZpt = ",";
            }
            sb.append("}");
            if (newLine)
            {
                sb.append("\r\n");
            }
        }
        else if (object instanceof Iterable)
        {
            String innerZpt = "";
            sb.append("[");
            Iterable list = (Iterable) object;
            for (Iterator iterator = list.iterator(); iterator.hasNext(); )
            {
                Object o1 = iterator.next();
                sb.append(innerZpt);
                sb.append(convertToJS(o1, newLine));
                innerZpt = ",";
            }
            sb.append("]");
        }
        else if (object instanceof Object[])
        {
            String innerZpt = "";
            sb.append("[");
            Object[] list = (Object[]) object;
            for (int i = 0; i < list.length; i++)
            {
                Object o1 = list[i];
                sb.append(innerZpt);
                sb.append(convertToJS(o1, newLine));
                innerZpt = ",";
            }
            sb.append("]");
        }
        else
        {
            //what here happening is simple - escape each backslash, double quote and every newline and return specials
            sb.append("\"")
                    .append(("" + object).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r"))
                    .append("\"");
        }
        return sb.toString();
    }
}
