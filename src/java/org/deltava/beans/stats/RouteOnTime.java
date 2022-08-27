// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.schedule.RoutePair;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to provide on-time statistics by flight route.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class RouteOnTime extends OnTimeStatsEntry implements Cacheable {
	private final String _cacheKey;
	
	/**
	 * Creates the bean.
	 * @param cacheKey the cache key
	 */
	public RouteOnTime(String cacheKey) {
		super(Instant.now());
		_cacheKey = cacheKey;
	}

	@Override
	public Object cacheKey() {
		return _cacheKey;
	}
	
	/**
	 * Creates an On-Time cache key.
	 * @param rp the RoutePair
	 * @param db the database name
	 * @return the cache key
	 */
	public static String createKey(RoutePair rp, String db) {
		StringBuilder buf = new StringBuilder(db);
		buf.append("!!");
		buf.append(rp.createKey());
		return buf.toString();
	}
}