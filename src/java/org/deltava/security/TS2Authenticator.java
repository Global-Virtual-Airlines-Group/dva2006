// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.io.IOException;

import org.deltava.beans.*;
import org.deltava.beans.ts2.Server;

import org.deltava.jdbc.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.apache.log4j.Logger;

/**
 * An Authenticator to authenticate against a TeamSpeak 2 user database. This differs from the standard
 * {@link SQLAuthenticator} by virtue of its using the standard ConnectionPool loaded via the SystemData object.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TS2Authenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(TS2Authenticator.class);
	private static final DateFormat _df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");

	private ConnectionPool _pool;
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

		// Get the connection pool
		_pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
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

		// Ensure we are a Pilot, not a Person
		if (!(usr instanceof Pilot))
			throw new SecurityException("Invalid object - " + usr.getClass().getName());
		
		// Make sure we're not locked out
		Pilot p = (Pilot) usr;
		if (p.getNoVoice())
			throw new SecurityException("Private Voice disabled");

		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(*) FROM ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients WHERE (UCASE(s_client_name)=?) AND (");
		sqlBuf.append(_props.getProperty("ts2.cryptFunc", ""));
		sqlBuf.append("(s_client_password)=?)");

		boolean isAuth = false;
		Connection c = null;
		try {
			c = _pool.getConnection(true);

			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, p.getPilotCode());
			ps.setString(2, pwd);
			ps.setMaxRows(1);

			// Execute the query
			ResultSet rs = ps.executeQuery();
			isAuth = rs.next() ? (rs.getInt(1) == 1) : false;

			// Clean up
			rs.close();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			_pool.release(c);
		}
		
		// If we haven't authenticated, throw an execption
		if (!isAuth)
			throw new SecurityException("Invalid password for " + p.getPilotCode());

		log.info(usr.getName() + " authenticated");
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists, otherwise FALSE
	 * @throws SecurityException if a JDBC error occurs
	 */
	public boolean contains(Person usr) throws SecurityException {
		
		// Ensure we are a Pilot, not a Person
		if (!(usr instanceof Pilot))
			throw new SecurityException("Invalid object - " + usr.getClass().getName());

		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(*) FROM ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients WHERE (s_client_name=?)");

		Connection c = null;
		boolean hasUser = false;
		try {
			c = _pool.getConnection(true);

			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, ((Pilot) usr).getPilotCode());
			ResultSet rs = ps.executeQuery();

			// Count the results
			hasUser = rs.next() ? (rs.getInt(1) > 0) : false;

			// Clean up after ourselves
			rs.close();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			_pool.release(c);
		}

		return hasUser;
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

		// Ensure we are a Pilot, not a Person
		if (!(usr instanceof Pilot))
			throw new SecurityException("Invalid object - " + usr.getClass().getName());
		
		// If we're locked out, just remove the user
		Pilot p = (Pilot) usr;
		if (p.getNoVoice()) {
			log.warn("Cannot update " + usr.getName() + " - Voice disabled");
			removeUser(usr);
			return;
		}

		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients SET s_client_password=");
		sqlBuf.append(_props.getProperty("ts2.cryptFunc", ""));
		sqlBuf.append("(?) WHERE (s_client_name=?)");

		Connection c = null;
		try {
			c = _pool.getConnection(true);

			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, usr.getPassword());
			ps.setString(2, ((Pilot) usr).getPilotCode());

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			_pool.release(c);
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

		// Ensure we are a Pilot, not a Person
		if (!(usr instanceof Pilot)) {
			log.warn("Cannot add " + usr.getName() + " - " + usr.getClass().getName());
			return;
		}
		
		// If our access has been locked out, then don't do the add
		Pilot p = (Pilot) usr;
		if (p.getNoVoice()) {
			log.warn("Cannot add " + usr.getName() + " - Voice disabled");
			return;
		}
		
		// Get the servers that this person may access
		@SuppressWarnings("unchecked")
		Collection<Server> srvs = new ArrayList<Server>((Collection) SystemData.getObject("ts2servers"));
		for (Iterator<Server> i = srvs.iterator(); i.hasNext(); ) {
			Server srv = i.next();
			if (!RoleUtils.hasAccess(usr.getRoles(), srv.getRoles()))
				i.remove();
		}
		
		// If no accessible servers, abort
		if (srvs.isEmpty())
			return;
		
		// Log addition
		log.warn("Adding " + p.getName() + " to " + StringUtils.listConcat(srvs, ", "));
		
		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients (i_client_server_id, b_client_privilege_serveradmin, s_client_name, s_client_password, " +
				"dt_client_created) VALUES (?, ?, ?, ");
		sqlBuf.append(_props.getProperty("ts2.cryptFunc", ""));
		sqlBuf.append("(?), ?)");

		Connection c = null;
		try {
			c = _pool.getConnection(true);
			
			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setInt(2, 0);
			ps.setString(3, p.getPilotCode());
			ps.setString(4, pwd);
			synchronized (_df) {
				ps.setString(5, _df.format(new java.util.Date()));
			}
			
			// Add to the servers
			for (Iterator<Server> i = srvs.iterator(); i.hasNext(); ) {
				Server srv = i.next();
				ps.setInt(1, srv.getID());
				ps.addBatch();
			}
			
			// Execute the update and clean up
			ps.executeBatch();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			_pool.release(c);
		}
	}

	/**
	 * Renames a user in the Directory. <i>NOT IMPLEMENTED</i>
	 */
	public void rename(Person usr, String newName) throws SecurityException {
		log.warn("TS2Authenticator does not support renames");
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void removeUser(Person usr) throws SecurityException {
		
		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients WHERE (UCASE(s_client_name)=?)");
		
		Connection c = null;
		try {
			c = _pool.getConnection(true);
			
			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, ((Pilot) usr).getPilotCode().toUpperCase());
			
			// Execute the query and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			_pool.release(c);
		}
	}
}