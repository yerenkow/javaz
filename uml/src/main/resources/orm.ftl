<#if bean??>
    <#assign beanName = bean.name >
    <#if !subpkg??>
        <#assign subpkg = "sample">
    </#if>
    <#assign attributes = bean.attributes>
package ${package}.${subpkg};

import ${package}.iface.*;

import java.util.*;
import java.sql.*;
import java.io.Serializable;

public class ${beanName}
{
    <#list attributes as attribute>
    private ${attribute.type} ${attribute.name};
    </#list>

    public ${abs}${beanName} ()
    {
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
}
</#if>