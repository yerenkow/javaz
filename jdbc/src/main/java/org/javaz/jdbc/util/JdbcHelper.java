package org.javaz.jdbc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.*;

/**
 *
 */
public class JdbcHelper extends AbstractJdbcHelper {
    private static Logger logger = LogManager.getLogger(JdbcHelper.class);

    @Override
    public ArrayList<List> runMassUpdatePairs(ArrayList<StringMapPair> objects) {
        return UnsafeSqlHelper.runMassSqlUnsafePairs(getProvider(), jdbcAddress, objects);
    }

    @Override
    public long runUpdate(StringMapPair pair) throws SQLException {
        return runUpdate(pair.getString(), pair.getMap());
    }

    @Override
    public void runUpdateDataIgnore(StringMapPair pair) {
        runUpdateDataIgnore(pair.getString(), pair.getMap());
    }

    @Override
    public long runUpdate(String query, Map parameters) throws SQLException {
        ArrayList list = UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_EXECUTE_UPDATE, parameters);
        if (list != null && !list.isEmpty()) {
            Object object = list.get(0);
            if (object != null && object instanceof Number) {
                return ((Number) object).longValue();
            }
        }

        return -1;
    }

    @Override
    public void runUpdateDataIgnore(String query, Map parameters) {
        try {
            UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_EXECUTE_UPDATE_DATA_IGNORE, parameters);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    @Override
    public List getRecordList(String query, Map parameters, boolean useCache) {
        try {
            return UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_MAP_RESULTS_SET, parameters);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }
}
