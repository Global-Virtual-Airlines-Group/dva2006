// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.ts2.*;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * An Authenticator to authenticate against a TeamSpeak 2 user database. This differs from the standard
 * {@link JDBCAuthenticator} by virtue of its using the standard ConnectionPool loaded via the SystemData object. Since
 * this implements {@link SQLAuthenticator}, this behavior can be overriden by providing a JDBC Connection to use.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TS2Authenticator extends ConnectionPoolAuthenticator {

	private static final Logger log = Logger.getLogger(TS2Authenticator.class);

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
		sqlBuf.append(".ts2_clients WHERE (UCASE(s_client_name)=?) AND (s_client_password=");
		sqlBuf.append(_props.getProperty("ts2.cryptFunc", ""));
		sqlBuf.append("(?)) AND (i_client_server_id > 0)");

		boolean isAuth = false;
		Connection c = null;
		try {
			c = getConnection();

			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, p.getPilotCode());
			ps.setString(2, pwd);
			ps.setMaxRows(1);

			// Execute the query
			ResultSet rs = ps.executeQuery();
			isAuth = rs.next() ? (rs.getInt(1) > 0) : false;

			// Clean up
			rs.close();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			closeConnection(c);
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
		sqlBuf.append(".ts2_clients WHERE (s_client_name=?) AND (i_client_server_id > 0)");

		Connection c = null;
		boolean hasUser = false;
		try {
			c = getConnection();

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
			closeConnection(c);
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
			remove(usr);
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
			c = getConnection();

			// Prepare the statement
			PreparedStatement ps = c.prepareStatement(sqlBuf.toString());
			ps.setString(1, pwd);
			ps.setString(2, ((Pilot) usr).getPilotCode());

			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			closeConnection(c);
		}
	}

	/**
	 * Returns wether this User can be added to this Authenticator. The user must have a non-empty pilot code and be
	 * authorized to access at least one TeamSpeak 2 virtual server.
	 * @param usr the user bean
	 * @return TRUE if the User is a Pilot and has access to at least one server, otherwise FALSE
	 */
	public boolean accepts(Person usr) {
		if (!(usr instanceof Pilot))
			return false;

		// Check the pilot code
		Pilot p = (Pilot) usr;
		if ((p.getPilotNumber() == 0) || (p.getStatus() != Pilot.ACTIVE) || (p.getNoVoice()))
			return false;

		// Check the servers
		Connection con = null;
		try {
			con = getConnection();
			
			// Get the DAO and the active server
			GetTS2Data dao = new GetTS2Data(con);
			Collection<Server> srvs = dao.getServers(usr.getRoles());
			for (Iterator<Server> i = srvs.iterator(); i.hasNext();) {
				Server srv = i.next();
				if (RoleUtils.hasAccess(usr.getRoles(), srv.getRoles().get(Server.ACCESS)))
					return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			closeConnection(con);
		}
	
		return false;
	}

	/**
	 * Adds a user to the Directory. If a database cryptographic function is set, it is applied to the password within
	 * the statement. <i>This may result in credential data being passed over the connection to the JDBC data source,
	 * depending on the driver implementation.</i>
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void add(Person usr, String pwd) throws SecurityException {

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
		} else if (StringUtils.isEmpty(p.getPilotCode())) {
			log.info("Cannot add " + usr.getName() + " - no pilot code");
			return;
		}
		
		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients SET s_client_password=");
		sqlBuf.append(_props.getProperty("ts2.cryptFunc", ""));
		sqlBuf.append("(?) WHERE (s_client_name=?)");

		Connection con = null;
		try {
			con = getConnection();
			
			// Get the servers that this person may access
			GetTS2Data dao = new GetTS2Data(con);
			Collection<Server> srvs = dao.getServers(usr.getRoles());
			
			// Create the client entries
			Collection<Client> usrs = new HashSet<Client>();
			for (Iterator<Server> i = srvs.iterator(); i.hasNext();) {
				Server srv = i.next();
				if (RoleUtils.hasAccess(usr.getRoles(), srv.getRoles().get(Server.ACCESS))) {
					Client c = new Client(p.getPilotCode());
					c.setPassword(pwd);
					c.addChannels(srv);
					c.setID(usr.getID());
					c.setServerID(srv.getID());
					c.setAutoVoice(RoleUtils.hasAccess(usr.getRoles(), srv.getRoles().get(Server.VOICE)));
					c.setServerOperator(RoleUtils.hasAccess(usr.getRoles(), srv.getRoles().get(Server.OPERATOR)));
					c.setServerAdmin(RoleUtils.hasAccess(usr.getRoles(), srv.getRoles().get(Server.ADMIN)));
					usrs.add(c);
				} else
					i.remove();
			}

			// If no accessible servers, abort
			if (usrs.isEmpty()) {
				closeConnection(con);
				return;
			}
				
			// Log addition
			log.warn("Adding " + p.getName() + " to " + StringUtils.listConcat(srvs, ", "));
			
			// Get the DAO and update
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.write(usrs);
			
			// Encrypt the password
			PreparedStatement ps = con.prepareStatement(sqlBuf.toString());
			ps.setString(1, pwd);
			ps.setString(2, ((Pilot) usr).getPilotCode());
			
			// Execute the update and clean up
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e.getMessage(), e);
		} finally {
			closeConnection(con);
		}
	}

	/**
	 * Renames a user in the Directory. <i>NOT IMPLEMENTED</i>
	 */
	public void rename(Person usr, String newName) throws SecurityException {
		log.warn("Rename not supported");
	}
	
	/**
	 * Disables a user's account. <i>This merely deletes the user.</i>
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	public void disable(Person usr) throws SecurityException {
		remove(usr);
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	public void remove(Person usr) throws SecurityException {

		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients WHERE (UCASE(s_client_name)=?)");

		Connection c = null;
		try {
			c = getConnection();

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
			closeConnection(c);
		}
	}
}