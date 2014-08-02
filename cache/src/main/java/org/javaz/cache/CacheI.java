package org.javaz.cache;

/**
 *
 */
public interface CacheI
{
    public long getTimeToLive();

    /**
     * Store TTL as property. If it changed to lesser value, expired should be removed.
     */
    public void setTimeToLive(long timeToLive);

    /**
     * Method to store value in cache
     *
     * @param key,  may not be null
     * @param value - any value object
     * @return old value or null
     */
    public Object put(Object key, Object value);

    /**
     * Clear all expired values, only if needed (this provided by storing time of nearest object expiration)
     * After removal objects calls findNextExpireTime(), to set next time to clearExpired.
     * <p>
     * This method should be called in almost all methods (containsKey, containsValue, get, isEmpty)
     */
    public void clearExpired();

    /**
     * Clears both HashMaps, but not re-initialize their sizes.
     */
    public void clear();

    public boolean containsKey(Object key);

    public boolean containsValue(Object value);

    public Object get(Object key);

    public boolean isEmpty();

    /**
     * Remove by key
     *
     * @param key - key to remove
     * @return - object, if any
     */
    public Object remove(Object key);

    public int size();
}
