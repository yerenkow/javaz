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
        <#assign subpkg = "helper">
    </#if>
    <#assign attributes = bean.attributes>
package ${package}.${subpkg};

import ${package}.iface.*;
import org.javaz.copse.abs.*;
import org.javaz.copse.beans.*;

import ${excpackage}.*;
import org.javaz.jdbc.base.*;
import java.util.*;
import java.sql.*;
import java.io.Serializable;

public class ${beanName}Helper extends AbstractMapConvertibleHelper<${beanName}I> {

    public ${beanName}I buildFromMap(Map h)
    {
        ${beanName}I b = createNewInstance();
    <#list attributes as attribute>
        <#assign found = "false">
        <#if attribute.type == "java.lang.Short" >
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).shortValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Integer" >
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).intValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Long" >
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).longValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Double" >
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).doubleValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.lang.Float" >
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}( new ${attribute.type}(((Number) h.get("${attribute.column_name}")).floatValue()));}
            <#assign found = "true">
        </#if>
        <#if attribute.type == "java.sql.Date" || attribute.type == "java.sql.Timestamp" >
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}( new ${attribute.type}(((java.util.Date) h.get("${attribute.column_name}")).getTime()));}
            <#assign found = "true">
        </#if>
        <#if found == "false">
        if(h.get("${attribute.column_name}") != null) { b.set${attribute.name?cap_first}((${attribute.type}) h.get("${attribute.column_name}")); }
        </#if>
    </#list>
        return b;
    }

    public ${beanName}I createNewInstance() {
        return new ${beanName}();
    }

    <#if dbQuery == "true">
    public Object[] getDbUpdateQuery(String tableName, ${beanName}I obj, boolean forceInsert) {
        StringBuilder sb = new StringBuilder();
        HashMap<Integer, Object> map = new HashMap<Integer, Object>();
        <#assign i = 1>
        <#assign iPk = 1>
        <#list attributes as attribute>
            <#if attribute.primary_key != 'true'>
                map.put(${i}, obj.get${attribute.name?cap_first}());
                <#assign i = i + 1>
            </#if>
            <#assign iPk = iPk + 1>
        </#list>
        if (obj.getId() != null && !forceInsert) {
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
                <#assign sb = sb + '\"\n                + " '>
            </#if>
        </#list>
        sb.append("update ").append(tableName).append(
        " set ${sb} where id = ?");
        } else {
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
                <#assign sb = sb + '\"\n                    + " '>
            </#if>
        </#list>
        <#assign sbPk = sb>
        <#assign qmsPk = qms>
        <#list attributes as attribute>
            <#if attribute.primary_key == 'true'>
                <#if comma>
                    <#assign sbPk = sbPk + ', '>
                    <#assign qmsPk = qmsPk + ', '>
                </#if>
                <#assign sbPk = sbPk + attribute.column_name>
                <#assign qmsPk = qmsPk + '?'>
                <#if !comma>
                    <#assign comma = true>
                </#if>
                <#assign sbPk = sbPk + '\"\n                    + " '>
            </#if>
        </#list>
        if (forceInsert) {
        <#list attributes as attribute>
            <#if attribute.primary_key = 'true'>
                map.put(${i}, obj.get${attribute.name?cap_first}());
            </#if>
        </#list>
        sb.append("insert into ").append(tableName).append(
        "(${sbPk}) VALUES (${qmsPk})");
        } else {
        sb.append("insert into ").append(tableName).append(
        "(${sb}) VALUES (${qms})");
        }
        }
        return new Object[]{sb.toString(), map};
    }
    </#if>
}
</#if>