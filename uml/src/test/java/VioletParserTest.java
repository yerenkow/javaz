import junit.framework.Assert;
import net.sf.json.JSONObject;
import org.javaz.uml.BasicVioletParser;
import org.javaz.uml.NewVioletParser;
import org.javaz.uml.VioletDiffer;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class VioletParserTest
{
    @Test
    public void testParser() throws Exception
    {
        BasicVioletParser vp = new NewVioletParser();
        File test = File.createTempFile("class.violet.html", "test");
        test.deleteOnExit();
        InputStream resourceAsStream = getClass().getResourceAsStream("test.class.violet.html");
        FileOutputStream fileOutputStream = new FileOutputStream(test);
        while(resourceAsStream.available() > 0) {
            fileOutputStream.write(resourceAsStream.read());
        }
        fileOutputStream.close();
        HashMap<String, Object> stringObjectHashMap = vp.parseVioletClass(test.getAbsolutePath());
        List beans = (List) stringObjectHashMap.get("beans");
        Assert.assertEquals(beans.size(), 3);
        for (Iterator iterator = beans.iterator(); iterator.hasNext(); ) {
            Map bean = (Map) iterator.next();
            Assert.assertEquals(((List) bean.get("attributes")).size(), 3);
            Assert.assertEquals(((List) bean.get("methods")).size(), 0);
        }
    }

    @Test
    public void testDiffer() throws Exception
    {
        String oldModel = "{\"beans\":[{\"table_name\":\"same_object\",\"name\":\"SameObject\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"object_changed_added\",\"name\":\"ObjectChangedAdded\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"deleted_object\",\"name\":\"DeletedObject\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"object_changed_modified\",\"name\":\"ObjectChangedModified\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"object_changed_deleted\",\"name\":\"ObjectChangedDeleted\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                "]}";
        String newModel = "{\"beans\":[{\"table_name\":\"same_object\",\"name\":\"SameObject\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"object_changed_added\",\"name\":\"ObjectChangedAdded\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"c\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"c\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"new_object\",\"name\":\"NewObject\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"object_changed_modified\",\"name\":\"ObjectChangedModified\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"integer\",\"name\":\"a\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.Integer\",\"column_name\":\"a\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                ",{\"table_name\":\"object_changed_deleted\",\"name\":\"ObjectChangedDeleted\",\"attributes\":[{\"sql_type\":\"integer\",\"name\":\"id\",\"length\":\"250\",\"primary_key\":\"true\",\"type\":\"java.lang.Integer\",\"column_name\":\"id\"}\n" +
                ",{\"sql_type\":\"character varying(250)\",\"name\":\"b\",\"length\":\"250\",\"primary_key\":\"false\",\"type\":\"java.lang.String\",\"column_name\":\"b\"}\n" +
                "]}\n" +
                "]}";

        JSONObject a = JSONObject.fromObject(newModel);
        JSONObject b = JSONObject.fromObject(oldModel);

        VioletDiffer violetDiffer = new VioletDiffer(a, b);
        violetDiffer.calculateDifference();
        Assert.assertEquals(violetDiffer.getNewBeans().size(), 1);
        Assert.assertEquals(violetDiffer.getDeletedBeans().size(), 1);
        Assert.assertEquals(violetDiffer.getAlteredBeansDeletedAttribute().size(), 1);
        Assert.assertEquals(violetDiffer.getAlteredBeansModifyAttribute().size(), 1);
        Assert.assertEquals(violetDiffer.getAlteredBeansNewAttribute().size(), 1);
    }
}
