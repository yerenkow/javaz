package org.javaz.uml;

import org.javaz.util.ObjectDifference;

import java.util.*;

/**
 *
 */
public class VioletDiffer
{
    private Map modelNew;
    private Map modelOld;

    private ArrayList newBeans = new ArrayList();
    private ArrayList deletedBeans = new ArrayList();
    private ArrayList alteredBeansNewAttribute = new ArrayList();
    private ArrayList alteredBeansModifyAttribute = new ArrayList();
    private ArrayList alteredBeansDeletedAttribute = new ArrayList();

    public VioletDiffer(Map modelNew, Map modelOld)
    {
        this.modelNew = modelNew;
        this.modelOld = modelOld;
    }

    public void calculateDifference()
    {
        HashMap aMap = ObjectDifference.packHashesByKey((List<Map>) modelNew.get("beans"), "name");
        HashMap bMap = ObjectDifference.packHashesByKey((List<Map>) modelOld.get("beans"), "name");

        HashMap newOne = ObjectDifference.getInANotInB(aMap, bMap);
        newBeans.addAll(newOne.values());
        HashMap oldOne = ObjectDifference.getInANotInB(bMap, aMap);
        deletedBeans.addAll(oldOne.values());

        //changed
        Set set = aMap.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext(); )
        {
            Object key = iterator.next();
            Map aBean = (Map) aMap.get(key);
            Map bBean = (Map) bMap.get(key);
            if (bBean == null)
            {
                continue;
            }
            HashMap aMapAttr = ObjectDifference.packHashesByKey((List<Map>) aBean.get("attributes"), "name");
            HashMap bMapAttr = ObjectDifference.packHashesByKey((List<Map>) bBean.get("attributes"), "name");

            HashMap newOneAttr = ObjectDifference.getInANotInB(aMapAttr, bMapAttr);
            if (!newOneAttr.isEmpty())
            {
                Map copyBean = new HashMap(aBean);
                copyBean.put("attributes", newOneAttr.values());
                alteredBeansNewAttribute.add(copyBean);
            }
            HashMap deletedOneAttr = ObjectDifference.getInANotInB(bMapAttr, aMapAttr);
            if (!deletedOneAttr.isEmpty())
            {
                Map copyBean = new HashMap(aBean);
                copyBean.put("attributes", deletedOneAttr.values());
                alteredBeansDeletedAttribute.add(copyBean);
            }
            HashMap changedAttr = ObjectDifference.getInAAndInBNotEquals(aMapAttr, bMapAttr);
            if (!changedAttr.isEmpty())
            {
                Map copyBean = new HashMap(aBean);
                copyBean.put("attributes", changedAttr.values());
                alteredBeansModifyAttribute.add(copyBean);
            }
        }
    }

    public ArrayList getNewBeans()
    {
        return newBeans;
    }

    public ArrayList getDeletedBeans()
    {
        return deletedBeans;
    }

    public ArrayList getAlteredBeansNewAttribute()
    {
        return alteredBeansNewAttribute;
    }

    public ArrayList getAlteredBeansModifyAttribute()
    {
        return alteredBeansModifyAttribute;
    }

    public ArrayList getAlteredBeansDeletedAttribute()
    {
        return alteredBeansDeletedAttribute;
    }
}
