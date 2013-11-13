package org.javaz.uml;

import net.sf.json.JSONObject;
import org.javaz.util.JsonUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is helper to parse Violet UML .class files
 */
public abstract class BasicVioletParser
{
    public static final Hashtable fullyQTypes = new Hashtable();
    public static final Hashtable sqlTypes = new Hashtable();

    public static final int DEFAULT_LENGTH = 250;

    public static String SIZE_REGEX = "(\\d+)";
    public static Pattern SIZE_PATTERN = Pattern.compile(SIZE_REGEX);

    public static String DEFAULT_TYPE_JAVA = "java.lang.Integer";

    static
    {

        fullyQTypes.put("int", "java.lang.Integer");
        fullyQTypes.put("java::lang::int", "java.lang.Integer");
        fullyQTypes.put("integer", "java.lang.Integer");
        fullyQTypes.put("java::lang::integer", "java.lang.Integer");
        fullyQTypes.put("double", "java.lang.Double");
        fullyQTypes.put("java::lang::double", "java.lang.Double");
        fullyQTypes.put("float", "java.lang.Float");
        fullyQTypes.put("java::lang::float", "java.lang.Float");
        fullyQTypes.put("boolean", "java.lang.Boolean");
        fullyQTypes.put("java::lang::boolean", "java.lang.Boolean");
        fullyQTypes.put("string", "java.lang.String");
        fullyQTypes.put("java::lang::string", "java.lang.String");
        fullyQTypes.put("char", "java.lang.Char");
        fullyQTypes.put("java::lang::char", "java.lang.Char");
        fullyQTypes.put("byte", "java.lang.Byte");
        fullyQTypes.put("java::lang::byte", "java.lang.Byte");
        fullyQTypes.put("long", "java.lang.Long");
        fullyQTypes.put("java::lang::long", "java.lang.Long");
        fullyQTypes.put("short", "java.lang.Short");
        fullyQTypes.put("java::lang::short", "java.lang.Short");
        fullyQTypes.put("date", "java.sql.Date");
        fullyQTypes.put("java::sql::date", "java.sql.Date");
        fullyQTypes.put("timestamp", "java.sql.Timestamp");
        fullyQTypes.put("java::sql::timestamp", "java.sql.Timestamp");
        fullyQTypes.put("hashmap", "java.util.HashMap");
        fullyQTypes.put("java::util::hashmap", "java.util.HashMap");
        fullyQTypes.put("arraylist", "java.util.ArrayList");
        fullyQTypes.put("java::util::arraylist", "java.util.ArrayList");

        sqlTypes.put("java.lang.Boolean", "boolean");
        sqlTypes.put("java.lang.Integer", "integer");
        sqlTypes.put("java.lang.Byte", "smallint");
        sqlTypes.put("java.lang.Short", "smallint");
        sqlTypes.put("java.lang.Long", "bigint");
        sqlTypes.put("java.lang.Float", "real");
        sqlTypes.put("java.lang.Double", "double precision");
        sqlTypes.put("java.lang.Char", "character varying({size})");
        sqlTypes.put("java.lang.String", "character varying({size})");
        sqlTypes.put("java.sql.Date", "date");
        sqlTypes.put("java.sql.Timestamp", "timestamp without time zone");
        sqlTypes.put("java.util.HashMap", "text");
        sqlTypes.put("java.util.ArrayList", "text");
    }

    public String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    public abstract HashMap<String, Object> parseVioletClass(String file);

    protected String getFullyQualifiedTypeName(String type)
    {
        if (type != null && fullyQTypes.containsKey(type.trim().toLowerCase()))
        {
            return (String) fullyQTypes.get(type.trim().toLowerCase());
        }
        return type;
    }

    public static String getDbName(String name)
    {
        if (name == null)
            return name;

        String result = "";
        char lrc = '_';
        char c;

        for (int i = 0; i < name.length(); ++i)
        {
            c = name.charAt(i);

            if (!((lrc == '_') && (c == '_')))
            {
                if (Character.isLowerCase(lrc) && Character.isUpperCase(c))
                    result += "_";

                result += c;
            }

            lrc = c;
        }

        return result.toLowerCase();
    }
}
