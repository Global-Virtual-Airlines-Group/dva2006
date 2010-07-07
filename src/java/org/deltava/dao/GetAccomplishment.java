// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.Accomplishment;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Accomplishment profiles.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class GetAccomplishment extends DAO implements CachingDAO {
	
	private static final Cache<Accomplishment> _cache = new ExpiringCache<Accomplishment>(32, 3600);

	/**
	 * Intiailizes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAccomplishment(Connection c) {
		super(c);
	}

	@Override
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}
	
	/**
	 * Helper method to allow the write DAO to invalidate the cache.
	 */
	static void invalidate() {
		_cache.clear();
	}
	
	/**
	 * Returns an Accomplishment profile in the current airline.
	 * @param id the database ID
	 * @return an Accomplishment bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Accomplishment get(int id) throws DAOException {
		return get(id, SystemData.get("airline.code"));
	}

	/**
	 * Returns an Accomplishment profile.
	 * @param id the database ID
	 * @param ownerCode the code of the owner airline
	 * @return an Accomplishment bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Accomplishment get(int id, String ownerCode) throws DAOException {
		
		// Check the cache
		AirlineInformation aInfo = SystemData.getApp(ownerCode);
		Accomplishment a = _cache.get(aInfo.getDB() + "!!" + String.valueOf(id));
		if (a != null)
			return a;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT A.*, COUNT(DISTINCT PA.PILOT_ID) AS CNT FROM ");
		sqlBuf.append(aInfo.getDB());
		sqlBuf.append(".ACCOMPLISHMENTS A LEFT JOIN ");
		sqlBuf.append(aInfo.getDB());
		sqlBuf.append(".PILOT_ACCOMPLISHMENTS PA ON (A.ID=PA.AC_ID) WHERE (A.ID=?) GROUP BY A.ID LIMIT 1");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			
			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				a = new Accomplishment(rs.getString(2));
				a.setID(rs.getInt(1));
				a.setUnit(Accomplishment.Unit.values()[rs.getInt(3)]);
				a.setValue(rs.getInt(4));
				a.setColor(rs.getInt(5));
				a.setActive(rs.getBoolean(6));
				a.setPilots(rs.getInt(7));
				a.setOwner(aInfo);
				_cache.add(a);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return a;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Accomplishment profiles <i>in the current database</i>.
	 * @return a Collection of Accomplishment beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Accomplishment> getAll() throws DAOException {
		AirlineInformation ai = SystemData.getApp(SystemData.get("airline.code"));
		try {
			prepareStatement("SELECT A.*, COUNT(DISTINCT PA.PILOT_ID) AS CNT FROM ACCOMPLISHMENTS A "
				+ "LEFT JOIN PILOT_ACCOMPLISHMENTS PA ON (A.ID=PA.AC_ID) GROUP BY A.ID ORDER BY A.UNIT, A.VAL");
			Collection<Accomplishment> results = new ArrayList<Accomplishment>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Accomplishment a = new Accomplishment(rs.getString(2));
				a.setID(rs.getInt(1));
				a.setUnit(Accomplishment.Unit.values()[rs.getInt(3)]);
				a.setValue(rs.getInt(4));
				a.setColor(rs.getInt(5));
				a.setActive(rs.getBoolean(6));
				a.setPilots(rs.getInt(7));
				a.setOwner(ai);
				_cache.add(a);
				results.add(a);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Accomplishment achieved by a particular user.
	 * @param ud the User's UserData record
	 * @return a Collection of Accomplishment beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Accomplishment> get(UserData ud) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT APP_ID FROM ");
		sqlBuf.append(ud.getDB());
		sqlBuf.append(".PILOT_ACCOMPLISHMENTS WHERE (PILOT_ID=?) ORDER BY DATE");
		
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				IDs.add(Integer.valueOf(rs.getInt(1)));
			
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Load the Accomplishments
		Collection<Accomplishment> results = new ArrayList<Accomplishment>();
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			Accomplishment a = get(i.next().intValue(), ud.getAirlineCode()); 
			if (a != null)
				results.add(a);
		}
		
		return results;
	}
	
	/**
	 * Loads all accomplishments for a set of users.
	 * @param udm a UserDataMap
	 * @return a Map of Collection of Accomplishments, keyed by user's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Collection<Accomplishment>> get(UserDataMap udm) throws DAOException {
		Map<Integer, Collection<Accomplishment>> results = new HashMap<Integer, Collection<Accomplishment>>();
		for (Iterator<Map.Entry<Integer, UserData>> i = udm.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Integer, UserData> me = i.next();
			Collection<Accomplishment> ac = get(me.getValue());
			if (!ac.isEmpty())
				results.put(me.getKey(), ac);
		}
		
		return results;
	}
}