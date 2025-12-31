// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

/**
 * A JMX bean to track logged-in users.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public interface UserCountMXBean {
	
	/**
	 * Returns the application code.
	 * @return the code
	 */
	public String getCode();
	
	/**
	 * Returns the number of authenticated users.
	 * @return the number of users
	 */
	public Integer getUsers();
	
	/**
	 * Returns the maximum number of authenticated users.
	 * @return the maximum number of users
	 */
	public Integer getMaxUsers();
}