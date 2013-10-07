<#if beans??>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#list beans as bean>
    CREATE SEQUENCE ${tablePrefix}${bean.table_name}_id_sequence;
    CREATE TABLE ${tablePrefix}${bean.table_name}
    (
        <#assign attributes = bean.attributes>
        <#list attributes as attribute>
        ${attribute.column_name} ${attribute.sql_type},
        </#list>

        <#list attributes as attribute>
            <#if attribute.primary_key == 'true' >
            CONSTRAINT pk_${tablePrefix}${bean.table_name} PRIMARY KEY (${attribute.column_name})
            </#if>
        </#list>
    );
        <#list attributes as attribute>
            <#if attribute.primary_key == 'true' >
            ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN ${attribute.column_name} SET NOT NULL;
            ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN ${attribute.column_name} SET DEFAULT nextval('${tablePrefix}${bean.table_name}_id_sequence'::regclass);

            </#if>
        </#list>
    </#list>
</#if>
<#if deletedBeans??>
    <#list deletedBeans as bean>
    ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN <#list attributes as attribute><#if attribute.primary_key == 'true' > ${attribute.column_name} </#if></#list> SET DEFAULT 0;
    DROP SEQUENCE ${tablePrefix}${bean.table_name}_id_sequence;
    DROP TABLE ${tablePrefix}${bean.table_name};
    </#list>
</#if>
<#if alteredBeansNewAttribute??>
    <#list alteredBeansNewAttribute as bean>
        <#list bean.attributes as attribute>
        ALTER TABLE ${tablePrefix}${bean.table_name} ADD COLUMN ${attribute.column_name} ${attribute.sql_type};
        </#list>
    </#list>
</#if>
<#if alteredBeansModifyAttribute??>
    <#list alteredBeansModifyAttribute as bean>
        <#list bean.attributes as attribute>
        ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN ${attribute.column_name} TYPE ${attribute.sql_type};
        </#list>
    </#list>
</#if>
<#if alteredBeansDeletedAttribute??>
    <#list alteredBeansDeletedAttribute as bean>
        <#list bean.attributes as attribute>
        ALTER TABLE ${tablePrefix}${bean.table_name} DROP COLUMN ${attribute.column_name};
        </#list>
    </#list>
</#if>
