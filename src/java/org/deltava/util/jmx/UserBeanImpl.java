// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import org.deltava.security.UserPool;

/**
 * A JMX bean to store the number of authenticated users.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class UserBeanImpl implements UserCountMXBean {
	
	private final String _code;
	private int _maxUsers;

	/**
	 * Initializes the bean.
	 * @param code the application code 
	 */
	public UserBeanImpl(String code) {
		super();
		_code = code;
	}

	@Override
	public String getCode() {
		return _code;
	}

	@Override
	public Integer getUsers() {
		int users = UserPool.getSize();
		_maxUsers = Math.max(users, _maxUsers);
		return Integer.valueOf(users);
	}
	
	@Override
	public Integer getMaxUsers() {
		return Integer.valueOf(_maxUsers);
	}
}