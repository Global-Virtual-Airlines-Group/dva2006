// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Flight Report statistics.
 * @author Luke
 * @version 2.6
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
	 * Returns the most popular route pairs filed by Pilots.
	 * @param noRoutes TRUE to include pairs without dispatch routes only, otherwise FALSE
	 * @param allFlights TRUE to include Flight Reports without ACARS, otherwise FALSE
	 * @return a Collection of RoutePair beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleRoute> getPopularRoutes(boolean noRoutes, boolean allFlights) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT P.AIRPORT_D, P.AIRPORT_A, COUNT(DISTINCT P.ID) AS CNT, "
				+ "COUNT(DISTINCT R.ID) AS RCNT FROM PIREPS P LEFT JOIN acars.ROUTES R ON "
				+ "(P.AIRPORT_D=R.AIRPORT_D) AND (P.AIRPORT_A=R.AIRPORT_A) AND (R.ACTIVE=?) ");
		buf.append("WHERE (P.STATUS=?) ");
		if (!allFlights)
			buf.append("AND ((P.ATTR & ?) > 0) ");
		if (_dayFilter > 0)
			buf.append("AND (P.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		buf.append("GROUP BY P.AIRPORT_D, P.AIRPORT_A ");
		if (noRoutes)
			buf.append("HAVING (RCNT=0) ");
		buf.append("ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setBoolean(++pos, true);
			_ps.setInt(++pos, FlightReport.OK);
			if (!allFlights)
				_ps.setInt(++pos, FlightReport.ATTR_ACARS);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);	
			
			// Execute the query
			Airline a = SystemData.getAirline(SystemData.get("airline.code"));
			Collection<ScheduleRoute> results = new ArrayList<ScheduleRoute>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ScheduleRoute rp = new ScheduleRoute(a, SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
				rp.setFlights(rs.getInt(3));
				rp.setRoutes(rs.getInt(4));
				results.add(rp);
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
	 * Returns statistical information about landing speeds and variations between them.
	 * @param eqType the aircraft type
	 * @param minLandings the minimum number of landings to qualify
	 * @return a Collection of LandingStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LandingStatistics> getLandings(String eqType, int minLandings) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT P.ID, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) AS PNAME, "
				+ "COUNT(PR.ID) AS CNT, ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(APR.LANDING_VSPEED) AS VS, "
				+ "STDDEV_POP(APR.LANDING_VSPEED) AS SD, AVG(RD.DISTANCE) AS DST, STDDEV_POP(RD.DISTANCE) AS DSD, "
				+ "IFNULL(IF(STDDEV_POP(RD.DISTANCE) < 20, NULL, (ABS(AVG(RD.DISTANCE))*3+STDDEV_POP(RD.DISTANCE)*2)/15), 650) "
				+ "+ (ABS(AVG(APR.LANDING_VSPEED)*3) + STDDEV_POP(APR.LANDING_VSPEED)*2) AS FACT FROM PILOTS P, "
				+ "PIREPS PR, ACARS_PIREPS APR LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON "
				+ "(C.ID=F.CON_ID) LEFT JOIN acars.RWYDATA RD ON (F.ID=RD.ID) AND (RD.ISTAKEOFF=?) WHERE (C.CLIENT_BUILD>?) "
				+ "AND (APR.ID=PR.ID) AND (APR.LANDING_VSPEED < 0) AND (PR.PILOT_ID=P.ID) AND (PR.STATUS=?) ");
		if (eqType != null)
			buf.append("AND (PR.EQTYPE=?) ");
		if (_dayFilter > 0)
			buf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		buf.append("GROUP BY P.ID HAVING (CNT>=?) ORDER BY FACT");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setBoolean(++pos, false);
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
				double dist = rs.getDouble(7);
				if (!rs.wasNull()) {
					ls.setAverageDistance(dist);
					ls.setDistanceStdDeviation(rs.getDouble(8));
				}
				
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
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.EQTYPE, COUNT(APR.ID) AS CNT, "
				+ "ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(APR.LANDING_VSPEED) AS VS, "
				+ "STDDEV_POP(APR.LANDING_VSPEED) AS SD, AVG(RD.DISTANCE) AS DST, "
				+ "STDDEV_POP(RD.DISTANCE) AS DSD, (ABS(AVG(APR.LANDING_VSPEED)*3)+"
				+ "STDDEV_POP(APR.LANDING_VSPEED)*2) AS FACT FROM PIREPS PR, ACARS_PIREPS APR "
				+ "LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON "
				+ "(C.ID=F.CON_ID) LEFT JOIN acars.RWYDATA RD ON (F.ID=RD.ID) AND (RD.ISTAKEOFF=?) "
				+ "WHERE (C.CLIENT_BUILD>?) AND (APR.ID=PR.ID) AND (APR.LANDING_VSPEED < 0) "
				+ "AND (PR.PILOT_ID=?) AND (PR.STATUS=?) ");
		if (_dayFilter > 0)
			sqlBuf.append("AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append("GROUP BY PR.EQTYPE HAVING (CNT>2) ORDER BY FACT");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, false);
			_ps.setInt(2, MIN_ACARS_CLIENT);
			_ps.setInt(3, pilotID);
			_ps.setInt(4, FlightReport.OK);
			if (_dayFilter > 0)
				_ps.setInt(5, _dayFilter);
			
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
				double avgDist = rs.getDouble(6);
				if (!rs.wasNull()) {
					ls.setAverageDistance(avgDist);
					ls.setDistanceStdDeviation(rs.getDouble(7));
				}
				
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
			results.put(Integer.valueOf(x), Integer.valueOf(0));
		
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
				results.put(Integer.valueOf(vs), Integer.valueOf(rs.getInt(2)));
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
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getSimStatistics(String groupBy, String orderBy) throws DAOException {
		
		// Get the SQL statement to use
		boolean isPilot = groupBy.contains("P.");
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, COUNT(F.FSVERSION) AS LEGS, SUM(F.DISTANCE) AS MILES, ROUND(SUM(F.FLIGHT_TIME), 1) "
				+ "AS HOURS, SUM(IF(F.FSVERSION=?,1,0)) AS FSX, SUM(IF(F.FSVERSION=?,1,0)) AS FS9, "
				+ "SUM(IF(F.FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS7, SUM(IF(FSVERSION<?,1,0)) AS FSO "
				+ "FROM PIREPS F");
		if (isPilot)
			sqlBuf.append(", PILOTS P");
		sqlBuf.append(" WHERE (F.STATUS=?)");
		if (isPilot)
			sqlBuf.append(" AND (P.ID=F.PILOT_ID)");
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, 2006);
			_ps.setInt(2, 2004);
			_ps.setInt(3, 2002);
			_ps.setInt(4, 2000);
			_ps.setInt(5, 2000);
			_ps.setInt(6, FlightReport.OK);
			
			// Execute the query
			List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(4), rs.getInt(3));
				entry.setFSVersionLegs(10, rs.getInt(5));
				entry.setFSVersionLegs(9, rs.getInt(6));
				entry.setFSVersionLegs(8, rs.getInt(7));
				entry.setFSVersionLegs(7, rs.getInt(8));
				entry.setFSVersionLegs(0, rs.getInt(9));
				results.add(entry);
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
	 * Retrieves aggregated approved Flight Report statistics for Flights flown using aircraft that are Primary Ratings for
	 * a particular Equipment Type program.
	 * @param eqType the Equipment type name
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @param descSort TRUE if a descending sort, otherwise FALSE
	 * @return a List of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightStatsEntry> getEQPIREPStatistics(String eqType, String groupBy, String orderBy, boolean descSort) throws DAOException {
		
		// Build the SQL statemnet
		boolean isPilot = groupBy.startsWith("P.");
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, "
				+ "AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS ACARSLEGS, "
				+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) "
				+ "AS DSPLEGS, ");
		sqlBuf.append(isPilot ? "1 AS PIDS" : "COUNT(DISTINCT F.PILOT_ID) AS PIDS");
		sqlBuf.append(" FROM EQRATINGS EQR, PIREPS F");
		if (isPilot)
			sqlBuf.append(" LEFT JOIN PILOTS P ON (P.ID=F.PILOT_ID)");
		sqlBuf.append(" WHERE (F.STATUS=?) AND ((EQR.EQTYPE=?) AND (EQR.RATING_TYPE=?) AND (EQR.RATED_EQ=F.EQTYPE))");
		if (_dayFilter > 0)
			sqlBuf.append("AND (F.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);
		if (descSort)
			sqlBuf.append(" DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.ATTR_ACARS);
			_ps.setInt(2, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(3, FlightReport.ATTR_HISTORIC);
			_ps.setInt(4, FlightReport.ATTR_DISPATCH);
			_ps.setInt(5, FlightReport.OK);
			_ps.setString(6, eqType);
			_ps.setInt(7, EquipmentType.PRIMARY_RATING);
			if (_dayFilter > 0)
				_ps.setInt(8, _dayFilter);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param pilotID the Pilot's database ID, or zero if airline-wide
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @param descSort TRUE if a descending sort, otherwise FALSE
	 * @return a List of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightStatsEntry> getPIREPStatistics(int pilotID, String groupBy, String orderBy, boolean descSort) throws DAOException {

		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		if (groupBy.contains("AP.")) {
			sqlBuf.append("AP.NAME");
			sqlBuf.append(getAirportJoinSQL(groupBy.replace("AP.", "F.")));
		} else if (groupBy.contains("P.")) {
			sqlBuf.append(groupBy);
			sqlBuf.append(getPilotJoinSQL());
		} else if (groupBy.contains("AL.")) {
			sqlBuf.append(groupBy);
			sqlBuf.append(getAirlineJoinSQL());
		} else {
			sqlBuf.append(groupBy);
			sqlBuf.append(getSQL());
		}
		
		if (pilotID != 0)
			sqlBuf.append("AND (F.PILOT_ID=?) ");
		sqlBuf.append("GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);
		if (descSort)
			sqlBuf.append(" DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.ATTR_ACARS);
			_ps.setInt(2, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(3, FlightReport.ATTR_HISTORIC);
			_ps.setInt(4, FlightReport.ATTR_DISPATCH);
			_ps.setInt(5, FlightReport.OK);
			if (pilotID != 0)
				_ps.setInt(6, pilotID);

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse stats entry result sets.
	 */
	private List<FlightStatsEntry> execute() throws SQLException {
		List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
		ResultSet rs = _ps.executeQuery();
		boolean hasPilotIDs = (rs.getMetaData().getColumnCount() > 10);
		while (rs.next()) {
			FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(4), rs.getInt(3));
			entry.setACARSLegs(rs.getInt(7));
			entry.setOnlineLegs(rs.getInt(8));
			entry.setHistoricLegs(rs.getInt(9));
			entry.setDispatchLegs(rs.getInt(10));
			if (hasPilotIDs)
				entry.setPilotIDs(rs.getInt(11));
			
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Private helper method to return SQL statement that doesn't involve joins on the <i>PILOTS </i> table.
	 */
	private String getSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, ROUND(SUM(F.FLIGHT_TIME), 1) "
				+ "AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) "
				+ "AS ACARSLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS,"
				+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS DSPLEGS, COUNT(DISTINCT F.PILOT_ID) AS PIDS FROM PIREPS F "
				+ "WHERE (F.STATUS=?) ";
	}

	/**
	 * Private helper method to return SQL statement that involves a join on the <i>PILOTS </i> table.
	 */
	private String getPilotJoinSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, "
			+ "ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS "
			+ "AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS ACARSLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS DSPLEGS, 1 AS PIDS "
			+ "FROM PILOTS P, PIREPS F WHERE (P.ID=F.PILOT_ID) AND (F.STATUS=?) ";
	}
	
	/**
	 * Private helper method to return SQL statement that involves a join on the <i>AIRLINES</i> table.
	 */
	private String getAirlineJoinSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS "
				+ "AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS ACARSLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, "
				+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS DSPLEGS, "
				+ "COUNT(DISTINCT F.PILOT_ID) AS PIDS FROM common.AIRLINES AL, PIREPS F WHERE (AL.CODE=F.AIRLINE) "
				+ "AND (F.STATUS=?) ";
	}

	/**
	 * Private helper method to return SQL statement that involves a join on the <i>AIRPORTS</i> table.
	 */
	private String getAirportJoinSQL(String apColumn) {
		return " AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS "
				+ "AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS ACARSLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, "
				+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS DSPLEGS, "
				+ "COUNT(DISTINCT F.PILOT_ID) AS PIDS FROM common.AIRPORTS AP, PIREPS F WHERE (AP.IATA="
				+ apColumn + ") AND (F.STATUS=?) ";
	}
}