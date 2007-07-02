// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

/**
 * A class used to track thread stack status when requesting JDBC Connections from a Connection Pool.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class StackTrace extends Throwable {
	
	/**
	 * Instantiates the stack trace for the current thread.
	 */
	StackTrace() {
		super(Thread.currentThread().getName());
	}
}