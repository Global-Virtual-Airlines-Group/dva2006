// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.FlightStatus;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Elite program-related statistics.
 * @author Luke
 * @version 11.1
 * @since 9.2
 */

public class GetEliteStatistics extends EliteDAO {
	
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
		
		// Check the cache
		YearlyTotal result = new YearlyTotal(year, pilotID);
		CacheableList<YearlyTotal> results = _cache.get(Integer.valueOf(pilotID));
		if (results != null)
			return results.stream().filter(yt -> yt.getYear() == year).findFirst().orElse(result);
		
		try (PreparedStatement ps = prepare("SELECT SUM(IF(PE.SCORE_ONLY,0,1)) AS LEGS, SUM(IF(PE.SCORE_ONLY,0,PE.DISTANCE)) AS DST, (SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE, PIREPS P2 WHERE (PEE.ID=P2.ID) AND (P.PILOT_ID=P2.PILOT_ID) "
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
			
			rollbackTransaction();
			results.addAll(totals.values());
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads flight/distance/point statistics by Pilot for a one year interval.
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
}