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
 * @version 11.0
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
		if (results != null) return results.clone();
		
		try (PreparedStatement ps = prepare("SELECT YEAR(P.DATE) AS Y, SUM(IF(PE.SCORE_ONLY,0,1)) AS LEGS, SUM(IF(PE.SCORE_ONLY,0,PE.DISTANCE)) AS DST, (SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE WHERE (PEE.ID=PE.ID)) AS PTS FROM "
			+ "PIREPS P, PIREP_ELITE PE WHERE (P.ID=PE.ID) AND (P.PILOT_ID=?) GROUP BY Y ORDER BY Y DESC")) {
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
	 * Loads flight/distance/point statistics by Pilot for a one year interval.
	 * @param sd the start date
	 * @return a List of YearlyTotal beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetFlightReportStatistics#getPilotTotals(LocalDate)
	 */
	public List<YearlyTotal> getPilotTotals(LocalDate sd) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT P.PILOT_ID, COUNT(P.ID) AS LEGS, SUM(PE.DISTANCE) AS DST,(SELECT SUM(PEE.SCORE) FROM PIREP_ELITE_ENTRIES PEE, PIREPS P2 WHERE (PEE.ID=P2.ID) AND (P.PILOT_ID=P2.PILOT_ID) "
			+ "AND ((P2.DATE>=MAKEDATE(?,?)) AND (P2.DATE<MAKEDATE(?,?)))) AS PTS FROM PIREPS P, PIREP_ELITE PE WHERE (P.ID=PE.ID) AND ((P.DATE>=MAKEDATE(?,?)) AND (P.DATE<MAKEDATE(?,?))) AND (P.STATUS=?) GROUP BY P.PILOT_ID")) {
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
			List<YearlyTotal> results = new ArrayList<YearlyTotal>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					YearlyTotal yt = new YearlyTotal(sd.getYear(), rs.getInt(1));
					yt.addLegs(rs.getInt(2), rs.getInt(3), rs.getInt(4));
					results.add(yt);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}