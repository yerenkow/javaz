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
<#if deletedBeans??>
    <#list deletedBeans as bean>
    DROP TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case};
    </#list>
</#if>
<#if alteredBeansNewAttribute??>
    <#list alteredBeansNewAttribute as bean>
        <#list bean.getAttribute() as attribute>
        ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ADD COLUMN `${attribute.columnName?lower_case}` ${attribute.sqlType};
        </#list>
    </#list>
</#if>
<#if alteredBeansModifyAttribute??>
    <#list alteredBeansModifyAttribute as bean>
        <#list bean.getAttribute() as attribute>
        ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN `${attribute.columnName?lower_case}` TYPE ${attribute.sqlType};
        </#list>
    </#list>
</#if>
<#if alteredBeansDeletedAttribute??>
    <#list alteredBeansDeletedAttribute as bean>
        <#list bean.getAttribute() as attribute>
        ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} DROP COLUMN `${attribute.columnName?lower_case}`;
        </#list>
    </#list>
</#if>
