// Copyright 2007, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Collection;

/**
 * An interface to label cacheable Collections.
 * @author Luke
 * @version 7.2
 * @since 1.1
 * @param <E> the cacheable object type
 */

public interface CacheableCollection<E> extends Collection<E>, Cacheable {

	@Override
	public Object cacheKey();
	
	/**
	 * Creates a copy of the collection.
	 * @return a copy of the Collection
	 */
	public CacheableCollection<E> clone();
}