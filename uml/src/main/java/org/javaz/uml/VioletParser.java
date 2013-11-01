package org.javaz.uml;

import org.javaz.util.JsonUtil;

import java.io.FileWriter;
import java.util.HashMap;

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
        FileWriter fw = new FileWriter(args[1]);
        fw.write(JsonUtil.convertToJS(model));
        fw.close();
    }
}
