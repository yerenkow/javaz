<#if beans??>
-- Create new tables part;
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
            <#if attribute.primary_key == 'true' >
            CONSTRAINT pk_${tablePrefix}${bean.table_name} PRIMARY KEY (`${attribute.column_name}`)
            </#if>
        </#list>
    );
    </#list>
</#if>
<#if deletedBeans??>
-- Drop obsolete tables
    <#list deletedBeans as bean>
    DROP TABLE ${tablePrefix}${bean.table_name};
    </#list>
</#if>
<#if alteredBeansNewAttribute??>
-- For some tables - new fields
<#list alteredBeansNewAttribute as bean>
    <#list bean.attributes as attribute>
    ALTER TABLE ${tablePrefix}${bean.table_name} ADD COLUMN `${attribute.column_name}` ${attribute.sql_type};
    </#list>
</#list>
</#if>
<#if alteredBeansModifyAttribute??>
-- For some tables - present fields changes types
<#list alteredBeansModifyAttribute as bean>
    <#list bean.attributes as attribute>
    ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN `${attribute.column_name}` TYPE ${attribute.sql_type};
    </#list>
</#list>
</#if>
<#if alteredBeansDeletedAttribute??>
-- For some tables - remove fields
<#list alteredBeansDeletedAttribute as bean>
    <#list bean.attributes as attribute>
    ALTER TABLE ${tablePrefix}${bean.table_name} DROP COLUMN `${attribute.column_name}`;
    </#list>
</#list>
</#if>
