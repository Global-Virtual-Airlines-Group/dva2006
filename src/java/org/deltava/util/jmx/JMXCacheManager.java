// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.util.*;
import java.time.Instant;

import org.deltava.util.cache.*;

/**
 * A JMX bean to export Cache Manager statistics.
 * @author Luke
 * @version 10.3
 * @since 10.2
 */

public class JMXCacheManager implements CacheManagerMXBean, JMXRefresh {
	
	private final String _code;
	private final Collection<? super CacheMBean> _info = new ArrayList<CacheMBean>();

	private long _reqs;
	private long _hits;
	
	private long _prevReqs;
	private long _prevHits;
	
	private Instant _lastUpdated;

	/**
	 * Creates the bean.
	 * @param code the virtual airline code
	 */
	public JMXCacheManager(String code) {
		super();
		_code = code;
	}
	
	@Override
	public Long getHits() {
		return Long.valueOf(_hits);
	}
	
	@Override
	public Long getRequests() {
		return Long.valueOf(_reqs);
	}
	
	@Override
	public Long getMisses() {
		return Long.valueOf(_reqs - _hits);
	}
	
	@Override
	public CacheMBean[] getCacheInfo() {
		return _info.toArray(new CacheMBean[0]);
	}
	
	@Override
	public Date getUpdateTime() {
		return new Date(_lastUpdated.toEpochMilli());
	}

	@Override
	public synchronized void update() {
		_info.clear();
		Collection<CacheInfo> info = CacheManager.getCacheInfo(); long reqs = 0; long hits = 0;
		for (CacheInfo inf : info) {
			CacheMBean mb = new CacheMBeanImpl(inf);
			reqs += inf.getRequests();
			hits += inf.getHits();
			_info.add(mb);
		}
		
		_reqs = Math.max(0, reqs - _prevReqs);
		_hits = Math.max(0, hits - _prevHits);
		_prevReqs = reqs;
		_prevHits = hits;
		_lastUpdated = Instant.now();
	}
	
	@Override
	public String toString() {
		return String.format("%s JMX Cache Manager", _code);
	}
}