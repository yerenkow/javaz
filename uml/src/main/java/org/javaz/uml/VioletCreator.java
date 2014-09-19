package org.javaz.uml;

import org.reflections.Reflections;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.tree.ReturnType;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by user on 05.05.2014.
 */
public class VioletCreator {
    public void saveViolet(String fileName, Collection<HashMap> toDraw) throws Exception {
        byte[] header = readResource("violet-header.html");
        byte[] footer = readResource("violet-footer.html");

        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(header);
        fos.write(convertClassesToViolet(toDraw));
        fos.write(footer);
    }

    private byte[] readResource(String name) throws Exception {
        DataInputStream stream = new DataInputStream(getClass().getClassLoader().getResourceAsStream(name));
        byte[] buffer = new byte[stream.available()];
        stream.readFully(buffer);
        return buffer;
    }

    //todo move hardcode const to properties
    //todo make this render via freemarker
    private byte[] convertClassesToViolet(Collection<HashMap> toDraw) {
        StringBuffer sb = new StringBuffer();

        int inColumn = 12;
        int perLetter = 7;
        int nameMargin = 16;
        int lettersMargin = 12;
        int startWidth = 20;
        int startHeight = 20;
        int curHeight = startHeight;
        int defHeight = 70;
        int perLineHeight = 16;

        int curWidth = startWidth;

        int nowH = 0;
        int id = 1;
        int bgId = -1;
        int borderId = -1;
        sb.append("<ClassDiagramGraph id=\"" + (id++) + "\">\n");
        sb.append("<nodes id=\"" + (id++) + " \">\n");
        ArrayList beanNames = new ArrayList();
        HashMap beanByNames = new HashMap();
        for (Iterator<HashMap> iterator = toDraw.iterator(); iterator.hasNext(); ) {
            HashMap next = iterator.next();
            String name = (String) next.get("name");
            beanNames.add(name);
            beanByNames.put(name, next);
        }

        Collections.sort(beanNames);

        int toAddWidth = 0;
        for (Iterator iterator = beanNames.iterator(); iterator.hasNext(); ) {
            String name = (String) iterator.next();
            HashMap next = (HashMap) beanByNames.get(name);
            String parent = "";
            String ifaces = "";
            int toAddHeight = 0;
            if(next.get("parent") != null) {
                    String parentClass = (String) next.get("parent");
                if(!parentClass.equals("Object")) {
                    parent = " extends " + parentClass;
                }
            }
            if(next.get("interfaces") != null) {
                String interfaces = (String) next.get("interfaces");
                if(interfaces.length() > 0) {
                    ifaces = " implements " + interfaces;
                }
            }

            ArrayList attributes = (ArrayList) next.get("attributes");
            if (toAddHeight < defHeight + attributes.size() * perLineHeight) {
                toAddHeight = defHeight + attributes.size() * perLineHeight;
            }
            sb.append("<ClassNode id=\"" + (id++) + "\">\n");
            sb.append("<children id=\"" + (id++) + "\" />\n");
            sb.append("<location class=\"Point2D.Double\" id=\"" + (id++) + "\" x=\"" +
                    curWidth + "\" y=\"" + curHeight + "\" />\n");

            sb.append("<id id=\"" + (id++) + "\" value=\"" + UUID.randomUUID() + "\" />\n");
            sb.append("<revision>1</revision>");
            if (bgId > 0) {
                sb.append("<backgroundColor reference=\"" + bgId + "\"/>\n");
            } else {
                bgId = (id++);
                sb.append("<backgroundColor id=\"" + bgId + "\" ><red>255</red><green>255</green><blue>255</blue><alpha>255</alpha></backgroundColor>\n");
            }
            if (borderId > 0) {
                sb.append("<borderColor reference=\"" + borderId + "\"/>\n");
            } else {
                borderId = (id++);
                sb.append("<borderColor id=\"" + borderId + "\" ><red>0</red><green>0</green><blue>0</blue><alpha>255</alpha></borderColor>\n");
            }
            sb.append("<textColor reference=\"" + borderId + "\"/>\n");

            sb.append("<name id=\"" + (id++) + "\" justification=\"1\" size=\"3\" underlined=\"false\">\n" +
                    "<text>" + name + parent + ifaces + "</text>\n" +
                    "</name>\n");
            if (toAddWidth < nameMargin + perLetter * (name.length())) {
                toAddWidth = nameMargin + perLetter * (name.length());
            }

            sb.append("<attributes id=\"" + (id++) + "\" justification=\"1\" size=\"3\" underlined=\"false\">\n" +
                    "<text>");
            ArrayList names = new ArrayList();
            HashMap typesByNames = new HashMap();
            for (Iterator iterator1 = attributes.iterator(); iterator1.hasNext(); ) {
                HashMap attr = (HashMap) iterator1.next();
                String attrName = (String) attr.get("name");
                if (attrName.equals("id")) {
                    sb.append(attrName).append(":").append(attr.get("type")).append("\n");
                } else {
                    names.add(attrName);
                    typesByNames.put(attrName, attr.get("type"));
                }
            }
            Collections.sort(names);
            for (Iterator iterator1 = names.iterator(); iterator1.hasNext(); ) {
                String attrName = (String) iterator1.next();
                String typeName = (String) typesByNames.get(attrName);
                sb.append(attrName).append(":").append(makeSafe(typeName)).append("\n");
                if (toAddWidth < lettersMargin + perLetter * (attrName.length() + typeName.length() + 1)) {
                    toAddWidth = lettersMargin + perLetter * (attrName.length() + typeName.length() + 1);
                }
            }
            curHeight += toAddHeight;
            if (nowH++ >= inColumn) {
                nowH = 0;
                curHeight = startHeight;
                curWidth += toAddWidth;
                toAddWidth = 0;
            }
            sb.append("</text>\n" + "</attributes>\n");
            sb.append("<methods id=\"" + (id++) + "\" justification=\"0\" size=\"4\" underlined=\"false\">\n" +
                    "<text></text>\n" +
                    "</methods>\n" +
                    "</ClassNode>\n");
        }
        sb.append("</nodes><edges></edges></ClassDiagramGraph>");
        return sb.toString().getBytes();
    }

    private String makeSafe(String typeName) {
        return typeName.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public static void main(String[] args) throws Exception {
        HashSet methodExceptions = new HashSet();
        methodExceptions.add("getClass");
        methodExceptions.add("getSerializer");
        methodExceptions.add("getDeserializer");
        methodExceptions.add("getTypeDesc");

        String methodPrefix = "exc.method.";
        String classPrefix = "exc.class.";
        if(args.length != 1) {
            System.out.println("Run with parameter properties file.");
            System.out.println("In properties file you should define such properties:");
            System.out.println("out.file=path to out violet file");
            System.out.println("package=to be parsed");
            System.out.println("parentNames=lava.lang.Object,java.io.Serializable");
            System.out.println(classPrefix + "*=(Name or Regexp of class to be omitted)");
            System.out.println(methodPrefix + "*=(Name or Regexp of method to be omiited)");
            System.out.println("Omitted methods by default = " + methodExceptions);
            System.exit(1);
        }
        Properties properties = new Properties();
        properties.load(new FileReader(args[0]));
        Set<String> strings = properties.stringPropertyNames();
        HashSet classExceptions = new HashSet();
        for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
            String name = iterator.next();
            if(name.startsWith(methodPrefix)) {
                methodExceptions.add(properties.getProperty(name));
            }
            if(name.startsWith(classPrefix)) {
                classExceptions.add(properties.getProperty(name));
            }
        }
        HashSet ignoreInterfaces = new HashSet();
        String ifc = properties.getProperty("ignore.interfaces");
        if(ifc != null) {
            String[] split = ifc.split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                ignoreInterfaces.add(s);
            }
        }
        VioletCreator creator = new VioletCreator();
        String fileName = properties.getProperty("out.file", "out.class.violet.html");
        String packageName = properties.getProperty("package", "lava.lang");
        String[] parentNames = properties.getProperty("parentNames", "lava.lang.Object,java.io.Serializable").split(",");
        ArrayList<HashMap> toDraw = creator.findAllClassesFromPackage(packageName, classExceptions, methodExceptions,
                ignoreInterfaces, parentNames);
        normalizeInheritance(toDraw);
        creator.saveViolet(fileName, toDraw);
    }

    private static void normalizeInheritance(ArrayList<HashMap> toDraw) {
        HashMap allClasses = new HashMap();
        for (Iterator<HashMap> iterator = toDraw.iterator(); iterator.hasNext(); ) {
            HashMap next = iterator.next();
            allClasses.put(next.get("name"), next);
        }

        for (Iterator<HashMap> iterator = toDraw.iterator(); iterator.hasNext(); ) {
            HashMap next = iterator.next();
            ArrayList attributes = (ArrayList) next.get("attributes");
            for (Iterator iterator1 = attributes.iterator(); iterator1.hasNext(); ) {
                Map attr = (Map) iterator1.next();
                boolean checkDeep = isThisAttributePresentInParent(allClasses, next.get("parent"), attr.get("name"));
                if(checkDeep) {
                    iterator1.remove();
                }
            }
        }
    }

    private static boolean isThisAttributePresentInParent(HashMap allClasses, Object className, Object attrName) {
        try {
            if(className == null || !allClasses.containsKey(className)) {
                return false;
            }
            Map next = (Map) allClasses.get(className);
            ArrayList attributes = (ArrayList) next.get("attributes");
            for (Iterator iterator1 = attributes.iterator(); iterator1.hasNext(); ) {
                Map attr = (Map) iterator1.next();
                if(attrName.equals(attr.get("name"))) {
                    return true;
                }
            }
            return isThisAttributePresentInParent(allClasses, next.get("parent"), attrName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private ArrayList<HashMap> findAllClassesFromPackage(String packageName, HashSet classExceptions,
                                                         HashSet methodExceptions, HashSet ignoreInterfaces,
                                                         String[] parentNames) {

        ArrayList<HashMap> list = new ArrayList<HashMap>();
        Reflections reflections = new Reflections(packageName);

        Set subTypes = new HashSet();
        for (int i = 0; i < parentNames.length; i++) {
            String parentName = parentNames[i];
            try {
                subTypes.addAll(reflections.getSubTypesOf(Class.forName(parentName)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Iterator iterator = subTypes.iterator(); iterator.hasNext(); ) {
            Class next = (Class) iterator.next();
            if (classExceptions.contains(next.getName())) {
                continue;
            }
            boolean foundMatch = false;
            for (Iterator iterator1 = classExceptions.iterator(); !foundMatch && iterator1.hasNext(); ) {
                String classExceptionRegex = (String) iterator1.next();
                if (next.getSimpleName().matches(classExceptionRegex)) {
                    foundMatch = true;
                }
            }
            if (foundMatch) {
                continue;
            }
            Method[] methods = next.getMethods();
            HashSet onceSeen = new HashSet();
            HashSet properties = new HashSet();
            HashMap propertiesTypes = new HashMap();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                if (methodExceptions.contains(name)) {
                    continue;
                }
                boolean getter = name.startsWith("get");
                boolean setter = name.startsWith("set");
                boolean getterBool = name.startsWith("is");

                if ((setter || getter) && name.length() > 3) {
                    String propName = name.substring(3);
                    if (onceSeen.contains(propName)) {
                        properties.add(propName);
                    }
                    onceSeen.add(propName);
                    if (getter) {
                        propertiesTypes.put(propName, getReturnType(packageName, method));
                    }
                }
                if (getterBool) {
                    String propName = name.substring(2);
                    if (onceSeen.contains(propName)) {
                        properties.add(propName);
                    }
                    onceSeen.add(propName);
                    propertiesTypes.put(propName, getReturnType(packageName, method));
                }
            }
            HashMap bean = new HashMap();
            bean.put("name", next.getSimpleName());
            bean.put("parent", next.getSuperclass().getSimpleName());
            Class[] interfaces = next.getInterfaces();
            StringBuffer ifaces = new StringBuffer();
            for (int i = 0; i < interfaces.length; i++) {
                Class anInterface = interfaces[i];
                if(!ignoreInterfaces.contains(anInterface.getSimpleName()) &&
                        !ignoreInterfaces.contains(anInterface.getCanonicalName())) {
                    ifaces.append(ifaces.length() == 0 ? "" : ", ").append(anInterface.getSimpleName());
                }
            }
            bean.put("interfaces", ifaces.toString());
            ArrayList beanAttributes = new ArrayList();
            bean.put("attributes", beanAttributes);
            for (Iterator iterator1 = properties.iterator(); iterator1.hasNext(); ) {
                String propName = (String) iterator1.next();
                HashMap attr = new HashMap();
                if(propName.length() > 1) {
                    attr.put("name", propName.substring(0, 1).toLowerCase() + propName.substring(1));
                } else {
                    attr.put("name", propName.toLowerCase());
                }
                String type = (String) propertiesTypes.get(propName);
                attr.put("type", type);
                beanAttributes.add(attr);
            }
            list.add(bean);
        }
        return list;
    }

    private String getReturnType(String thisPackage, Method method) {
        String type;
        Class<?> c = method.getReturnType();
        String cName = c.getName();
        if (cName.startsWith("[")) {
            type = c.getComponentType().getName() + "[]";
        } else {
            type = cName;
        }
        if (type.startsWith(thisPackage + ".")) {
            type = type.substring(thisPackage.length() + 1);
        }
        try {
            Field signature = method.getClass().getDeclaredField("signature");
            signature.setAccessible(true);
            String sig = (String) signature.get(method);
            if (sig != null) {
                MethodTypeSignature methodSig = SignatureParser.make().parseMethodSig(sig);
                ReturnType returnType = methodSig.getReturnType();
                if (returnType != null) {
                    Field path0 = returnType.getClass().getDeclaredField("path");
                    path0.setAccessible(true);
                    ArrayList path0List = (ArrayList) path0.get(returnType);
                    if (path0List != null && !path0List.isEmpty()) {
                        Object o = path0List.get(0);
                        if (o instanceof SimpleClassTypeSignature) {
                            SimpleClassTypeSignature ctSig = (SimpleClassTypeSignature) o;
                            String name = ctSig.getName();
                            TypeArgument[] typeArguments = ctSig.getTypeArguments();
                            if (typeArguments != null && typeArguments.length > 0) {
                                TypeArgument argument = typeArguments[0];
                                Field path1 = argument.getClass().getDeclaredField("path");
                                path1.setAccessible(true);
                                ArrayList path1List = (ArrayList) path1.get(argument);
                                if (path1List != null && !path1List.isEmpty()) {
                                    Object o2 = path1List.get(0);
                                    if (o2 instanceof SimpleClassTypeSignature) {
                                        SimpleClassTypeSignature paramSig = (SimpleClassTypeSignature) o2;
                                        String paramType = paramSig.getName();
                                        if (paramType.startsWith(thisPackage + ".")) {
                                            paramType = paramType.substring(thisPackage.length() + 1);
                                        }
                                        // at last
                                        type = name + "<" + paramType + ">";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // in any case we still have original type
            e.printStackTrace();
        }
        return type;
    }
}
