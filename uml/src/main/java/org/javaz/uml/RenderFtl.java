package org.javaz.uml;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;

import java.io.*;
import java.util.*;

/**
 */
public class RenderFtl
{
    public static final int RENDER_WHOLE_NEW_BEAN = 1;
    public static final int RENDER_NEW_BEAN_BY_ONE = 2;
    public static final int RENDER_DIFFERENCE = 4;

    private HashMap oldModel = new HashMap();
    private HashMap newModel = new HashMap();
    private String template = "none";
    private String templatePath = "templates";
    private String outPath = "out";
    private int parseType = RENDER_WHOLE_NEW_BEAN;
    private HashMap additionalValues = new HashMap();

    public RenderFtl(HashMap newModel)
    {
        this.newModel = newModel;
    }

    public RenderFtl(HashMap oldModel, HashMap newModel)
    {
        this.oldModel = oldModel;
        this.newModel = newModel;
    }

    public HashMap getOldModel()
    {
        return oldModel;
    }

    public void setOldModel(HashMap oldModel)
    {
        this.oldModel = oldModel;
    }

    public HashMap getNewModel()
    {
        return newModel;
    }

    public void setNewModel(HashMap newModel)
    {
        this.newModel = newModel;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public String getTemplatePath()
    {
        return templatePath;
    }

    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }

    public String getOutPath()
    {
        return outPath;
    }

    public void setOutPath(String outPath)
    {
        this.outPath = outPath;
    }

    public int getParseType()
    {
        return parseType;
    }

    public void setParseType(int parseType)
    {
        this.parseType = parseType;
    }

    public HashMap getAdditionalValues()
    {
        return additionalValues;
    }

    public void renderTemplate()
    {
        try
        {
            new File(outPath).mkdirs();
            SimpleHash simpleHash = new SimpleHash();
            simpleHash.putAll(additionalValues);

            if (parseType == RENDER_WHOLE_NEW_BEAN)
            {
                simpleHash.put("beans", newModel.get("beans"));
                StringWriter writer = new StringWriter();
                proceedTemplate(simpleHash, templatePath + (templatePath.endsWith("/") ? "" : "/") + template + "-name.ftl", writer);
                String fileName = writer.getBuffer().toString().trim();
                File fileOut = new File(outPath, fileName);
                fileOut.getParentFile().mkdirs();
                proceedTemplate(simpleHash, templatePath + (templatePath.endsWith("/") ? "" : "/") + template + ".ftl", fileOut);
            }
            if (parseType == RENDER_NEW_BEAN_BY_ONE)
            {
                Iterable beans = (Iterable) newModel.get("beans");
                for (java.util.Iterator iterator = beans.iterator(); iterator.hasNext(); )
                {
                    Object bean = iterator.next();
                    simpleHash.put("bean", bean);
                    StringWriter writer = new StringWriter();
                    proceedTemplate(simpleHash, templatePath + (templatePath.endsWith("/") ? "" : "/") + template + "-name.ftl", writer);
                    String fileName = writer.getBuffer().toString().trim();
                    File fileOut = new File(outPath, fileName);
                    fileOut.getParentFile().mkdirs();
                    proceedTemplate(simpleHash, templatePath + (templatePath.endsWith("/") ? "" : "/") + template + ".ftl", fileOut);
                }
            }
            if (parseType == RENDER_DIFFERENCE)
            {
                trimNames(newModel);
                trimNames(oldModel);
                VioletDiffer diffCalculator = new VioletDiffer(newModel, oldModel);
                diffCalculator.calculateDifference();
                ArrayList newBeans = diffCalculator.getNewBeans();
                ArrayList deletedBeans = diffCalculator.getDeletedBeans();
                ArrayList alteredBeansNewAttribute = diffCalculator.getAlteredBeansNewAttribute();
                ArrayList alteredBeansModifyAttribute = diffCalculator.getAlteredBeansModifyAttribute();
                ArrayList alteredBeansDeletedAttribute = diffCalculator.getAlteredBeansDeletedAttribute();
                restoreNames(newBeans, deletedBeans, alteredBeansNewAttribute, alteredBeansModifyAttribute, alteredBeansDeletedAttribute);

                simpleHash.put("beans", newBeans);
                simpleHash.put("deletedBeans", deletedBeans);
                simpleHash.put("alteredBeansNewAttribute", alteredBeansNewAttribute);
                simpleHash.put("alteredBeansModifyAttribute", alteredBeansModifyAttribute);
                simpleHash.put("alteredBeansDeletedAttribute", alteredBeansDeletedAttribute);
                if (!additionalValues.containsKey("v1"))
                    simpleHash.put("v1", "old");
                if (!additionalValues.containsKey("v2"))
                    simpleHash.put("v2", "new");

                StringWriter writer = new StringWriter();
                proceedTemplate(simpleHash, templatePath + (templatePath.endsWith("/") ? "" : "/") + template + "-name.ftl", writer);
                String fileName = writer.getBuffer().toString().trim();
                File fileOut = new File(outPath, fileName);
                fileOut.getParentFile().mkdirs();
                proceedTemplate(simpleHash, templatePath + (templatePath.endsWith("/") ? "" : "/") + template + ".ftl", fileOut);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void restoreNames(ArrayList newBeans, ArrayList deletedBeans,
        ArrayList alteredBeansNewAttribute, ArrayList alteredBeansModifyAttribute,
        ArrayList alteredBeansDeletedAttribute) {
        ArrayList[] allOfThem = new ArrayList[] {newBeans, deletedBeans,alteredBeansNewAttribute,
            alteredBeansModifyAttribute,alteredBeansDeletedAttribute };
        for (int i = 0; i < allOfThem.length; i++) {
            ArrayList beans = allOfThem[i];
            for (Iterator iterator = beans.iterator(); iterator.hasNext(); ) {
                Map bean = (Map)  iterator.next();
                Object originalName = bean.remove("originalName");
                if(originalName != null) {
                    bean.put("name", originalName);
                }
            }

        }
    }

    private void trimNames(HashMap model) {
        List<Map> beans = (List<Map>) model.get("beans");
        for (Iterator<Map> iterator = beans.iterator(); iterator.hasNext(); ) {
            Map bean = iterator.next();
            if(bean.containsKey("name")) {
                String name = (String) bean.get("name");
                if(name.contains(" ")) {
                    bean.put("originalName", name);
                    bean.put("name", name.substring(0, name.indexOf(" ")));
                }
            }
        }
    }

    public static void proceedTemplate(SimpleHash simpleHash, String template, File fileOut)
    {
        try
        {
            proceedTemplate(simpleHash, template, new OutputStreamWriter(new FileOutputStream(fileOut)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void proceedTemplate(SimpleHash simpleHash, String template, Writer output)
    {
        try
        {
            Configuration cfg = new Configuration();
            File topParent = new File(template).getCanonicalFile();
            if (topParent.getParentFile() != null)
            {
                topParent = topParent.getParentFile();
            }
            cfg.setDirectoryForTemplateLoading(topParent);
            //very ugly hack due to dumb freemarker!!!
            Template tpl = cfg.getTemplate(new File(template).getCanonicalPath().substring(topParent.getCanonicalPath().length()));
            tpl.process(simpleHash, output);
            output.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.out.println("To use it, you need specify at least three parameters:");
            System.out.println("in-file template RENDER_TYPE");
            System.out.println("where RENDER_TYPE:");
            System.out.println("\t1\trender whole model");
            System.out.println("\t2\trender each model bean in it's own file");
            System.out.println("Or you can specify 4 parameters:");
            System.out.println("in-file template 4 old-in-file");
            System.out.println("where RENDER_TYPE:");
            System.out.println("\t4\trender models difference.");
            System.out.println("\tTo pass other parameters into template engine use parameters as -Dkey=value");
            System.out.println("\tExamples: -DtemplatePath=other-template-path");
            System.out.println("\t\t-DoutPath=other-out-path");
            System.exit(0);
        }

        String fileInNew = args[0];
        String template = args[1];
        String parseTypeString = args[2];
        String fileInOld = null;
        if (args.length >= 4 && !args[3].startsWith("-D"))
        {
            fileInOld = args[3];
        }

        int parseType = 0;
        try
        {
            parseType = Integer.parseInt(parseTypeString);
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        if (parseType != RENDER_WHOLE_NEW_BEAN && parseType != RENDER_NEW_BEAN_BY_ONE && parseType != RENDER_DIFFERENCE)
        {
            System.out.println("Unknown render type = " + parseTypeString);
            System.out.println("Must be one of following: 1,2 or 4");
            System.exit(0);
        }

        if (parseType == RENDER_DIFFERENCE && fileInOld == null)
        {
            System.out.println("To use difference, please specify old-model in fourth parameter");
            System.exit(0);
        }

        HashMap newModel = VioletParser.parseVioletModel(fileInNew);

        HashMap oldModel = new HashMap();
        if (parseType == RENDER_DIFFERENCE)
        {
            oldModel = VioletParser.parseVioletModel(fileInOld);
        }

        RenderFtl renderFtl = new RenderFtl(oldModel, newModel);
        renderFtl.setParseType(parseType);
        renderFtl.setTemplate(template);

        HashMap additionalValues = new HashMap();
        for (int j = 3; j < args.length; j++)
        {
            String arg = args[j];
            if (arg.startsWith("-D"))
            {
                String[] strings = arg.substring(2).split("=");
                if (strings.length == 2)
                {
                    if (strings[0].equalsIgnoreCase("templatePath"))
                    {
                        renderFtl.setTemplatePath(strings[1]);
                    }
                    else if (strings[0].equalsIgnoreCase("outPath"))
                    {
                        renderFtl.setOutPath(strings[1]);
                    }
                    else
                    {
                        additionalValues.put(strings[0], strings[1]);
                    }
                }
            }
        }


        renderFtl.getAdditionalValues().putAll(additionalValues);

        renderFtl.renderTemplate();
    }
}
