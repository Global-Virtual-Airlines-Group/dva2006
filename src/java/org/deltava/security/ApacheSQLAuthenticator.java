// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.crypt.MessageDigester;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.util.Base64;
import org.deltava.util.ConfigLoader;
import org.deltava.util.system.SystemData;

/**
 * An Authenticator to authenticate users against Apache2-style database tables. Unlike the {@link SQLAuthenticator}
 * class, this uses the existing JDBC Connection Pool.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApacheSQLAuthenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(ApacheSQLAuthenticator.class);

	private ConnectionPool _pool;
	private Properties _props;

	/**
	 * Initialize the authenticator.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {
		_pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		
		_props = new Properties();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading " + propsFile + " - " + ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}
	}

	/**
	 * Authenticates a user by validating the password against the database.
	 * @param usr the User bean
	 * @param pwd the supplied password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {

		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String pwdHash = "{SHA}" + Base64.encode(md.digest(pwd.getBytes()));
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(*) FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?) AND (PWD=?)");
		
		boolean isOK = false;
		Connection con = null;
		try {
			con = _pool.getConnection(true);
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());
			ps.setString(2, pwdHash);
			
			// Execute the Query
			ResultSet rs = ps.executeQuery();
			isOK = rs.next() ? (rs.getInt(1) == 1) : false;
			
			// Clean up and return
			rs.close();
			ps.close();
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			_pool.release(con);
		}

		// Fail if we're not authenticated
		if (!isOK)
			throw new SecurityException("Cannot authenticate " + usr.getDN() + " - Invalid Credentials");
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists, otherwise FALSE
	 * @throws SecurityException if a JDBC error occurs
	 */
	public boolean contains(Person usr) throws SecurityException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(*) FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");
		
		Connection con = null;
		try {
			con = _pool.getConnection(true);
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());

			// Execute the query
			ResultSet rs = ps.executeQuery();
			boolean hasUser = rs.next() ? (rs.getInt(1) > 0) : false;

			// Clean up and return
			rs.close();
			ps.close();
			return hasUser;
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			_pool.release(con);
		}
	}

	/**
	 * Updates a User's password.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		log.debug("Updating password for " + usr.getDN() + " in Directory");
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" (ID, PWD) VALUES (?,?)");
		
		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String pwdHash = "{SHA}" + Base64.encode(md.digest(pwd.getBytes())); 

		Connection con = null;
		try {
			con = _pool.getConnection(true);
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());
			ps.setString(2, pwdHash);
			
			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			_pool.release(con);
		}
	}

	/**
	 * Adds a user to the Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		log.debug("Adding user " + usr.getDN() + " to Directory");
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" (ID, PWD) VALUES (?,?)");
		
		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String pwdHash = "{SHA}" + Base64.encode(md.digest(pwd.getBytes())); 

		Connection con = null;
		try {
			con = _pool.getConnection(true);
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());
			ps.setString(2, pwdHash);
			
			// Write the row
			ps.executeUpdate();
			ps.close();
			
			// If we have an alias, then update it
			if ((usr instanceof Pilot) && (((Pilot)usr).getLDAPName() != null)) {
				ps = con.prepareStatement("INSERT INTO common.AUTH_ALIAS (ID, USERID) VALUES (?, ?)");
				ps.setInt(1, usr.getID());
				ps.setString(2, ((Pilot) usr).getLDAPName());
				ps.executeUpdate();
				ps.close();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			_pool.release(con);
		}
	}

	/**
	    * Renames a user in the Directory. Since the Apache Authenticator relies upon database IDs, this is not implemented.
	    * @param usr the user bean
	    * @param newName the new fully-qualified directory 
	    * @throws SecurityException if an error occurs
	    */
	public void rename(Person usr, String newName) throws SecurityException {
		if (!contains(usr))
			throw new SecurityException(usr.getID() + " not found");
	}

	/**
	 * Removes a user from the Directory. This will fail silently if the user does not exist in the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void removeUser(Person usr) throws SecurityException {
		log.debug("Removing user " + usr.getName() + " from Directory");
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");
		
		Connection con = null;
		try {
			con = _pool.getConnection(false);
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			_pool.release(con);
		}
	}
}