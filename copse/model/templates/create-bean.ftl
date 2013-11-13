<#if bean??>
    <#assign beanName = bean.name >
    <#if beanName?index_of(":") &gt; 0 >
        <#assign beanName=beanName?substring(0, bean.name?index_of(":")) >
    </#if>
    <#if !dbQuery??>
        <#assign dbQuery = "true">
    </#if>
    <#if !excpackage??>
        <#assign excpackage = package + ".exc">
    </#if>
    <#if !comma??>
        <#assign comma = false>
    </#if>
    <#if !subpkg??>
        <#assign subpkg = "abs">
    </#if>
    <#assign attributes = bean.attributes>
    <#assign abs = "">
    <#assign absClass = "">
    <#if bean.methods?size != 0 >
        <#assign abs = "Abstract">
        <#assign absClass = "abstract">
    </#if>
package ${package}.${subpkg};

import ${package}.iface.*;

import ${excpackage}.*;
import java.util.*;
import java.sql.*;
import java.io.Serializable;

public ${absClass} class ${abs}${beanName} implements ${beanName}I
{
    <#list attributes as attribute>
    private ${attribute.type} ${attribute.name};
    </#list>

    public ${abs}${beanName} ()
    {
    }

    public ${abs}${beanName} (Map h)
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

    <#if bean.methods?size != 0 >
    public abstract ${abs}${beanName} createNewInstance(Map h);
    <#else>
    public ${abs}${beanName} createNewInstance(Map h)
    {
        return new ${abs}${beanName}(h);
    }
    </#if>

    public ${abs}${beanName} fromMap(Map h)
    {
        return createNewInstance(h);
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
        ${abs}${beanName} clone = createNewInstance(new HashMap());
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

    <#if dbQuery == "true">
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
    </#if>

<#list bean.methods as method>
    public abstract ${method.type} ${method.name};
</#list>
}
</#if>