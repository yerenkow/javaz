begin;
<#if beans??>
    <#if !tablePrefix??>
        <#assign tablePrefix = "tbl_">
    </#if>
    <#list beans as bean>
    CREATE SEQUENCE ${tablePrefix?lower_case}${bean.tableName?lower_case}_id_sequence;
    CREATE TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case}
    (
        <#assign attributes = bean.getAttribute()>
        <#list attributes as attribute>
        ${attribute.columnName?lower_case} ${attribute.sqlType},
        </#list>

        <#list attributes as attribute>
            <#if attribute.primaryKey >
            CONSTRAINT pk_${tablePrefix?lower_case}${bean.tableName?lower_case} PRIMARY KEY (${attribute.columnName?lower_case})
            </#if>
        </#list>
    );
        <#list attributes as attribute>
            <#if attribute.primaryKey >
            ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN ${attribute.columnName?lower_case} SET NOT NULL;
            ALTER TABLE ${tablePrefix?lower_case}${bean.tableName?lower_case} ALTER COLUMN ${attribute.columnName?lower_case} SET DEFAULT nextval('${tablePrefix?lower_case}${bean.tableName?lower_case}_id_sequence'::regclass);

            </#if>
        </#list>
    </#list>
</#if>
rollback;