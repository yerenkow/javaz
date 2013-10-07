begin;
<#if beans??>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#list beans as bean>
    CREATE TABLE ${tablePrefix}${bean.table_name}
    (
        <#assign attributes = bean.attributes>
        <#list attributes as attribute>
        `${attribute.column_name}` ${attribute.sql_type} <#if attribute.primary_key == 'true' >NOT NULL AUTO_INCREMENT </#if>,
        </#list>

        <#list attributes as attribute>
            <#if attribute.primary_key  == 'true' >
            CONSTRAINT pk_${tablePrefix}${bean.table_name} PRIMARY KEY (`${attribute.column_name}`)
            </#if>
        </#list>
    );
    </#list>
</#if>
rollback;