package org.javaz.test.queues;

import org.javaz.queues.impl.SimplePartialSender;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Test for SimplePartialSender
 */
public class SenderTest
{
    public static SimplePartialSender sender = null;

    @BeforeClass
    public static void testInit()
    {
        TestFeed feedI = new TestFeed();
        sender = new SimplePartialSender();
        sender = new SimplePartialSender(feedI);
        sender.getSenderFeedI();
        sender.setSenderFeedI(feedI);

        sender.setSmallDelayPeriod(1000);
        sender.setSendPeriod(1000);
        sender.setChunkSize(25);
        sender.setOnlyUniqueAllowed(true);
        sender.setWaitDelayForMinimalSize(1000);

        System.out.println("sender.isRepeatFailedSend() = " + sender.isRepeatFailedSend());
        System.out.println("sender.getSendPeriod() = " + sender.getSendPeriod());
        System.out.println("sender.getChunkSize() = " + sender.getChunkSize());
        System.out.println("sender.getSmallDelayPeriod() = " + sender.getSmallDelayPeriod());
        System.out.println("sender.isOnlyUniqueAllowed() = " + sender.isOnlyUniqueAllowed());
        System.out.println("sender.getWaitDelayForMinimalSize() = " + sender.getWaitDelayForMinimalSize());
        sender.setMaxLogsCount(1);
        System.out.println("sender.getMaxLogsCount() = " + sender.getMaxLogsCount());
        sender.setRepeatFailedSend(true);                                                                          ;

        sleep(1000);
    }

    @Test
    public void testAcceptingData()
    {
        String x1 = "x1";
        sender.addToQueue(x1);

        sleep(2000);
        sender.setRepeatFailedSend(false);
        sleep(2000);

        ArrayList manyObjects = new ArrayList();
        for (int i =0; i < 100; i++)
        {
            manyObjects.add("many " + i);
        }
        System.out.println("adding many");
        sender.addToQueueAll(manyObjects);
        sleep(1000);
        System.out.println("adding many again thee times");
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);


        System.out.println("sender.getStartWhenIteration() = " + sender.getStartWhenIteration());
        System.out.println("sender.getSendingQueueLength() = " + sender.getSendingQueueLength());
        System.out.println("sender.getQueueLength() = " + sender.getQueueLength());
        System.out.println("sender.getLogs() = " + sender.getLogs().size());
        System.out.println("sender.getCurrentStep() = " + sender.getCurrentStep());
        System.out.println("sender.getSteps() = " + sender.getSteps());

        sleep(1000);
        sender.setOnlyUniqueAllowed(false);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        while(sender.getSendingQueueLength() == 0)
        {
            sleep(10);
        }
        sender.addToQueueAll(manyObjects);
        sender.setOnlyUniqueAllowed(true);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sender.addToQueueAll(manyObjects);
        sleep(1000);
        sender.stop();
    }

    public static void sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
