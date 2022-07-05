// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.util.*;

import org.deltava.util.cache.*;

/**
 * A JMX bean to export Cache Manager statistics.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class JMXCacheManager implements CacheManagerMXBean, JMXRefresh {
	
	private final String _code;
	private final Collection<? super CacheMBean> _info = new ArrayList<CacheMBean>();

	/**
	 * 
	 * @param code
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
	public synchronized void update() {
		_info.clear();
		Collection<CacheInfo> info = CacheManager.getCacheInfo();
		info.stream().map(CacheMBeanImpl::new).forEach(_info::add);
	}
	
	@Override
	public String toString() {
		return _code + " JMX Cache Manager";
	}
}