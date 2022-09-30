// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.time.Instant;

import javax.management.MXBean;

/**
 * An interface for cache manager JMX Beans. 
 * @author Luke
 * @version 10.3
 * @since 10.2
 * @see org.deltava.util.cache.CacheManager
 */

@MXBean
public interface CacheManagerMXBean {
	
	/**
	 * The last update time.
	 * @return the update date/time
	 */
	public Instant getUpdateTime();

	/**
	 * Returns a collection of cache information.
	 * @return a Collection of CacheMBeans
	 */
	public CacheMBean[] getCacheInfo();
}