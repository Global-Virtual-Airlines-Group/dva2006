// Copyright 2006, 2007, 2010, 2012, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.ts2.*;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to write TeamSpeak 2 configuration data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetTS2Data extends DAO {

	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("ddMMyyyyHHmmss").appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true).toFormatter();

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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO teamspeak.ts2_channels (i_channel_server_id, b_channel_flag_moderated, b_channel_flag_default, i_channel_codec, i_channel_maxusers, s_channel_name, "
				+ "s_channel_topic, s_channel_description, s_channel_password, dt_channel_created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, c.getServerID());
				ps.setInt(2, c.getModerated() ? -1 : 0);
				ps.setInt(3, c.getDefault() ? -1 : 0);
				ps.setInt(4, c.getCodec().ordinal());
				ps.setInt(5, c.getMaxUsers());
				ps.setString(6, c.getName());
				ps.setString(7, c.getTopic());
				ps.setString(8, c.getDescription());
				ps.setString(9, c.getPassword());
				ps.setString(10, _df.format(LocalDateTime.ofInstant(c.getCreatedOn(), ZoneOffset.UTC)));
				executeUpdate(ps, 1);
			}

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
			try (PreparedStatement ps = prepare("UPDATE teamspeak.ts2_channels SET b_channel_flag_moderated=?, b_channel_flag_default=?, i_channel_codec=?, i_channel_maxusers=?, s_channel_name=?, s_channel_topic=?, "
				+ "s_channel_description=?, s_channel_password=? WHERE (i_channel_id=?)")) {
				ps.setInt(1, c.getModerated() ? -1 : 0);
				ps.setInt(2, c.getDefault() ? -1 : 0);
				ps.setInt(3, c.getCodec().ordinal());
				ps.setInt(4, c.getMaxUsers());
				ps.setString(5, c.getName());
				ps.setString(6, c.getTopic());
				ps.setString(7, c.getDescription());
				ps.setString(8, c.getPassword());
				ps.setInt(9, c.getID());
				executeUpdate(ps, 1);
			}

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
		if (CollectionUtils.isEmpty(usrs))
			return;
		
		// Get the first entry
		Client usr = usrs.iterator().next();
		try {
			startTransaction();

			// Prepare the statement
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO teamspeak.ts2_clients (i_client_id, i_client_server_id, b_client_privilege_serveradmin, s_client_name, s_client_password, dt_client_created, dt_client_lastonline) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, usr.getID());
				ps.setInt(3, usr.getServerAdmin() ? -1 : 0);
				ps.setString(4, usr.getUserID());
				ps.setString(5, usr.getPassword());
				ps.setString(6, _df.format(LocalDateTime.ofInstant(usr.getCreatedOn(), ZoneOffset.UTC)));
				ps.setString(7, (usr.getLastOnline() == null) ? null : _df.format(LocalDateTime.ofInstant(usr.getLastOnline(), ZoneOffset.UTC)));

				// Write one entry per server
				for (Client c : usrs) {
					ps.setInt(2, c.getServerID());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, usrs.size());
			}
			
			// Clean out the privileges
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM teamspeak.ts2_channel_privileges where (i_cp_client_id=?)")) {
				ps.setInt(1, usr.getID());
				executeUpdate(ps, 0);
			}
			
			// Write the client/channel privileges
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO teamspeak.ts2_channel_privileges (i_cp_server_id, i_cp_channel_id, i_cp_client_id, b_cp_flag_admin, b_cp_flag_autoop, b_cp_flag_autovoice) VALUES (?, ?, ?, ?, ?, ?)")) {
				for (Client c : usrs) {
					ps.setInt(1, c.getServerID());
					ps.setInt(3, usr.getID());
					for (Integer id : c.getChannelIDs()) {
						ps.setInt(2, id.intValue());
						ps.setInt(4, c.getServerAdmin() ? -1 : 0);
						ps.setInt(5, c.getServerOperator() ? -1 : 0);
						ps.setInt(6, c.getAutoVoice() ? -1 : 0);
						ps.addBatch();
					}
				}
			
				executeUpdate(ps, 1, usrs.size());
			}
			
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
			boolean hasDefault = false;
			try (PreparedStatement ps = prepare("SELECT COUNT(*) FROM teamspeak.ts2_channels WHERE (i_channel_server_id=?) AND (b_channel_flag_default=?)")) {
				ps.setInt(1, serverID);
				ps.setInt(2, -1);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						hasDefault = (rs.getInt(1) > 0); 
				}
			}

			if (hasDefault)
				return;
			
			// Update the default channel
			try (PreparedStatement ps = prepare("UPDATE teamspeak.ts2_channels SET b_channel_flag_default=? WHERE (i_channel_server_id=?) ORDER BY RAND()")) {
				ps.setInt(1, -1);
				ps.setInt(2, serverID);
				executeUpdate(ps, 0);
			}
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
		try (PreparedStatement ps = prepare("DELETE FROM teamspeak.ts2_channels WHERE (i_channel_id=?)")) {
			ps.setInt(1, c.getID());
			executeUpdate(ps, 0);
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
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM teamspeak.ts2_clients WHERE (i_client_server_id=?)")) {
				ps.setInt(1, srv.getID());
				executeUpdate(ps, 0);
			}

			// Delete the server
			try (PreparedStatement ps = prepare("DELETE FROM teamspeak.ts2_servers WHERE (i_server_id=?)")) {
				ps.setInt(1, srv.getID());
				executeUpdate(ps, 0);
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM teamspeak.ts2_clients WHERE (i_client_id=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Removes a number of users from a TeamSpeak server. This should typically be called when updating a server's roles.
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

		sqlBuf.append(')');

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, srv.getID());
			executeUpdate(ps, 0);
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
			try (PreparedStatement ps = prepare("INSERT INTO teamspeak.ts2_servers (s_server_name, s_server_welcomemessage, i_server_maxusers, i_server_udpport, s_server_password, b_server_active, dt_server_created, "
				+ "s_server_description, b_server_no_acars) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?) ON DUPLICATE KEY UPDATE s_server_name=VALUES(s_server_name), s_server_welcomemessage=VALUES(s_server_welcomemessage), "
				+ "i_server_maxusers=VALUES(i_server_maxusers), i_server_udpport=VALUES(i_server_udpport), s_server_password=VALUES(s_server_password), b_server_active=VALUES(b_server_active), "
				+ "s_server_description=VALUES(s_server_description), b_server_no_acars=VALUES(b_server_no_acars)")) {
				ps.setString(1, srv.getName());
				ps.setString(2, srv.getWelcomeMessage());
				ps.setInt(3, srv.getMaxUsers());
				ps.setInt(4, srv.getPort());
				ps.setString(5, srv.getPassword());
				ps.setInt(6, srv.getActive() ? -1 : 0);
				ps.setString(7, srv.getDescription());
				ps.setBoolean(8, srv.getACARSOnly());
				executeUpdate(ps, 1);
			}

			// Get the new server ID if new, otherwise clear out the roles entries
			if (isNew) {
				srv.setID(getNewID());
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM teamspeak.ts2_server_roles WHERE (i_server_id=?)")) {
					ps.setInt(1, srv.getID());
					executeUpdate(ps, 0);
				}
			}

			// Get all of the roles
			Map<ServerAccess, Collection<String>> roles = srv.getRoles();
			Collection<String> allRoles = roles.get(ServerAccess.ACCESS);

			// Write the server roles
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO teamspeak.ts2_server_roles (i_server_id, s_role_name, b_server_admin, b_channel_admin, b_autovoice) VALUES (?, ?, ?, ?, ?)")) {
				ps.setInt(1, srv.getID());
				for (String role : allRoles) {
					ps.setString(2, role);
					ps.setBoolean(3, roles.get(ServerAccess.ADMIN).contains(role));
					ps.setBoolean(4, roles.get(ServerAccess.OPERATOR).contains(role));
					ps.setBoolean(5, roles.get(ServerAccess.VOICE).contains(role));
					ps.addBatch();
				}

				executeUpdate(ps, 1, allRoles.size());
			}
			
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
		try (PreparedStatement ps = prepare("UPDATE teamspeak.ts2_clients SET b_on_acars=? WHERE (s_client_name=?)")) {
			ps.setBoolean(1, isActive);
			ps.setString(2, pCode);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE teamspeak.ts2_clients SET b_on_acars=? WHERE (b_on_acars=?)")) {
			ps.setBoolean(1, false);
			ps.setBoolean(2, true);
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to clear the default channel flag for all channels but one.
	 */
	private void clearDefaultChannelFlag(int id, int serverID) throws SQLException {
		try (PreparedStatement ps = prepare("UPDATE teamspeak.ts2_channels SET b_channel_flag_default=0 WHERE (i_channel_id <> ?) AND (i_channel_server_id=?)")) {
			ps.setInt(1, id);
			ps.setInt(2, serverID);
			executeUpdate(ps, 0);
		}
	}
}