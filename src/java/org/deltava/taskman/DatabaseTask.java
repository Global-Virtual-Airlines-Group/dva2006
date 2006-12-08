// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.sql.*;

import org.deltava.jdbc.*;
import org.deltava.util.system.SystemData;

/**
 * A class to support scheduled Tasks that access the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DatabaseTask extends Task {

	private Connection _con;
	private boolean _oldCommitLevel;

	/**
	 * Creates a new Database Task.
	 * @param name the task name
	 */
	protected DatabaseTask(String name, Class loggerClass) {
		super(name, loggerClass);
	}

	/**
	 * Obtains a connection from the system connection pool.
	 * @return a JDBC Connection
	 * @throws ConnectionPoolException if a Connection Pool error occurs
	 */
	protected Connection getConnection() throws ConnectionPoolException {
		ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		if (pool == null)
			throw new ConnectionPoolException("No Connection Pool defined", false);

		// Check if a connection has already been reserved
		if (_con != null)
			throw new IllegalStateException("Connection already reserved");

		_con = pool.getConnection(false);
		return _con;
	}

	/**
	 * Releases the connection used by this task.
	 */
	protected void release() {
		ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		if ((pool == null) || (_con == null))
			return;

		// Return the connection and record the back-end usage
		pool.release(_con);
		_con = null;
	}

	/**
	 * Marks the start of a multi-step database transaction. This turns off the autoCommit property of the JDBC
	 * connection, if it is already set.
	 * @throws TransactionException if a JDBC error occurs.
	 * @see DatabaseTask#commitTX()
	 * @see DatabaseTask#rollbackTX()
	 * @see Connection#setAutoCommit(boolean)
	 */
	protected void startTX() throws TransactionException {
		try {
			_oldCommitLevel = _con.getAutoCommit();
			_con.setAutoCommit(false);
		} catch (SQLException se) {
			throw new TransactionException(se);
		}
	}

	/**
	 * Commits a multi-step transaction to the database. This calls {@link Connection#commit()}on the JDBC connection,
	 * and then restores the old autoCommit property for the Connection.
	 * @throws TransactionException if a JDBC error occurs
	 * @see DatabaseTask#startTX()
	 * @see DatabaseTask#rollbackTX()
	 * @see Connection#setAutoCommit(boolean)
	 */
	protected void commitTX() throws TransactionException {
		try {
			_con.commit();
			_con.setAutoCommit(_oldCommitLevel);
		} catch (SQLException se) {
			throw new TransactionException(se);
		}
	}

	/**
	 * Rolls back a multi-step transaction before it is completed. This calls {@link Connection#rollback()}on the JDBC
	 * connection, and then resotres the old autoCommit property for the Connection. Since this is designed to be called
	 * in catch blocks, it eats exceptions.
	 * @see DatabaseTask#startTX()
	 * @see DatabaseTask#commitTX()
	 * @see Connection#setAutoCommit(boolean)
	 */
	protected void rollbackTX() {
		try {
			_con.rollback();
			_con.setAutoCommit(_oldCommitLevel);
		} catch (Exception e) {
		}
	}
}