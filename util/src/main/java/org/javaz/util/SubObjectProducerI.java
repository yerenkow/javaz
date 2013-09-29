package org.javaz.util;

/**
 *
 */
public abstract class SubObjectProducerI
{
    protected SubObjectProducerI nested = null;

    public SubObjectProducerI getNested()
    {
        return nested;
    }

    public void setNested(SubObjectProducerI nested)
    {
        this.nested = nested;
    }

    public abstract Comparable getPartForComparing(Object in);
}
