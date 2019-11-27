// Copyright 2006, 2007, 2009, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

import org.apache.log4j.Logger;

import org.deltava.beans.ts2.*;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load TeamSpeak 2 configuration data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetTS2Data extends DAO {

	private static final Logger log = Logger.getLogger(GetTS2Data.class);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetTS2Data(Connection c) {
		super(c);
	}
	
	/*
	 * Helper method to convert a date from the godawful TS2 format. The JDK has a bug where
	 * we cannot parse fractional seconds if there are no separators in the format string.
	 */
	private static Instant getDate(String dt) {
		try {
			if (dt == null) return null;
			DateTimeFormatter df = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
			Instant i = LocalDateTime.parse(dt.substring(0, 14), df).toInstant(ZoneOffset.UTC);
			return i.plusMillis(Long.parseLong(dt.substring(15)));
		} catch (Exception pe) {
			log.warn("Error parsing date " + dt + " - " + pe.getMessage());
			return Instant.now();
		}
	}

	/**
	 * Returns an individual TeamSpeak voice Channel.
	 * @param id the Channel database ID
	 * @return a Channel profile, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Channel getChannel(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM teamspeak.ts2_channels WHERE (i_channel_id=?) LIMIT 1")) {
			ps.setInt(1, id);
			return executeChannels(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all TeamSpeak voice Channels.
	 * @return a Collection of Channel profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Channel> getChannels() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM teamspeak.ts2_channels ORDER BY i_channel_server_id")) {
			return executeChannels(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns an individual's TeamSpeak 2 user profiles.
	 * @param id the user's database ID
	 * @return a Collection of User profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Client> getUsers(int id) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM teamspeak.ts2_clients WHERE (i_client_id=?)")) {
			ps.setInt(1, id);
			return executeUsers(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all TeamSpeak 2 user profiles.
	 * @return a Collection of User profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Client> getUsers() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM teamspeak.ts2_clients ORDER BY s_client_name")) {
			return executeUsers(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all TeamSpeak 2 users authorized to access a virtual server.
	 * @param serverID the server database ID
	 * @return a Collection of User profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Client> getUsersByServer(int serverID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM teamspeak.ts2_clients WHERE (i_client_server_id=?) ORDER BY s_client_name")) {
			ps.setInt(1, serverID);
			return executeUsers(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all TeamSpeak 2 virtual server profiles.
	 * @return a Collection of Server beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Server> getServers() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT i_server_id, s_server_name, s_server_welcomemessage, i_server_maxusers, i_server_udpport, s_server_password, b_server_active, dt_server_created, "
			+ "s_server_description, b_server_no_acars FROM teamspeak.ts2_servers")) {
			return executeServers(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all TeamSpeak 2 virtual servrs a user has access to.
	 * @param id the pilot's database ID
	 * @return a Collection of Server beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Server> getServers(int id) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT s.i_server_id, s.s_server_name, s.s_server_welcomemessage, s.i_server_maxusers, s.i_server_udpport, s.s_server_password, s.b_server_active, "
			+ "s.dt_server_created, s.s_server_description, b_server_no_acars FROM teamspeak.ts2_servers s, teamspeak.ts2_clients c WHERE (c.i_client_id=?) AND (s.i_server_id=c.i_client_server_id)")) {
			ps.setInt(1, id);
			return executeServers(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all TeamSpeak 2 virtual servers able to be accessed by members of particular security roles.
	 * @param roles a Collection of role names
	 * @return a Collection of server beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Server> getServers(Collection<String> roles) throws DAOException {
		if (roles.isEmpty())
			return Collections.emptySet();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT s.i_server_id, s.s_server_name, s.s_server_welcomemessage, s.i_server_maxusers, s.i_server_udpport, s.s_server_password, s.b_server_active, s.dt_server_created, "
				+ "s.s_server_description, b_server_no_acars FROM teamspeak.ts2_servers s, teamspeak.ts2_server_roles r WHERE (s.i_server_id=r.i_server_id) AND (");
		for (Iterator<String> i = roles.iterator(); i.hasNext();) {
			String role = i.next();
			sqlBuf.append("(r.s_role_name=\'");
			sqlBuf.append(role);
			sqlBuf.append("\')");
			if (i.hasNext())
				sqlBuf.append(" OR ");
		}

		sqlBuf.append(')');

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return executeServers(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a TeamSpeak 2 virtual server profile.
	 * @param id the Server database ID
	 * @return a Server bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Server getServer(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT i_server_id, s_server_name, s_server_welcomemessage, i_server_maxusers, i_server_udpport, s_server_password, b_server_active, "
			+ "dt_server_created, s_server_description, b_server_no_acars FROM teamspeak.ts2_servers WHERE (i_server_id=?) LIMIT 1")) {
			ps.setInt(1, id);
			return executeServers(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse Channel result sets.
	 */
	private static List<Channel> executeChannels(PreparedStatement ps) throws SQLException {
		List<Channel> results = new ArrayList<Channel>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Channel c = new Channel(rs.getString(10));
				c.setID(rs.getInt(1));
				c.setServerID(rs.getInt(2));
				c.setModerated(rs.getInt(4) == -1);
				c.setHierarchical(rs.getInt(5) == -1);
				c.setDefault(rs.getInt(6) == -1);
				c.setCodec(Codec.values()[rs.getInt(7)]);
				c.setMaxUsers(rs.getInt(9));
				c.setTopic(rs.getString(11));
				c.setDescription(rs.getString(12));
				c.setPassword(rs.getString(13));
				c.setCreatedOn(getDate(rs.getString(14)));
				results.add(c);
			}
		}

		return results;
	}

	/*
	 * Helper method to parse User result sets.
	 */
	private List<Client> executeUsers(PreparedStatement ps) throws SQLException {
		Map<String, Client> results = new HashMap<String, Client>();
		Collection<Integer> userIDs = new HashSet<Integer>();

		// Execute the query
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Client usr = new Client(rs.getString(4));
				usr.setID(rs.getInt(1));
				usr.setServerID(rs.getInt(2));
				usr.setServerAdmin(rs.getInt(3) == -1);
				usr.setPassword(rs.getString(5));
				usr.setCreatedOn(getDate(rs.getString(6)));
				usr.setLastOnline(getDate(rs.getString(7)));
				usr.setIsACARS(rs.getBoolean(8));
				userIDs.add(Integer.valueOf(usr.getID()));
				results.put(usr.cacheKey().toString(), usr);
			}
		}

		// Load client privileges
		if (!results.isEmpty()) {
			StringBuilder buf = new StringBuilder("SELECT * FROM teamspeak.ts2_channel_privileges WHERE (i_cp_client_id ");
			if (results.size() == 1) {
				buf.append(" = ");
				buf.append(StringUtils.listConcat(userIDs, ""));
				buf.append(')');
			} else {
				buf.append(" IN (");
				buf.append(StringUtils.listConcat(userIDs, ","));
				buf.append("))");
			}
			
			try (PreparedStatement ps2 = prepareWithoutLimits(buf.toString())) {
				try (ResultSet rs = ps2.executeQuery()) {
					while (rs.next()) {
						// Build the key
						StringBuilder keyBuf = new StringBuilder(String.valueOf(rs.getInt(4)));
						keyBuf.append('-');
						keyBuf.append(String.valueOf(rs.getInt(2)));
				
						// Get the client record
						Client c = results.get(keyBuf.toString());
						if (c != null) {
							c.setServerAdmin(rs.getInt(5) == -1);
							c.setServerOperator(rs.getInt(6) == -1);
							c.setAutoVoice(rs.getInt(7) == -1);
						}
					}
				}
			}
		}

		return new ArrayList<Client>(results.values());
	}

	/*
	 * Helper method to parse TS2 server result sets.
	 */
	private List<Server> executeServers(PreparedStatement ps) throws SQLException {
		Map<Integer, Server> results = new TreeMap<Integer, Server>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Server srv = new Server(rs.getString(2));
				srv.setID(rs.getInt(1));
				srv.setWelcomeMessage(rs.getString(3));
				srv.setMaxUsers(rs.getInt(4));
				srv.setPort(rs.getInt(5));
				srv.setPassword(rs.getString(6));
				srv.setActive(rs.getInt(7) == -1);
				srv.setCreatedOn(getDate(rs.getString(8)));
				srv.setDescription(rs.getString(9));
				srv.setACARSOnly(rs.getBoolean(10));
				results.put(Integer.valueOf(srv.getID()), srv);
			}
		}

		// Load roles for each server
		try (PreparedStatement ps2 = prepareWithoutLimits("SELECT * FROM teamspeak.ts2_server_roles")) {
			try (ResultSet rs = ps2.executeQuery()) {
				while (rs.next()) {
					Server srv = results.get(Integer.valueOf(rs.getInt(1)));
					if (srv == null)
						continue;
				
					String role = rs.getString(2);
					srv.addRole(ServerAccess.ACCESS, role);
					if (rs.getBoolean(3))
						srv.addRole(ServerAccess.ADMIN, role);
					if (rs.getBoolean(4))
						srv.addRole(ServerAccess.OPERATOR, role);
					if (rs.getBoolean(5))
						srv.addRole(ServerAccess.VOICE, role);
				}
			}
		}

		// Load servers
		try (PreparedStatement ps2 = prepareWithoutLimits("SELECT * FROM teamspeak.ts2_channels ORDER BY s_channel_name")) {
			Collection<Channel> channels = executeChannels(ps2);
			for (Channel c : channels) {
				Server srv = results.get(Integer.valueOf(c.getServerID()));
				if (srv != null)
					srv.addChannel(c);
			}
		}

		return new ArrayList<Server>(results.values());
	}
}