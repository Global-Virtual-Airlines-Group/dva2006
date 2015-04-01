// Copyright 2006, 2007, 2008, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.ts2.*;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * An Authenticator to authenticate against a TeamSpeak 2 user database.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class TS2Authenticator extends SQLAuthenticator {

	private static final Logger log = Logger.getLogger(TS2Authenticator.class);

	/**
	 * Authenticates a user by validating the password against the database. If a database cryptographic function is
	 * set, it is applied to the password within the statement. <i>This may result in credential data being passed over
	 * the connection to the JDBC data source, depending on the driver implementation. </i>
	 * @param usr the User bean
	 * @param pwd the supplied password
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
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
		sqlBuf.append("(?)) AND (i_client_server_id > 0) LIMIT 1");

		boolean isAuth = false;
		Connection c = null;
		try {
			c = getConnection();
			try (PreparedStatement ps = c.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, p.getPilotCode());
				ps.setString(2, pwd);
				try (ResultSet rs = ps.executeQuery()) {
					isAuth = rs.next() ? (rs.getInt(1) > 0) : false;
				}
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}

		// If we haven't authenticated, throw an execption
		if (!isAuth)
			throw new SecurityException("Invalid password for " + p.getPilotCode());
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists, otherwise FALSE
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
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
			try (PreparedStatement ps = c.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, ((Pilot) usr).getPilotCode());
				try (ResultSet rs = ps.executeQuery()) {
					// Count the results
					hasUser = rs.next() ? (rs.getInt(1) > 0) : false;
				}
			}
		} catch (Exception e) {
			throw new SecurityException(e);
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
	@Override
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
		sqlBuf.append("(?), b_enabled=? WHERE (s_client_name=?)");

		Connection c = null;
		try {
			c = getConnection();
			try (PreparedStatement ps = c.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, pwd);
				ps.setBoolean(2, true);
				ps.setString(3, ((Pilot) usr).getPilotCode());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Returns whether this User can be added to this Authenticator. The user must have a non-empty pilot code and be
	 * authorized to access at least one TeamSpeak 2 virtual server.
	 * @param usr the user bean
	 * @return TRUE if the User is a Pilot and has access to at least one server, otherwise FALSE
	 */
	@Override
	public boolean accepts(Person usr) {
		if (!(usr instanceof Pilot))
			return false;

		// Check the pilot code
		Pilot p = (Pilot) usr;
		if ((p.getPilotNumber() == 0) || (p.getStatus() != Pilot.ACTIVE) || (p.getNoVoice()))
			return false;

		// Check the servers
		Connection con = null; boolean isOK = false;
		try {
			con = getConnection();

			// Get the DAO and the active server
			GetTS2Data dao = new GetTS2Data(con);
			Collection<Server> srvs = dao.getServers(usr.getRoles());
			for (Iterator<Server> i = srvs.iterator(); !isOK && i.hasNext();) {
				Server srv = i.next();
				isOK = RoleUtils.hasAccess(usr.getRoles(), srv.getRoles().get(ServerAccess.ACCESS));
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}

		return isOK;
	}

	/**
	 * Adds a user to the Directory. If a database cryptographic function is set, it is applied to the password within
	 * the statement. <i>This may result in credential data being passed over the connection to the JDBC data source,
	 * depending on the driver implementation.</i>
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
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
				Map<ServerAccess, Collection<String>> roles = srv.getRoles();
				if (RoleUtils.hasAccess(usr.getRoles(), roles.get(ServerAccess.ACCESS))) {
					Client c = new Client(p.getPilotCode());
					c.setPassword(pwd);
					c.addChannels(srv);
					c.setID(usr.getID());
					c.setServerID(srv.getID());
					c.setAutoVoice(RoleUtils.hasAccess(usr.getRoles(), roles.get(ServerAccess.VOICE)));
					c.setServerOperator(RoleUtils.hasAccess(usr.getRoles(), roles.get(ServerAccess.OPERATOR)));
					c.setServerAdmin(RoleUtils.hasAccess(usr.getRoles(), roles.get(ServerAccess.ADMIN)));
					usrs.add(c);
				} else
					i.remove();
			}

			// If no accessible servers, abort
			if (usrs.isEmpty())
				return;

			// Log addition
			log.warn("Adding " + p.getName() + " to " + StringUtils.listConcat(srvs, ", "));

			// Get the DAO and update
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.write(usrs);

			// Encrypt the password
			try (PreparedStatement ps = con.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, pwd);
				ps.setString(2, ((Pilot) usr).getPilotCode());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Renames a user in the Directory. <i>NOT IMPLEMENTED</i>
	 */
	@Override
	public void rename(Person usr, String newName) throws SecurityException {
		log.warn("Rename not supported");
	}

	/**
	 * Disables a user's account.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void disable(Person usr) throws SecurityException {
		
		// Ensure we are a Pilot, not a Person
		if (!(usr instanceof Pilot)) {
			log.warn("Cannot disable " + usr.getName() + " - " + usr.getClass().getName());
			return;
		}
		
		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients SET b_enabled=? WHERE (s_client_name=?)");
		
		Connection c = null;
		try {
			c = getConnection();
			try (PreparedStatement ps = c.prepareStatement(sqlBuf.toString())) {
				ps.setBoolean(1, false);
				ps.setString(2, ((Pilot) usr).getPilotCode());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if a JDBC error occurs
	 */
	@Override
	public void remove(Person usr) throws SecurityException {

		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(_props.getProperty("ts2.db", "teamspeak"));
		sqlBuf.append(".ts2_clients WHERE (UCASE(s_client_name)=?)");

		Connection c = null;
		try {
			c = getConnection();
			try (PreparedStatement ps = c.prepareStatement(sqlBuf.toString())) {
				ps.setString(1, ((Pilot) usr).getPilotCode().toUpperCase());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}
}