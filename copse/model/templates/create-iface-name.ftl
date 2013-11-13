<#assign beanName = bean.name >
<#if beanName?index_of(":") &gt; 0 >
    <#assign beanName=beanName?substring(0, bean.name?index_of(":")) >
</#if>
${beanName}I.java