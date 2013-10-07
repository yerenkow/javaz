begin;
<#if beans??>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#list beans as bean>
    CREATE TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case}
    (
        <#assign attributes = bean.getAttribute()>
        <#list attributes as attribute>
        `${attribute.columnName?lower_case}` ${attribute.sqlType} <#if attribute.primaryKey >NOT NULL AUTO_INCREMENT </#if>,
        </#list>

        <#list attributes as attribute>
            <#if attribute.primaryKey >
            CONSTRAINT pk_${tablePrefix?lower_case}${bean.tableName?lower_case} PRIMARY KEY (`${attribute.columnName?lower_case}`)
            </#if>
        </#list>
    );
    </#list>
</#if>
rollback;