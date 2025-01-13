// Copyright 2020, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.econ.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load Elite status levels. 
 * @author Luke
 * @version 11.5
 * @since 9.2
 */

public class GetElite extends EliteDAO {
	
	private static final Cache<EliteLifetimeStatus> _lstCache = CacheManager.get(EliteLifetimeStatus.class, "EliteLTStatus");
	
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
	 * @return a TreeSet of EliteLevel beans, in ascending order
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
	 * Returns all Lifetime status levels.
	 * @return a TreeSet of EliteLifetime beans, in asecnding order
	 * @throws DAOException if a JDBC error occurs
	 */
	public TreeSet<EliteLifetime> getLifetimeLevels() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT EL.*, DATABASE() FROM ELITE_LIFETIME EL ORDER BY LEGS, DISTANCE")) {
			List<EliteLifetime> results = executeLifetime(ps);
			populateLevels(results);
			return new TreeSet<EliteLifetime>(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves Elite lifetime program status for a number of Pilots.
	 * @param IDs a Collection of pilot IDs. This can either be a Collection of Integers, a Collection of {@link DatabaseBean} beans
	 * @param dbName the database name
	 * @return a Map of EliteLifetimeStatus beans for the specified year, keyed by Pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer,EliteLifetimeStatus> getLifetimeStatus(Collection<?> IDs, String dbName) throws DAOException {
		
		// Load from the cache
		Collection<EliteLifetimeStatus> results = new ArrayList<EliteLifetimeStatus>();
		Collection<Integer> dbKeys = toID(IDs);
		Map<Object, EliteLifetimeStatus> ce = _lstCache.getAll(dbKeys);
		results.addAll(ce.values());
		ce.values().stream().map(es -> Integer.valueOf(es.getID())).forEach(dbKeys::remove);
		
		if (dbKeys.size() > 0) {
			String db = formatDBName(dbName);
			StringBuilder sqlBuf = new StringBuilder("SELECT EL.*, ELS.ID, ELS.CREATED, ELS.UPD_REASON, DATABASE() FROM ");
			sqlBuf.append(db);
			sqlBuf.append(".ELITE_LIFETIME EL, ");
			sqlBuf.append(db);
			sqlBuf.append(".ELITE_LT_STATUS ELS WHERE (ELS.ABBR=EL.ABBR) AND (ELS.ID IN (");
			sqlBuf.append(StringUtils.listConcat(dbKeys, ","));
			sqlBuf.append(") ORDER BY ELS.CREATED DESC GROUP BY ELS.ID");

			Collection<EliteLifetimeStatus> dbResults = new ArrayList<EliteLifetimeStatus>();
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				dbResults.addAll(executeLTStatus(ps));
			} catch (SQLException se) {
				throw new DAOException(se);
			}
		}
		
		populateLevels(results);
		_lstCache.addAll(results);
		return CollectionUtils.createMap(results, EliteLifetimeStatus::getID);
	}

	/**
	 * Retrieves Elite program status for a number of Pilots.
	 * @param IDs a Collection of pilot IDs. This can either be a Collection of Integers, a Collection of {@link DatabaseBean} beans
	 * @param year the plan year
	 * @param db the database name
	 * @return a Map of EliteStatus beans for the specified year, keyed by Pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer,EliteStatus> getStatus(Collection<?> IDs, int year, String db) throws DAOException {
		
		// Load from the cache
		Collection<EliteStatus> results = new ArrayList<EliteStatus>();
		Collection<Integer> dbKeys = toID(IDs);
		Collection<Long> keys = dbKeys.stream().map(id -> EliteStatus.generateKey(year, id.intValue())).collect(Collectors.toSet());
		Map<Object, EliteStatus> ce = _stCache.getAll(keys);
		results.addAll(ce.values());
		ce.values().stream().map(es -> Integer.valueOf(es.getID())).forEach(dbKeys::remove);

		if (dbKeys.size() > 0) {
			StringBuilder sqlBuf = new StringBuilder("SELECT PILOT_ID, NAME, YR, DATABASE(), CREATED, UPD_REASON FROM ");
			sqlBuf.append(formatDBName(db));
			sqlBuf.append(".ELITE_STATUS WHERE (YR=?) AND (PILOT_ID IN (");
			sqlBuf.append(StringUtils.listConcat(dbKeys, ","));
			sqlBuf.append(")) ORDER BY PILOT_ID, CREATED DESC");
	
			Collection<EliteStatus> dbResults = new ArrayList<EliteStatus>();
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, year);
				dbResults.addAll(executeStatus(ps));
			} catch (SQLException se) {
				throw new DAOException(se);
			}
			
			// Now filter - at most one per pilot ID
			for (Iterator<EliteStatus> i = dbResults.iterator(); i.hasNext(); ) {
				EliteStatus es = i.next();
				Integer id = Integer.valueOf(es.getID());
				if (!dbKeys.remove(id))
					results.add(es);
			}
		}
		
		populateLevels(results);
		_stCache.addAll(results);
		return CollectionUtils.createMap(results, EliteStatus::getID);
	}
	
	/**
	 * Returns the most recent lifetime Elite status for a particular Pilot.
	 * @param pilotID the Pilot database ID
	 * @param dbName the database name
	 * @return an EliteLifetime bean, or null if no status
	 * @throws DAOException if a JDBC error occurs
	 */
	public EliteLifetimeStatus getLifetimeStatus(int pilotID, String dbName) throws DAOException {
		List<EliteLifetimeStatus> results = getAllLifetimeStatus(pilotID, dbName);
		return results.isEmpty() ? null : results.getFirst();
	}

	/**
	 * Returns all lifetime Elite status for a particular Pilot.
	 * @param pilotID the Pilot database ID
	 * @param dbName the database name
	 * @return a List of EliteLifetime beans, sorted by recency
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EliteLifetimeStatus> getAllLifetimeStatus(int pilotID, String dbName) throws DAOException {
		
		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT EL.*, ELS.ID, ELS.CREATED, ELS.UPD_REASON, DATABASE() FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".ELITE_LIFETIME EL, ");
		sqlBuf.append(db);
		sqlBuf.append(".ELITE_LT_STATUS ELS WHERE (ELS.ABBR=EL.ABBR) AND (ELS.ID=?) ORDER BY ELS.CREATED DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			List<EliteLifetimeStatus> results = executeLTStatus(ps);
			populateLevels(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
		return results.isEmpty() ? null : results.getLast();
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

		_stCache.addAll(results);
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
	 * Loads all Pilots who have obtained a particular lifetime Elite status.
	 * @param el the EliteLifetime
	 * @return a Collection of Pilot database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPilots(EliteLifetime el) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT PILOT_ID FROM ELITE_LT_STATUS WHERE (ABBR=?)")) {
			ps.setString(1, el.getCode());
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
	
	/**
	 * Counts the number of Pilots who have achieved a given lifetime Elite status level.
	 * @param el the EliteLifetime
	 * @return the number of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPilotCount(EliteLifetime el) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(ID) FROM ELITE_LT_STATUS WHERE (NAME=?)")) {
			ps.setString(1, el.getName());
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
	
	/*
	 * Helper method to parse lifetime elite status result sets, this has the raw data not the populated EliteLevel bean.
	 */
	private static List<EliteLifetimeStatus> executeLTStatus(PreparedStatement ps) throws SQLException {
		List<EliteLifetimeStatus> results = new ArrayList<EliteLifetimeStatus>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				EliteLifetime elt = new EliteLifetime(rs.getString(1));
				elt.setCode(rs.getString(2));
				elt.setDistance(rs.getInt(3));
				elt.setLegs(rs.getInt(4));
				EliteLevel el = new EliteLevel(rs.getInt(6), rs.getString(5), rs.getString(10));
				elt.setLevel(el);				
				EliteLifetimeStatus els = new EliteLifetimeStatus(rs.getInt(7), elt);
				els.setEffectiveOn(toInstant(rs.getTimestamp(8)));
				els.setUpgradeReason(UpgradeReason.values()[rs.getInt(9)]);
				results.add(els);
			}
		}
		
		return results;
	}
}