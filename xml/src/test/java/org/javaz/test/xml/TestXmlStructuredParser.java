package org.javaz.test.xml;

import junit.framework.Assert;
import org.javaz.util.JsonUtil;
import org.javaz.xml.XpathSaxHandler;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Created by user on 21.01.14.
 */
public class TestXmlStructuredParser
{
    @Test
    public void runXmlTest() throws Exception
    {

        String sampleDataInJson = "[{alls1:[{id:1,tc:[{id:3},{id:4}],tcids:[3,4]},{id:5,tc:[{id:7},{id:8}],tcids:[7,8]},{id:9,tc:[{id:B},{id:C},{id:D}],tcids:[B,C,D]}],alltc:[{id:3},{id:4},{id:7},{id:8},{id:B},{id:C},{id:D}]}]";
        StringBuffer s = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>");
        s.append("<start>");

        s.append("<section1>");
        s.append("<tagA id='1'>");
        s.append("<tagB id='2'>");
        s.append("<tagC id='3'></tagC>");
        s.append("<tagC id='4'></tagC>");
        s.append("</tagB>");
        s.append("</tagA>");
        s.append("</section1>");

        s.append("<section1>");
        s.append("<tagA id='5'>");
        s.append("<tagB id='6'>");
        s.append("<tagC id='7'></tagC>");
        s.append("<tagC id='8'></tagC>");
        s.append("</tagB>");
        s.append("</tagA>");
        s.append("</section1>");

        s.append("<section1>");
        s.append("<tagA id='9'>");
        s.append("<tagB id='A'>");
        s.append("<tagC id='B'></tagC>");
        s.append("<tagC id='C'></tagC>");
        s.append("<tagC id='D'></tagC>");
        s.append("</tagB>");
        s.append("</tagA>");
        s.append("</section1>");

        s.append("</start>");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(s.toString().getBytes());
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        XpathSaxHandler dh = new XpathSaxHandler();

        dh.addHashToHashFillingRule("allsections", XpathSaxHandler.RESULTS);
        dh.addHashToHashFillingRule("resultInJson", "allsections@alls1,list");
        dh.addHashToHashFillingRule("tc", "resultInJson@tc,list");
        dh.addHashToHashFillingRule("tc", "allsections@alltc,list");

        dh.addNewObjectRule("/start", "allsections");
        dh.addNewObjectRule("/start/section1", "resultInJson");
        dh.addNewObjectRule("/start/section1/tagA/tagB/tagC", "tc");

        dh.addObjectFillingRule("/start/section1/tagA@id", "resultInJson@id");
        dh.addObjectFillingRule("/start/section1/tagA/tagB/tagC@id", "resultInJson@tcids,list");
        dh.addObjectFillingRule("/start/section1/tagA/tagB/tagC@id", "tc@id");

        long l = System.currentTimeMillis();
        parser.parse(inputStream, dh);

        ArrayList objects = dh.getResults();

        String resultInJson = JsonUtil.convertToJS(objects, false, true).replace("\"", "");

        Assert.assertEquals(1, objects.size());
        Object o1 = objects.get(0);
        Assert.assertTrue(o1 instanceof HashMap);

        HashMap allsections = (HashMap) o1;

        Assert.assertEquals(2, allsections.size());

        Assert.assertTrue(allsections.containsKey("alls1"));
        Assert.assertTrue(allsections.containsKey("alltc"));

        Assert.assertTrue(allsections.get("alls1") instanceof ArrayList);
        Assert.assertTrue(allsections.get("alltc") instanceof ArrayList);

        Assert.assertEquals(sampleDataInJson, resultInJson);
    }
}
