// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;

import org.deltava.beans.*;
import org.deltava.beans.econ.YearlyTotal;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Flight Report statistics.
 * @author Luke
 * @version 11.6
 * @since 2.1
 */

public class GetFlightReportStatistics extends DAO {

	private static final int MAX_VSPEED = -2500;
	
	private static final Cache<CacheableList<LandingStatistics>> _cache = CacheManager.getCollection(LandingStatistics.class, "LandingStats");
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
	 * Returns Airports with flights.
	 * @param isACARS TRUE to include only ACARS flights, otherwise FALSE 
	 * @return a Collection of RoutePair objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<RoutePair> getAirport(boolean isACARS) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AIRPORT_D, AIRPORT_A FROM PIREPS WHERE (STATUS=?) AND (DATE > DATE_SUB(CURDATE(), INTERVAL ? DAY))");
		if (isACARS)
			sqlBuf.append(" AND ((ATTR & ?)> 0)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, _dayFilter);
			if (isACARS) ps.setInt(3, FlightReport.ATTR_ACARS);
			
			Collection<RoutePair> results = new ArrayList<RoutePair>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(RoutePair.of(SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2))));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the most popular route pairs filed by a particular Pilot.
	 * @param pilotID the Pilot database ID
	 * @return a Collection of RouteStats beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<RouteStats> getPopularRoutes(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.AIRPORT_D, P.AIRPORT_A, COUNT(P.ID) AS CNT, SUM(IF((P.ATTR & ?)>0, 1,0)) AS ACARS, MAX(P.DATE) FROM PIREPS P WHERE (P.PILOT_ID=?) AND (P.STATUS=?) GROUP BY P.AIRPORT_D, P.AIRPORT_A ORDER BY CNT DESC")) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, pilotID);
			ps.setInt(3, FlightStatus.OK.ordinal());
			Collection<RouteStats> results = new ArrayList<RouteStats>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					RouteStats rt = new RouteStats(SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)), 0);
					rt.add(rs.getInt(3), rs.getInt(4));
					rt.setLastFlight(toInstant(rs.getTimestamp(5)));
					results.add(rt);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			int pos = 0;
			ps.setBoolean(++pos, true);
			ps.setBoolean(++pos, false);
			ps.setInt(++pos, FlightStatus.OK.ordinal());
			if (!allFlights)
				ps.setInt(++pos, FlightReport.ATTR_ACARS);
			if (_dayFilter > 0)
				ps.setInt(++pos, _dayFilter);	
			ps.setInt(++pos, 5);
			
			// Execute the query
			Airline a = SystemData.getAirline(SystemData.get("airline.code"));
			Collection<ScheduleRoute> results = new ArrayList<ScheduleRoute>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					DispatchScheduleRoute rp = new DispatchScheduleRoute(a, SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
					rp.setFlights(rs.getInt(3));
					rp.setRoutes(rs.getInt(4));
					rp.setInactiveRoutes(rs.getInt(5));
					results.add(rp);
				}
			}
			
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
		StringBuilder buf = new StringBuilder("SELECT P.ID, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) AS PNAME, COUNT(L.ID) AS CNT, ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(L.VSPEED) AS VS, "
			+ "STDDEV_POP(L.VSPEED) AS SD, AVG(L.RWYDISTANCE) AS DST, STDDEV_POP(L.RWYDISTANCE) AS DSD, AVG(L.SCORE) AS FACT FROM PILOTS P, PIREPS PR, FLIGHTSTATS_LANDING L WHERE (PR.ID=L.ID) "
			+ "AND (PR.PILOT_ID=P.ID) AND (PR.STATUS=?) ");
		if (eqType != null)
			buf.append("AND (PR.EQTYPE=?) ");
		if (_dayFilter > 0)
			buf.append("AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		buf.append("GROUP BY P.ID HAVING (CNT>=?) ORDER BY FACT DESC");
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			int pos = 0;
			ps.setInt(++pos, FlightStatus.OK.ordinal());
			if (eqType != null)
				ps.setString(++pos, eqType);
			if (_dayFilter > 0)
				ps.setInt(++pos, _dayFilter);
			ps.setInt(++pos, minLandings);
			
			// Execute the query
			List<LandingStatistics> results = new ArrayList<LandingStatistics>(32);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LandingStatistics ls = new LandingStatistics(rs.getString(2), null);
					ls.setID(rs.getInt(1));
					ls.setLegs(rs.getInt(3));
					ls.setHours(rs.getDouble(4));
					ls.setAverageSpeed(rs.getDouble(5));
					ls.setStdDeviation(rs.getDouble(6));
					ls.setAverageScore(rs.getDouble(9) / 100);
					double dist = rs.getDouble(7);
					if (!rs.wasNull()) {
						ls.setAverageDistance(dist);
						ls.setDistanceStdDeviation(rs.getDouble(8));
					}
				
					results.add(ls);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns touchdown speed and runway distance data for all of a Pilot's flights. 
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of TouchdownData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TouchdownData> getLandingData(int pilotID) throws DAOException {

		try (PreparedStatement ps = prepare("SELECT PR.ID, APR.LANDING_VSPEED, RD.DISTANCE, RD.LENGTH FROM PIREPS PR, ACARS_PIREPS APR, acars.RWYDATA RD WHERE (APR.ACARS_ID=RD.ID) AND "
			+ "(RD.ISTAKEOFF=?) AND (APR.CLIENT_BUILD>?) AND (RD.DISTANCE<RD.LENGTH) AND (APR.ID=PR.ID) AND (PR.PILOT_ID=?) AND (PR.STATUS=?) ORDER BY ABS(? - APR.LANDING_VSPEED)")) {
			ps.setBoolean(1, false);
			ps.setInt(2, FlightReport.MIN_ACARS_CLIENT);
			ps.setInt(3, pilotID);
			ps.setInt(4, FlightStatus.OK.ordinal());
			ps.setInt(5, LandingScorer.OPT_VSPEED);

			// Execute the query
			Collection<TouchdownData> results = new ArrayList<TouchdownData>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					TouchdownData td = new TouchdownData(rs.getInt(1));
					td.setVSpeed(rs.getInt(2));
					td.setDistance(rs.getInt(3));
					td.setLength(rs.getInt(4));
					results.add(td);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns statistical information about a pilot's landing speeds and variations between them.
	 * @param pilotID the Pilot's database ID
	 * @return a List of LandingStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<LandingStatistics> getLandings(int pilotID) throws DAOException {
		
		// Check the cache
		StatsCacheKey key = new StatsCacheKey("$PILOT", pilotID);
		CacheableList<LandingStatistics> results = _cache.get(key);
		if ((results != null) && (_queryMax > 0) && (results.size() >= _queryMax))
			return results.clone().subList(0, _queryMax);
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT L.EQTYPE, COUNT(L.ID) AS CNT, ROUND(SUM(PR.FLIGHT_TIME),1) AS HRS, AVG(L.VSPEED) AS VS, STDDEV_POP(L.VSPEED) AS SD, "
			+ "AVG(L.RWYDISTANCE) AS DST, STDDEV_POP(L.RWYDISTANCE) AS DSD, AVG(L.SCORE) AS FACT FROM PIREPS PR, FLIGHTSTATS_LANDING L WHERE (PR.ID=L.ID) AND (L.PILOT_ID=?) ");
		if (_dayFilter > 0)
			sqlBuf.append("AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append("GROUP BY L.EQTYPE HAVING (CNT>1) ORDER BY FACT");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			if (_dayFilter > 0)
				ps.setInt(2, _dayFilter);
			
			// Execute the query
			results = new CacheableList<LandingStatistics>(key);
			try (ResultSet rs = ps.executeQuery()) {
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
						ls.setAverageScore(rs.getDouble(8) / 100);
					}
				
					results.add(ls);
				}
			}
			
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
		Map<Integer, Integer> results = new TreeMap<Integer, Integer>();
		for (int x = MAX_VSPEED; x <= 0; x += range)
			results.put(Integer.valueOf(x), Integer.valueOf(0));
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT ROUND(VSPEED / ?) * ? AS RNG, COUNT(ID) FROM FLIGHTSTATS_LANDING WHERE (PILOT_ID=?) AND (VSPEED>?) GROUP BY RNG")) {
			ps.setInt(1, range);
			ps.setInt(2, range);
			ps.setInt(3, pilotID);
			ps.setInt(4, MAX_VSPEED);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
			}
			
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Trim out any without a value
		results.entrySet().removeIf(me -> (me.getValue().intValue() == 0));
		return results;
	}

	/**
	 * Calculcates landing scores over time.
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of LandingStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LandingStatsEntry> getLandingScores(int pilotID) throws DAOException {
		
		Collection<LandingStatsEntry> results = new ArrayList<LandingStatsEntry>(); 
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DATE_SUB(S.DATE, INTERVAL (DAYOFMONTH(S.DATE)-1) DAY) AS DT, S.SCORE, P.DISTANCE, P.FLIGHT_TIME FROM FLIGHTSTATS_LANDING S, PIREPS P WHERE (P.ID=S.ID) AND (S.PILOT_ID=?) ORDER BY DT")) {
			ps.setInt(1, pilotID);
			try (ResultSet rs = ps.executeQuery()) {
				LandingStatsEntry lse = null;
				while (rs.next()) {
					Instant dt = toInstant(rs.getTimestamp(1));
					if ((lse == null) || dt.isAfter(lse.getDate())) {
						lse = new LandingStatsEntry(dt);
						results.add(lse);
					}
					
					lse.add(rs.getInt(2) / 100, rs.getInt(3), rs.getDouble(4));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves aggregated approved Flight Report statistics for Flights flown using aircraft that are Primary Ratings for a particular Equipment Type program.
	 * @param eqType the Equipment type name
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getEQPIREPStatistics(String eqType, String groupBy, String orderBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SAL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SBL, SUM(IF(F.TOUR_ID>0, 1, 0)) AS TLEGS, COUNT(DISTINCT F.PILOT_ID) AS PIDS, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM EQRATINGS EQR, PIREPS F WHERE (F.STATUS=?) AND ((EQR.EQTYPE=?) AND (EQR.RATING_TYPE=?) AND (EQR.RATED_EQ=F.EQTYPE))");
		if (_dayFilter > 0)
			sqlBuf.append("AND (F.DATE > DATE_SUB(NOW(), INTERVAL ? DAY)) ");
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, FlightReport.ATTR_HISTORIC);
			ps.setInt(3, FlightReport.ATTR_DISPATCH);
			ps.setInt(4, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(5, FlightReport.ATTR_ONLINE_MASK);
			ps.setInt(6, FlightStatus.OK.ordinal());
			ps.setString(7, eqType);
			ps.setInt(8, EquipmentType.Rating.PRIMARY.ordinal());
			if (_dayFilter > 0)
				ps.setInt(9, _dayFilter);
			
			// Check the cache
			String cacheKey = getCacheKey(ps.toString());
			CacheableCollection<FlightStatsEntry> results = _statCache.get(cacheKey);
			if (results != null)
				return results.clone();
			
			// Do the query
			results = new CacheableList<FlightStatsEntry>(cacheKey);
			results.addAll(execute(ps));
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
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, FlightReport.ATTR_HISTORIC);
			ps.setInt(3, FlightReport.ATTR_DISPATCH);
			ps.setInt(4, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(5, FlightReport.ATTR_ONLINE_MASK);
			ps.setInt(6, FlightStatus.OK.ordinal());
			ps.setInt(7, FlightReport.ATTR_CHARTER);
			
			// Check the cache
			String cacheKey = getCacheKey(ps.toString());
			CacheableCollection<FlightStatsEntry> results = _statCache.get(cacheKey);
			if (results != null)
				return results.clone();
			
			// Get the results
			results = new CacheableList<FlightStatsEntry>(cacheKey);
			results.addAll(execute(ps));
			_statCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads flight/distance statistics by Pilot for a one year interval.
	 * @param sd the start date
	 * @return a List of YearlyTotal beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetEliteStatistics#getPilotTotals(LocalDate)
	 */
	public List<YearlyTotal> getPilotTotals(LocalDate sd) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT PILOT_ID, COUNT(ID) AS LEGS, SUM(DISTANCE) AS DST FROM PIREPS USE INDEX (PIREP_DT_IDX) WHERE ((DATE>=MAKEDATE(?,?)) AND (DATE<MAKEDATE(?,?))) AND (STATUS=?) GROUP BY PILOT_ID")) {
			ps.setInt(1, sd.getYear());
			ps.setInt(2, sd.getDayOfYear());
			ps.setInt(3, sd.getYear() + 1);
			ps.setInt(4, sd.getDayOfYear());
			ps.setInt(5, FlightStatus.OK.ordinal());
			
			// Execute the query
			List<YearlyTotal> results = new ArrayList<YearlyTotal>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					YearlyTotal yt = new YearlyTotal(sd.getYear(), rs.getInt(1));
					yt.addLegs(rs.getInt(2), rs.getInt(3), 0);
					results.add(yt);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the number of Charter flights flown by a Pilot in a particular time interval.
	 * @param pilotID the Pilot's datbase ID
	 * @param days the number of days backwards, zero for all
	 * @param dt the end date/time
	 * @return the number of approved Charter flights
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCharterCount(int pilotID, int days, Instant dt) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(ID) FROM PIREPS WHERE (PILOT_ID=?) AND (STATUS<>?) AND ((ATTR & ?) > 0)");
		if ((days > 0) && (dt != null))
			sqlBuf.append(" AND (DATE >= DATE_SUB(DATE(?), INTERVAL ? DAY))");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			ps.setInt(2, FlightStatus.REJECTED.ordinal());
			ps.setInt(3, FlightReport.ATTR_CHARTER);
			if ((days > 0) && (dt != null)) {
				ps.setTimestamp(4, createTimestamp(dt));
				ps.setInt(5, days);
			}
			
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns flight statistics by date and stage.
	 * @param pilotID the Pilot's database ID or zero for all pilots
	 * @return a Collection of StageStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<StageStatsEntry> getStageStatistics(int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DATE_SUB(F.DATE, INTERVAL (DAY(F.DATE)-1) DAY) AS LABEL, IFNULL(ES.MAXSTAGE, 1) AS STG, COUNT(F.ID) AS LEGS, SUM(F.DISTANCE) AS DST, SUM(F.FLIGHT_TIME) AS HRS "
			+ "FROM PIREPS F LEFT JOIN EQSTAGES ES ON (F.EQTYPE=ES.RATED_EQ) WHERE (F.STATUS=?) ");
		if (pilotID != 0)
			sqlBuf.append("AND (F.PILOT_ID=?) ");
		sqlBuf.append("GROUP BY LABEL, STG ORDER BY LABEL, STG");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			if (pilotID != 0)
				ps.setInt(2, pilotID);
			
			// Execute the query
			Collection<StageStatsEntry> results = new ArrayList<StageStatsEntry>(); StageStatsEntry sse = null;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Instant dt = toInstant(rs.getTimestamp(1));
					if ((sse == null) || !sse.getDate().equals(dt)) {
						sse = new StageStatsEntry(dt);
						results.add(sse);
					}
					
					sse.setStage(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getDouble(5));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns simulator statistics by date and stage.
	 * @param pilotID the Pilot's database ID or zero for all pilots
	 * @return a Collection of SimStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SimStatsEntry> getSimulatorStatistics(int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DATE_SUB(F.DATE, INTERVAL (DAY(F.DATE)-1) DAY) AS LABEL, F.FSVERSION, COUNT(F.ID) AS LEGS, SUM(F.DISTANCE) AS DST, SUM(F.FLIGHT_TIME) AS HRS FROM PIREPS F WHERE (F.STATUS=?) ");
		if (pilotID != 0)
			sqlBuf.append("AND (F.PILOT_ID=?) ");
		sqlBuf.append("GROUP BY LABEL, F.FSVERSION ORDER BY LABEL, F.FSVERSION");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			if (pilotID != 0)
				ps.setInt(2, pilotID);

			// Execute the query
			Collection<SimStatsEntry> results = new ArrayList<SimStatsEntry>(); SimStatsEntry sse = null;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Instant dt = toInstant(rs.getTimestamp(1));
					if ((sse == null) || !sse.getDate().equals(dt)) {
						sse = new SimStatsEntry(dt);
						results.add(sse);
					}
					
					sse.setSimulator(Simulator.fromVersion(rs.getInt(2), Simulator.UNKNOWN), rs.getInt(3), rs.getInt(4), rs.getDouble(5));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all the Flight Reports without an Elite score. 
	 * @return a Collection of Flight Report database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getUnscoredFlights() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.ID, PE.ID AS EID FROM PIREPS P LEFT JOIN PIREP_ELITE PE ON (P.ID=PE.ID) WHERE (P.STATUS=?) AND (P.DATE>DATE_SUB(CURDATE(), INTERVAL ? DAY)) HAVING (EID IS NULL) "
			+ "ORDER BY P.PILOT_ID, P.DATE, P.SUBMITTED")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, (_dayFilter < 1) ? 7 : _dayFilter);
			
			Collection<Integer> results = new LinkedHashSet<Integer>();
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
	 * Retrieves flight statistics for an arbitrary set of Pilots.
	 * @param IDs a Collection of database IDs 
	 * @param days the number of days back to aggregate
	 * @param s the statistics sorting option
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getStatistics(Collection<Integer> IDs, int days, FlightStatsSort s) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(FlightStatsGroup.PILOT.getSQL());
		sqlBuf.append(getPilotJoinSQL());
		sqlBuf.append("AND (P.ID IN (");
		sqlBuf.append(StringUtils.listConcat(IDs, ","));
		sqlBuf.append(")) AND (F.DATE>=DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, FlightReport.ATTR_HISTORIC);
			ps.setInt(3, FlightReport.ATTR_DISPATCH);
			ps.setInt(4, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(5, FlightReport.ATTR_ONLINE_MASK);
			ps.setInt(6, FlightStatus.OK.ordinal());
			ps.setInt(7, days);
			return execute(ps);
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
			sqlBuf.append(getAirportJoinSQL(grp.getSQL()));
		} else {
			sqlBuf.append(grp.getSQL());
			sqlBuf.append(grp.isPilotGroup() ? getPilotJoinSQL() : getSQL());
		}
		
		if (pilotID != 0)
			sqlBuf.append("AND (F.PILOT_ID=?) ");
		
		sqlBuf.append("GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int param = 0;
			ps.setInt(++param, FlightReport.ATTR_ACARS);
			ps.setInt(++param, FlightReport.ATTR_HISTORIC);
			ps.setInt(++param, FlightReport.ATTR_DISPATCH);
			ps.setInt(++param, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(++param, FlightReport.ATTR_ONLINE_MASK);
			ps.setInt(++param, FlightStatus.OK.ordinal());
			if (pilotID != 0)
				ps.setInt(++param, pilotID);
			
			// Check the cache
			String cacheKey = getCacheKey(ps.toString());
			CacheableCollection<FlightStatsEntry> results = _statCache.get(cacheKey);
			if (results != null)
				return results.clone();
			
			// Get the results
			results = new CacheableList<FlightStatsEntry>(cacheKey);
			results.addAll(execute(ps));
			_statCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse stats entry result sets.
	 */
	private static List<FlightStatsEntry> execute(PreparedStatement ps) throws SQLException {
		List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(4), rs.getInt(3));
				entry.setACARSLegs(rs.getInt(7));
				entry.setHistoricLegs(rs.getInt(8));
				entry.setDispatchLegs(rs.getInt(9));
				entry.setSimBriefLegs(rs.getInt(10));
				entry.setTourLegs(rs.getInt(11));
				entry.setPilotIDs(rs.getInt(12));
				entry.setOnlineLegs(rs.getInt(13));
				entry.setPax(rs.getInt(14));
				entry.setLoadFactor(rs.getDouble(15));
				results.add(entry);
			}
		}

		return results;
	}
	
	/*
	 * Private helper method to return SQL statement that doesn't involve joins on the <i>PILOTS</i> table.
	 */
	private static String getSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) "
			+ "AS SAL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SBL, SUM(IF(F.TOUR_ID>0, 1, 0)) AS TLEGS, COUNT(DISTINCT F.PILOT_ID) AS PIDS, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(F.PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM PIREPS F WHERE (F.STATUS=?) ";
	}
	
	/*
	 * Private helper method to return SQL statement that involves a join on the <i>PILOTS</i> table.
	 */
	private static String getPilotJoinSQL() {
		return " AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SAL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SBL, SUM(IF(F.TOUR_ID>0, 1, 0)) AS TLEGS, 1 AS PIDS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, "
			+ "SUM(F.PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM PILOTS P, PIREPS F WHERE (P.ID=F.PILOT_ID) AND (F.STATUS=?) ";
	}
	
	/*
	 * Private helper method to return SQL statement that involves a join on the <i>AIRPORTS</i> table.
	 */
	private static String getAirportJoinSQL(String apColumn) {
		return " AS LABEL, COUNT(F.DISTANCE) AS SL, SUM(F.DISTANCE) AS SM, ROUND(SUM(F.FLIGHT_TIME), 1) AS SH, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SAL, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SHL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS SDL, SUM(IF(F.TOUR_ID>0, 1, 0)) AS TLEGS, COUNT(DISTINCT F.PILOT_ID) AS PIDS, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(F.PAX) AS SP, AVG(F.LOADFACTOR) AS LF FROM common.AIRPORTS AP, PIREPS F WHERE (AP.IATA=" + apColumn + ") AND (F.STATUS=?) ";
	}
}