package org.javaz.queues.iface;

import java.util.*;

/**
 *
 */
public interface PartialSenderI extends Runnable
{
    public static int DEFAULT_SEND_PERIOD = 60000;
    public static int DEFAULT_SMALL_DELAY_PERIOD = 10000;
    public static int DEFAULT_SEND_SIZE = 64;
    public static int DEFAULT_LOGS_COUNT = 32;

    /**
     * Stops this Thread
     */
    public void stop();


    /**
     * @param senderFeedI - feed, to which all data is pushed
     */
    public void setSenderFeedI(PartialSenderFeedI senderFeedI);

    public PartialSenderFeedI getSenderFeedI();

    /**
     * @param o - to be added
     * @return if object allowed to be added
     *         <p>
     *         Used to prevent data doubling, for example
     */
    public boolean canBeAdded(Object o);

    /**
     * @param o - Object to calculate its hashCode
     * @return hashcode of object. If you adding complex data to sender
     *         like Arrays or Collections, you should override hash calculation for them.
     *         OR use objectHashCalculator
     */
    public Object calculateObjectHash(Object o);

    /**
     * @param o - object to be added to Sender's queue
     */
    public void addToQueue(Object o);

    /**
     * @param c - objects to be added to Sender's queue
     */
    public void addToQueueAll(Collection c);

    /**
     * Internal method, called when next iteration of sending data happens.
     * It called sendByPortions, and can be overridden to provide any pre-logic
     *
     * @param toSend - all queue at moment of next tick happened
     */
    public void preSendPartially(ArrayList toSend);

    /**
     * @param allData - data to be chunked and pushed to real send method
     * @return - results, if any
     */
    public Collection sendByPortions(List allData);

    /**
     * Tuning part
     */

    public int getChunkSize();

    public void setChunkSize(int byHowMany);

    public int getSendPeriod();

    public void setSendPeriod(int sendPeriod);

    public int getSmallDelayPeriod();

    public void setSmallDelayPeriod(int smallDelayPeriod);

    public boolean isOnlyUniqueAllowed();

    public void setOnlyUniqueAllowed(boolean onlyUnique);

    public int getMaxLogsCount();

    public void setMaxLogsCount(int logsCount);

    public int getWaitDelayForMinimalSize();

    public void setWaitDelayForMinimalSize(int waitDelayForMinimalSize);

    public boolean isRepeatFailedSend();

    public void setRepeatFailedSend(boolean repeatFailedSend);

    /**
     * Getter and Setter for objectHashCalculator -
     * which can calculate hashes of complex Objects, liket Lists, Arrays, etc.
     */
    public ObjectHashCalculator getObjectHashCalculator();

    public void setObjectHashCalculator(ObjectHashCalculator hashCodeCalculator);

    /**
     * Methods useful for debugging/monitoring
     */

    public ArrayList getLogs();

    public int getSteps();

    public int getCurrentStep();

    public int getQueueLength();

    public int getSendingQueueLength();

    public long getStartWhenIteration();

    public void startRotating();
}
