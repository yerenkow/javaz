<#if bean??>
    <#if !excpackage??>
        <#assign excpackage = package + ".exc">
    </#if>
    <#if !dbQuery??>
        <#assign dbQuery = "true">
    </#if>
    <#if !comma??>
        <#assign comma = false>
    </#if>
<#assign attributes = bean.attributes>
package ${package}.iface;

import ${excpackage}.*;
import org.javaz.jdbc.base.*;
import java.util.*;
import java.io.Serializable;

<#assign beanName = bean.name >
<#assign impl="">
<#if bean.name?index_of(":") &gt; 0 >

<#assign impl=", " + bean.name?substring( 1 + bean.name?index_of(":")) >
<#assign beanName=beanName?substring(0, bean.name?index_of(":")) >
</#if>

public interface ${beanName}I extends MapConvertibleI ${impl}
{
<#list attributes as attribute>
    public ${attribute.type} get${attribute.name?cap_first}();
    public void set${attribute.name?cap_first}(${attribute.type} ${attribute.name});

</#list>

    public Map toStringMap();

<#list bean.methods as method>
    public ${method.type} ${method.name};
</#list>
}</#if>
