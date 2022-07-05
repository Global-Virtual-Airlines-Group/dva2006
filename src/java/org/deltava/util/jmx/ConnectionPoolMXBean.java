// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import javax.management.MXBean;

/**
 * An interface for JMX Beans that monitor a JDBC connection pool.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

@MXBean
public interface ConnectionPoolMXBean {

	/**
	 * Returns a collection of connection information.
	 * @return a Collection of ConnectionMBeans
	 */
	public ConnectionMBean[] getPoolInfo();
}
