<#if bean??>
    <#assign beanName = bean.name >
    <#assign attributes = bean.attributes>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#assign table = "${tablePrefix}_${bean.table_name}">
package ${package}.${subpkg};

import java.util.*;
import java.sql.*;
import java.io.Serializable;

public class ${beanName}DAOPostgresql
{
    private static final String dsAddress;
    private static String table = "${table}";

    public ${beanName}DAOPostgresql(String dsAddress) {
        this.dsAddress = dsAddress;
    }

    public void saveOrUpdate(${beanName} obj) throws Exception {
        Object[] update = ${beanName}Builder.getDbUpdateQuery(table, obj);
        JdbcCachedHelper.getInstance(dsAddress).runUpdate((String) update[0], (Map) update[1]);
    }

    public List<${beanName}> all() throws Exception {
        List mapList = JdbcCachedHelper.getInstance(dsAddress).getRecordList("select * from " + table, null, false);
        ArrayList<${beanName}> list = new ArrayList<${beanName}>();
        for (Iterator iterator = mapList.iterator(); iterator.hasNext(); ) {
            Map map = (Map) iterator.next();
            list.add(${beanName}Builder.buildFromMap(map));
        }
        return list;
    }

    public ${beanName} getById(Comparable id) throws Exception {
        HashMap params = new HashMap();
        params.put(1, id);
        List mapList = JdbcCachedHelper.getInstance(dsAddress).getRecordList("select * from " + table + " where id = ?" , params, false);
        for (Iterator iterator = mapList.iterator(); iterator.hasNext(); ) {
            Map map = (Map) iterator.next();
            return ${beanName}Builder.buildFromMap(map);
        }
        return null;
    }
}
</#if>