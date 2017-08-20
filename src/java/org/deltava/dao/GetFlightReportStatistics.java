// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Flight Report statistics.
 * @author Luke
 * @version 7.5
 * @since 2.1
 */

public class GetFlightReportStatistics extends DAO {
	
	private static final int MAX_VSPEED = -2500;
	private static final int OPT_VSPEED = -225;
	private static final Cache<CacheableCollection<LandingStatistics>> _cache = CacheManager.getCollection(LandingStatistics.class, "LandingStats");
	private static final Cache<CacheableCollection<FlightStatsEntry>> _statCache = CacheManager.getCollection(FlightStatsEntry.class, "FlightStats");
	
	private int _dayFilter;

	public class DispatchScheduleRoute extends ScheduleRoute {
		private int _inactiveRoutes;
		
		protected DispatchScheduleRoute(Airline a, Airport ad, Airport aa) {
			super(a, ad, aa);
		}
		
		public int getInactiveRoutes() {
			return _inactiveRoutes;
		}
		
		public void setInactiveRoutes(int cnt) {
			_inactiveRoutes = cnt; 
		}
	}
	
	private class StatsCacheKey {
		private final String _eqType;
		private final int _minCount;
		
		StatsCacheKey(String eqType, int minCount) {
			super();
			_eqType = eqType;
			_minCount = minCount;
		}
		
		@Override
		public String toString() {
			return _eqType + "$" + _minCount;
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			return ((o instanceof StatsCacheKey) && (toString().equals(String.valueOf(o))));
		}
	}
	
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

	/*
	 * Helper method to extract a cache key from a prepared statment.
	 */
	private static String getCacheKey(String ps) {
		int ofs = ps.indexOf("SELECT");
		return (ofs == -1) ? ps : ps.substring(ofs);
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
			+ "(SELECT COUNT(DISTINCT R.ID) FROM acars.ROUTES R WHERE (R.ACTIVE=?) AND (R.AIRPORT_D=P.AIRPORT_D) "
			+ "AND (R.AIRPORT_A=P.AIRPORT_A)) AS RCNT, (SELECT COUNT(DISTINCT R.ID) FROM acars.ROUTES R WHERE "
			+ "(R.ACTIVE=?) AND (R.AIRPORT_D=P.AIRPORT_D) AND (R.AIRPORT_A=P.AIRPORT_A)) AS IRCNT FROM PIREPS P "
			+ "LEFT JOIN acars.ROUTES R ON (P.AIRPORT_D=R.AIRPORT_D) AND (P.AIRPORT_A=R.AIRPORT_A) WHERE (P.STATUS=?) ");
		if (!allFlights)
			buf.append("AND ((P.ATTR & ?) > 0) ");
		if (_dayFilter > 0)
			buf.append("AND (P.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		buf.append("GROUP BY P.AIRPORT_D, P.AIRPORT_A HAVING (CNT>?) ");
		if (noRoutes)
			buf.append("AND (RCNT=0) ");
		buf.append("ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setBoolean(++pos, true);
			_ps.setBoolean(++pos, false);
			_ps.setInt(++pos, FlightReport.OK);
			if (!allFlights)
				_ps.setInt(++pos, FlightReport.ATTR_ACARS);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);	
			_ps.setInt(++pos, 5);
			
			// Execute the query
			Airline a = SystemData.getAirline(SystemData.get("airline.code"));
			Collection<ScheduleRoute> results = new ArrayList<ScheduleRoute>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					DispatchScheduleRoute rp = new DispatchScheduleRoute(a, SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
					rp.setFlights(rs.getInt(3));
					rp.setRoutes(rs.getInt(4));
					rp.setInactiveRoutes(rs.getInt(5));
					results.add(rp);
				}
			}
			
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
		StringBuilder buf = new StringBuilder("SELECT P.ID, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) AS PNAME, COUNT(L.ID) AS CNT, "
			+ "ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(L.VSPEED) AS VS, STDDEV_POP(L.VSPEED) AS SD, AVG(L.RWYDISTANCE) AS DST, "
			+ "STDDEV_POP(L.RWYDISTANCE) AS DSD, IFNULL(IF(STDDEV_POP(L.RWYDISTANCE) < 20, NULL, (ABS(AVG(L.RWYDISTANCE))*3 + "
			+ "STDDEV_POP(L.RWYDISTANCE)*2)/15), 650) + (ABS(AVG(ABS(? - L.VSPEED))*3) + STDDEV_POP(L.VSPEED)*2) AS FACT FROM "
			+ "PILOTS P, PIREPS PR, FLIGHTSTATS_LANDING L WHERE (PR.ID=L.ID) AND (PR.PILOT_ID=P.ID) AND (PR.STATUS=?) ");
		if (eqType != null)
			buf.append("AND (PR.EQTYPE=?) ");
		if (_dayFilter > 0)
			buf.append("AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		buf.append("GROUP BY P.ID HAVING (CNT>=?) ORDER BY FACT");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setInt(++pos, OPT_VSPEED);
			_ps.setInt(++pos, FlightReport.OK);
			if (eqType != null)
				_ps.setString(++pos, eqType);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, minLandings);
			
			// Execute the query
			List<LandingStatistics> results = new ArrayList<LandingStatistics>(32);
			try (ResultSet rs = _ps.executeQuery()) {
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
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns touchdown speed and runway distance data for all of a Pilot's flights. 
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of LandingStats beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LandingStatistics> getLandingData(int pilotID) throws DAOException {

		// Check the cache
		StatsCacheKey key = new StatsCacheKey("$PILOTIND", pilotID);
		CacheableCollection<LandingStatistics> results = _cache.get(key);
		if (results != null)
			return results.clone();

		try {
			prepareStatement("SELECT PR.EQTYPE, APR.LANDING_VSPEED, RD.DISTANCE, RD.LENGTH FROM PIREPS PR, "
				+ "ACARS_PIREPS APR, acars.RWYDATA RD WHERE (APR.ACARS_ID=RD.ID) AND (RD.ISTAKEOFF=?) AND "
				+ "(APR.CLIENT_BUILD>?) AND (RD.DISTANCE<RD.LENGTH) AND (APR.ID=PR.ID) AND (PR.PILOT_ID=?) "
				+ "AND (PR.STATUS=?) ORDER BY ABS(? - APR.LANDING_VSPEED)");
			_ps.setBoolean(1, false);
			_ps.setInt(2, FlightReport.MIN_ACARS_CLIENT);
			_ps.setInt(3, pilotID);
			_ps.setInt(4, FlightReport.OK);
			_ps.setInt(5, OPT_VSPEED);

			// Execute the query
			results = new CacheableList<LandingStatistics>(key);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					LandingStatistics ls = new LandingStatistics(null, rs.getString(1));
					ls.setID(pilotID);
					ls.setLegs(1);
					ls.setAverageSpeed(rs.getInt(2));
					ls.setAverageDistance(rs.getInt(3));
					ls.setDistanceStdDeviation(rs.getInt(4));
					results.add(ls);
				}
			}
			
			_ps.close();
			_cache.add(results);
			return results.clone();
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
		
		// Check the cache
		StatsCacheKey key = new StatsCacheKey("$PILOT", pilotID);
		CacheableCollection<LandingStatistics> results = _cache.get(key);
		if (results != null)
			return results.clone();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT L.EQTYPE, COUNT(L.ID) AS CNT, ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(L.VSPEED) AS VS, "
			+ "STDDEV_POP(L.VSPEED) AS SD, AVG(L.RWYDISTANCE) AS DST, STDDEV_POP(L.RWYDISTANCE) AS DSD, (ABS(AVG(ABS(?-L.VSPEED))*3)+"
			+ "STDDEV_POP(L.VSPEED)*2) AS FACT FROM PIREPS PR, FLIGHTSTATS_LANDING L WHERE (PR.ID=L.ID) AND (L.PILOT_ID=?) ");
		if (_dayFilter > 0)
			sqlBuf.append("AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append("GROUP BY L.EQTYPE HAVING (CNT>1) ORDER BY FACT");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, OPT_VSPEED);
			_ps.setInt(2, pilotID);
			if (_dayFilter > 0)
				_ps.setInt(3, _dayFilter);
			
			// Execute the query
			results = new CacheableList<LandingStatistics>(key);
			try (ResultSet rs = _ps.executeQuery()) {
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
			}
			
			_ps.close();
			_cache.add(results);
			return results.clone();
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
		final Integer ZERO = Integer.valueOf(0);
		Map<Integer, Integer> results = new TreeMap<Integer, Integer>();
		for (int x = MAX_VSPEED; x <= 0; x += range)
			results.put(Integer.valueOf(x), ZERO);
		
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT ROUND(VSPEED / ?) * ? AS RNG, COUNT(ID) "
				+ "FROM FLIGHTSTATS_LANDING WHERE (PILOT_ID=?) AND (VSPEED>?) GROUP BY RNG");
			_ps.setInt(1, range);
			_ps.setInt(2, range);
			_ps.setInt(3, pilotID);
			_ps.setInt(4, MAX_VSPEED);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Trim out any without a value
		for (Iterator<Map.Entry<Integer, Integer>> i = results.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Integer, Integer> me = i.next();
			if (me.getValue() == ZERO)
				i.remove();
			else
				return results;
		}
		
		return results;
	}

	/**
	 * Retrieves aggregated approved Flight Report statistics for Flights flown using aircraft that are Primary Ratings for
	 * a particular Equipment Type program.
	 * @param eqType the Equipment type name
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getEQPIREPStatistics(String eqType, String groupBy, String orderBy) throws DAOException {
		
		// Build the SQL statemnet
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, "
			+ "AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SAL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OVL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OIL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, COUNT(DISTINCT F.PILOT_ID) AS PIDS, SUM(PAX), SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS "
			+ "FROM EQRATINGS EQR, PIREPS F WHERE (F.STATUS=?) AND ((EQR.EQTYPE=?) AND (EQR.RATING_TYPE=?) AND (EQR.RATED_EQ=F.EQTYPE))");
		if (_dayFilter > 0)
			sqlBuf.append("AND (F.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.ATTR_ACARS);
			_ps.setInt(2, FlightReport.ATTR_VATSIM);
			_ps.setInt(3, FlightReport.ATTR_IVAO);
			_ps.setInt(4, FlightReport.ATTR_HISTORIC);
			_ps.setInt(5, FlightReport.ATTR_DISPATCH);
			_ps.setInt(6, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(7, FlightReport.OK);
			_ps.setString(8, eqType);
			_ps.setInt(9, EquipmentType.Rating.PRIMARY.ordinal());
			if (_dayFilter > 0)
				_ps.setInt(10, _dayFilter);
			
			// Check the cache
			String cacheKey = getCacheKey(_ps.toString());
			CacheableCollection<FlightStatsEntry> results = _statCache.get(cacheKey);
			if (results != null) {
				_ps.close();
				return results.clone();
			}
			
			// Do the query
			results = new CacheableList<FlightStatsEntry>(cacheKey);
			results.addAll(execute());
			_statCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated Charter Flight Report statistics.
	 * @param s the statistics sorting option
	 * @param grp the statistics grouping option
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getCharterStatistics(FlightStatsSort s, FlightStatsGroup grp) throws DAOException {
		
		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(grp.getSQL());
		sqlBuf.append(grp.isPilotGroup() ? getPilotJoinSQL() : getSQL());
		sqlBuf.append("AND ((F.ATTR & ?) > 0) GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.ATTR_ACARS);
			_ps.setInt(2, FlightReport.ATTR_VATSIM);
			_ps.setInt(3, FlightReport.ATTR_IVAO);
			_ps.setInt(4, FlightReport.ATTR_HISTORIC);
			_ps.setInt(5, FlightReport.ATTR_DISPATCH);
			_ps.setInt(6, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(7, FlightReport.OK);
			_ps.setInt(8, FlightReport.ATTR_CHARTER);
			
			// Check the cache
			String cacheKey = getCacheKey(_ps.toString());
			CacheableCollection<FlightStatsEntry> results = _statCache.get(cacheKey);
			if (results != null) {
				_ps.close();
				return results.clone();
			}
			
			// Get the results
			results = new CacheableList<FlightStatsEntry>(cacheKey);
			results.addAll(execute());
			_statCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the total number of passengers carried by a Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return the number of passengers
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPassengers(int pilotID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT SUM(PAX) FROM PIREPS WHERE (STATUS=?) AND (PILOT_ID=?)");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, pilotID);
			
			int result = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				result = rs.next() ? rs.getInt(1) : 0;
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of Charter flights flown by a Pilot in a particular time interval.
	 * @param pilotID the Pilot's datbase ID
	 * @param days the number of days, zero for all
	 * @return the number of approved Charter flights
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCharterCount(int pilotID, int days) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(ID) FROM PIREPS WHERE (PILOT_ID=?) AND (STATUS=?) AND ((ATTR & ?) > 0)");
		if (days > 0)
			sqlBuf.append(" AND (DATE >= DATE_SUB(CURDATE(), INTERVAL ? DAY))");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, pilotID);
			_ps.setInt(2, FlightReport.OK);
			_ps.setInt(3, FlightReport.ATTR_CHARTER);
			if (days > 0)
				_ps.setInt(4, days);
			
			// Execute the query
			int count = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					count = rs.getInt(1);
			}
			
			_ps.close();
			return count;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param pilotID the Pilot's database ID, or zero if airline-wide
	 * @param s the statistics sorting option
	 * @param grp the statistics grouping option
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getPIREPStatistics(int pilotID, FlightStatsSort s, FlightStatsGroup grp) throws DAOException {

		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		if (grp.isAirportGroup()) {
			sqlBuf.append("AP.NAME");
			sqlBuf.append(getAirportJoinSQL(grp.getSQL().replace("AP.", "F.")));
		} else {
			sqlBuf.append(grp.getSQL());
			sqlBuf.append(grp.isPilotGroup() ? getPilotJoinSQL() : getSQL());
		}
		
		if (pilotID != 0)
			sqlBuf.append("AND (F.PILOT_ID=?) ");
		
		sqlBuf.append("GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());

		try {
			prepareStatement(sqlBuf.toString());
			int param = 0;
			_ps.setInt(++param, FlightReport.ATTR_ACARS);
			_ps.setInt(++param, FlightReport.ATTR_VATSIM);
			_ps.setInt(++param, FlightReport.ATTR_IVAO);
			_ps.setInt(++param, FlightReport.ATTR_HISTORIC);
			_ps.setInt(++param, FlightReport.ATTR_DISPATCH);
			_ps.setInt(++param, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(++param, FlightReport.OK);
			if (pilotID != 0)
				_ps.setInt(++param, pilotID);
			
			// Check the cache
			String cacheKey = getCacheKey(_ps.toString());
			CacheableCollection<FlightStatsEntry> results = _statCache.get(cacheKey);
			if (results != null) {
				_ps.close();
				_ps = null;
				return results.clone();
			}
			
			// Get the results
			results = new CacheableList<FlightStatsEntry>(cacheKey);
			results.addAll(execute());
			_statCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse stats entry result sets.
	 */
	private List<FlightStatsEntry> execute() throws SQLException {
		List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(4), rs.getInt(3));
				entry.setACARSLegs(rs.getInt(7));
				entry.setVATSIMLegs(rs.getInt(8));
				entry.setIVAOLegs(rs.getInt(9));
				entry.setHistoricLegs(rs.getInt(10));
				entry.setDispatchLegs(rs.getInt(11));
				entry.setPilotIDs(rs.getInt(12));
				entry.setPax(rs.getInt(14));
				entry.setLoadFactor(rs.getDouble(15));
				results.add(entry);
			}
		}

		_ps.close();
		return results;
	}
	
	/*
	 * Private helper method to return SQL statement that doesn't involve joins on the <i>PILOTS</i> table.
	 */
	private static String getSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) "
			+ "AS SAL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OVL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OIL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, "
			+ "COUNT(DISTINCT F.PILOT_ID) AS PIDS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(F.PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM PIREPS F WHERE (F.STATUS=?) ";
	}
	
	/*
	 * Private helper method to return SQL statement that involves a join on the <i>PILOTS</i> table.
	 */
	private static String getPilotJoinSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS "
			+ "AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SAL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OVL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OIL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, 1 AS PIDS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(F.PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM PILOTS P, PIREPS F WHERE (P.ID=F.PILOT_ID) AND (F.STATUS=?) ";
	}
	
	/*
	 * Private helper method to return SQL statement that involves a join on the <i>AIRPORTS</i> table.
	 */
	private static String getAirportJoinSQL(String apColumn) {
		return " AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS "
			+ "AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SAL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OVL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OIL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, COUNT(DISTINCT F.PILOT_ID) AS PIDS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(F.PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM "
			+ "common.AIRPORTS AP, PIREPS F WHERE (AP.IATA=" + apColumn + ") AND (F.STATUS=?) ";
	}
}