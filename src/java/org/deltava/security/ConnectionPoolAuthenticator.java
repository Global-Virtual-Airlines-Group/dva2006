// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.util.Properties;
import java.io.IOException;

import org.deltava.jdbc.*;

import org.deltava.util.ConfigLoader;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to support Authenticators that use a JDBC Connection Pool.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ConnectionPoolAuthenticator implements SQLAuthenticator {
	
	private ConnectionPool _pool;
	protected final Properties _props = new Properties();
	private final ThreadLocal<Connection> _con = new ThreadLocal<Connection>();

	/**
	 * Provides the JDBC connection for this Authenticator to use.
	 * @param c the Connection to use
	 */
	public void setConnection(Connection c) {
		_con.set(c);
	}
	
	/**
	 * Clears the explicit JDBC connection for an Authenticator to use, reverting to default behavior.
	 */
	public void clearConnection() {
		_con.remove();
	}

	/**
	 * Initializes the Authenticator and gets a handle to the Connection Pool.
	 * @param propsFile the property file
	 * @throws SecurityException if the Connection Pool cannot be found
	 */
	public void init(String propsFile) throws SecurityException {
		_pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		if (_pool ==  null)
			throw new SecurityException("JDBC Connection Pool not found");
		
		// Load the properties
		_props.clear();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			throw new SecurityException(ie.getMessage());
		}
	}

	/**
	 * Helper method to return a JDBC connection to the data source.
	 * @return a JDBC connection
	 */
	protected Connection getConnection() throws ConnectionPoolException {
		return (_con.get() == null) ? _pool.getConnection(true) : _con.get();
	}
	
	/**
	 * Helper method to close the JDBC connection if not provided by external code.
	 * @param c the Connection to release
	 */
	protected void closeConnection(Connection c) {
		if (_con.get() == null)
			_pool.release(c);
	}
}