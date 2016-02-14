package org.javaz.test.xml;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.text.MessageFormat;
import java.util.*;

/**
 */
public class PropertiesCreator extends DefaultHandler
{
    private String firstTag = null;
    private String currentPath = "";
    private String placeholder0 = "[{0}]";

    public HashMap<String, Integer> tagCounts = new HashMap<String, Integer>();
    public HashSet<String> contentPathes = new HashSet<String>();

    @Test
    public void test1() throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        PropertiesCreator dh = new PropertiesCreator();
        parser.parse(
            "test.xml",
            dh);
    }


    @Override
    public void endDocument() throws SAXException
    {
        ArrayList<String> tagNames = new ArrayList<String>(tagCounts.keySet());
        Collections.sort(tagNames);
        ArrayList<String> reverseTagNames = new ArrayList<String>();
        for (String tagName : tagNames) {
            Integer cnt = tagCounts.get(tagName);
            if (cnt > 1) {
                tagName = tagName.replaceAll("\\[\\d+\\]", "");
                if (!reverseTagNames.contains(tagName)) {
                    reverseTagNames.add(tagName);
                }
            }
        }
        ArrayList<String> constsForClass = new ArrayList<String>();
        Collections.reverse(reverseTagNames);
        for (String tagName : tagNames) {
            Integer cnt = tagCounts.get(tagName);
            if (cnt > 1) {
                tagName = tagName.replaceAll("\\[\\d+\\]", "");
                String tagValue = properPath(tagName, reverseTagNames, false);
                if (!constsForClass.contains("COUNT_" + clear(underscorize(tagName)))) {
                    System.out.println(
                        "COUNT_" + clear(underscorize(tagName)) + "=count(" + tagValue + ")");
                    constsForClass.add("COUNT_" + clear(underscorize(tagName)));
                }
            }
        }

        for (String tagName : tagNames) {
            tagName = tagName.replaceAll("\\[\\d+\\]", "");
            boolean hasText = contentPathes.contains(tagName);
            if (hasText) {
                String tagValue = properPath(tagName, reverseTagNames, true);
                String cleanString = clear(underscorize(tagName));
                if (!constsForClass.contains(cleanString)) {
                    constsForClass.add(cleanString);
                    System.out.println(cleanString + "=" + tagValue + "/text()");
                }
            }
        }
        System.out.println("");

        for (String o : constsForClass) {
            System.out.println(o+",");
        }

    }


    private String properPath(String tagName, ArrayList<String> reverseTagNames, boolean includeFull)
    {
        String tagValue = tagName;
        for (String reverseTagName : reverseTagNames) {
            if (tagValue.contains(reverseTagName)
                //&& !tagValue.equalsIgnoreCase(reverseTagName)
                ) {
                if((includeFull && tagValue.equals(reverseTagName)) ||
                    (tagValue.length() > reverseTagName.length() && tagValue.charAt(reverseTagName.length()) == '/')) {
                    tagValue = tagValue.replace(reverseTagName, reverseTagName + placeholder0);
                }
            }
        }
        int index = tagValue.indexOf(placeholder0);
        int next = 1;
        while (tagValue.indexOf(placeholder0, index + 1) != -1) {
            int pos = tagValue.indexOf(placeholder0, index + 1);
            tagValue = tagValue.substring(0, pos) +
                MessageFormat.format(placeholder0, "{" + next++ + "}") +
                tagValue.substring(pos + placeholder0.length());
        }
        return tagValue;
    }


    private String clear(String underscorize)
    {
        String cleanString = underscorize.replace("/", "").replace("(",
            "").replace(")", "").replace("[{0}]", "")
            .replace(underscorize(firstTag), "");
        if (cleanString.startsWith("_")) {
            return cleanString.substring(1);
        }
        return cleanString;

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        setCurrentPath(qName);
    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (new String(ch, start, length).trim().length() > 0)
        {
            contentPathes.add(currentPath.replaceAll("\\[\\d+\\]", ""));
        }
        super.characters(ch, start, length);
    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/" + qName));
    }


    private String underscorize(String last)
    {
        StringBuffer stringBuffer = new StringBuffer();
        char[] chars = last.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if(Character.isUpperCase(aChar)) {
                stringBuffer.append("_");
            }
            stringBuffer.append(Character.toUpperCase(aChar));
        }
        return stringBuffer.toString();
    }


    public void setCurrentPath(String qName)
    {
        if (currentPath.length() == 0) {
            firstTag = qName;
        }
        currentPath += "/" + qName;
        int currentIndex = 1;
        if (tagCounts.containsKey(currentPath)) {
            currentIndex = 1 + tagCounts.get(currentPath);
        }
        tagCounts.put(currentPath, currentIndex);
        currentPath += "[" + currentIndex + "]";
    }
}
