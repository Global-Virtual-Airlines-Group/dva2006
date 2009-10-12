// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import org.deltava.util.cache.CacheInfo;

/**
 * This interface is used by Data Access Objects that implement an internal cache, to provide
 * visibility for cache statistics.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public interface CachingDAO {
	
	/**
	 * Returns the cache information.
	 * @return a CacheInfo bean
	 */
	public abstract CacheInfo getCacheInfo();
}