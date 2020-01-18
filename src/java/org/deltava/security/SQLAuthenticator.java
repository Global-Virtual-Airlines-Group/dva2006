// Copyright 2007, 2009, 2010, 2012, 2015, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.io.*;
import java.sql.*;
import java.util.Properties;

import org.deltava.util.ConfigLoader;

/**
 * An abstract class to support Authenticators that use a JDBC Connection Pool.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public abstract class SQLAuthenticator implements Authenticator {
	
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
	 * Clears the JDBC connection for an Authenticator to use.
	 */
	@Override
	public void close() {
		_con.remove();
	}

	/**
	 * Initializes the Authenticator.
	 * @param propsFile the property file
	 * @throws SecurityException if the properties cannot be loaded
	 */
	@Override
	public void init(String propsFile) throws SecurityException {
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
	protected Connection getConnection() {
		return _con.get();
	}
}