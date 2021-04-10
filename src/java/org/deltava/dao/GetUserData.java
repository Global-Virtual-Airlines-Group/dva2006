// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load cross-application User data.
 * @author Luke
 * @version 9.2
 * @since 1.0
 */

public class GetUserData extends DAO {

	private static final Cache<UserData> _usrCache = CacheManager.get(UserData.class, "UserData");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetUserData(Connection c) {
		super(c);
	}

	/**
	 * Returns cross-application Airline data for a specific Airline.
	 * @param code the Airline code
	 * @return an AirlineInformation bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 * @see GetUserData#getAirlines(boolean)
	 */
	public AirlineInformation get(String code) throws DAOException {
		if (code == null) return null;
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.AIRLINEINFO WHERE (CODE=?) LIMIT 1")) {
			ps.setString(1, code.toUpperCase());
			return executeAirlineInfo(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all available Airlines on this application server.
	 * @param includeSelf TRUE if we include this Airline, otherwise FALSE
	 * @return a Map of AirlineInformation beans, indexed by code
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, AirlineInformation> getAirlines(boolean includeSelf) throws DAOException {
		Collection<AirlineInformation> results = null;
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.AIRLINEINFO ORDER BY CODE")) {
			results = executeAirlineInfo(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Strip out our airline if we need to
		if (!includeSelf)
			results.removeIf(ai -> ai.getCode().equals(SystemData.get("airline.code")));

		return CollectionUtils.createMap(results, AirlineInformation::getCode);
	}

	/**
	 * Returns cross-application data for a particular database ID.
	 * @param id the User's database ID
	 * @return the UserData object for that user, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public UserData get(int id) throws DAOException {

		// Check the cache
		UserData ud = _usrCache.get(Integer.valueOf(id));
		if (ud != null)
			return ud;

		try (PreparedStatement ps = prepareWithoutLimits("SELECT UD.*, AI.DOMAIN, AI.DBNAME, GROUP_CONCAT(DISTINCT XDB.ID SEPARATOR ?) AS IDS FROM common.AIRLINEINFO AI, common.USERDATA UD "
			+ "LEFT JOIN common.XDB_IDS XDB ON ((UD.ID=XDB.ID) OR (UD.ID=XDB.OTHER_ID)) WHERE (UD.AIRLINE=AI.CODE) AND (UD.ID=?) GROUP BY UD.ID LIMIT 1")) {
			ps.setString(1, ",");
			ps.setInt(2, id);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns cross-application data for a particular Water Cooler message thread.
	 * @param threadID the Message Thread database ID
	 * @return a UserDataMap
	 * @throws DAOException if a JDBC error occurs
	 */
	public UserDataMap getByThread(int threadID) throws DAOException {
		try (PreparedStatement ps = 	prepare("SELECT DISTINCT AUTHOR_ID FROM common.COOLER_POSTS WHERE (THREAD_ID=?)")) {
			ps.setInt(1, threadID);
			return get(executeIDs(ps));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns cross-application data for a particular Online Event.
	 * @param eventID the Online Event database ID
	 * @return a UserDataMap
	 * @throws DAOException if a JDBC error occurs
	 */
	public UserDataMap getByEvent(int eventID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT PILOT_ID FROM events.EVENT_SIGNUPS WHERE (ID=?)")) {
			ps.setInt(1, eventID);
			return get(executeIDs(ps));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns cross-application data for a Set of User IDs.
	 * @param ids a Collection of Integers with user IDs
	 * @return a UserDataMap
	 * @throws DAOException if a JDBC error occurs
	 */
	public UserDataMap get(Collection<Integer> ids) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT UD.*, AI.DOMAIN, AI.DBNAME, GROUP_CONCAT(DISTINCT XDB.ID SEPARATOR ?) AS IDS FROM common.AIRLINEINFO AI, "
			+ "common.USERDATA UD LEFT JOIN common.XDB_IDS XDB ON ((XDB.ID=UD.ID) OR (XDB.OTHER_ID=UD.ID)) WHERE (UD.AIRLINE=AI.CODE) AND (UD.ID IN (");

		UserDataMap result = new UserDataMap();
		for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
			Integer id = i.next();

			// Pull from the cache if at all possible; this is an evil query
			UserData usr = _usrCache.get(id);
			if (usr != null) {
				result.put(id, usr);
				i.remove();
			}
		}

		// Only execute the prepared statement if we haven't gotten anything from the cache
		if (ids.size() > 0) {
			sqlBuf.append(StringUtils.listConcat(ids, ",")).append(")) GROUP BY UD.ID");
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setString(1, ",");
				Map<Integer, UserData> results = CollectionUtils.createMap(execute(ps), UserData::getID);
				result.putAll(results);
			} catch (SQLException se) {
				throw new DAOException(se);
			}
		}

		return result;
	}

	/*
	 * Helper method to iterate through the result set.
	 */
	private static List<UserData> execute(PreparedStatement ps) throws SQLException {
		List<UserData> results = new ArrayList<UserData>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				UserData usr = new UserData(rs.getInt(1));
				usr.setAirlineCode(rs.getString(2));
				usr.setTable(rs.getString(3));
				usr.setDomain(rs.getString(4));
				usr.setDB(rs.getString(5));

				// Get the crossdomain IDs
				Collection<String> xdb_ids = StringUtils.split(rs.getString(6), ",");
				if (xdb_ids != null) {
					for (String xid : xdb_ids) {
						int xdb_id = StringUtils.parse(xid, 0);
						if (xdb_id > 0)
							usr.addID(xdb_id);
					}
				}

				results.add(usr);
				_usrCache.add(usr);
			}
		}

		return results;
	}

	/*
	 * Helper method to iterate through AirlineInformation result sets.
	 */
	private static List<AirlineInformation> executeAirlineInfo(PreparedStatement ps) throws SQLException {
		List<AirlineInformation> results = new ArrayList<AirlineInformation>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				AirlineInformation info = new AirlineInformation(rs.getString(1), rs.getString(2));
				info.setDB(rs.getString(3));
				info.setDomain(rs.getString(4));
				info.setEliteProgram(rs.getString(5));
				info.setCanTransfer(rs.getBoolean(6));
				info.setHistoricRestricted(rs.getBoolean(7));
				info.setAllowMultiAirline(rs.getBoolean(8));
				results.add(info);
			}
		}

		return results;
	}
}