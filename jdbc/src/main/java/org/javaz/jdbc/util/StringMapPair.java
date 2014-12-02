package org.javaz.jdbc.util;

import java.util.Map;

/**
 * This should replace Object[] in JDBC logic.
 */
public class StringMapPair {

    private String string;
    private Map map;

    public StringMapPair(String string, Map map) {
        this.string = string;
        this.map = map;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
