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
		Collection<CacheInfo> info = CacheManager.getCacheInfo();
		info.stream().map(CacheMBeanImpl::new).forEach(_info::add);
		_lastUpdated = Instant.now();
	}
	
	@Override
	public String toString() {
		return _code + " JMX Cache Manager";
	}
}