// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load cross-application User data.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class GetUserData extends DAO implements CachingDAO {

	private static final Logger log = Logger.getLogger(GetUserData.class);

	private static final Cache<AirlineInformation> _appCache = new AgingCache<AirlineInformation>(2);
	private static final Cache<UserData> _usrCache = new AgingCache<UserData>(2048);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetUserData(Connection c) {
		super(c);
	}
	
	public CacheInfo getCacheInfo() {
		CacheInfo info = new CacheInfo(_appCache);
		info.add(_usrCache);
		return info;
	}
	
	/**
	 * Removes an entry from the user cache.
	 * @param id the database ID
	 */
	static void invalidate(int id) {
		_usrCache.remove(Integer.valueOf(id));
	}
	
	/**
	 * Removes an entry from the web application cache.
	 * @param id the airline code
	 * @throws NullPointerException if id is null
	 */
	static void invalidate(String id) {
		_appCache.remove(id.toUpperCase());
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

		// Check if we're in the cache
		AirlineInformation result = _appCache.get(code.toUpperCase());
		if (result != null)
			return result;

		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM common.AIRLINEINFO WHERE (CODE=?)");
			_ps.setString(1, code.toUpperCase());

			// Get the results, if empty return null
			setQueryMax(0);
			List<AirlineInformation> results = executeAirlineInfo();
			result = results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		if (result != null)
			_appCache.add(result);

		return result;
	}

	/**
	 * Returns all available Airlines on this application server.
	 * @param includeSelf TRUE if we include this Airline, otherwise FALSE
	 * @return a Map of AirlineInformation beans, indexed by code
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, AirlineInformation> getAirlines(boolean includeSelf) throws DAOException {
		Collection<AirlineInformation> results = null;
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.AIRLINEINFO ORDER BY CODE");
			results = executeAirlineInfo();
			_appCache.addAll(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Strip out our airline if we need to
		if (!includeSelf) {
			for (Iterator<AirlineInformation> i = results.iterator(); i.hasNext();) {
				AirlineInformation info = i.next();
				if (info.getCode().equals(SystemData.get("airline.code")))
					i.remove();
			}
		}

		// Convert to a map
		return CollectionUtils.createMap(results, "code");
	}

	/**
	 * Returns cross-application data for a particular database ID.
	 * @param id the User's database ID
	 * @return the UserData object for that user, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public UserData get(int id) throws DAOException {
		
		// Check the cache
		if (_usrCache.contains(new Integer(id)))
			return _usrCache.get(new Integer(id));
		
		try {
			setQueryMax(1);
			prepareStatement("SELECT UD.*, AI.DOMAIN, AI.DBNAME, GROUP_CONCAT(DISTINCT XDB.ID SEPARATOR ?) "
					+ "AS IDS FROM common.AIRLINEINFO AI, common.USERDATA UD LEFT JOIN common.XDB_IDS XDB ON "
					+ "((UD.ID=XDB.ID) OR (UD.ID=XDB.OTHER_ID)) WHERE (UD.AIRLINE=AI.CODE) AND (UD.ID=?) GROUP BY UD.ID");
			_ps.setString(1, ",");
			_ps.setInt(2, id);

			// Get the results, if empty return null
			List<UserData> results = execute();
			setQueryMax(0);
			return results.isEmpty() ? null : results.get(0);
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
		try {
			prepareStatement("SELECT UD.*, AI.DOMAIN, AI.DBNAME, GROUP_CONCAT(DISTINCT XDB.ID SEPARATOR ?) AS IDS "
					+ "FROM common.AIRLINEINFO AI, common.USERDATA UD LEFT JOIN common.XDB_IDS XDB ON "
					+ "((UD.ID=XDB.ID) OR (UD.ID=XDB.OTHER_ID)) LEFT JOIN common.COOLER_POSTS P ON (P.AUTHOR_ID=UD.ID) "
					+ "WHERE (UD.AIRLINE=AI.CODE) AND (P.THREAD_ID=?) GROUP BY UD.ID");
			_ps.setString(1, ",");
			_ps.setInt(2, threadID);
			return new UserDataMap(execute());
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
		try {
			prepareStatement("SELECT UD.*, AI.DOMAIN, AI.DBNAME, GROUP_CONCAT(DISTINCT XDB.ID SEPARATOR ?) AS IDS "
					+ "FROM common.AIRLINEINFO AI, common.USERDATA UD LEFT JOIN common.XDB_IDS XDB ON"
					+ "((XDB.ID=UD.ID) OR (XDB.OTHER_ID=UD.ID)) LEFT JOIN events.EVENT_SIGNUPS ES ON (ES.PILOT_ID=UD.ID) "
					+ "WHERE (UD.AIRLINE=AI.CODE) AND (ES.ID=?) GROUP BY UD.ID");
			_ps.setString(1, ",");
			_ps.setInt(2, eventID);
			return new UserDataMap(execute());
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
		StringBuilder sqlBuf = new StringBuilder("SELECT UD.*, AI.DOMAIN, AI.DBNAME, GROUP_CONCAT(DISTINCT XDB.ID "
				+ "SEPARATOR ?) AS IDS FROM common.AIRLINEINFO AI, common.USERDATA UD LEFT JOIN common.XDB_IDS XDB "
				+ "ON ((XDB.ID=UD.ID) OR (XDB.OTHER_ID=UD.ID)) WHERE (UD.AIRLINE=AI.CODE) AND (UD.ID IN (");

		// Strip out entries already in the cache
		if (log.isDebugEnabled())
			log.debug("Raw set size = " + ids.size());
		
		int querySize = 0;
		UserDataMap result = new UserDataMap();
		for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
			Integer id = i.next();

			// Pull from the cache if at all possible; this is an evil query
			UserData usr = _usrCache.get(id);
			if (usr == null) {
				querySize++;
				sqlBuf.append(id.toString());
				if (i.hasNext())
					sqlBuf.append(',');
			} else {
				result.put(null, usr);
			}
		}

		// Only execute the prepared statement if we haven't gotten anything from the cache
		if (log.isDebugEnabled())
			log.debug("Uncached set size = " + querySize);
		
		if (querySize > 0) {
			if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
				sqlBuf.setLength(sqlBuf.length() - 1);
			sqlBuf.append(")) GROUP BY UD.ID");

			// Execute the query
			setQueryMax(querySize);
			try {
				prepareStatementWithoutLimits(sqlBuf.toString());
				_ps.setString(1, ",");
				result.putAll(CollectionUtils.createMap(execute(), "ID"));
			} catch (SQLException se) {
				throw new DAOException(se);
			}
		}

		// Return the result container
		return result;
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List<UserData> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<UserData> results = new ArrayList<UserData>();
		while (rs.next()) {
			UserData usr = new UserData(rs.getInt(1));
			usr.setAirlineCode(rs.getString(2));
			usr.setTable(rs.getString(3));
			usr.setDomain(rs.getString(4));
			usr.setDB(rs.getString(5));
			
			// Get the crossdomain IDs
			Collection<String> xdb_ids = StringUtils.split(rs.getString(6), ",");
			if (xdb_ids != null) {
				for (Iterator<String> i = xdb_ids.iterator(); i.hasNext(); ) {
					int xdb_id = StringUtils.parse(i.next(), 0);
					if (xdb_id > 0)
						usr.addID(xdb_id);
				}
			}

			// Add to results and the cache
			results.add(usr);
			_usrCache.add(usr);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to iterate through AirlineInformation result sets.
	 */
	private List<AirlineInformation> executeAirlineInfo() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		List<AirlineInformation> results = new ArrayList<AirlineInformation>();
		while (rs.next()) {
			AirlineInformation info = new AirlineInformation(rs.getString(1), rs.getString(2));
			info.setDB(rs.getString(3));
			info.setDomain(rs.getString(4));
			info.setCanTransfer(rs.getBoolean(5));
			results.add(info);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}