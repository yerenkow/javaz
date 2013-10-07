begin;
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
            <#if attribute.primary_key == 'true'  >
            ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN ${attribute.column_name} SET NOT NULL;
            ALTER TABLE ${tablePrefix}${bean.table_name} ALTER COLUMN ${attribute.column_name} SET DEFAULT nextval('${tablePrefix}${bean.table_name}_id_sequence'::regclass);

            </#if>
        </#list>
    </#list>
</#if>
rollback;