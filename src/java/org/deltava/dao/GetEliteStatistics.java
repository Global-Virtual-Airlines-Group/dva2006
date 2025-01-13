// Copyright 2020, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.FlightStatus;

import org.deltava.util.MutableInteger;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read Elite program-related statistics.
 * @author Luke
 * @version 11.5
 * @since 9.2
 */

public class GetEliteStatistics extends EliteDAO {
	
	private static final Cache<YearlyTotal> _ltCache = CacheManager.get(YearlyTotal.class, "EliteLifetimeTotal");
	private static final Cache<CacheableList<YearlyTotal>> _cache = CacheManager.getCollection(YearlyTotal.class, "EliteYearlyTotal");

	/**
	 * Initalizes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetEliteStatistics(Connection c) {
		super(c);
	}
	
	/**
	 * Returns Elite status totals for a particiular year.
	 * @param pilotID the Pilot's database ID
	 * @param year the statistics year
	 * @return a YearlyTotals bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public YearlyTotal getEliteTotals(int pilotID, int year) throws DAOException {
		
		// Load single year from cache
		RolloverYearlyTotal result = new RolloverYearlyTotal(year, pilotID);
		CacheableList<YearlyTotal> results = _cache.get(result.cacheKey());
		if (results != null)
			return results.stream().filter(yt -> yt.getYear() == year).findFirst().orElse(result);
		
		// Check the cache for multi-year; if found then add this year
		results = _cache.get(Integer.valueOf(pilotID));
		if (results != null) {
			YearlyTotal yt2 = results.stream().filter(yt -> yt.getYear() == year).findFirst().orElse(result);
			CacheableList<YearlyTotal> r2 = new CacheableList<YearlyTotal>(result.cacheKey());
			r2.add(yt2);
			_cache.add(r2);
			return yt2;
		}
		
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT SUM(IF(PE.SCORE_ONLY,0,1)) AS LEGS, SUM(IF(PE.SCORE_ONLY,0,PE.DISTANCE)) AS DST, (SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE, PIREPS P2 WHERE (PEE.ID=P2.ID) AND (P.PILOT_ID=P2.PILOT_ID) "
					+ "AND (P2.STATUS=?) AND ((P2.DATE>=MAKEDATE(?,1)) AND (P2.DATE<MAKEDATE(?,1)))) AS PTS FROM PIREPS P, PIREP_ELITE PE WHERE (P.ID=PE.ID) AND (P.PILOT_ID=?) AND (P.STATUS=?) AND ((P.DATE>=MAKEDATE(?,1)) AND (P.DATE<MAKEDATE(?,1)))")) {
				ps.setInt(1, FlightStatus.OK.ordinal());
				ps.setInt(2, year);
				ps.setInt(3, year + 1);
				ps.setInt(4, pilotID);
				ps.setInt(5, FlightStatus.OK.ordinal());
				ps.setInt(6, year);
				ps.setInt(7, year + 1);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						result.addLegs(rs.getInt(1), rs.getInt(2), rs.getInt(3));
				}
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("SELECT LEGS, DISTANCE FROM ELITE_ROLLOVER WHERE (ID=?) AND (YEAR=?)")) {
				ps.setInt(1, pilotID);
				ps.setInt(2, year);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						result.addRollover(rs.getInt(1), rs.getInt(2), 0);
				}
			}
			
			rollbackTransaction();
			results = new CacheableList<YearlyTotal>(result.cacheKey());
			results.add(result);
			_cache.add(results);
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Elite status totals per year.
	 * @param pilotID the Pilot's database ID
	 * @return a List of YearlyTotals beans, ordered by year descending
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<YearlyTotal> getEliteTotals(int pilotID) throws DAOException {
		
		// Check the cache
		CacheableList<YearlyTotal> results = _cache.get(Integer.valueOf(pilotID));
		if (results != null) return results.clone();
		
		try {
			startTransaction();
			Map<Integer, YearlyTotal> totals = new TreeMap<Integer, YearlyTotal>(Collections.reverseOrder());
			try (PreparedStatement ps = prepare("SELECT YEAR(P.DATE) AS Y, SUM(IF(PE.SCORE_ONLY,0,1)) AS LEGS, SUM(IF(PE.SCORE_ONLY,0,PE.DISTANCE)) AS DST FROM PIREPS P, PIREP_ELITE PE WHERE (P.ID=PE.ID) AND (P.PILOT_ID=?) AND (P.STATUS=?) GROUP BY Y")) {
				ps.setInt(1, pilotID);
				ps.setInt(2, FlightStatus.OK.ordinal());
			
				results = new CacheableList<YearlyTotal>(Integer.valueOf(pilotID));
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						YearlyTotal t = new YearlyTotal(rs.getInt(1), pilotID);
						t.addLegs(rs.getInt(2), rs.getInt(3), 0);
						totals.put(Integer.valueOf(t.getYear()), t);
					}
				}
			}
			
			// This avoids a bad subquery
			try (PreparedStatement ps = prepare("SELECT YEAR(P.DATE) AS Y, SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE, PIREPS P WHERE (PEE.ID=P.ID) AND (P.PILOT_ID=?) AND (P.STATUS=?) GROUP BY Y")) {
				ps.setInt(1, pilotID);
				ps.setInt(2, FlightStatus.OK.ordinal());
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						YearlyTotal yt = totals.get(Integer.valueOf(rs.getInt(1)));
						yt.addLegs(0, 0, rs.getInt(2));
					}
				}
			}
			
			// Load rollover miles
			try (PreparedStatement ps = prepare("SELECT YEAR, LEGS, DISTANCE FROM ELITE_ROLLOVER WHERE (ID=?)")) {
				ps.setInt(1, pilotID);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						int year = rs.getInt(1);
						YearlyTotal yt = totals.get(Integer.valueOf(year)); RolloverYearlyTotal ryt = null;
						if (yt == null) { // Right after rollover, we may have rollover miles/legs but no logged flights yet
							ryt = new RolloverYearlyTotal(year, pilotID);
							totals.put(Integer.valueOf(year), ryt);
						} else {
							ryt = new RolloverYearlyTotal(yt);
							totals.put(Integer.valueOf(year), ryt);
						}
						
						ryt.addRollover(rs.getInt(2), rs.getInt(3), 0);
					}
				}
			}
			
			rollbackTransaction();
			results.addAll(totals.values());
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads flight/distance/point statistics by Pilot for a one year interval. <i>This excludes rollover legs and distance</i>
	 * @param sd the start date
	 * @return a List of YearlyTotal beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetFlightReportStatistics#getPilotTotals(LocalDate)
	 */
	public List<YearlyTotal> getPilotTotals(LocalDate sd) throws DAOException {
		
		// Check the cache
		Integer key = Integer.valueOf(-sd.getYear());
		CacheableList<YearlyTotal> results = _cache.get(key);
		if (results != null)
			return results.clone();
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT P.PILOT_ID, SUM(IF(PE.SCORE_ONLY,0,1)) AS LEGS, SUM(IF(PE.SCORE_ONLY,0,PE.DISTANCE)) AS DST, (SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE, PIREPS P2 WHERE (PEE.ID=P2.ID) AND "
			+ "(P.PILOT_ID=P2.PILOT_ID) AND ((P2.DATE>=MAKEDATE(?,?)) AND (P2.DATE<MAKEDATE(?,?)))) AS PTS FROM PIREPS P, PIREP_ELITE PE WHERE (P.ID=PE.ID) AND ((P.DATE>=MAKEDATE(?,?)) AND (P.DATE<MAKEDATE(?,?))) AND (P.STATUS=?) GROUP BY P.PILOT_ID")) {
			ps.setInt(1, sd.getYear());
			ps.setInt(2, sd.getDayOfYear());
			ps.setInt(3, sd.getYear() + 1);
			ps.setInt(4, sd.getDayOfYear());
			ps.setInt(5, sd.getYear());
			ps.setInt(6, sd.getDayOfYear());
			ps.setInt(7, sd.getYear() + 1);
			ps.setInt(8, sd.getDayOfYear());
			ps.setInt(9, FlightStatus.OK.ordinal());
			
			// Execute the query
			results = new CacheableList<YearlyTotal>(key);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					YearlyTotal yt = new YearlyTotal(sd.getYear(), rs.getInt(1));
					yt.addLegs(rs.getInt(2), rs.getInt(3), rs.getInt(4));
					results.add(yt);
				}
			}
			
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads total elite program mileage for a given Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return the total number of miles
	 * @throws DAOException if a JDBC error occurs
	 */
	public YearlyTotal getLifetimeTotals(int pilotID) throws DAOException {
		
		// Check the cache
		Integer k = Integer.valueOf(pilotID);
		YearlyTotal result = _ltCache.get(k);
		if (result != null)
			return result;
		
		YearlyTotal yt = new YearlyTotal(0, pilotID);
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SUM(IF(PE.SCORE_ONLY,0,1)) AS LEGS, SUM(IF(PE.SCORE_ONLY,0,PE.DISTANCE)) AS DST FROM PIREPS P LEFT JOIN PIREP_ELITE PE ON (P.ID=PE.ID) WHERE (P.PILOT_ID=?) AND (PE.ID IS NOT NULL)")) {
			ps.setInt(1, pilotID);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					yt.addLegs(rs.getInt(1), rs.getInt(2), 0);
					_ltCache.add(yt);
				}
			}
			
			return yt;
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
	 * Loads a Pilot's rollover totals for a given year.
	 * @param pilotID the Pilot's database ID
	 * @param year the year
	 * @return a YearlyTotal bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public YearlyTotal getRollover(int pilotID, int year) throws DAOException {
		RolloverYearlyTotal yt = new RolloverYearlyTotal(year, pilotID);
		try (PreparedStatement ps = prepareWithoutLimits("SELECT LEGS, DISTANCE, PTS FROM ELITE_ROLLOVER WHERE (ID=?) AND (YEAR=?) LIMIT 1")) {
			ps.setInt(1, pilotID);
			ps.setInt(2, year);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					yt.addRollover(rs.getInt(1), rs.getInt(2), rs.getInt(3));
			}
			
			return yt;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all rollover totals for a given year.
	 * @param year the year
	 * @return a Collection of YearlyTotal beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RolloverYearlyTotal> getRollover(int year) throws DAOException {
		List<RolloverYearlyTotal> results = new ArrayList<RolloverYearlyTotal>();
		try (PreparedStatement ps = prepare("SELECT ID, SUM(LEGS), SUM(DISTANCE), SUM(PTS) FROM ELITE_ROLLOVER WHERE (YEAR=?) GROUP BY ID")) {
			ps.setInt(1, year);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					RolloverYearlyTotal yt = new RolloverYearlyTotal(year, rs.getInt(1));
					yt.addRollover(rs.getInt(2), rs.getInt(3), rs.getInt(4));
					results.add(yt);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}