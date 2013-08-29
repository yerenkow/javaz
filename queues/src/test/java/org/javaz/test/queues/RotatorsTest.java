package org.javaz.test.queues;

import org.javaz.queues.iface.RecordsRotatorI;
import org.javaz.queues.impl.RotatorsHolder;
import org.junit.Test;

import java.util.ArrayList;

/**
 *
 */
public class RotatorsTest
{
    @Test
    public void testRotators() throws InterruptedException
    {
        new RotatorsHolder();
        ArrayList<RecordsRotatorI> forDebug = RotatorsHolder.getAllRotatersForDebug();
        TestFetcher key = new TestFetcher();
        System.out.println("key.getDescriptiveName() = " + key.getDescriptiveName());
        RotatorsHolder.getRotater(key);
        RecordsRotatorI rotater = RotatorsHolder.getRotater(key);
        rotater.setFetchDelay(100);
        rotater.setFetchSize(100);
        rotater.setFillTries(10);
        rotater.setInsufficientDataDelay(100);
        rotater.setMaxBulksCount(5);
        rotater.setMaxLogsCount(1);
        rotater.setNoDataDelay(1000);
        rotater.setMinSize(1000);
        System.out.println("rotater.getFetcherDescriptiveName() = " + rotater.getFetcherDescriptiveName());
        System.out.println("rotater.toString() = " + rotater.toString());
        System.out.println("rotater.getMaxLogsCount() = " + rotater.getMaxLogsCount());
        System.out.println("rotater.getLogs() = " + rotater.getLogs().size());
        System.out.println("rotater.getMinSize() = " + rotater.getMinSize());
        System.out.println("rotater.getFetchSize() = " + rotater.getFetchSize());
        System.out.println("rotater.getFetchDelay() = " + rotater.getFetchDelay());
        System.out.println("rotater.getFillTries() = " + rotater.getFillTries());
        System.out.println("rotater.getNoDataDelay() = " + rotater.getNoDataDelay());
        System.out.println("rotater.getInsufficientDataDelay() = " + rotater.getInsufficientDataDelay());
        System.out.println("rotater.getMaxBulksCount() = " + rotater.getMaxBulksCount());
        System.out.println("rotater.getCurrentQueueSize() = " + rotater.getCurrentQueueSize());
        Thread.sleep(5000);
        System.out.println("rotater.getNextElement() = " + rotater.getNextElement());
        System.out.println("rotater.getManyElements(1).size() = " + rotater.getManyElements(1).size());
        System.out.println("rotater.getManyElements(10).size() = " + rotater.getManyElements(10).size());
        System.out.println("rotater.getNextElement() = " + rotater.getNextElement());
        rotater.stop();


    }
}
