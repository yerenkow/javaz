package org.javaz.uml;

import org.javaz.util.GenericDeepComparator;
import org.javaz.util.JsonUtil;
import org.javaz.util.MapValueProducer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * This is helper to parse Violet UML .class files
 */
public class VioletParser
{
    public static String DATATYPE_FILENAME = "datatypes.properties";
    public static String TYPE_PREFIX = "type.";
    public static String SQL_PREFIX = "sql.";

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
        File file = new File(DATATYPE_FILENAME);
        if(file.exists()) {
            System.out.println("Found " + DATATYPE_FILENAME +", using...");
            try {
                Properties properties = new Properties();
                properties.load(new FileReader(file));

                Set<Object> set = properties.keySet();
                for (Iterator<Object> iterator = set.iterator(); iterator.hasNext(); ) {
                    String key = (String) iterator.next();
                    if(key.startsWith(TYPE_PREFIX)) {
                        BasicVioletParser.fullyQTypes.put(key.substring(TYPE_PREFIX.length()), properties.get(key));
                    }
                    if(key.startsWith(SQL_PREFIX)) {
                        BasicVioletParser.sqlTypes.put(key.substring(SQL_PREFIX.length()), properties.get(key));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Object> model = parseVioletModel(args[0]);

        ArrayList<Map> allBeans = (ArrayList<Map>) model.get("beans");


        GenericDeepComparator beansComparator = new GenericDeepComparator();
        beansComparator.setProducerI(new MapValueProducer("name"));

        Collections.sort(allBeans, beansComparator);

        for (Map bean : allBeans) {
            ArrayList<Map> attributes = (ArrayList<Map>) bean.get("attributes");

            GenericDeepComparator c = new GenericDeepComparator();
            c.setProducerI(new MapValueProducer("primary_key"));
            c.setInverted(true);

            GenericDeepComparator c2 = new GenericDeepComparator();
            c.setSecondarySort(c2);
            c2.setProducerI(new MapValueProducer("name"));

            Collections.sort(attributes, c);
        }

        FileWriter fw = new FileWriter(args[1]);
        fw.write(JsonUtil.convertToJS(model, true, true));
        fw.close();
    }
}
