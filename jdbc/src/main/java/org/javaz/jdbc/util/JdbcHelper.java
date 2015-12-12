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
    public Number runUpdate(String query, Map<Integer, Object> parameters) {
        ArrayList list = null;
        try {
            list = UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_EXECUTE_UPDATE, parameters);
        } catch (SQLException e) {
            logger.error(e);
        }
        if (list != null && !list.isEmpty()) {
            Object object = list.get(0);
            if (object != null && object instanceof Number) {
                return (Number) object;
            }
        }

        return -1;
    }

    @Override
    @Deprecated
    public void runUpdateDataIgnore(String query, Map<Integer, Object> parameters) {
        runUpdate(query, parameters);
    }

    @Override
    public List getRecordList(String query, Map<Integer, Object> parameters) {
        try {
            return UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_MAP_RESULTS_SET, parameters);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }
}
