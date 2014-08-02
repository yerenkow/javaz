package org.javaz.jdbc.base;

import org.javaz.jdbc.util.JdbcCachedHelper;

import java.util.*;

/**
 * Basic DB work, based on MapConvertibleI &amp; AbstractMapConvertibleHelper
 */
public class GenericMapConvertibleDAO {
    private String databaseAddress;

    public GenericMapConvertibleDAO(String databaseAddress) {
        this.databaseAddress = databaseAddress;
    }

    public String getDatabaseAddress() {
        return databaseAddress;
    }

    public void setDatabaseAddress(String databaseAddress) {
        this.databaseAddress = databaseAddress;
    }

    private <T extends MapConvertibleI> void internalSave(T obj, AbstractMapConvertibleHelper<T> builder, boolean forceInsert) throws Exception {
        boolean idIsMissing = obj.getPrimaryKey() == null;
        Object[] update = builder.getDbUpdateQuery(obj, forceInsert);
        long newId = JdbcCachedHelper.getInstance(databaseAddress).runUpdate((String) update[0], (Map) update[1]);
        if (idIsMissing) {
            obj.setGeneratedPrimaryKey(newId);
        }
    }

    public <T extends MapConvertibleI> void add(T obj, AbstractMapConvertibleHelper<T> builder) throws Exception {
        internalSave(obj, builder, !(obj.getPrimaryKey() == null));
    }

    public <T extends MapConvertibleI> void saveOrUpdate(T obj, AbstractMapConvertibleHelper<T> builder) throws Exception {
        internalSave(obj, builder, false);
    }

    public <T extends MapConvertibleI> List<T> all(AbstractMapConvertibleHelper<T> builder) throws Exception {
        return findByConditions(null, null, builder);
    }

    public <T extends MapConvertibleI> T findById(Comparable id, AbstractMapConvertibleHelper<T> builder) throws Exception {
        HashMap<Integer, Object> params = new HashMap<Integer, Object>();
        params.put(params.size() + 1, id);
        List<T> objects = findByConditions("where " + builder.getIdName() + " = ?", params, builder);
        if (objects != null && !objects.isEmpty()) {
            return objects.iterator().next();
        }
        return null;
    }

    public <T extends MapConvertibleI> List<T> findByConditions(String where, Map<Integer, Object> objects, AbstractMapConvertibleHelper<T> builder) {
        return findByFullQuery("select * from " + builder.getTableName() + " " + (where != null ? where : ""), objects, builder);
    }

    public <T extends MapConvertibleI> List<T> findByFullQuery(String query, Map<Integer, Object> objects, AbstractMapConvertibleHelper<T> builder) {
        List mapList = JdbcCachedHelper.getInstance(databaseAddress).getRecordList(query, objects, false);
        ArrayList<T> list = new ArrayList<T>();
        for (Object aMapList : mapList) {
            Map map = (Map) aMapList;
            list.add(builder.buildFromMap(map));
        }
        return list;
    }

    public <T extends MapConvertibleI> void delete(T object, AbstractMapConvertibleHelper<T> builder) throws Exception {
        Object[] delete = builder.getDbDeleteQuery(object);
        JdbcCachedHelper.getInstance(databaseAddress).runUpdateDataIgnore((String) delete[0], (Map) delete[1]);
    }
}
