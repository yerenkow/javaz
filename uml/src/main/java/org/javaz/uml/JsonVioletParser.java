package org.javaz.uml;

import net.sf.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;

/**
 */
public class JsonVioletParser extends BasicVioletParser
{
    @Override
    public HashMap<String, Object> parseVioletClass(String file)
    {
        StringBuilder buffer = new StringBuilder();
        try
        {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            String line = null;
            while ((line = lnr.readLine()) != null)
            {
                buffer.append(line);
            }
            return new HashMap(JSONObject.fromObject(buffer.toString()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
