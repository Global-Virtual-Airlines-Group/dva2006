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
	 * Returns a collection of connection information.
	 * @return a Collection of ConnectionMBeans
	 */
	public ConnectionMBean[] getPoolInfo();
}