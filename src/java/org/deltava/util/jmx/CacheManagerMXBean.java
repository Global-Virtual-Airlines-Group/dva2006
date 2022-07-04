// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import javax.management.MXBean;

/**
 * An interface for cache manager JMX Beans. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 * @see org.deltava.util.cache.CacheManager
 */

@MXBean
public interface CacheManagerMXBean {

	/**
	 * Returns a collection of cache information.
	 * @return a Collection of CacheMBeans
	 */
	public CacheMBean[] getCacheInfo();
	
	/**
	 * Updates cache statistics when called by the JMX client.
	 */
	public void update();
}