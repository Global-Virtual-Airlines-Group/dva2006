// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.FlightReport;
import org.deltava.beans.stats.*;

/**
 * A Data Access Object to retrieve Flight Report statistics.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class GetFlightReportStatistics extends DAO {
	
	private static final int MIN_ACARS_CLIENT = 61;
	
	private int _dayFilter;

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReportStatistics(Connection c) {
		super(c);
	}

	/**
	 * Sets the maximum number of days in the past to include.
	 * @param days the number of days
	 * @since 2.1
	 */
	public void setDayFilter(int days) {
		_dayFilter = Math.max(0, days);
	}
	
	/**
	 * Determines the ratio of in-schedule to out-of-schedule flight legs. This counts all non-draft and non-rejected
	 * Flight Reports when calculating the ratio.
	 * @param pilotID the Pilot's database ID
	 * @return the ratio of scheduled to non-scheduled flights, or zero if no flights flown
	 * @throws DAOException if a JDBC error occurs
	 */
	public double getScheduledRatio(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(ID), SUM(IF((ATTR & ?) > 0, 1, 0)) FROM PIREPS WHERE (PILOT_ID=?) AND "
					+ "(STATUS <> ?) AND (STATUS <> ?)");
			_ps.setInt(1, FlightReport.ATTR_ROUTEWARN);
			_ps.setInt(2, pilotID);
			_ps.setInt(3, FlightReport.REJECTED);
			_ps.setInt(4, FlightReport.DRAFT);

			// Execute the query - return zero if no flights flown
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return 0;
			}

			// Calculate the numbers
			int totalFlights = rs.getInt(1);
			int invalidFlights = rs.getInt(2);

			// Clean up and return ratio
			rs.close();
			_ps.close();
			return (totalFlights - invalidFlights) * 1.0 / invalidFlights;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns statistical information about landing speeds and variations between them.
	 * @param eqType the aircraft type
	 * @param minLandings the minimum number of landings to qualify
	 * @return a Collection of LandingStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LandingStatistics> getLandings(String eqType, int minLandings) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT P.ID, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) AS PNAME, COUNT(PR.ID) AS CNT, "
				+ "ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(APR.LANDING_VSPEED) AS VS, STDDEV_POP(APR.LANDING_VSPEED) AS SD, "
				+ "(ABS(AVG(APR.LANDING_VSPEED)*3)+STDDEV_POP(APR.LANDING_VSPEED)*2) AS FACT FROM PILOTS P, PIREPS PR, "
				+ "ACARS_PIREPS APR LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) "
				+ "WHERE (C.CLIENT_BUILD>?) AND (APR.ID=PR.ID) AND (PR.PILOT_ID=P.ID) AND (PR.STATUS=?) ");
		if (eqType != null)
			buf.append("AND (PR.EQTYPE=?) ");
		if (_dayFilter > 0)
			buf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		buf.append("GROUP BY P.ID HAVING (CNT>=?) ORDER BY FACT");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setInt(++pos, MIN_ACARS_CLIENT);
			_ps.setInt(++pos, FlightReport.OK);
			if (eqType != null)
				_ps.setString(++pos, eqType);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, minLandings);
			
			// Execute the query
			Collection<LandingStatistics> results = new ArrayList<LandingStatistics>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				LandingStatistics ls = new LandingStatistics(rs.getString(2), null);
				ls.setID(rs.getInt(1));
				ls.setLegs(rs.getInt(3));
				ls.setHours(rs.getDouble(4));
				ls.setAverageSpeed(rs.getDouble(5));
				ls.setStdDeviation(rs.getDouble(6));
				results.add(ls);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns statistical information about a pilot's landing speeds and variations between them.
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of LandingStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LandingStatistics> getLandings(int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.EQTYPE, COUNT(PR.ID) AS CNT, ROUND(SUM(PR.FLIGHT_TIME),1) "
				+ "AS HRS, AVG(APR.LANDING_VSPEED) AS VS, STDDEV_POP(APR.LANDING_VSPEED) AS SD, "
				+ "(ABS(AVG(APR.LANDING_VSPEED)*3)+STDDEV_POP(APR.LANDING_VSPEED)*2) AS FACT FROM "
				+ "PIREPS PR, ACARS_PIREPS APR LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN "
				+ "acars.CONS C ON (C.ID=F.CON_ID) WHERE (C.CLIENT_BUILD>?) AND (APR.ID=PR.ID) AND "
				+ "(PR.PILOT_ID=?) AND (PR.STATUS=?) ");
		if (_dayFilter > 0)
			sqlBuf.append("AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append("GROUP BY PR.EQTYPE HAVING (CNT>1) ORDER BY FACT");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, MIN_ACARS_CLIENT);
			_ps.setInt(2, pilotID);
			_ps.setInt(3, FlightReport.OK);
			if (_dayFilter > 0)
				_ps.setInt(4, _dayFilter);
			
			// Execute the query
			Collection<LandingStatistics> results = new ArrayList<LandingStatistics>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				LandingStatistics ls = new LandingStatistics(null, rs.getString(1));
				ls.setID(pilotID);
				ls.setLegs(rs.getInt(2));
				ls.setHours(rs.getDouble(3));
				ls.setAverageSpeed(rs.getDouble(4));
				ls.setStdDeviation(rs.getDouble(5));
				results.add(ls);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of landings within a particular vertical speed range.
	 * @param pilotID the Pilot's database ID
	 * @param range the size of the vertical speed ranges
	 * @return a Map of landing counts keyed by vertical speed range
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Integer> getLandingCounts(int pilotID, int range) throws DAOException {
		
		// Init the Map
		Map<Integer, Integer> results = new TreeMap<Integer, Integer>();
		for (int x = -1200; x <= 0; x += range)
			results.put(new Integer(x), Integer.valueOf(0));
		
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT ROUND(APR.LANDING_VSPEED / ?) * ? AS RNG, "
					+ "COUNT(APR.ACARS_ID) FROM ACARS_PIREPS APR, PIREPS PR WHERE (APR.ID=PR.ID) AND "
					+ "(PR.STATUS=?) AND (PR.PILOT_ID=?) GROUP BY RNG");
			_ps.setInt(1, range);
			_ps.setInt(2, range);
			_ps.setInt(3, FlightReport.OK);
			_ps.setInt(4, pilotID);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				int vs = Math.max(-1200, rs.getInt(1));
				results.put(new Integer(vs), new Integer(rs.getInt(2)));
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}