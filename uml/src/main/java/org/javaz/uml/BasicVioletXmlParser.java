package org.javaz.uml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is helper to parse Violet UML .class files
 */
public abstract class BasicVioletXmlParser extends BasicVioletParser
{
    public HashMap<String, Object> parseVioletClass(String file)
    {
        HashMap<String, Object> result = new HashMap<String, Object>();

        ArrayList<Map> allBeans = new ArrayList<Map>();
        result.put("beans", allBeans);

        try
        {
            String readContent = readContent(file);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new BufferedInputStream(new ByteArrayInputStream(readContent.getBytes())));

            parseDocument(document, allBeans);
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

    protected abstract void parseDocument(Document document, ArrayList<Map> allBeans);

    protected abstract String readContent(String file);
}
