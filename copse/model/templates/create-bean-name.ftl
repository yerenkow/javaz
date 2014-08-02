<#assign abs = "">

<#assign beanName = bean.name >
<#if beanName?index_of(":") &gt; 0 >
    <#assign beanName=beanName?substring(0, bean.name?index_of(":")) >
</#if>
<#if bean.methods?size != 0 >
    <#assign abs = "Abstract">
</#if>
<#assign pk = "">
<#list bean.attributes as attribute>
    <#if attribute.primary_key == "true" >
        <#assign pk = "true">
    </#if>
</#list>
<#if pk != "true" >
    <#assign abs = "Abstract">
</#if>
${abs}${beanName}.java