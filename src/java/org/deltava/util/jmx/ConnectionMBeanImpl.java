// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.time.Instant;

import org.gvagroup.jdbc.ConnectionInfo;

/**
 * A JMX bean for JDBC connection information.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class ConnectionMBeanImpl implements ConnectionMBean {
	
	private final Integer _id;
	private final Boolean _isDynamic;
	private final Boolean _inUse;
	
	private final Long _useCount;
	private final Long _totalUse;
	
	private final Instant _lastUse;

	/**
	 * Creates the bean.
	 * @param inf a ConnectionInfo bean
	 */
	ConnectionMBeanImpl(ConnectionInfo inf) {
		super();
		_id = Integer.valueOf(inf.getID());
		_isDynamic = Boolean.valueOf(inf.getDynamic());
		_inUse = Boolean.valueOf(inf.getInUse());
		_useCount = Long.valueOf(inf.getUseCount());
		_totalUse = Long.valueOf(inf.getTotalUse());
		_lastUse = inf.getLastUsed();
	}

	@Override
	public Integer getID() {
		return _id;
	}

	@Override
	public Boolean isDynamic() {
		return _isDynamic;
	}

	@Override
	public Boolean inUse() {
		return _inUse;
	}

	@Override
	public Long getUseCount() {
		return _useCount;
	}

	@Override
	public Long getTotalUse() {
		return _totalUse;
	}

	@Override
	public Integer getAverageUse() {
		int avgUse = (_useCount.longValue() == 0) ? 0 : (int)(_totalUse.longValue() / _useCount.longValue());
		return Integer.valueOf(avgUse);
	}

	@Override
	public Instant getLastUse() {
		return _lastUse;
	}
}