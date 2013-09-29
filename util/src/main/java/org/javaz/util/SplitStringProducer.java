package org.javaz.util;

/**
 */
public class SplitStringProducer extends SubObjectProducerI
{
    private String regex = "\t";
    private int position = 0;

    public SplitStringProducer(String pattern, int position)
    {
        this.regex = pattern;
        this.position = position;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    public Comparable getPartForComparing(Object in)
    {
        if (in == null)
        {
            return null;
        }
        String[] split = ((String) in).split(regex);
        if (split.length > position)
            return nested != null ? nested.getPartForComparing(split[position]) : split[position];
        return null;
    }
}
