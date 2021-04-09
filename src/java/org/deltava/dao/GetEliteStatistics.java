// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.FlightStatus;
import org.deltava.beans.stats.ElitePercentile;
import org.deltava.beans.stats.EliteStats;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Elite program-related statistics.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class GetEliteStatistics extends EliteDAO {
	
	private static final Cache<CacheableList<YearlyTotal>> _eliteTotalCache = CacheManager.getCollection(YearlyTotal.class, "EliteYearlyTotal");

	/**
	 * Initalizes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetEliteStatistics(Connection c) {
		super(c);
	}

	/**
	 * Returns Elite status totals per year.
	 * @param pilotID the Pilot's database ID
	 * @return a List of YearlyTotals beans, ordered by year descending
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<YearlyTotal> getEliteTotals(int pilotID) throws DAOException {
		
		// Check the cache
		CacheableList<YearlyTotal> results = _eliteTotalCache.get(Integer.valueOf(pilotID));
		if (results != null)
			return results.clone();
		
		try (PreparedStatement ps = prepare("SELECT PE.YR AS Y, COUNT(DISTINCT P.ID), SUM(PE.DISTANCE) AS DST, (SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE WHERE (PEE.ID=PE.ID)) AS PTS FROM PIREPS P, PIREP_ELITE PE "
			+ "WHERE (P.ID=PE.ID) AND (P.PILOT_ID=?) GROUP BY Y ORDER BY Y DESC")) {
			ps.setInt(1, pilotID);
			
			results = new CacheableList<YearlyTotal>(Integer.valueOf(pilotID));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					YearlyTotal t = new YearlyTotal(rs.getInt(1), pilotID);
					t.addLegs(rs.getInt(2), rs.getInt(3), rs.getInt(4));
					results.add(t);
				}
			}
			
			_eliteTotalCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads flight/distance percentiles by Pilot for a one year interval.
	 * @param year the program year
	 * @param granularity the percentile granularity
	 * @param isAvg TRUE to do the average across the percentile, FALSE for the base
	 * @return a PercentileStatsEntry
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ElitePercentile> getElitePercentiles(int year, int granularity, boolean isAvg) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.PILOT_ID, COUNT(DISTINCT P.ID) AS CNT, SUM(PE.DISTANCE) AS DST, (SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE WHERE (PEE.ID=PE.ID)) AS PTS FROM PIREPS P, PIREP_ELITE PE "
			+ "WHERE (P.ID=PE.ID) AND ((P.DATE>=?) AND (P.DATE<=?)) AND (P.STATUS=?) GROUP BY P.PILOT_ID ORDER BY CNT")) {
			ZonedDateTime sd = LocalDate.of(year, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC);
			ps.setTimestamp(1, createTimestamp(sd.toInstant()));
			ps.setTimestamp(2, createTimestamp(sd.plusYears(1).minusSeconds(1).toInstant()));
			ps.setInt(3, FlightStatus.OK.ordinal());
			
			// Load from the database
			List<YearlyTotal> rawResults = new ArrayList<YearlyTotal>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					YearlyTotal yt = new YearlyTotal(year, rs.getInt(1));
					yt.addLegs(rs.getInt(2), rs.getInt(3), rs.getInt(4));
					rawResults.add(yt);
				}
			}
			
			// Convert to percentiles
			List<ElitePercentile> results = new ArrayList<ElitePercentile>();
			for (int pct = 0; pct < 100; pct += granularity) {
				int startIdx = rawResults.size() * pct / 100;
				int endIdx = isAvg ? Math.min(rawResults.size() - 1, rawResults.size() * (pct + 1) / 100) : startIdx + 1;
				
				// Average the percentile (you may want to do just the minimum)
				YearlyTotal totals = new YearlyTotal(year, 1);
				for (int idx = startIdx; idx < endIdx; idx++) {
					YearlyTotal yt = rawResults.get(idx);
					totals.addLegs(yt.getLegs(), yt.getDistance(), yt.getPoints());
				}

				ElitePercentile ep = new ElitePercentile(pct); int pilots = (endIdx - startIdx);
				ep.setInfo(totals.getLegs(), totals.getDistance() / pilots, totals.getPoints() / pilots);
				results.add(ep);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads Elite program statistics for a given year. 
	 * @param year the program year
	 * @return a List of EliteStatus beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EliteStats> getStatistics(int year) throws DAOException {
		List<EliteStats> results = new ArrayList<EliteStats>();
		try (PreparedStatement ps = prepare("SELECT EP.NAME, P.PILOT_ID, COUNT(PE.ID) AS CNT, SUM(PE.DISTANCE) AS DST, SUM(PE.SCORE) AS PTS, MAX(SUM(PE.DISTANCE)) OVER(PARTITION BY EP.NAME) AS MAXLDST, "
			+ "STDDEV(SUM(PE.DISTANCE)) OVER (PARTITION BY EP.NAME) AS SDLDST, MAX(COUNT(PE.ID)) OVER (PARTITION BY EP.NAME) AS MAXLLEGS, STDDEV(COUNT(PE.ID)) OVER (PARTITION BY EP.NAME) AS SDLLEGS FROM "
			+ "ELITE_PILOT EP, PIREPS P, PIREP_ELITE PE WHERE (EP.PILOT_ID=P.PILOT_ID) AND (EP.YR=?) AND (P.STATUS=?) AND (P.ID=PE.ID) AND (PE.YR=EP.YR) GROUP BY EP.NAME, P.PILOT_ID ORDER BY EP.NAME, P.PILOT_ID")) {
			ps.setInt(1, year);
			ps.setInt(2, FlightStatus.OK.ordinal());
			
			EliteStats lastStats = new EliteStats(EliteLevel.EMPTY);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					EliteLevel lvl = new EliteLevel(year, rs.getString(1));
					if (!lvl.getName().equals(lastStats.getLevel().getName())) {
						lastStats = new EliteStats(lvl);
						results.add(lastStats);
						lastStats.setMaxLegs(8);
						lastStats.setMaxDistance(6);
						lastStats.setStandardDeviation(rs.getDouble(9), rs.getDouble(7));
					}
				
					lastStats.add(1, rs.getInt(3), rs.getInt(4), rs.getInt(5));
				}
			}
			
			populateLevels(results);
			Collections.sort(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}