// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

/**
 * A Flushable Data Access Object contains a static buffer that allows code to buffer writes to the database. Once
 * the buffer has reached some pre-determined size or maximum age, an instance can be created and the buffer
 * can be flushed.
 * @author Luke
 * @version 1.0
 * @since 1.1
 */

public interface FlushableDAO<E> {
	
	/**
	 * Flushes the buffer.
	 * @return the number of entries written to the database
	 * @throws DAOException if a JDBC error occurs
	 */
	public int flush() throws DAOException;
}
