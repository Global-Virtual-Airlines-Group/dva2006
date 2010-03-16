// Copyright 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Collection;

/**
 * An interface to label cacheable Collections.
 * @author LKolin
 * @version 3.0
 * @since 1.1
 */

public interface CacheableCollection<E> extends Collection<E>, Cacheable {

	/**
	 * Returns the cache key.
	 * @return the cache key
	 */
	public Object cacheKey();
	
	/**
	 * Creates a copy of the collection.
	 * @return a copy of the Collection
	 */
	public CacheableCollection<E> clone();
}