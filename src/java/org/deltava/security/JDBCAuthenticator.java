// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.deltava.beans.Person;

import org.deltava.util.*;

/**
 * An authenticator to validate users against a JDBC data source.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class JDBCAuthenticator implements SQLAuthenticator {

	private static final Logger log = Logger.getLogger(JDBCAuthenticator.class);

	private final ThreadLocal<Connection> _con = new ThreadLocal<Connection>();
	private final Properties _props = new Properties();

	/**
	 * Initialize the authenticator.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {

		_props.clear();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			throw new SecurityException(ie.getMessage());
		}

		// Convert the authenticator properties into JDBC properties
		_props.setProperty("user", _props.getProperty("jdbc.user"));
		_props.setProperty("password", _props.getProperty("jdbc.pwd"));
	}

	/**
	 * Helper method to return a JDBC connection to the data source.
	 */
	private Connection getConnection() throws SQLException {
		return (_con.get() == null) ? DriverManager.getConnection(_props.getProperty("jdbc.url"), _props) : _con.get();
	}
	
	/**
	 * Helper method to close the JDBC connection if not provided by external code.
	 */
	private void closeConnection() {
		Connection con = _con.get();
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				log.warn("Cannot close connection - " + e.getMessage());
			} finally {
				_con.remove();
			}
		}
	}
	
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
		_con.set(null);
	}

	/**
	 * Authenticates a user by validating the password against the database. If a database cryptographic function is
	 * set, it is applied to the password within the statement. <i>This may result in credential data being passed over
	 * the connection to the JDBC data source, depending on the driver implementation. </i>
	 * @param usr the User bean
	 * @param pwd the supplied password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {
		boolean isAuth = false;
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM AUTH WHERE (USER=?) AND (PWD="
					+ _props.getProperty("jdbc.cryptfunc", "") + "(?)) AND (ENABLED=?)");
			ps.setString(1, usr.getDN());
			ps.setString(2, pwd);
			ps.setBoolean(3, true);

			// Execute the query and check what we get back
			ResultSet rs = ps.executeQuery();
			isAuth = rs.next() ? (rs.getInt(1) == 1) : false;

			// Clean up
			rs.close();
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("Authentication failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
		
		// If we haven't authenticated, throw an execption
		if (!isAuth)
			throw new SecurityException("Invalid password for " + usr.getDN());

		log.info(usr.getName() + " authenticated");
	}

	/**
	 * Updates a User's password. If a database cryptographic function is set, it is applied to the password within the
	 * statement. <i>This may result in credential data being passed over the connection to the JDBC data source,
	 * depending on the driver implementation. </i>
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		log.debug("Updating password for " + usr.getDN() + " in Directory");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO AUTH (USER, PWD, ENABLED) VALUES (?, ");
		sqlBuf.append(_props.getProperty("jdbc.cryptfunc"));
		sqlBuf.append("(?), ?)");
		
		int rowsUpdated = 0;
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, usr.getDN());
			ps.setString(2, pwd);
			ps.setBoolean(3, true);

			// Update the directory
			rowsUpdated = ps.executeUpdate();

			// Clean up
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("Password Update failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
		
		// If no rows were updated, throw an exception
		if (rowsUpdated == 0)
			throw new SecurityException("Unknown User Name - " + usr.getDN());
	}

	/**
	 * Adds a user to the Directory. If a database cryptographic function is set, it is applied to the password within
	 * the statement. <i>This may result in credential data being passed over the connection to the JDBC data source,
	 * depending on the driver implementation. </i>
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void add(Person usr, String pwd) throws SecurityException {
		log.debug("Adding user " + usr.getDN() + " to Directory");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO AUTH (USER, PWD, ALIAS, ENABLED) VALUES (?, ");
		sqlBuf.append(_props.getProperty("jdbc.cryptfunc"));
		sqlBuf.append("(?), ?, ?)");

		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, pwd);
			ps.setString(2, usr.getDN());
			ps.setString(3, null);
			ps.setBoolean(4, true);

			// Update the directory and clean up
			ps.executeUpdate();
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("User addition failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * Checks wether this Authenticator will accept a particular Person. The Person's Directory Name
	 * field must be non-empty.
	 * @param usr the user bean
	 * @return TRUE if the Directory Name field is not empty, otherwise FALSE
	 */
	public boolean accepts(Person usr) {
		return ((usr != null) && (!StringUtils.isEmpty(usr.getDN())));
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists, otherwise FALSE
	 * @throws SecurityException if a JDBC error occurs
	 */
	public boolean contains(Person usr) throws SecurityException {
		boolean isOK = false;
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM AUTH WHERE (USER=?)");
			ps.setString(1, usr.getDN());

			// Execute the query
			ResultSet rs = ps.executeQuery();
			isOK = rs.next() ? (rs.getInt(1) == 1) : false;

			// Clean up
			rs.close();
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("User search failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
		
		return isOK;
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void remove(Person usr) throws SecurityException {
		log.debug("Removing user " + usr.getDN() + " from Directory");
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("DELETE FROM AUTH WHERE (USER=?)");
			ps.setString(1, usr.getDN());

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("User removal failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * Disables a user's account.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	public void disable(Person usr) throws SecurityException {
		log.debug("Disabling user " + usr.getName());
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("UPDATE AUTH SET ENABLED=? WHERE (USER=?)");
			ps.setBoolean(1, true);
			ps.setString(2, usr.getDN());

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("User disable failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
	}
	
	/**
    * Renames a user in the Directory.
    * @param usr the user bean
    * @param newName the new fully-qualified directory 
    * @throws SecurityException if an error occurs
    */
	public void rename(Person usr, String newName) throws SecurityException {
	   log.debug("Renaming user " + usr.getDN() + " to " + newName);
		if (!contains(usr))
			throw new SecurityException(usr.getDN() + " not found");
		
		try {
		   Connection c = getConnection();
		   PreparedStatement ps = c.prepareStatement("UPDATE AUTH SET USER=? WHERE (USER=?)");
		   ps.setString(1, newName);
		   ps.setString(2, usr.getDN());
		   
			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (SQLException se) {
			throw new SecurityException("User rename failure for " + usr.getDN(), se);
		} finally {
			closeConnection();
		}
	}
}