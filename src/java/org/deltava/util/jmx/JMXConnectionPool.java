// Copyright 2022, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.time.Instant;
import java.util.*;

import org.gvagroup.pool.*;

/**
 * A JMX bean to export Connection Pool statistics.
 * @author Luke
 * @version 12.1
 * @since 10.2
 */

public class JMXConnectionPool implements ConnectionPoolMXBean, JMXRefresh {
	
	private final ConnectionPool<?> _pool;
	private final String _code;
	private final Collection<? super ConnectionMBean> _info = new ArrayList<ConnectionMBean>();
	
	private Instant _lastUpdated;
	
	private long _reqs;
	private long _prevReqs; 
	
	private int _size;
	private int _maxBorrowTime;
	private int _maxWaitTime;
	
	/**
	 * Initializes the bean.
	 * @param code the application code
	 * @param pool the JDBC connection pool
	 */
	public JMXConnectionPool(String code, ConnectionPool<?> pool) {
		super();
		_code = code;
		_pool = pool;
	}
	
	@Override
	public Integer getSize() {
		return Integer.valueOf(_size);
	}
	
	@Override
	public Long getRequests() {
		return Long.valueOf(_reqs);
	}

	@Override
	public ConnectionMBean[] getPoolInfo() {
		return _info.toArray(new ConnectionMBean[0]);
	}
	
	@Override
	public Date getUpdateTime() {
		return new Date(_lastUpdated.toEpochMilli());
	}
	
	@Override
	public Integer getMaxBorrowTime() {
		return Integer.valueOf(_maxBorrowTime);
	}
	
	@Override
	public Integer getMaxWaitTime() {
		return Integer.valueOf(_maxWaitTime);
	}
	
	@Override
	public synchronized void update() {
		_info.clear();
		Collection<ConnectionInfo> info = _pool.getPoolInfo();
		info.stream().map(ConnectionMBeanImpl::new).forEach(_info::add);
		_lastUpdated = Instant.now();
		long reqs = _pool.getTotalRequests();
		_size = (int) info.stream().filter(ConnectionInfo::getConnected).count();
		_reqs = reqs - _prevReqs;
		_maxBorrowTime = (int) _pool.getMaxBorrowTime().toMillis();
		_maxWaitTime = (int) _pool.getMaxWaitTime().toMillis();
		_pool.resetMaxTimes();
		_prevReqs = reqs;
	}
	
	@Override
	public String toString() {
		return _code + " JMX JDBC Connection Pool";
	}
}