<#if bean??>
    <#if !comma??>
        <#assign comma = false>
    </#if>
    <#assign attributes = bean.attributes>
package ${package}.impl;

import ${package}.logic.iface.*;
import ${package}.exc.*;
import java.util.*;
import java.sql.*;
import java.io.Serializable;

public abstract class Abstract${bean.name} implements ${bean.name}I
{
<#list attributes as attribute>
    private ${attribute.type} ${attribute.name};
</#list>

    public Abstract${bean.name} ()
    {
    }
<#list bean.methods as method>
    public abstract ${method.type} ${method.name};
</#list>
}
</#if>