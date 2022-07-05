// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

/**
 * A JMX bean interface for JDBC connection information.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public interface ConnectionMBean {

	/**
	 * Returns the connection ID.
	 * @return the ID
	 */
	public Integer getID();

	/**
	 * Returns whether the connection is dynamic.
	 * @return TRUE if dynamic, otherwise FALSE
	 */
	public Boolean isDynamic();
	
	/**
	 * Returns whether the connection is currently in use.
	 * @return TRUE if in use, otherwise FALSE
	 */
	public Boolean inUse();
	
	/**
	 * Returns the number of times this connection has been reserved.
	 * @return the number of reservations
	 */
	public Long getUseCount();
	
	/**
	 * Returns the total amount of time this connection has been reserved.
	 * @return the total time in milliseconds
	 */
	public Long getTotalUse();
	
	/**
	 * Returns the average amount of time a connection has been used when reserved.
	 * @return the average reservation time in milliseconds, or zero if never reserved
	 */
	public Integer getAverageUse();
	
	/**
	 * Returns the time this connection was last reserved.
	 * @return the last reservation date/time
	 */
	public java.time.Instant getLastUse();
}