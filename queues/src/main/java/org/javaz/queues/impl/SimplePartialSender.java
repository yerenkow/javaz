package org.javaz.queues.impl;

import org.javaz.queues.iface.PartialSenderFeedI;
import org.javaz.queues.iface.PartialSenderI;

import java.util.*;

/**
 *
 */
public class SimplePartialSender implements PartialSenderI
{
	protected int sendPeriod = DEFAULT_SEND_PERIOD;
	protected int smallDelayPeriod = DEFAULT_SMALL_DELAY_PERIOD;
	protected int chunkSize = DEFAULT_SEND_SIZE;
    protected int maxLogsCount = DEFAULT_LOGS_COUNT;

    protected boolean onlyUniqueAllowed = false;
    protected boolean repeatFailedSend = true;

    protected HashMap uniqueKeys = null;
    protected boolean running = true;
    protected long startWhenIteration = 0l;
    protected int steps = 0;
    protected int currentStep = 0;
    protected int waitDelayForMinimalSize = 0;
    protected int sendingQueueSize = 0;

    protected final ArrayList queue = new ArrayList();
    protected final ArrayList toSend = new ArrayList();
    protected ArrayList<Object[]> logs = new ArrayList<Object[]>();
    protected PartialSenderFeedI senderFeedI = null;

    public SimplePartialSender(PartialSenderFeedI senderFeedI)
    {
        this.senderFeedI = senderFeedI;
    }

    public PartialSenderFeedI getSenderFeedI()
    {
        return senderFeedI;
    }

    public void setSenderFeedI(PartialSenderFeedI senderFeedI)
    {
        this.senderFeedI = senderFeedI;
    }

    public boolean canBeAdded(Object o)
    {
        if(!onlyUniqueAllowed)
        {
            return true;
        }
        
        ensureKeys();
        
        return !uniqueKeys.containsKey(getObjectHashCode(o));
    }

    public Object getObjectHashCode(Object o)
    {
        return o.hashCode();
    }

    public void addToQueue(Object o)
	{
		synchronized (queue)
		{
			if(canBeAdded(o))
            {
                queue.add(o);
                markObjectAsAdded(o);
            }
		}
	}

    private void markObjectAsAdded(Object o)
    {
        if(!onlyUniqueAllowed)
            return;

        if(uniqueKeys != null)
        {
            synchronized (uniqueKeys)
            {
                {
                    uniqueKeys.put(getObjectHashCode(o), 1);
                }
            }
        }
    }

    public void addToQueueAll(Collection c)
	{
		synchronized (queue)
		{
            for (Iterator iterator = c.iterator(); iterator.hasNext(); )
            {
                Object o = iterator.next();
                if(canBeAdded(o))
                {
                    queue.add(o);
                    markObjectAsAdded(o);
                }
            }
		}
	}


	public void run()
	{
		while(running)
		{
			if(!queue.isEmpty())
            {
                if(waitDelayForMinimalSize > 0 && queue.size() < chunkSize)
                {
                    try
                    {
                        Thread.sleep(waitDelayForMinimalSize);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                synchronized (queue)
                {
                    toSend.addAll(queue);
                    queue.clear();
                }

                if(!toSend.isEmpty())
                {
                    preSendPartially(toSend);
                }
            }
			Thread.yield();
			try
			{
				Thread.sleep(sendPeriod);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

    public void preSendPartially(ArrayList toSend)
    {
        sendByPortions(toSend);
    }

    public void stop()
	{
		running = false;
	}


    public void setOnlyUniqueAllowed(boolean onlyUniqueAllowed)
    {
        this.onlyUniqueAllowed = onlyUniqueAllowed;
        if(!onlyUniqueAllowed)
        {
            uniqueKeys = null;
        }
    }

    public ArrayList sendByPortions(List allData)
	{
        startWhenIteration = System.currentTimeMillis();
        sendingQueueSize = allData.size();
        ArrayList returnList = new ArrayList();
        int byHowManyIteration = chunkSize;
        steps = allData.size() / byHowManyIteration + 1;
        int totalSend = 0;
        int totalFailed = 0;
        for(currentStep = 0; currentStep < steps; currentStep++)
		{
			try
			{
				List subList = allData.subList(currentStep * byHowManyIteration, Math.min((currentStep + 1) * byHowManyIteration, allData.size()));
                int subListSize = subList.size();
				boolean ok = false;
				while(!ok)
				{
                    try
					{
						Collection result = senderFeedI.sendData(subList);
						if(result != null)
						{
                            returnList.addAll(result);
						}
						ok = true;
                        totalSend += subListSize;
					}
					catch (Exception e)
					{
						e.printStackTrace();
                        if(!repeatFailedSend)
                        {
                            ok = true;
                            totalFailed += subListSize;
                        }
                        else
                        {
                            Thread.sleep(smallDelayPeriod);
                        }
					}
				}
                if(onlyUniqueAllowed)
                {
                    for (Iterator iterator = subList.iterator(); iterator.hasNext(); )
                    {
                        Object o = iterator.next();
                        uniqueKeys.remove(getObjectHashCode(o));
                    }
                }
            }
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
        sendingQueueSize = 0;
        currentStep = 0;
        steps = 0;
        if(logs.size() > maxLogsCount)
        {
            logs.remove(0);
        }
        toSend.clear();
        logs.add(new Object[]{startWhenIteration, System.currentTimeMillis(), totalSend, totalFailed});
        
		return returnList;
	}

    private void ensureKeys()
    {
        if(uniqueKeys == null)
        {
            uniqueKeys = new HashMap();
            for (Iterator iterator = queue.iterator(); iterator.hasNext(); )
            {
                Object next = iterator.next();
                uniqueKeys.put(getObjectHashCode(next), 1);
            }
            for (Iterator iterator = toSend.iterator(); iterator.hasNext(); )
            {
                Object next = iterator.next();
                uniqueKeys.put(getObjectHashCode(next), 1);
            }
        }
    }

    public int getChunkSize()
	{
		return chunkSize;
	}

	public void setChunkSize(int chunkSize)
	{
		this.chunkSize = chunkSize;
	}

	public int getSendPeriod()
	{
		return sendPeriod;
	}

	public void setSendPeriod(int sendPeriod)
	{
		this.sendPeriod = sendPeriod;
	}

	public int getSmallDelayPeriod()
	{
		return smallDelayPeriod;
	}

	public void setSmallDelayPeriod(int smallDelayPeriod)
	{
		this.smallDelayPeriod = smallDelayPeriod;
	}

    public boolean isOnlyUniqueAllowed()
    {
        return onlyUniqueAllowed;
    }

    public int getSteps()
    {
        return steps;
    }

    public int getCurrentStep()
    {
        return currentStep;
    }

    public int getMaxLogsCount()
    {
        return maxLogsCount;
    }

    public void setMaxLogsCount(int maxLogsCount)
    {
        this.maxLogsCount = maxLogsCount;
    }

    public ArrayList getLogs()
    {
        return logs;
    }

    public int getQueueLength()
    {
        return queue.size();
    }

    public int getSendingQueueLength()
    {
        return sendingQueueSize;
    }

    public long getStartWhenIteration()
    {
        return startWhenIteration;
    }

    public int getWaitDelayForMinimalSize()
    {
        return waitDelayForMinimalSize;
    }

    public void setWaitDelayForMinimalSize(int waitDelayForMinimalSize)
    {
        this.waitDelayForMinimalSize = waitDelayForMinimalSize;
    }

    public boolean isRepeatFailedSend()
    {
        return repeatFailedSend;
    }

    public void setRepeatFailedSend(boolean repeatFailedSend)
    {
        this.repeatFailedSend = repeatFailedSend;
    }
}

