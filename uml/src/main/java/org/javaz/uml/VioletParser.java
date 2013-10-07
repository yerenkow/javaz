package org.javaz.uml;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is helper to parse Violet UML .class files
 */
public class VioletParser
{
    public static final Hashtable fullyQTypes = new Hashtable();
    public static final Hashtable sqlTypes = new Hashtable();

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
    }

    public HashMap<String, Object> parseVioletClass(String file)
    {
        HashMap<String, Object> result = new HashMap<String, Object>();

        ArrayList<Map> allBeans = new ArrayList<Map>();
        result.put("beans", allBeans);

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList list = document.getElementsByTagName("object");
            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);
                NamedNodeMap map = node.getAttributes();
                if (map != null && map.getNamedItem("class") != null && map.getNamedItem("class").getNodeValue().equals("com.horstmann.violet.ClassNode"))
                {
                    String name = findName(node, "name");
                    String attributes = findName(node, "attributes");
                    HashMap<String, Object> bean = new HashMap<String, Object>();
                    bean.put("name", name);
                    bean.put("table_name", getDbName(name));
                    ArrayList<Map> beanAttributes = new ArrayList<Map>();
                    bean.put("attributes", beanAttributes);

                    String[] splittedAttributes = attributes.split("\\n");
                    for (int j = 0; j < splittedAttributes.length; j++)
                    {
                        String s = splittedAttributes[j];
                        String[] nameTypePair = s.split(":");
                        if (nameTypePair.length < 2)
                        {
                            System.out.println("Error with: " + name + "." + s + ", ignoring");
                        }
                        else
                        {
                            HashMap<String, String> attribute = new HashMap<String, String>();
                            String atrributeName = nameTypePair[0].trim();
                            attribute.put("name", atrributeName);
                            attribute.put("column_name", getDbName(atrributeName));

                            String type = nameTypePair[1].trim();
                            String sqlType = "";
                            int length = 250;

                            Matcher matcher = SIZE_PATTERN.matcher(type);
                            if (matcher.find())
                            {
                                String sizeString = matcher.group(1);
                                try
                                {
                                    length = Integer.parseInt(sizeString);
                                    type = type.substring(0, type.indexOf(sizeString));
                                }
                                catch (NumberFormatException e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            type = getFullyQualifiedTypeName(type);
                            sqlType = (String) sqlTypes.get(type);
                            if (sqlType == null)
                            {
                                System.out.println("Unknown type = " + type + " of attribute " + bean.get("name") + "." + attribute.get("name") + ", using " + DEFAULT_TYPE_JAVA + ".");
                                type = DEFAULT_TYPE_JAVA;
                                sqlType = (String) sqlTypes.get(type);
                            }
                            sqlType = sqlType.replace("{size}", "" + length);

                            attribute.put("type", type);
                            attribute.put("sql_type", sqlType);
                            attribute.put("length", "" + length);

                            attribute.put("primary_key", attribute.get("name").equalsIgnoreCase("id") ? "true" : "false");

                            beanAttributes.add(attribute);
                        }
                    }
                    allBeans.add(bean);
                }
            }
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private String findName(Node node, String s)
    {
        return NodeParserUtil.getNodeValueByAttribute(node, "property", s, "string");
    }

    private String getFullyQualifiedTypeName(String type)
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
