// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import java.util.Date;

import javax.management.MXBean;

/**
 * An interface for JMX Beans that monitor a JDBC connection pool.
 * @author Luke
 * @version 10.3
 * @since 10.2
 */

@MXBean
public interface ConnectionPoolMXBean {

	/**
	 * The last update time.
	 * @return the update date/time
	 */
	public Date getUpdateTime();
	
	/**
	 * Returns the number of requests since the last update.
	 * @return the number of connection pool requests
	 */
	public Long getRequests();
	
	/**
	 * Returns the current size of the connection pool.
	 * @return the number of connections
	 */
	public Integer getSize();
	
	/**
	 * Returns the length of the maximum connection borrow time since the last refresh.
	 * @return the borrow time in milliseconds
	 */
	public Integer getMaxBorrowTime();
	
	/**
	 * Returns the length of the maximum connection wait time since the last refresh.
	 * @return the wait time in milliseconds
	 */
	public Integer getMaxWaitTime();
	
	/**
	 * Returns a collection of connection information.
	 * @return a Collection of ConnectionMBeans
	 */
	public ConnectionMBean[] getPoolInfo();
}