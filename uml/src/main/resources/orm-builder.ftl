<#if bean??>
    <#assign beanName = bean.name >
    <#if !subpkg??>
        <#assign subpkg = "sample">
    </#if>
    <#if !abs??>
        <#assign abs = "">
    </#if>
    <#if !package??>
        <#assign package = "package">
    </#if>
    <#assign attributes = bean.attributes>
package ${package}.${subpkg};

import java.util.*;
import java.sql.*;
import java.io.Serializable;

public class ${beanName}Builder
{
    public static ${beanName} buildFromMap (Map h)
    {
        ${beanName} o = new ${beanName}();
    <#list attributes as attribute>
        <#assign found = "false">
        <#if attribute.type == "java.lang.Short" >
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).shortValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Integer" >
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).intValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Long" >
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).longValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Double" >
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).doubleValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Float" >
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).floatValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.sql.Date" || attribute.type == "java.sql.Timestamp" >
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}( new ${attribute.type}(((java.util.Date) h.get("${attribute.column_name}")).getTime()));}
            <#assign found = "true">
        </#if>
        <#if found == "false">
        if(h.get("${attribute.column_name}") != null) { o.set${attribute.name?cap_first}((${attribute.type}) h.get("${attribute.column_name}")); }
        </#if>
    </#list>
        return o;
    }

    public static Object[] getDbUpdateQuery(String tableName, ${beanName} obj)
    {
        StringBuilder sb = new StringBuilder();
        HashMap map = new HashMap();
            <#assign i = 1>
            <#list attributes as attribute>
                <#if attribute.primary_key != 'true'>
                map.put(${i}, obj.get${attribute.name?cap_first}());
                    <#assign i = i + 1>
                </#if>
            </#list>
        if(obj.getId() != null)
        {
        map.put(${i}, obj.getId());
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
        sb.append("update ").append(tableName).append(" set ${sb} where id = ?");
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
        sb.append("insert into ").append(tableName).append("(${sb}) VALUES (${qms})");
        }
        return new Object[]{sb.toString(), map};
    }
}
</#if>