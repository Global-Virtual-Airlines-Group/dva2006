// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

/**
 * An interface to mark a class as cacheable.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Cacheable {

    /**
     * Returns the cache key for this object. Caches call this method when adding the object.
     * @return the cache key for the object
     */
    public Object cacheKey();
}