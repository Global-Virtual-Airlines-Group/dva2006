// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.econ.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Elite status levels. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class GetElite extends EliteDAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use 
	 */
	public GetElite(Connection c) {
		super(c);
	}

	/**
	 * Returns all Elite status levels.
	 * @return a List of EliteLevel beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EliteLevel> getLevels() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT EL.*, DATABASE() FROM ELITE_LEVELS EL ORDER BY YR DESC, LEGS, DISTANCE")) {
			return executeLevel(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Elite status levels for a given year.
	 * @param year the program year
	 * @return a TreeSet of EliteLevel beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public TreeSet<EliteLevel> getLevels(int year) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT EL.*, DATABASE() FROM ELITE_LEVELS EL WHERE (YR=?)")) {
			ps.setInt(1, year);
			return new TreeSet<EliteLevel>(executeLevel(ps));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads the number of pilots with Elite status for a given year.
	 * @param year the program year
	 * @return a sorted Map of Pilot counts, keyed by EliteLevel
	 * @throws DAOException if a JDBC error occurs
	 */
	public SortedMap<EliteLevel, Integer> getEliteCounts(int year) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT PILOT_ID, (SELECT NAME FROM ELITE_STATUS ES2 WHERE (ES2.PILOT_ID=ES.PILOT_ID) AND (ES2.YR=ES.YR) ORDER BY ES2.CREATED DESC LIMIT 1) AS LVL FROM ELITE_STATUS ES "
			+ "WHERE (ES.YR=?) GROUP BY ES.PILOT_ID")) {
			ps.setInt(1, year);
			
			Map<String, MutableInteger> rawResults = new TreeMap<String, MutableInteger>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String levelName = rs.getString(2);
					MutableInteger cnt = rawResults.get(levelName);
					if (cnt == null) {
						cnt = new MutableInteger(0);
						rawResults.put(levelName, cnt);
					}
					
					cnt.inc();
				}
			}

			String db = SystemData.get("airline.db");
			SortedMap<EliteLevel, Integer> results = new TreeMap<EliteLevel, Integer>();
			for (Map.Entry<String, MutableInteger> me : rawResults.entrySet()) {
				EliteLevel lvl = get(me.getKey(), year, db);
				results.put(lvl, me.getValue().getValue());
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * 
	 * @param IDs a Collection of pilot IDs. This can either be a Collection of Integers, a Collection of {@link DatabaseBean} beans
	 * @param year the plan year
	 * @param db the database name
	 * @return a Map of EliteStatus beans for the specified year, keyed by Pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer,EliteStatus> getStatus(Collection<?> IDs, int year, String db) throws DAOException {
		Collection<Integer> dbIDs = new HashSet<Integer>();
		
		// Load from the cache
		Collection<EliteStatus> results = new ArrayList<EliteStatus>();
		for (Iterator<?> i = IDs.iterator(); i.hasNext();) {
			Integer id = toID(i.next());
			Long cacheKey = EliteStatus.generateKey(year, id.intValue());
			EliteStatus st = _stCache.get(cacheKey);
			if (st != null )
				results.add(st);
			else
				dbIDs.add(id);
		}

		if (dbIDs.size() > 0) {
			StringBuilder sqlBuf = new StringBuilder("SELECT PILOT_ID, NAME, YR, DATABASE(), MAX(CREATED) AS CD, UPD_REASON FROM ");
			sqlBuf.append(formatDBName(db));
			sqlBuf.append(".ELITE_STATUS WHERE (YR=?) AND (PILOT_ID IN (");
			sqlBuf.append(StringUtils.listConcat(dbIDs, ","));
			sqlBuf.append(")) GROUP BY PILOT_ID ORDER BY CD DESC");
			
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, year);
				results.addAll(executeStatus(ps));
				populateLevels(results);
			} catch (SQLException se) {
				throw new DAOException(se);
			}
		}
		
		return CollectionUtils.createMap(results, EliteStatus::getID);
	}

	/**
	 * Loads a Pilot's most recent Elite status for a given year.
	 * @param pilotID the Pilot's database ID
	 * @param year the status year
	 * @return an EliteStatus bean, or null
	 * @throws DAOException if a JDBC error occurs
	 */
	public EliteStatus getStatus(int pilotID, int year) throws DAOException {
		List<EliteStatus> results = getAllStatus(pilotID, year);
		return results.isEmpty() ? null : results.get(results.size() - 1);
	}
	
	/**
	 * Loads a Pilot's Elite status updates for a particular year.
	 * @param pilotID the Pilot's database ID
	 * @param year the year
	 * @return a List of EliteStatus beans, ordered by acheivement date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EliteStatus> getAllStatus(int pilotID, int year) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PILOT_ID, NAME, YR, DATABASE(), CREATED, UPD_REASON FROM ELITE_STATUS WHERE (PILOT_ID=?)");
		if (year > 0) sqlBuf.append(" AND (YR=?)");
		sqlBuf.append(" ORDER BY CREATED");
		
		List<EliteStatus> results = new ArrayList<EliteStatus>();
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			if (year > 0) ps.setInt(2, year);
			results.addAll(executeStatus(ps));
			populateLevels(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return results;
	}
	
	/**
	 * Loads all Pilots who have obtained a particular Elite status in a given year.
	 * @param lvl the EliteLevel
	 * @return a Collection of Pilot database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPilots(EliteLevel lvl) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT PILOT_ID FROM ELITE_STATUS WHERE (NAME=?) AND (YR=?)")) {
			ps.setString(1, lvl.getName());
			ps.setInt(2, lvl.getYear());
		
			Collection<Integer> results = new HashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Counts the number of Pilots who have achieved a given Elite status level.
	 * @param lvl the EliteLevel
	 * @return the number of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPilotCount(EliteLevel lvl) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(PILOT_ID) FROM ELITE_STATUS WHERE (NAME=?) AND (YR=?)")) {
			ps.setString(1, lvl.getName());
			ps.setInt(2, lvl.getYear());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse elite status result sets, this has the raw data not the populated EliteLevel bean.
	 */
	private static List<EliteStatus> executeStatus(PreparedStatement ps) throws SQLException {
		List<EliteStatus> results = new ArrayList<EliteStatus>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				EliteLevel lvl = new EliteLevel(rs.getInt(3), rs.getString(2), rs.getString(4));
				EliteStatus st = new EliteStatus(rs.getInt(1), lvl);
				st.setEffectiveOn(toInstant(rs.getTimestamp(5)).plusSeconds(3600 * 12));
				st.setUpgradeReason(UpgradeReason.values()[rs.getInt(6)]);
				results.add(st);
			}
		}
		
		return results;
	}
}