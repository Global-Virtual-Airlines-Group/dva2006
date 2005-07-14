package org.deltava.util.cache;

/**
 * An interface to mark a class as cacheable.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Cacheable {

    /**
     * Returns the cache key for this object. Caches call this method when 
     * @return the cache key for the object
     */
    public Object cacheKey();
}