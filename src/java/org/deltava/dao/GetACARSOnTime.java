// Copyright 2018, 2019, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.OnTimeStatsEntry;
import org.deltava.beans.stats.RouteOnTime;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load ACARS on-time data from the database.  
 * @author Luke
 * @version 10.3
 * @since 8.4
 */

public class GetACARSOnTime extends DAO {
	
	private static final Cache<RouteOnTime> _cache = CacheManager.get(RouteOnTime.class, "OnTimeRoute");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSOnTime(Connection c) {
		super(c);
	}

	/**
	 * Loads on-time data. The flight report will have its departure and arrival times set.
	 * @param afr the ACARSFlightReport
	 * @return a ScheduleEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ScheduleEntry getOnTime(ACARSFlightReport afr) throws DAOException {
		if (afr.getOnTime() == OnTime.UNKNOWN) return null;
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRLINE, FLIGHT, LEG, TIME_D, TIME_A, ATIME_D, ATIME_A FROM ACARS_ONTIME WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, afr.getID());
			ScheduleEntry se = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					se = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
					se.setAirportD(afr.getAirportD()); se.setAirportA(afr.getAirportA());
					se.setTimeD(rs.getTimestamp(4).toLocalDateTime());
					se.setTimeA(rs.getTimestamp(5).toLocalDateTime());
					afr.setDepartureTime(toInstant(rs.getTimestamp(6)));
					afr.setArrivalTime(toInstant(rs.getTimestamp(7)));
				}
			}
			
			return se;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated on-time performance values for a Pilot.
	 * @param pilotID the Pilot's database ID, or zero for the entire airline
	 * @return a Map of Flight counts, keyed by OnTime values
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<OnTime,Integer> getOnTimeStatistics(int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AO.ONTIME, COUNT(P.ID) FROM ACARS_ONTIME AO, PIREPS P WHERE (AO.ID=P.ID) AND (P.STATUS=?) ");
		if (pilotID > 0)
			sqlBuf.append("AND (P.PILOT_ID=?) ");
		sqlBuf.append("GROUP BY AO.ONTIME");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			if (pilotID > 0) ps.setInt(2, pilotID);
			Map<OnTime,Integer> results = new TreeMap<OnTime, Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.put(OnTime.values()[rs.getInt(1)], Integer.valueOf(rs.getInt(2)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves airline-wide on-time flight statistics.
	 * @param maxDays the maximum number of days in the past to retrieve
	 * @return a Collection of OnTimeStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<OnTimeStatsEntry> getByDate(int maxDays) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.DATE, AO.ONTIME, COUNT(P.ID), SUM(P.DISTANCE), SUM(P.FLIGHT_TIME) FROM PIREPS P STRAIGHT_JOIN ACARS_ONTIME AO WHERE (AO.ID=P.ID) AND (P.STATUS=?) AND "
			+ "(P.DATE>DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY P.DATE, AO.ONTIME")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, maxDays);
			Collection<OnTimeStatsEntry> results = new ArrayList<OnTimeStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				OnTimeStatsEntry stats = null;
				while (rs.next()) {
					Instant dt = toInstant(rs.getTimestamp(1));
					if (stats == null) stats = new OnTimeStatsEntry(dt);
					else if (!dt.equals(stats.getDate())) {
						results.add(stats);
						stats = new OnTimeStatsEntry(dt);
					}
					
					stats.set(OnTime.values()[rs.getInt(2)], rs.getInt(3), rs.getInt(4), rs.getDouble(5));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns on-time flight statistics for a particular flight route.
	 * @param rp the RoutePair
	 * @param db the database name
	 * @return an OnlineStatsEntry bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public OnTimeStatsEntry getOnTimeStatistics(RoutePair rp, String db) throws DAOException {
		
		// Check the cache
		String dbName = formatDBName(db); String key = RouteOnTime.createKey(rp, dbName);
		RouteOnTime st = _cache.get(key);
		if (st != null)
			return st;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(P.ID), SUM(P.DISTANCE), SUM(P.FLIGHT_TIME), AO.ONTIME FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".ACARS_ONTIME AO, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS P WHERE (AO.ID=P.ID) AND (P.STATUS=?) AND (P.AIRPORT_D=?) AND (P.AIRPORT_A=?) GROUP BY AO.ONTIME");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setString(2, rp.getAirportD().getIATA());
			ps.setString(3, rp.getAirportA().getIATA());
			st = new RouteOnTime(key);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					st.set(OnTime.values()[rs.getInt(4)], rs.getInt(1), rs.getInt(2), rs.getDouble(3));
			}
			
			_cache.add(st);
			return st;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}