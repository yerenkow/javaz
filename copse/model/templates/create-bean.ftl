<#if bean??>
    <#if !comma??>
        <#assign comma = false>
    </#if>
    <#assign attributes = bean.attributes>
package ${package}.abs;

import ${package}.iface.${bean.name}I;
import java.util.*;
import java.sql.*;
import java.io.Serializable;

public abstract class Abstract${bean.name} implements ${bean.name}I
{
    <#list attributes as attribute>
    private ${attribute.type} ${attribute.name};
    </#list>

    public ${bean.name}VO ()
    {
    }

    public ${bean.name}VO (Map h)
    {
    <#list attributes as attribute>
        <#assign found = "false">
        <#if attribute.type == "java.lang.Short" >
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).shortValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Integer" >
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).intValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Long" >
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).longValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Double" >
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).doubleValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Float" >
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).floatValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.sql.Date" || attribute.type == "java.sql.Timestamp" >
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}( new ${attribute.type}(((java.util.Date) h.get("${attribute.column_name}")).getTime()));}
            <#assign found = "true">
        </#if>
        <#if found == "false">
        if(h.get("${attribute.column_name}") != null) { set${attribute.name?cap_first}((${attribute.type}) h.get("${attribute.column_name}")); }
        </#if>
    </#list>
    }

    public ${bean.name}VO fromMap(Map h)
    {
        return new ${bean.name}VO(h);
    }

    <#list attributes as attribute>
    public ${attribute.type} get${attribute.name?cap_first}()
    {
        return ${attribute.name};
    }

    public void set${attribute.name?cap_first}(${attribute.type} ${attribute.name})
    {
        this.${attribute.name} = ${attribute.name};
    }

    </#list>

    public Object clone() throws CloneNotSupportedException
    {
        ${bean.name}VO clone = new ${bean.name}VO();
        <#list attributes as attribute>
        clone.set${attribute.name?cap_first}(get${attribute.name?cap_first}());
        </#list>
        return clone;
    }


    public Map toStringMap()
    {
        Map h = new HashMap();
        <#list attributes as attribute>
        if(${attribute.name} != null) { h.put("${attribute.column_name}", "" + get${attribute.name?cap_first}()); }
        </#list>
        return h;
    }

    public Object[] getDbUpdateQuery()
    {
        return getDbUpdateQuery("${tablePrefix?lower_case}${bean.table_name?lower_case}");
    }

    public Object[] getDbUpdateQuery(String table_name)
    {
        StringBuilder sb = new StringBuilder();
        HashMap map = new HashMap();
            <#assign i = 1>
            <#list attributes as attribute>
                <#if attribute.primary_key != 'true'>
        map.put(${i}, ${attribute.name});
                    <#assign i = i + 1>
                </#if>
            </#list>
        if(id != null)
        {
            map.put(${i}, id);
            <#assign comma = false>
            <#assign sb = ''>
            <#list attributes as attribute>
                <#if attribute.primary_key != 'true'>
                    <#if comma>
                        <#assign sb = sb + ', '>
                    </#if>
                    <#assign sb = sb + attribute.column_name + ' = ?'>
                    <#if !comma>
                        <#assign comma = true>
                    </#if>
                </#if>
            </#list>
            sb.append("update ").append(table_name).append(" set ${sb} where id = ?");
        }
        else
        {
            <#assign i = 1>
            <#assign comma = false>
            <#assign sb = ''>
            <#assign qms = ''>
            <#list attributes as attribute>
                <#if attribute.primary_key != 'true'>
                    <#if comma>
                        <#assign sb = sb + ', '>
                        <#assign qms = qms + ', '>
                    </#if>
                    <#assign sb = sb + attribute.column_name>
                    <#assign qms = qms + '?'>
                    <#if !comma>
                        <#assign comma = true>
                    </#if>
                    <#assign i = i + 1>
                </#if>
            </#list>
            sb.append("insert into ").append(table_name).append("(${sb}) VALUES (${qms})");
        }
        return new Object[]{sb.toString(), map};
    }

    <#list bean.methods as method>
    public abstract ${method.type} ${method.name};
    </#list>
}
</#if>