// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.util.Date;

import javax.management.MXBean;

/**
 * An interface for cache manager JMX Beans. 
 * @author Luke
 * @version 10.4
 * @since 10.2
 * @see org.deltava.util.cache.CacheManager
 */

@MXBean
public interface CacheManagerMXBean {
	
	/**
	 * The last update time.
	 * @return the update date/time
	 */
	public Date getUpdateTime();
	
	/**
	 * Recent cache request count.
	 * @return the number of cache requests since the last refresh
	 */
	public Long getRequests();
	
	/**
	 * Recent cache hit count.
	 * @return the number of cache hits since the last refresh
	 */
	public Long getHits();
	
	/**
	 * Recent cache miss count.
	 * @return the number of cache misses since the last refresh
	 */
	public Long getMisses();
	
	/**
	 * Returns the cache hit ratio.
	 * @return the cache hit percentage
	 */
	public Float getRatio();
	
	/**
	 * Returns a collection of cache information.
	 * @return a Collection of CacheMBeans
	 */
	public CacheMBean[] getCacheInfo();
}