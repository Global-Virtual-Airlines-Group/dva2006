// Copyright 2006, 2007, 2008, 2010, 2012, 2014, 2015, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.util.Base64;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.crypt.MessageDigester;
import org.deltava.util.StringUtils;

/**
 * An Authenticator to authenticate users against Apache2-style database tables.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ApacheSQLAuthenticator extends SQLAuthenticator {

	private static final Logger log = Logger.getLogger(ApacheSQLAuthenticator.class);

	/**
	 * Authenticates a user by validating the password against the database.
	 * @param usr the User bean
	 * @param pwd the supplied password
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
	public void authenticate(Person usr, String pwd) throws SecurityException {

		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		Base64.Encoder b64e = Base64.getEncoder();
		String pwdHash = "{SHA}" + b64e.encodeToString(md.digest(pwd.getBytes()));

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PWD FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");

		try {
			String goodPwd = null; Connection con = getConnection(); 
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setInt(1, usr.getID());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						goodPwd = rs.getString(1);
				}
			}

			// If we got nothing or failed, throw an exception
			boolean isOK = pwdHash.equals(goodPwd);
			if (goodPwd == null)
				throw new SecurityException("Unknown User ID - " + usr.getName() + " (" + usr.getID() + ")");
			if (!isOK)
				throw new SecurityException("Cannot authenticate " + usr.getName() + " (" + usr.getID() + ") - Invalid Credentials");
		} catch (SQLException se) {
			throw new SecurityException(se);
		}
	}

	/**
	 * This Authenticator will accept all users.
	 * @param usr the user bean 
	 */
	@Override
	public boolean accepts(Person usr) {
		return true;
	}
	
	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists, otherwise FALSE
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
	public boolean contains(Person usr) throws SecurityException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(*) FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");

		try {
			boolean hasUser = false; Connection con = getConnection();
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setInt(1, usr.getID());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						hasUser = (rs.getInt(1) > 0);
				}
			}

			return hasUser;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Updates a User's password.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		if (log.isDebugEnabled())
			log.debug("Updating password for " + usr.getDN() + " in Directory");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" SET PWD=?, ENABLED=? WHERE (ID=?)");

		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		Base64.Encoder b64e = Base64.getEncoder();
		String pwdHash = "{SHA}" + b64e.encodeToString(md.digest(pwd.getBytes()));

		try {
			Connection con = getConnection();
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, pwdHash);
				ps.setBoolean(2, true);
				ps.setInt(3, usr.getID());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Adds a user to the Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
	public void add(Person usr, String pwd) throws SecurityException {
		if (log.isDebugEnabled())
			log.debug("Adding user " + usr.getDN() + " to Directory");

		// Get the ID
		int id = (usr instanceof Applicant) ? ((Applicant) usr).getPilotID() : usr.getID();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" (ID, PWD, NAME, ENABLED) VALUES (?, ?, ?, ?)");

		// Generate the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		Base64.Encoder b64e = Base64.getEncoder();
		String pwdHash = "{SHA}" + b64e.encodeToString(md.digest(pwd.getBytes()));
		
		boolean hasAlias = false;
		if (usr instanceof Pilot) {
			Pilot p = (Pilot) usr;
			hasAlias = !StringUtils.isEmpty(p.getLDAPName()) || !StringUtils.isEmpty(p.getPilotCode());
		}

		try {
			Connection con = getConnection();
			boolean isAutoCommit = con.getAutoCommit();
			if (isAutoCommit)
				con.setAutoCommit(false);
			
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setInt(1, id);
				ps.setString(2, pwdHash);
				ps.setString(3, usr.getName());
				ps.setBoolean(4, true);
				ps.executeUpdate();
			}
			
			sqlBuf = new StringBuilder("DELETE FROM ");
			sqlBuf.append(_props.getProperty("apachesql.alias", "common.AUTH_ALIAS"));
			sqlBuf.append(" WHERE (ID=?)");
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setInt(1, id);
				ps.executeUpdate();
			}

			// If we have an alias, then update it
			if (hasAlias) {
				Pilot p = (Pilot) usr;	
				sqlBuf = new StringBuilder("INSERT INTO ");
				sqlBuf.append(_props.getProperty("apachesql.alias", "common.AUTH_ALIAS"));
				sqlBuf.append(" (ID, USERID, ISCODE) VALUES (?, ?, ?)");
				try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
					ps.setInt(1, usr.getID());
					if (!StringUtils.isEmpty(p.getPilotCode())) {
						ps.setString(2, p.getPilotCode());
						ps.setBoolean(3, true);
						ps.addBatch();
						if (log.isDebugEnabled())
							log.debug("Adding " + p.getPilotCode() + " as alias");
					}
					
					if (!StringUtils.isEmpty(p.getLDAPName())) {
						ps.setString(2, p.getLDAPName());
						ps.setBoolean(3, false);
						ps.addBatch();
						if (log.isDebugEnabled())
							log.debug("Adding " + p.getLDAPName() + " as alias");
					}
					
					ps.executeBatch();
				}
			}
			
			// Commit the transaction
			if (isAutoCommit) {
				con.commit();
				con.setAutoCommit(true);
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Renames a user in the Directory.
	 * @param usr the user bean
	 * @param newName the new fully-qualified directory
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void rename(Person usr, String newName) throws SecurityException {
		if (log.isDebugEnabled())
			log.debug("Renaming user " + usr.getDN() + " to " + newName);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" SET NAME=? WHERE (ID=?)");
		
		try {
			Connection con = getConnection();
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, newName);
				ps.setInt(2, usr.getID());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}
	
	/**
	 * Disables a user's account.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void disable(Person usr) throws SecurityException {
		log.debug("Disabling user " + usr.getName());
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" SET ENABLED=? WHERE (ID=?)");
		
		try {
			Connection con = getConnection();
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setBoolean(1, false);
				ps.setInt(2, usr.getID());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Removes a user from the Directory. This will fail silently if the user does not exist in the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
	public void remove(Person usr) throws SecurityException {
		if (log.isDebugEnabled())
			log.debug("Removing user " + usr.getName() + " from Directory");

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(_props.getProperty("apachesql.table", "AUTH"));
		sqlBuf.append(" WHERE (ID=?)");

		try {
			Connection con = getConnection();
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setInt(1, usr.getID());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}
}