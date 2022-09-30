// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.time.Instant;
import java.util.*;

import org.gvagroup.jdbc.*;

/**
 * A JMX bean to export JDBC Connection Pool statistics.
 * @author Luke
 * @version 10.3
 * @since 10.2
 */

public class JMXConnectionPool implements ConnectionPoolMXBean, JMXRefresh {
	
	private final ConnectionPool _pool;
	private final String _code;
	private final Collection<? super ConnectionMBean> _info = new ArrayList<ConnectionMBean>();
	
	private Instant _lastUpdated;
	
	/**
	 * Initializes the bean.
	 * @param code the application code
	 * @param pool the JDBC connection pool
	 */
	public JMXConnectionPool(String code, ConnectionPool pool) {
		super();
		_code = code;
		_pool = pool;
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
	public synchronized void update() {
		_info.clear();
		Collection<ConnectionInfo> info = _pool.getPoolInfo();
		info.stream().map(ConnectionMBeanImpl::new).forEach(_info::add);
		_lastUpdated = Instant.now();
	}
	
	@Override
	public String toString() {
		return _code + " JMX JDBC Connection Pool";
	}
}