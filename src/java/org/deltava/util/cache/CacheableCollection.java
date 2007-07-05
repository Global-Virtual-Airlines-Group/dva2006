// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Collection;

/**
 * An interface to label cacheable Collections.
 * @author LKolin
 * @version 1.0
 * @since 1.1
 */

public interface CacheableCollection<E> extends Collection<E>, Cacheable {

	/**
	 * Returns the cache key.
	 */
	public Object cacheKey();
}