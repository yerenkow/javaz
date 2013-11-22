package org.javaz.queues.impl;

import org.javaz.queues.iface.PartialSenderFeedI;
import org.javaz.queues.iface.RecordsFetcherI;
import org.javaz.queues.iface.RecordsRotatorI;

/**
 * This class implements both pre-fetching data from one source and sending it batched to other
 * using SenderFeederI
 */
public class RotatorPartialSender extends SimplePartialSender
{
    protected int rotatorFetchSize = 1000;
    protected int rotatorFetchDelay = 60000;
    protected RotatorFetcher target = null;
    protected RecordsFetcherI fetcherI;

    public RotatorPartialSender() {
    }

    public RotatorPartialSender(RecordsFetcherI fetcherI, PartialSenderFeedI senderFeedI) {
        this.fetcherI = fetcherI;
        this.senderFeedI = senderFeedI;
        startRotating();
    }


    public RecordsFetcherI getFetcherI() {
        return fetcherI;
    }

    public void setFetcherI(RecordsFetcherI fetcherI) {
        this.fetcherI = fetcherI;
    }

    public int getRotatorFetchSize() {
        return rotatorFetchSize;
    }

    public void setRotatorFetchSize(int rotatorFetchSize) {
        this.rotatorFetchSize = rotatorFetchSize;
    }

    public int getRotatorFetchDelay() {
        return rotatorFetchDelay;
    }

    public void setRotatorFetchDelay(int rotatorFetchDelay) {
        this.rotatorFetchDelay = rotatorFetchDelay;
    }

    public void startFetching() {
        target = new RotatorFetcher();
        new Thread(target).start();
    }

    @Override
    public void stop() {
        super.stop();
        if(target != null) {
            target.setRunning(false);
        }
    }

    class RotatorFetcher implements Runnable
    {
        boolean isRunning() {
            return running;
        }

        void setRunning(boolean running) {
            this.running = running;
        }

        protected boolean running = true;
        @Override
        public void run() {
            while(running) {
                try {

                    RecordsFetcherI fetcherI = getFetcherI();
                    if(fetcherI != null) {
                        RecordsRotatorI rotater = RotatorsHolder.getRotater(fetcherI);
                        addToQueueAll(rotater.getManyElements(getRotatorFetchSize()));
                    }

                    Thread.sleep(getRotatorFetchDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
