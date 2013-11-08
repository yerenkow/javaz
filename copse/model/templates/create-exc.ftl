<#if bean??>
    <#if !comma??>
        <#assign comma = false>
    </#if>
package ${package}.exc;

public class ${bean.name} extends Exception
{
}
</#if>
