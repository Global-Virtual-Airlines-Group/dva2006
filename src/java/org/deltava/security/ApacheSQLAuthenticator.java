// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.jdbc.*;
import org.deltava.util.*;

/**
 * An Authenticator to authenticate users against Apache2-style database tables. Unlike the {@link JDBCAuthenticator}
 * class, this uses the existing JDBC Connection Pool. Since this implements {@link SQLAuthenticator}, this behavior can
 * be overriden by providing a JDBC Connection to use.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApacheSQLAuthenticator extends ConnectionPoolAuthenticator {

	private static final Logger log = Logger.getLogger(ApacheSQLAuthenticator.class);

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
		StringBuilder sqlBuf = new StringBuilder("SELECT PWD FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");

		boolean isOK = false;
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());

			// Execute the Query
			ResultSet rs = ps.executeQuery();
			String goodPwd = rs.next() ? rs.getString(1) : null;

			// Clean up
			rs.close();
			ps.close();

			// If we got nothing, throw an exception
			isOK = pwdHash.equals(goodPwd);
			if (goodPwd == null) {
				SecurityException se = new SecurityException("Unknown User ID - " + usr.getName() + " (" + usr.getID() + ")");
				log.warn(se.getMessage());
				throw se;
			}
		} catch (ConnectionPoolException cpe) {
			throw new SecurityException(cpe);
		} catch (SQLException se) {
			throw new SecurityException(se);
		} finally {
			closeConnection(con);
		}

		// Fail if we're not authenticated
		if (!isOK) {
			SecurityException se = new SecurityException("Cannot authenticate " + usr.getName() + " (" + usr.getID()
					+ ") - Invalid Credentials");
			log.warn(se.getMessage());
			throw se;
		}
	}

	/**
	 * This Authenticator will accept all users.
	 * @param usr the user bean 
	 */
	public boolean accepts(Person usr) {
		return true;
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
			con = getConnection();
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
			closeConnection(con);
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
		sqlBuf.append(" (ID, PWD, ENABLED) VALUES (?, ?, ?)");

		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String pwdHash = "{SHA}" + Base64.encode(md.digest(pwd.getBytes()));

		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());
			ps.setString(2, pwdHash);
			ps.setBoolean(3, true);

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			closeConnection(con);
		}
	}

	/**
	 * Adds a user to the Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void add(Person usr, String pwd) throws SecurityException {
		log.debug("Adding user " + usr.getDN() + " to Directory");

		// Get the ID
		int id = (usr instanceof Applicant) ? ((Applicant) usr).getPilotID() : usr.getID();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" (ID, PWD, ENABLED) VALUES (?, ?, ?)");

		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String pwdHash = "{SHA}" + Base64.encode(md.digest(pwd.getBytes()));

		Connection con = null;
		boolean isAutoCommit = false;
		try {
			con = getConnection();
			isAutoCommit = con.getAutoCommit();
			if (isAutoCommit)
				con.setAutoCommit(false);
			
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, id);
			ps.setString(2, pwdHash);
			ps.setBoolean(3, true);

			// Write the row
			ps.executeUpdate();
			ps.close();

			// If we have an alias, then update it
			if ((usr instanceof Pilot) && (((Pilot) usr).getLDAPName() != null)) {
				ps = con.prepareStatement("INSERT INTO common.AUTH_ALIAS (ID, USERID) VALUES (?, ?)");
				ps.setInt(1, usr.getID());
				ps.setString(2, ((Pilot) usr).getLDAPName());
				ps.executeUpdate();
				ps.close();
			}
			
			// Commit the transaction
			con.commit();
			if (isAutoCommit)
				con.setAutoCommit(true);
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			closeConnection(con);
		}
	}

	/**
	 * Renames a user in the Directory. Since the Apache Authenticator relies upon database IDs, this is not
	 * implemented.
	 * @param usr the user bean
	 * @param newName the new fully-qualified directory
	 * @throws SecurityException if an error occurs
	 */
	public void rename(Person usr, String newName) throws SecurityException {
		if (!contains(usr))
			throw new SecurityException(usr.getID() + " not found");
	}
	
	/**
	 * Disables a user's account.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	public void disable(Person usr) throws SecurityException {
		log.debug("Disabling user " + usr.getName());
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" SET ENABLED=? WHERE (ID=?)");
		
		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setBoolean(1, false);
			ps.setInt(2, usr.getID());
			
			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			closeConnection(con);
		}
	}

	/**
	 * Removes a user from the Directory. This will fail silently if the user does not exist in the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void remove(Person usr) throws SecurityException {
		log.debug("Removing user " + usr.getName() + " from Directory");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");

		Connection con = null;
		try {
			con = getConnection();
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setInt(1, usr.getID());

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new SecurityException(e);
		} finally {
			closeConnection(con);
		}
	}
}