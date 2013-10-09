<#if beans??>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#list beans as bean>
    <#assign attributes = bean.attributes>
        <#assign zpt = "">
    CREATE TABLE ${tablePrefix}${bean.table_name} (<#list attributes as attribute>${zpt}${attribute.column_name} ${attribute.sql_type}<#if attribute.primary_key == 'true' > NOT NULL IDENTITY</#if><#assign zpt = ", "></#list>);
    </#list>
</#if>