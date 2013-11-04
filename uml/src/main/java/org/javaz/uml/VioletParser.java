package org.javaz.uml;

import org.javaz.util.GenericDeepComparator;
import org.javaz.util.JsonUtil;
import org.javaz.util.MapValueProducer;

import java.io.FileWriter;
import java.util.*;

/**
 * This is helper to parse Violet UML .class files
 */
public class VioletParser
{

    public static HashMap parseVioletModel(String fileInNew)
    {
        BasicVioletParser vp = null;
        if (fileInNew.endsWith(".json"))
        {
            vp = new JsonVioletParser();
        }
        else if (fileInNew.endsWith(".html"))
        {
            vp = new NewVioletParser();
        }
        else
        {
            vp = new OldVioletParser();
        }
        return vp.parseVioletClass(fileInNew);
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Please, specify two parameters - in violet model file and out json file");
            System.exit(0);
        }

        HashMap<String, Object> model = parseVioletModel(args[0]);

        ArrayList<Map> allBeans = (ArrayList<Map>) model.get("beans");
        GenericDeepComparator c = new GenericDeepComparator();
        c.setProducerI(new MapValueProducer("name"));
        Collections.sort(allBeans, c);
        for (Map bean : allBeans) {
            ArrayList<Map> attributes = (ArrayList<Map>) bean.get("attributes");
            Collections.sort(attributes, c);
        }

        FileWriter fw = new FileWriter(args[1]);
        fw.write(JsonUtil.convertToJS(model, true, true));
        fw.close();
    }
}
