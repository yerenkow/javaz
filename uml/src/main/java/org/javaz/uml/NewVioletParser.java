package org.javaz.uml;

import org.javaz.util.JsonUtil;
import org.javaz.xml.XpathSaxHandler;

import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is helper to parse Violet UML .class files
 */
public class NewVioletParser extends BasicVioletParser
{
    public static String START_MARKER = "<![CDATA[";
    public static String END_MARKER = "]]>";

    public HashMap<String, Object> parseVioletClass(String file)
    {
        try
        {
            String readContent = readContent(file);
            return parseWithXpath(readContent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected HashMap<String, Object> parseWithXpath(String content) throws Exception {

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        XpathSaxHandler dh = new XpathSaxHandler();

        dh.addHashToHashFillingRule("all", XpathSaxHandler.RESULTS);
        dh.addHashToHashFillingRule("bean", "all@beans,list");
        dh.addHashToHashFillingRule("edge", "all@edges,list");

        dh.addNewObjectRule("/ClassDiagramGraph", "all");
        dh.addNewObjectRule("/ClassDiagramGraph/nodes/ClassNode", "bean");
        dh.addNewObjectRule("/ClassDiagramGraph/edges/AssociationEdge", "edge");

        dh.addObjectFillingRule("/ClassDiagramGraph/nodes/ClassNode/name/text", "bean@name");
        dh.addObjectFillingRule("/ClassDiagramGraph/nodes/ClassNode@id", "bean@umlid");
        dh.addObjectFillingRule("/ClassDiagramGraph/nodes/ClassNode/attributes/text", "bean@attributes");
        dh.addObjectFillingRule("/ClassDiagramGraph/nodes/ClassNode/methods/text", "bean@methods");
        dh.addObjectFillingRule("/ClassDiagramGraph/edges/AssociationEdge/start@reference", "edge@start");
        dh.addObjectFillingRule("/ClassDiagramGraph/edges/AssociationEdge/end@reference", "edge@end");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        parser.parse(inputStream, dh);

        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList objects = dh.getResults();
        if(!objects.isEmpty()) {
            Map all = (Map) objects.iterator().next();
            ArrayList beans = (ArrayList) all.get("beans");
            for (Iterator iterator = beans.iterator(); iterator.hasNext(); ) {
                Map bean = (Map) iterator.next();
                String name = (String) bean.get("name");
                bean.put("table_name", getDbName(name));
                bean.put("attributes", createAttributesFromString((String) bean.get("attributes"), name));
                bean.put("methods", createMethodsFromString((String) bean.get("methods"), name));
            }
            result.put("beans", beans);
            result.put("edges", all.get("edges"));
        }

        return result;
    }

    protected String readContent(String file)
    {
        String content = null;
        try
        {
            String wholeFile = readFile(file, Charset.forName("UTF-8"));

            content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + wholeFile.substring(wholeFile.indexOf(START_MARKER) + START_MARKER.length(), wholeFile.indexOf(END_MARKER));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return content;
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Please, specify two parameters - in violet model file and out json file");
            System.exit(0);
        }
        BasicVioletParser vp = new NewVioletParser();
        HashMap<String, Object> model = vp.parseVioletClass(args[0]);
        FileWriter fw = new FileWriter(args[1]);
        fw.write(JsonUtil.convertToJS(model));
        fw.close();
    }
}
