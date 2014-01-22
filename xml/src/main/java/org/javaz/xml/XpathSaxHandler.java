package org.javaz.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Created by user on 21.01.14.
 */
public class XpathSaxHandler extends DefaultHandler
{
    public static final String RESULTS = "RESULTS";
    public static final String LIST = "LIST";
    private String currentPath = "";

    private ArrayList<HashMap<String, String>> newObjectRules = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> objectFillingRules = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> hashToHashFillingRules = new ArrayList<HashMap<String, String>>();

    private HashMap<String, Object> hashesByName = new HashMap();
    private HashMap<String, StringBuffer> contentsByName = new HashMap();

    private ArrayList results = new ArrayList();

    public ArrayList getResults() {
        return results;
    }

    public ArrayList<HashMap<String, String>> getNewObjectRules() {
        return newObjectRules;
    }

    public ArrayList<HashMap<String, String>> getObjectFillingRules() {
        return objectFillingRules;
    }

    public ArrayList<HashMap<String, String>> getHashToHashFillingRules() {
        return hashToHashFillingRules;
    }

    public void addNewObjectRule(String xpath, String hashName)  throws Exception {
        validateHashName(hashName);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(xpath, hashName);
        newObjectRules.add(map);
    }

    public void addObjectFillingRule(String xpath, String rule)  throws Exception {
        validateRule(rule);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(xpath, rule);
        objectFillingRules.add(map);
    }

    public void addHashToHashFillingRule(String name, String rule) throws Exception {
        validateHashTarget(rule);
        validateRule(rule);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(name, rule);
        hashToHashFillingRules.add(map);
    }

    private void validateRule(String rule) throws Exception {
        if(rule.equals(RESULTS)) {
            return;
        }
        String[] split = rule.split("@");
        String where = split[1];
        if(where.trim().isEmpty()) {
            throw new Exception("Key in target HashMap can't be empty. Hash@key, mymap@ids,list - valid examples, while '" + rule + "' - is not.");
        }
        if(where.contains(",")) {
            String[] strings = where.split(",");
            if(strings[0].trim().isEmpty()) {
                throw new Exception("Key in target HashMap can't be empty. Hash@key, mymap@id,list - valid examples, while '" + rule + "' - is not.");
            }
            String type = strings[1];
            if(!type.equalsIgnoreCase(LIST)) {
                throw new Exception("Ending of your rule '"+type+"' isn't supported, currently only " + LIST + " is supported.");
            }
        }
    }

    private void validateHashName(String hashName) throws Exception {
        ArrayList knownHashNames = new ArrayList();
        for (Iterator<HashMap<String, String>> stringIterator = hashToHashFillingRules.iterator(); stringIterator.hasNext(); ) {
            HashMap<String, String> hashToHash = stringIterator.next();
            Set<String> strings = hashToHash.keySet();
            for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
                String knownHashName = iterator.next();
                if(hashName.equals(knownHashName)) {
                    return;
                }
                knownHashNames.add(knownHashName);
            }
        }
        knownHashNames.add(RESULTS);
        Collections.sort(knownHashNames);
        throw new Exception("Unknown target hash name: '" + hashName + "'. The only known hash names at this time is: " + knownHashNames);
    }

    private void validateHashTarget(String rule) throws Exception {
        if(rule.equals(RESULTS)) {
            return;
        }
        String[] split = rule.split("@");
        String hashName = split[0];
        validateHashName(hashName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        handleElementsContentOrAttributes(null, ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        currentPath += "/" + qName;
        handleElementStart(attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        handleElementEnd();
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/" + qName));
    }

    private void handleElementStart(Attributes attributes) {
        for (Iterator<HashMap<String, String>> iteratorOuter = newObjectRules.iterator(); iteratorOuter.hasNext(); ) {
            HashMap<String, String> next = iteratorOuter.next();

            Set<String> strings = next.keySet();
            for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
                String newObjectPath = iterator.next();
                if(currentPath.equals(newObjectPath)) {
                    String hashName = next.get(newObjectPath);
                    HashMap map = new HashMap();
                    hashesByName.put(hashName, map);
                }
            }


        }
        handleElementsContentOrAttributes(attributes, null, 0, 0);
    }

    private void handleElementsContentOrAttributes(Attributes attributes, char[] ch, int start, int length) {

        for (Iterator<HashMap<String, String>> iteratorOuter = objectFillingRules.iterator(); iteratorOuter.hasNext(); ) {
            HashMap<String, String> next = iteratorOuter.next();

            Set<String> strings = next.keySet();
            for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
                String pathAndAttribute = iterator.next();

                if(pathAndAttribute.equals(currentPath)) {
                    // so, we are getting content of tags, if this tag start (e.g. Attributes != null)
                    if(attributes != null) {
                        contentsByName.put(currentPath, new StringBuffer());
                    } else {
                        // if attributes == null, then we in characters mode.
                        // get according StringBuffer and append to it
                        contentsByName.get(currentPath).append(ch, start, length);
                    }
                }

                if(pathAndAttribute.startsWith(currentPath + "@") && attributes != null) {
                    //if we are in Attributes mode, and we some from this tag, let's extract it.
                    String where = next.get(pathAndAttribute);
                    String[] split = pathAndAttribute.split("@");
                    String attributeName = split[1];
                    String value = attributes.getValue(attributeName);
                    if(value != null) {
                        addValueToHashMap(where, value);
                    }
                }

                if(ch != null && length > 0) {
                }
            }
        }
    }

    private void handleElementEnd() {
        for (Iterator<HashMap<String, String>> iteratorOuter = newObjectRules.iterator(); iteratorOuter.hasNext(); ) {
            HashMap<String, String> next = iteratorOuter.next();

            Set<String> strings = next.keySet();
            for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
                String newObjectPath = iterator.next();
                if(currentPath.equals(newObjectPath)) {
                    String hashName = next.get(newObjectPath);
                    Object o = hashesByName.remove(hashName);

                    for (Iterator<HashMap<String, String>> stringIterator = hashToHashFillingRules.iterator(); stringIterator.hasNext(); ) {
                        HashMap<String, String> hashToHash = stringIterator.next();
                        if(hashToHash.containsKey(hashName)) {
                            String where = hashToHash.get(hashName);
                            addValueToHashMap(where, o);
                        }
                    }
                }
            }
        }

        for (Iterator<HashMap<String, String>> iteratorOuter = objectFillingRules.iterator(); iteratorOuter.hasNext(); ) {
            HashMap<String, String> next = iteratorOuter.next();

            Set<String> strings = next.keySet();
            for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
                String pathAndAttribute = iterator.next();

                if(pathAndAttribute.equals(currentPath)) {
                    // we need to get content of tag, and put it somewhere.
                    // GET! Not REMOVE, since we could need this Value in multiple places
                    StringBuffer buffer = contentsByName.get(currentPath);
                    String where = next.get(pathAndAttribute);
                    addValueToHashMap(where, buffer.toString());
                }
            }
        }
        //we remove StringBuffer ONLY after all iterations
        contentsByName.remove(currentPath);
    }

    private void addValueToHashMap(String where, Object value) {
        if(where.equals(RESULTS)) {
            results.add(value);
            return;
        }
        String[] split = where.split("@");
        String hashName = split[0];
        String key = split[1];

        HashMap map = (HashMap) hashesByName.get(hashName);
        if(map != null) {
            if(key.toUpperCase().endsWith("," + LIST)) {
                //list handling
                key = key.split(",")[0];
                if(!map.containsKey(key)) {
                    map.put(key, new ArrayList());
                }
                ((ArrayList) map.get(key)).add(value);
            }
            else {
                map.put(key, value);
            }
        }

    }
}
