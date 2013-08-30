package org.javaz.util.test;

import junit.framework.Assert;
import org.javaz.util.DayUtil;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class DayUtilTest
{
    @Test
    public void testDayUtil()
    {
        new DayUtil();

        int currentDay = DayUtil.getDayShort();
        Date testDate = DayUtil.getDateFromDay(currentDay);
        int dayShort = DayUtil.getDayShort(testDate);
        Assert.assertEquals(currentDay, dayShort);

        DayUtil.getIntegerTime();

        int firstDayOfMonth = DayUtil.getFirstDayOfMonth();
        int firstDayOfYear = DayUtil.getFirstDayOfYear();

        int firstDayOfMonth2 = DayUtil.getFirstDayOfMonth(testDate);
        int firstDayOfYear2 = DayUtil.getFirstDayOfYear(testDate);

        Assert.assertEquals(firstDayOfMonth, firstDayOfMonth2);
        Assert.assertEquals(firstDayOfYear, firstDayOfYear2);

        Assert.assertTrue(firstDayOfMonth <= currentDay);
        Assert.assertTrue(firstDayOfYear <= currentDay);
        Assert.assertTrue(firstDayOfYear <= firstDayOfMonth);

        DayUtil.main(new String[]{});

    }
}
