package org.javaz.util;

import java.util.Comparator;

/**
 *
 */
public class GenericDeepComparator implements Comparator
{
    private SubObjectProducerI producerI = null;
    private Comparator secondarySort = null;
    private boolean inverted = false;

    public SubObjectProducerI getProducerI()
    {
        return producerI;
    }

    public void setProducerI(SubObjectProducerI producerI)
    {
        this.producerI = producerI;
    }

    public Comparator getSecondarySort()
    {
        return secondarySort;
    }

    public void setSecondarySort(Comparator secondarySort)
    {
        this.secondarySort = secondarySort;
    }

    public boolean isInverted()
    {
        return inverted;
    }

    public void setInverted(boolean inverted)
    {
        this.inverted = inverted;
    }

    @Override
    public int compare(Object o1, Object o2)
    {
        Comparable comp1 = producerI != null ? producerI.getPartForComparing(inverted ? o2 : o1) : (Comparable) (inverted ? o2 : o1);
        Comparable comp2 = producerI != null ? producerI.getPartForComparing(inverted ? o1 : o2) : (Comparable) (inverted ? o1 : o2);

        int compare = 0;
        if (comp1 != null && comp2 != null)
        {
            compare = comp1.compareTo(comp2);
        }
        else if (comp1 == null)
        {
            return 1;
        }
        else // if(comp2 == null)
        {
            return -1;
        }

        if (secondarySort != null && compare == 0)
        {
            return secondarySort.compare(o1, o2);
        }
        return compare;
    }
}
