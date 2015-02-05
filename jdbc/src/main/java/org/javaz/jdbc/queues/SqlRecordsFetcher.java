package org.javaz.jdbc.queues;

import org.javaz.jdbc.util.ConnectionProviderI;
import org.javaz.jdbc.util.JdbcConstants;
import org.javaz.jdbc.util.SimpleConnectionProvider;
import org.javaz.jdbc.util.UnsafeSqlHelper;
import org.javaz.queues.iface.RecordsFetcherI;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of RecordsFetcherI using SQL.
 * Can be used to form queue of given size, when table is huge.
 */
public class SqlRecordsFetcher implements RecordsFetcherI {
    public static long dbErrorDelay = 10000;

    public SqlRecordsFetcher(String dsAddress, String fieldsClause, String fromClause, String whereClause) {
        this.dsAddress = dsAddress;
        this.fieldsClause = fieldsClause;
        this.fromClause = fromClause;
        this.whereClause = whereClause;
    }

    private String dsAddress = "";
    private String fieldsClause = "";
    private String fromClause = "";
    private String whereClause = "";
    private String idColumn = "id";
    private int selectType = JdbcConstants.ACTION_MAP_RESULTS_SET;
    private ConnectionProviderI providerI = new SimpleConnectionProvider();

    public Object[] getMinMaxBounds() {
        Object min = null;
        Object max = null;
        Object count = null;
        boolean ok = false;
        while (!ok) {
            try {
                ArrayList list = UnsafeSqlHelper.runSqlUnsafe(providerI, dsAddress, getMinMaxQuery(), JdbcConstants.ACTION_COMPLEX_LIST_NO_METADATA, null);
                if (!list.isEmpty()) {
                    ArrayList record = (ArrayList) list.get(0);
                    min = record.get(0);
                    max = record.get(1);
                    if (record.size() > 2) {
                        count = record.get(2);
                    }
                }
                ok = true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(dbErrorDelay);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return count != null ? new Object[]{min, max, count} : new Object[]{min, max};
    }

    public Object[] getRecordsArray(long offset, long limit) {
        Collection collection = getRecordsCollection(offset, limit);
        if (collection != null) {
            return collection.toArray(new Object[collection.size()]);
        }
        return null;
    }

    public Collection getRecordsCollection(long offset, long limit) {
        try {
            return UnsafeSqlHelper.runSqlUnsafe(providerI, dsAddress, getRecordsQuery(offset, limit), selectType, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getMinMaxQuery() {
        return "select min(" + getIdColumn() + "), max(" + getIdColumn() + "), count(" + getIdColumn() + ") from " + getFromClause() + " where (" + getQueryWhere() + ") and " + getIdColumn() + " is not null";
    }

    protected String getRecordsQuery(long offset, long limit) {
        return "select " + getFieldsClause() + " from " + getFromClause() + " where (" + getQueryWhere() + ") and " + getIdColumn() + " >= " + offset + " and " + getIdColumn() + " < " + (offset + limit);
    }

    protected String getQueryWhere() {
        return (getWhereClause().isEmpty() ? "true" : getWhereClause());
    }

    public ConnectionProviderI getProviderI() {
        return providerI;
    }

    public void setProviderI(ConnectionProviderI providerI) {
        this.providerI = providerI;
    }

    public String getFieldsClause() {
        return fieldsClause;
    }

    public void setFieldsClause(String fieldsClause) {
        this.fieldsClause = fieldsClause;
    }

    public String getFromClause() {
        return fromClause;
    }

    public void setFromClause(String fromClause) {
        this.fromClause = fromClause;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public int getSelectType() {
        return selectType;
    }

    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SqlRecordsFetcher that = (SqlRecordsFetcher) o;

        if (selectType != that.selectType) return false;
        if (dsAddress != null ? !dsAddress.equals(that.dsAddress) : that.dsAddress != null) return false;
        if (fieldsClause != null ? !fieldsClause.equals(that.fieldsClause) : that.fieldsClause != null) return false;
        if (fromClause != null ? !fromClause.equals(that.fromClause) : that.fromClause != null) return false;
        if (idColumn != null ? !idColumn.equals(that.idColumn) : that.idColumn != null) return false;
        if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null) return false;

        return true;
    }

    public int hashCode() {
        int result = dsAddress != null ? dsAddress.hashCode() : 0;
        result = 31 * result + (fieldsClause != null ? fieldsClause.hashCode() : 0);
        result = 31 * result + (fromClause != null ? fromClause.hashCode() : 0);
        result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
        result = 31 * result + (idColumn != null ? idColumn.hashCode() : 0);
        result = 31 * result + selectType;
        return result;
    }

    public String getDescriptiveName() {
        return "{SqlRecordsFetcher (" + dsAddress + " // " + getFieldsClause() + " @ " + getFromClause() + " where={" + getWhereClause() + "}  id={" + getIdColumn() + "})}";
    }

}
