package org.javaz.jdbc.util;

import java.util.Map;

/**
 * This should replace Object[] in JDBC logic.
 */
public class StringMapPair {

    private String string;
    private Map<Integer, Object> map;

    public StringMapPair(String string, Map<Integer, Object> map) {
        this.string = string;
        this.map = map;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Map<Integer, Object> getMap() {
        return map;
    }

    public void setMap(Map<Integer, Object> map) {
        this.map = map;
    }
}
