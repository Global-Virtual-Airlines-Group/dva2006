// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

/**
 * A JMX interface for caches.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public interface CacheMBean {

	/**
	 * Returns the number of requests made to this cache.
	 * @return the number of requests
	 */
	public Long getRequests();
	
	/**
	 * Returns the number of success requests made to this cache.
	 * @return the number of hits
	 */
	public Long getHits();
	
	/**
	 * Returns the cache name.
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Returns the cache type.
	 * @return the type name
	 */
	public String getType();
	
	/**
	 * Returns the number of items in the cache.
	 * @return the number of items
	 */
	public Long getSize();
	
	/**
	 * Returns the cache hit rate percentage.
	 * @return the percentage, or zero if no requests have been made
	 */
	public Float getHitRate();
}