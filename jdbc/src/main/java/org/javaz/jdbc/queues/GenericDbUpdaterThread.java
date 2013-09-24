package org.javaz.jdbc.queues;

import org.javaz.jdbc.util.ConnectionProviderI;
import org.javaz.jdbc.util.JdbcConstants;
import org.javaz.jdbc.util.UnsafeSqlHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 */
public class GenericDbUpdaterThread implements Runnable
{
    private String db;
    private String query;
    private List list;
    private ConnectionProviderI providerI;

    public GenericDbUpdaterThread(String db, String query, List list, ConnectionProviderI providerI)
    {
        this.db = db;
        this.query = query;
        this.list = list;
        this.providerI = providerI;
    }

    @Override
    public void run()
    {
        updateManyObjects(db, query, list);
    }

    public void updateManyObjects(String db, String queryPart, List dataForUpdateList)
    {
        List subList = null;
        if (dataForUpdateList.size() > 0)
        {
            int steps = dataForUpdateList.size() / GenericDbUpdater.MAX_OBJECTS_PER_UPDATE + 1;
            for (int currentStep = 0; currentStep < steps; currentStep++)
            {
                try
                {
                    subList = dataForUpdateList.subList(currentStep * GenericDbUpdater.MAX_OBJECTS_PER_UPDATE, Math.min((currentStep + 1) * GenericDbUpdater.MAX_OBJECTS_PER_UPDATE, dataForUpdateList.size()));
                    HashMap queryParamsMap = new HashMap(subList.size() + 1, 1.0f);
                    UnsafeSqlHelper.addArrayParameters(queryParamsMap, subList);
                    UnsafeSqlHelper.runSqlUnsafe(providerI, db, queryPart + " in (" + UnsafeSqlHelper.repeatQuestionMark(subList.size()) + ")", JdbcConstants.ACTION_EXECUTE_UPDATE_DATA_IGNORE, queryParamsMap);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        String subQ = query;

        int whereIndex = subQ.toLowerCase().indexOf("where");
        if (whereIndex > -1)
        {
            subQ = subQ.substring(0, whereIndex).trim();
        }

        //todo logging here
    }
}
