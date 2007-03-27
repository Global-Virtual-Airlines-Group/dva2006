// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.text.*;
import java.util.*;

import org.deltava.beans.ts2.*;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to write TeamSpeak 2 configuration data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetTS2Data extends DAO {

	private static final DateFormat _df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetTS2Data(Connection c) {
		super(c);
	}

	/**
	 * Creates a new TeamSpeak voice channel.
	 * @param c the Channel bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Channel c) throws DAOException {
		try {
			startTransaction();
			prepareStatement("INSERT INTO teamspeak.ts2_channels (i_channel_server_id, b_channel_flag_moderated, "
					+ "b_channel_flag_default, i_channel_codec, i_channel_maxusers, s_channel_name, s_channel_topic, "
					+ "s_channel_description, s_channel_password, dt_channel_created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, c.getServerID());
			_ps.setInt(2, c.getModerated() ? -1 : 0);
			_ps.setInt(3, c.getDefault() ? -1 : 0);
			_ps.setInt(4, c.getCodec());
			_ps.setInt(5, c.getMaxUsers());
			_ps.setString(6, c.getName());
			_ps.setString(7, c.getTopic());
			_ps.setString(8, c.getDescription());
			_ps.setString(9, c.getPassword());
			_ps.setString(10, _df.format(c.getCreatedOn()));
			executeUpdate(1);

			// Get the new channel ID
			c.setID(getNewID());

			// Clear default channel flag and commit
			if (c.getDefault())
				clearDefaultChannelFlag(c.getID(), c.getServerID());

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing TeamSpeak voice channel.
	 * @param c the Channel bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Channel c) throws DAOException {
		try {
			startTransaction();
			prepareStatement("UPDATE teamspeak.ts2_channels SET b_channel_flag_moderated=?, b_channel_flag_default=?, "
					+ "i_channel_codec=?, i_channel_maxusers=?, s_channel_name=?, s_channel_topic=?, s_channel_description=?, "
					+ "s_channel_password=? WHERE (i_channel_id=?)");
			_ps.setInt(1, c.getModerated() ? -1 : 0);
			_ps.setInt(2, c.getDefault() ? -1 : 0);
			_ps.setInt(3, c.getCodec());
			_ps.setInt(4, c.getMaxUsers());
			_ps.setString(5, c.getName());
			_ps.setString(6, c.getTopic());
			_ps.setString(7, c.getDescription());
			_ps.setString(8, c.getPassword());
			_ps.setInt(9, c.getID());
			executeUpdate(1);

			// Clear default channel flag and commit
			if (c.getDefault())
				clearDefaultChannelFlag(c.getID(), c.getServerID());

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new TeamSpeak user record to the database. <i>This is generally used to copy existing User records to
	 * additional TeamSpeak servers when user roles change.</i>
	 * @param usrs a Collection of Client beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Collection<Client> usrs) throws DAOException {
		
		// Exit if empty
		if (CollectionUtils.isEmpty(usrs))
			return;
		
		// Get the first entry
		Client usr = usrs.iterator().next();
		try {
			startTransaction();

			// Prepare the statement
			prepareStatement("REPLACE INTO teamspeak.ts2_clients (i_client_id, i_client_server_id, b_client_privilege_serveradmin, "
					+ "s_client_name, s_client_password, dt_client_created, dt_client_lastonline) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, usr.getID());
			_ps.setInt(3, usr.getServerAdmin() ? -1 : 0);
			_ps.setString(4, usr.getUserID());
			_ps.setString(5, usr.getPassword());
			_ps.setString(6, _df.format(usr.getCreatedOn()));
			_ps.setString(7, (usr.getLastOnline() == null) ? null : _df.format(usr.getLastOnline()));

			// Write one entry per server
			for (Iterator<Client> i = usrs.iterator(); i.hasNext();) {
				Client c = i.next();
				_ps.setInt(2, c.getServerID());
				_ps.addBatch();
			}
			
			// Write the entries
			_ps.executeBatch();
			_ps.close();
			
			// Clean out the privileges
			prepareStatementWithoutLimits("DELETE FROM teamspeak.ts2_channel_privileges where (i_cp_client_id=?)");
			_ps.setInt(1, usr.getID());
			
			// Write the client/channel privileges
			prepareStatement("REPLACE INTO teamspeak.ts2_channel_privileges (i_cp_server_id, i_cp_channel_id, "
					+ "i_cp_client_id, b_cp_flag_admin, b_cp_flag_autoop, b_cp_flag_autovoice) VALUES (?, ?, ?, ?, ?, ?)");
			for (Iterator<Client> i = usrs.iterator(); i.hasNext();) {
				Client c = i.next();
				_ps.setInt(1, c.getServerID());
				_ps.setInt(3, usr.getID());
				for (Iterator<Integer> ci = c.getChannelIDs().iterator(); ci.hasNext(); ) {
					Integer id = ci.next();
					_ps.setInt(2, id.intValue());
					_ps.setInt(4, c.getServerAdmin() ? -1 : 0);
					_ps.setInt(5, c.getServerOperator() ? -1 : 0);
					_ps.setInt(6, c.getAutoVoice() ? -1 : 0);
					_ps.addBatch();
				}
			}
			
			// Write the entries and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Sets a random Channel as the default for a Server if no default is specified.
	 * @param serverID the virtual Server ID 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setDefault(int serverID) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM teamspeak.ts2_channels WHERE (i_channel_server_id=?) AND "
					+ "(b_channel_flag_default=?)");
			_ps.setInt(1, serverID);
			_ps.setInt(2, -1);
			
			// Check for a default channel
			ResultSet rs = _ps.executeQuery();
			boolean hasDefault = rs.next() ? (rs.getInt(1) > 0) : false;
			rs.close();
			_ps.close();
			if (hasDefault)
				return;
			
			// Update the default channel
			setQueryMax(1);
			prepareStatement("UPDATE teamspeak.ts2_channels SET b_channel_flag_default=? WHERE (i_channel_server_id=?) "
					+ "ORDER BY RAND()");
			_ps.setInt(1, -1);
			_ps.setInt(2, serverID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a TeamSpeak voice channel.
	 * @param c the Channel bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Channel c) throws DAOException {
		try {
			prepareStatement("DELETE FROM teamspeak.ts2_channels WHERE (i_channel_id=?)");
			_ps.setInt(1, c.getID());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a TeamSpeak virtual server.
	 * @param srv the Server bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Server srv) throws DAOException {
		try {
			startTransaction();

			// Delete the clients
			prepareStatementWithoutLimits("DELETE FROM teamspeak.ts2_clients WHERE (i_client_server_id=?)");
			_ps.setInt(1, srv.getID());
			executeUpdate(0);

			// Delete the server
			prepareStatement("DELETE FROM teamspeak.ts2_servers WHERE (i_server_id=?)");
			_ps.setInt(1, srv.getID());
			executeUpdate(0);

			// Commit
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes TeamSpeak 2 credentials for a Pilot.
	 * @param id the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM teamspeak.ts2_clients WHERE (i_client_id=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Removes a number of users from a TeamSpeak server. This should typically be called when updating a server's
	 * roles.
	 * @param srv the Server bean
	 * @param pilotIDs a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public void removeUsers(Server srv, Collection<Integer> pilotIDs) throws DAOException {
		if (pilotIDs.isEmpty() || (srv.getID() == 0))
			return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM teamspeak.ts2_clients WHERE (i_client_server_id=?) AND (");
		for (Iterator<Integer> i = pilotIDs.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append("(i_client_id=");
			sqlBuf.append(id.intValue());
			sqlBuf.append(')');
			if (i.hasNext())
				sqlBuf.append(" OR ");
		}

		sqlBuf.append(")");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, srv.getID());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Creates/updates a TeamSpeak server entry.
	 * @param srv the Server bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Server srv) throws DAOException {

		boolean isNew = (srv.getID() == 0);
		try {
			startTransaction();
			if (isNew) {
				prepareStatement("INSERT INTO teamspeak.ts2_servers (s_server_name, s_server_welcomemessage, i_server_maxusers, "
						+ "i_server_udpport, s_server_password, b_server_active, dt_server_created, s_server_description, b_server_no_acars) "
						+ "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?)");
			} else {
				prepareStatement("UPDATE teamspeak.ts2_servers SET s_server_name=?, s_server_welcomemessage=?, "
						+ "i_server_maxusers=?, i_server_udpport=?, s_server_password=?, b_server_active=?, s_server_description=?, "
						+ "b_server_no_acars=? WHERE (i_server_id=?)");
				_ps.setInt(9, srv.getID());
			}

			// Set the parameters
			_ps.setString(1, srv.getName());
			_ps.setString(2, srv.getWelcomeMessage());
			_ps.setInt(3, srv.getMaxUsers());
			_ps.setInt(4, srv.getPort());
			_ps.setString(5, srv.getPassword());
			_ps.setInt(6, srv.getActive() ? -1 : 0);
			_ps.setString(7, srv.getDescription());
			_ps.setBoolean(8, srv.getACARSOnly());

			// Write the server
			executeUpdate(1);

			// Get the new server ID if new, otherwise clear out the roles entries
			if (isNew) {
				srv.setID(getNewID());
			} else {
				prepareStatementWithoutLimits("DELETE FROM teamspeak.ts2_server_roles WHERE (i_server_id=?)");
				_ps.setInt(1, srv.getID());
				executeUpdate(0);
			}

			// Get all of the roles
			Collection<String> allRoles = srv.getRoles().get(Server.ACCESS);

			// Write the server roles
			prepareStatement("INSERT INTO teamspeak.ts2_server_roles (i_server_id, s_role_name, b_server_admin, "
					+ "b_channel_admin, b_autovoice) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, srv.getID());
			for (Iterator<String> i = allRoles.iterator(); i.hasNext();) {
				String role = i.next();
				_ps.setString(2, role);
				_ps.setBoolean(3, srv.getRoles().get(Server.ADMIN).contains(role));
				_ps.setBoolean(4, srv.getRoles().get(Server.OPERATOR).contains(role));
				_ps.setBoolean(5, srv.getRoles().get(Server.VOICE).contains(role));
				_ps.addBatch();
			}

			// Execute the batch update and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Marks the Pilot as currently logged in.
	 * @param pCode the Pilot Code
	 * @param isActive TRUE if the Pilot is logged in, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setActive(String pCode, boolean isActive) throws DAOException {
		try {
			prepareStatement("UPDATE teamspeak.ts2_clients SET b_on_acars=? WHERE (s_client_name=?)");
			_ps.setBoolean(1, isActive);
			_ps.setString(2, pCode);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Marks all clients as logged out of ACARS. This is used on system startup to reset flags.
	 * @return the number of flags that needed to be cleared
	 * @throws DAOException if a JDBC error occurs
	 */
	public int clearActiveFlags() throws DAOException {
		try {
			prepareStatement("UPDATE teamspeak.ts2_clients SET b_on_acars=? WHERE (b_on_acars=?)");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, true);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to clear the default channel flag for all channels but one.
	 */
	private void clearDefaultChannelFlag(int id, int serverID) throws SQLException {
		prepareStatement("UPDATE teamspeak.ts2_channels SET b_channel_flag_default=0 WHERE "
				+ "(i_channel_id <> ?) AND (i_channel_server_id=?)");
		_ps.setInt(1, id);
		_ps.setInt(2, serverID);
		executeUpdate(0);
	}
}