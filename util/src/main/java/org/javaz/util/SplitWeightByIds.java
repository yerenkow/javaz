package org.javaz.util;

import java.util.*;

/**
 * Created by user on 21.03.14.
 */
public class SplitWeightByIds<K> {

    Random r;

    public SplitWeightByIds(int seed) {
        r = new Random(seed);
    }

    public HashMap<K, Integer> splitByIdsWithWeights(HashMap<K, Double> weights, Integer count) {
        HashMap<K, Integer>  results = new HashMap<K, Integer> ();
        if(count == null || count < 1 || weights.isEmpty())
        {
            return results;
        }
        if(weights.size() == 1)
        {
            //totally simple case.
            results.put(weights.keySet().iterator().next(), count);
            return results;
        }
        ArrayList<K> allKeys = new ArrayList<K>(weights.keySet());
        double allWeight = 0;
        for (Iterator<K> iterator = allKeys.iterator(); iterator.hasNext(); )
        {
            K id = iterator.next();
            Double aDouble = weights.get(id);
            allWeight += aDouble;
        }
        boolean randomMode = false;
        while(count > 0) {
            boolean anyGiven = false;
            if(!randomMode)
            {
                for (Iterator<K> iterator = allKeys.iterator(); count > 0 && iterator.hasNext(); )
                {
                    K id = iterator.next();
                    Double aDouble = weights.get(id);
                    int needToGive = (int) (aDouble / allWeight);
                    if(needToGive > 0)
                    {
                        anyGiven = true;
                        count -= needToGive;
                        incrementValue(results, id, needToGive);
                    }
                }
            }
            else
            {
                //let's gamble.
                double position = r.nextDouble() * allWeight;
                double zero = 0;
                for (Iterator<K> iterator = allKeys.iterator(); count > 0 && iterator.hasNext(); )
                {
                    K id = iterator.next();
                    Double aDouble = weights.get(id);
                    if(position >= zero && position < zero + aDouble)
                    {
                        //got it.
                        count --;
                        incrementValue(results, id, 1);
                        break;
                    }
                }
            }

            if(!anyGiven)
            {
                randomMode = true;
            }
        }

        return results;
    }

    public HashMap<K, Integer> splitByIdsEvenly(Collection<K> keys, Integer count) {
        HashMap<K, Double>  weights = new HashMap<K, Double> ();
        for (Iterator<K> iterator = keys.iterator(); iterator.hasNext(); )
        {
            K id = iterator.next();
            weights.put(id, 1.0/keys.size());
        }
        return splitByIdsWithWeights(weights, count);
    }

    private void incrementValue(HashMap<K, Integer> results, K id, int needToGive) {
        if(results.containsKey(id))
        {
            results.put(id, results.get(id) + needToGive);
            return;
        }
        results.put(id, needToGive);
    }
}
