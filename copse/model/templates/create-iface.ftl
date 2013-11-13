<#if bean??>
    <#if !comma??>
        <#assign comma = false>
    </#if>
<#assign attributes = bean.attributes>
package ${package}.iface;

import java.util.*;
import java.io.Serializable;

<#assign beanName = bean.name >
<#assign impl="">
<#if bean.name?index_of(":") &gt; 0 >

<#assign impl=", " + bean.name?substring( 1 + bean.name?index_of(":")) >
<#assign beanName=beanName?substring(0, bean.name?index_of(":")) >
</#if>

public interface ${beanName}I extends Serializable, Cloneable ${impl}
{
<#list attributes as attribute>
    public ${attribute.type} get${attribute.name?cap_first}();
    public void set${attribute.name?cap_first}(${attribute.type} ${attribute.name});

</#list>
    public ${beanName}I fromMap(Map h);

    public Map toStringMap();

    public Object[] getDbUpdateQuery();

    public Object[] getDbUpdateQuery(String table_name);

<#list bean.methods as method>
    public ${method.type} ${method.name};
</#list>
}</#if>
