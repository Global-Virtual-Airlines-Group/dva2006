// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

/**
 * This interface is used by caching Data Access Objects whose cache can
 * be invalidated by an external source. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public interface ClearableCachingDAO extends CachingDAO {

	/**
	 * Clears the cache.
	 */
	public void clear();
}