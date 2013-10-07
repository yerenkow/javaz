<#if beans??>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#list beans as bean>
    CREATE SEQUENCE ${tablePrefix?lower_case}${bean.tableName?lower_case}_id_sequence;
    CREATE TABLE ${tablePrefix}${bean.tableName}
    (
        <#assign attributes = bean.getAttribute()>
        <#list attributes as attribute>
        ${attribute.columnName} ${attribute.sqlType},
        </#list>

        <#list attributes as attribute>
            <#if attribute.primaryKey >
            CONSTRAINT pk_${tablePrefix?lower_case}${bean.tableName?lower_case} PRIMARY KEY (${attribute.columnName})
            </#if>
        </#list>
    );
        <#list attributes as attribute>
            <#if attribute.primaryKey >
            ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN ${attribute.columnName} SET NOT NULL;
            ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN ${attribute.columnName} SET DEFAULT nextval('${tablePrefix?lower_case}${bean.tableName?lower_case}_id_sequence'::regclass);

            </#if>
        </#list>
    </#list>
</#if>
<#if deletedBeans??>
    <#list deletedBeans as bean>
    ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN id SET DEFAULT 0;
    DROP SEQUENCE ${tablePrefix?lower_case}${bean.tableName?lower_case}_id_sequence;
    DROP TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case};
    </#list>
</#if>
<#if alteredBeansNewAttribute??>
    <#list alteredBeansNewAttribute as bean>
        <#list bean.getAttribute() as attribute>
        ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ADD COLUMN ${attribute.columnName} ${attribute.sqlType};
        </#list>
    </#list>
</#if>
<#if alteredBeansModifyAttribute??>
    <#list alteredBeansModifyAttribute as bean>
        <#list bean.getAttribute() as attribute>
        ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN ${attribute.columnName} TYPE ${attribute.sqlType};
        </#list>
    </#list>
</#if>
<#if alteredBeansDeletedAttribute??>
    <#list alteredBeansDeletedAttribute as bean>
        <#list bean.getAttribute() as attribute>
        ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} DROP COLUMN ${attribute.columnName};
        </#list>
    </#list>
</#if>
