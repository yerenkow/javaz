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
    <#assign pk = "">
    <#list attributes as attribute>
        <#if attribute.primary_key == "true" >
            <#assign pk = "true">
        </#if>
    </#list>
    <#if pk != "true" >
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

    <#if abs == "Abstract" >
    public abstract ${abs}${beanName} createNewInstance();
    <#else>
    public ${abs}${beanName} createNewInstance()
    {
        return new ${abs}${beanName}();
    }
    </#if>

    <#list attributes as attribute>
    public ${attribute.type} get${attribute.name?cap_first}()
    {
        return ${attribute.name};
    }

    public void set${attribute.name?cap_first}(${attribute.type} ${attribute.name})
    {
        this.${attribute.name} = ${attribute.name};
    }
    <#if attribute.primary_key == "true" >
    public Comparable getPrimaryKey() {
        return ${attribute.name};
    }

    public void setGeneratedPrimaryKey(Comparable key) {
        <#assign conversion = "((java.lang.Number) key).longValue()" />
        <#if attribute.type == "java.lang.Integer" >
            <#assign conversion = "((java.lang.Number) key).intValue()" />
        </#if>
        this.${attribute.name} = ${conversion};
    }
    </#if>
    </#list>

    public Object clone() throws CloneNotSupportedException
    {
        ${abs}${beanName} clone = createNewInstance();
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

<#list bean.methods as method>
    public abstract ${method.type} ${method.name};
</#list>
}
</#if>