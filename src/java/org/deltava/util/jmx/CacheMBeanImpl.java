// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import org.deltava.util.cache.CacheInfo;

/**
 * A JMX MBean implementation for caches.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class CacheMBeanImpl implements CacheMBean {
	
	private final String _name;
	private final String _type;
	
	private final Long _hits;
	private final Long _requests;
	private final Long _size;
	private final Float _hitRate;

	/**
	 * Creates the bean.
	 * @param inf the CacheInfo
	 */
	CacheMBeanImpl(CacheInfo inf) {
		super();
		_name = inf.getID();
		_type = inf.getType();
		_hits = Long.valueOf(inf.getHits());
		_requests = Long.valueOf(inf.getRequests());
		_size = Long.valueOf(inf.getSize());
		_hitRate = Float.valueOf((inf.getRequests() == 0) ? 0f : (inf.getHits() * 1.0f / inf.getRequests()));
	}

	@Override
	public Long getRequests() {
		return _requests;
	}

	@Override
	public Long getHits() {
		return _hits;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public String getType() {
		return _type;
	}

	@Override
	public Long getSize() {
		return _size;
	}
	
	@Override
	public Float getHitRate() {
		return _hitRate;
	}
}