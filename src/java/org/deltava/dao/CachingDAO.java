// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

/**
 * This interface is used by Data Access Objects that implement an internal cache, to provide
 * visibility for cache statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface CachingDAO {

	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public abstract int getHits();

	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public abstract int getRequests();
}