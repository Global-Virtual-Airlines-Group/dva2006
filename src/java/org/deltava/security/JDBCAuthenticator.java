// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.deltava.beans.Person;
import org.deltava.util.ConfigLoader;

/**
 * An authenticator to validate users against a JDBC data source.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class JDBCAuthenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(JDBCAuthenticator.class);

	private Properties _props;

	/**
	 * Initialize the authenticator.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {

		_props = new Properties();
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
		return DriverManager.getConnection(_props.getProperty("jdbc.url"), _props);
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
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM AUTH WHERE (USER=?) AND (PWD="
					+ _props.getProperty("jdbc.cryptfunc", "") + "(?))");
			ps.setString(1, usr.getDN());
			ps.setString(2, pwd);

			// Execute the query and check what we get back
			ResultSet rs = ps.executeQuery();
			boolean isAuth = rs.next() ? (rs.getInt(1) == 1) : false;

			// Clean up
			rs.close();
			ps.close();
			c.close();

			// If we haven't authenticated, throw an execption
			if (!isAuth)
				throw new SecurityException("Invalid password for " + usr.getDN());

			log.info(usr.getName() + " authenticated");
		} catch (SQLException se) {
			log.warn(usr.getDN() + " Authentication FAILURE - " + se.getMessage());
			SecurityException e = new SecurityException("Authentication failure for " + usr.getDN());
			e.initCause(se);
			throw e;
		}
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
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO AUTH (USER, PWD) VALUES (?, ");
		sqlBuf.append(_props.getProperty("jdbc.cryptfunc"));
		sqlBuf.append("(?))");
		
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, usr.getDN());
			ps.setString(2, pwd);

			// Update the directory
			int rowsUpdated = ps.executeUpdate();

			// Clean up
			ps.close();
			c.close();

			// If no rows were updated, throw an exception
			if (rowsUpdated == 0) {
				log.warn(usr.getDN() + " password update FAILURE - Unknown User");
				throw new SecurityException("Unknown User Name - " + usr.getDN());
			}
		} catch (SQLException se) {
			log.warn(usr.getDN() + " password update FAILURE - " + se.getMessage());
			SecurityException e = new SecurityException("Password Update failure for " + usr.getDN());
			e.initCause(se);
			throw e;
		}
	}

	/**
	 * Adds a user to the Directory. If a database cryptographic function is set, it is applied to the password within
	 * the statement. <i>This may result in credential data being passed over the connection to the JDBC data source,
	 * depending on the driver implementation. </i>
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		log.debug("Adding user " + usr.getDN() + " to Directory");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO AUTH (USER, PWD, ALIAS) VALUES (?, ");
		sqlBuf.append(_props.getProperty("jdbc.cryptfunc"));
		sqlBuf.append("(?), ?)");

		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, pwd);
			ps.setString(2, usr.getDN());
			ps.setString(3, null);

			// Update the directory
			ps.executeUpdate();

			// Clean up
			ps.close();
			c.close();
		} catch (SQLException se) {
			log.warn(usr.getDN() + " user addition FAILURE - " + se.getMessage());
			SecurityException e = new SecurityException("User addition failure for " + usr.getDN());
			e.initCause(se);
			throw e;
		}
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists, otherwise FALSE
	 * @throws SecurityException if a JDBC error occurs
	 */
	public boolean contains(Person usr) throws SecurityException {
		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM AUTH WHERE (USER=?)");
			ps.setString(1, usr.getDN());

			// Execute the query
			ResultSet rs = ps.executeQuery();
			boolean isOK = rs.next() ? (rs.getInt(1) == 1) : false;

			// Clean up
			rs.close();
			ps.close();
			c.close();

			// Return result
			return isOK;
		} catch (SQLException se) {
			log.warn(usr.getDN() + " user search FAILURE - " + se.getMessage());
			SecurityException e = new SecurityException("User search failure for " + usr.getDN());
			e.initCause(se);
			throw e;
		}
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void removeUser(Person usr) throws SecurityException {
		log.debug("Removing user " + usr.getDN() + " from Directory");

		try {
			Connection c = getConnection();
			PreparedStatement ps = c.prepareStatement("DELETE FROM AUTH WHERE (USER=?)");
			ps.setString(1, usr.getDN());

			// Execute the update
			ps.executeUpdate();

			// Clean up
			ps.close();
			c.close();
		} catch (SQLException se) {
			log.warn(usr.getDN() + " user removal FAILURE - " + se.getMessage());
			SecurityException e = new SecurityException("User removal failure for " + usr.getDN());
			e.initCause(se);
			throw e;
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
		   
			// Execute the update
			ps.executeUpdate();

			// Clean up
			ps.close();
			c.close();
		} catch (SQLException se) {
		   log.warn(usr.getDN() + " user removal FAILURE - " + se.getMessage());
			SecurityException e = new SecurityException("User rename failure for " + usr.getDN());
			e.initCause(se);
			throw e;
		}
	}
}