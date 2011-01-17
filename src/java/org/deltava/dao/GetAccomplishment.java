// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.*;
import org.deltava.beans.stats.Accomplishment.Unit;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Accomplishment profiles.
 * @author Luke
 * @version 3.6
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
	 * @param dbName the database name
	 * @return an Accomplishment bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Accomplishment get(int id, String dbName) throws DAOException {
		
		// Check the cache
		String db = formatDBName(dbName);
		Accomplishment a = _cache.get(db + "!!" + String.valueOf(id));
		if (a != null)
			return a;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT A.*, AI.CODE, COUNT(DISTINCT PA.PILOT_ID) AS CNT FROM common.AIRLINEINFO AI, ");
		sqlBuf.append(db);
		sqlBuf.append(".ACCOMPLISHMENTS A LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PILOT_ACCOMPLISHMENTS PA ON (A.ID=PA.AC_ID) WHERE (A.ID=?) AND (AI.DBNAME=?) GROUP BY A.ID LIMIT 1");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, id);
			_ps.setString(2, db);
			
			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				a = new Accomplishment(rs.getString(2));
				a.setID(rs.getInt(1));
				a.setUnit(Accomplishment.Unit.values()[rs.getInt(3)]);
				a.setValue(rs.getInt(4));
				a.setColor(rs.getInt(5));
				a.setChoices(StringUtils.split(rs.getString(6), ","));
				a.setActive(rs.getBoolean(7));
				a.setOwner(SystemData.getApp(rs.getString(8)));
				a.setPilots(rs.getInt(9));
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
	 * Returns all Accomplishments involving a particular Unit of measure, order by value.
	 * @param u the Unit
	 * @return a List of Accomplishment beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Accomplishment> getByUnit(Unit u) throws DAOException {
		try {
			prepareStatement("SELECT ID FROM ACCOMPLISHMENTS WHERE (UNIT=?) ORDER BY VAL");
			_ps.setInt(1, u.ordinal());
			
			// Execute the query
			Collection<Integer> IDs = new LinkedHashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				IDs.add(Integer.valueOf(rs.getInt(1)));
			
			rs.close();
			_ps.close();
			
			// Load the accomplishments 
			List<Accomplishment> results = new ArrayList<Accomplishment>();
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				Integer id = i.next();
				Accomplishment a = get(id.intValue());
				if (a != null)
					results.add(a);
			}
			
			return results;
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
				a.setChoices(StringUtils.split(rs.getString(6), ","));
				a.setActive(rs.getBoolean(7));
				a.setPilots(rs.getInt(8));
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
	 * Retrieves Accomplishments for a particular Pilot.
	 * @param p the Pilot
	 * @param dbName the database name
	 * @return a Collection of DatedAccomplishment beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DatedAccomplishment> getByPilot(Pilot p, String dbName) throws DAOException {
		Collection<DatedAccomplishment> results = new TreeSet<DatedAccomplishment>();
		for (DatedAccomplishmentID id : p.getAccomplishmentIDs()) {
			Accomplishment a = get(id.getID(), dbName);
			if (a != null) {
				DatedAccomplishment da = new DatedAccomplishment(id.getDate(), a);
				results.add(da);
			}
		}
		
		return results;
	}
	
	/**
	 * Retruns whether a Pilot was awarded a particular Accomplishment.
	 * @param pilotID the Pilot database ID
	 * @param a the Accomplishment bean
	 * @return TRUE if the Accomplishment was awarded, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean has(int pilotID, Accomplishment a) throws DAOException {
		try {
			prepareStatement("SELECT DATE FROM PILOT_ACCOMPLISHMENTS WHERE (PILOT_ID=?) AND (AC_ID=?)");
			_ps.setInt(1, pilotID);
			_ps.setInt(2, a.getID());
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			boolean isOK = rs.next() ? (rs.getTimestamp(1) != null) : false;
			rs.close();
			_ps.close();
			return isOK;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all accomplishments for a set of users.
	 * @param pilots a Map of Pilots, keyed by database ID
	 * @param dbName the database name
	 * @return a Map of Collection of DatedAccomplishments, keyed by user's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Collection<DatedAccomplishment>> get(Map<Integer, Pilot> pilots, String dbName) throws DAOException {
		Map<Integer, Collection<DatedAccomplishment>> results = new HashMap<Integer, Collection<DatedAccomplishment>>();
		for (Iterator<Map.Entry<Integer, Pilot>> i = pilots.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Integer, Pilot> me = i.next();
			Collection<DatedAccomplishmentID> IDs = me.getValue().getAccomplishmentIDs();
			if (!IDs.isEmpty()) {
				Collection<DatedAccomplishment> accs = getByPilot(me.getValue(), dbName);
				results.put(me.getKey(), accs);
			}
		}
		
		return results;
	}
}