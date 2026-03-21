// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import org.deltava.util.dns.Resolver;

/**
 * A JMX bean to export DNS resolver statistics.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class JMXResolver implements ResolverMXBean, JMXRefresh {
	
	private final String _code;
	private final Resolver _solv;
	
	private long _reqs;
	private long _hits;
	private long _errors;
	
	private long _prevReqs;
	private long _prevHits;
	private long _prevErrors;

	/**
	 * Creates the bean.
	 * @param code the application code
	 * @param solv the DNS resolver 
	 */
	public JMXResolver(String code, Resolver solv) {
		super();
		_code = code;
		_solv = solv;
	}

	@Override
	public String getCode() {
		return _code;
	}

	@Override
	public Integer getThreads() {
		return Integer.valueOf(_solv.getThreadCount());
	}

	@Override
	public Long getRequests() {
		return Long.valueOf(_reqs);
	}

	@Override
	public Long getHits() {
		return Long.valueOf(_hits);
	}
	
	@Override
	public Long getMisses() {
		return Long.valueOf(_reqs - _hits);
	}
	
	@Override
	public Long getRejected() {
		return Long.valueOf(_errors);
	}

	@Override
	public Float getHitRate() {
		return Float.valueOf((_reqs == 0) ? 0 : _hits * 1f / _reqs);
	}

	@Override
	public synchronized void update() {
		long hits = _solv.getHits();
		long reqs = _solv.getRequests();
		long errors = _solv.getRejected();
		
		_reqs = Math.max(0, reqs - _prevReqs);
		_hits = Math.max(0, hits - _prevHits);
		_errors = Math.max(0, errors - _prevErrors);
		_prevReqs = reqs;
		_prevHits = hits;
		_prevErrors = errors;
	}
}