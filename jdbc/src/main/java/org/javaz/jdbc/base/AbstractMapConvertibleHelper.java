package org.javaz.jdbc.base;

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class helps make work with DB easier
 */
public abstract class AbstractMapConvertibleHelper<T extends MapConvertibleI> {
    private String idName;
    private String tableName;

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Object[] getDbUpdateQuery(T obj) {
        return getDbUpdateQuery(tableName, obj, false);
    }
    public Object[] getDbUpdateQuery(T obj, boolean forceInsert) {
        return getDbUpdateQuery(tableName, obj, forceInsert);
    }
    public Object[] getDbUpdateQuery(String tableName, T obj) {
        return getDbUpdateQuery(tableName, obj, false );
    }

    public Object[] getDbDeleteQuery(T obj) {
        return getDbDeleteQuery(tableName, obj);
    }

    public Object[] getDbDeleteQuery(String tableName, T obj) {
        HashMap<Integer, Object> params = new HashMap<Integer, Object>();
        params.put(params.size() + 1, obj.getPrimaryKey());
        return new Object[] {"delete from " + tableName + " where " + idName + " = ?", params } ;
    }

    public abstract Object[] getDbUpdateQuery(String tableName, T obj, boolean forceInsert);
    public abstract T buildFromMap(Map h);
}
