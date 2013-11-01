package org.javaz.uml;

import org.javaz.util.JsonUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * This is helper to parse Violet UML .class files
 */
public class OldVioletParser extends BasicVioletXmlParser
{
    @Override
    protected void parseDocument(Document document, ArrayList<Map> allBeans)
    {
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
                        int length = DEFAULT_LENGTH;

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

    @Override
    protected String readContent(String file)
    {
        try
        {
            return readFile(file, Charset.forName("UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    protected String findName(Node node, String s)
    {
        return NodeParserUtil.getNodeValueByAttribute(node, "property", s, "string");
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Please, specify two parameters - in violet model file and out json file");
            System.exit(0);
        }
        OldVioletParser vp = new OldVioletParser();
        HashMap<String, Object> model = vp.parseVioletClass(args[0]);
        FileWriter fw = new FileWriter(args[1]);
        fw.write(JsonUtil.convertToJS(model));
        fw.close();
    }
}
