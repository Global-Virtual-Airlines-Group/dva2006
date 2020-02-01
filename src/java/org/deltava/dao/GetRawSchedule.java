// Copyright 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load raw schedule entries and tail codes.
 * @author Luke
 * @version 9.0
 * @since 8.0
 */

public class GetRawSchedule extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetRawSchedule(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all raw schedule sources.
	 * @return a Collection of ScheduleSourceInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleSourceInfo> getSources() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SRC, AIRLINE, COUNT(*) AS TOTAL FROM RAW_SCHEDULE GROUP BY SRC, AIRLINE ORDER BY SRC")) {
			Collection<ScheduleSourceInfo> results = new LinkedHashSet<ScheduleSourceInfo>();
			ScheduleSourceInfo inf = null;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleSource ss = ScheduleSource.values()[rs.getInt(1)];
					if ((inf == null) || (ss != inf.getSource())) {
						inf = new ScheduleSourceInfo(ss);
						results.add(inf);
					}
					
					inf.setLegs(SystemData.getAirline(rs.getString(2)), rs.getInt(3));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the mapping of Airlines to Schedule sources.
	 * @return a Map of Collections of Airlines, keyed by ScheduleSource
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<ScheduleSource, Collection<Airline>> getSourceAirlines() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT SRC, AIRLINE FROM RAW_SCHEDULE_AIRLINES")) {
			Map<ScheduleSource, Collection<Airline>> results = new TreeMap<ScheduleSource, Collection<Airline>>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleSource src = ScheduleSource.values()[rs.getInt(1)];
					Collection<Airline> airlines = results.get(src);
					if (airlines == null) {
						airlines = new TreeSet<Airline>();
						results.put(src, airlines);
					}
					
					airlines.add(SystemData.getAirline(rs.getString(2)));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads an individual raw schedule entry.
	 * @param src the ScheduleSource
	 * @param line the source line number
	 * @return a RawScheduleEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public RawScheduleEntry get(ScheduleSource src, int line) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?) AND (SRCLINE=?)")) {
			ps.setInt(1, src.ordinal());
			ps.setInt(2, line);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Lists all raw schedule entries between two Airports from a particular schedule source.
	 * @param src a ScheduleSource
	 * @param aD the departure Airport
	 * @param aA the arrival Airport, or null for all
	 * @return a List of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> list(ScheduleSource src, Airport aD, Airport aA) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?) AND (AIRPORT_D=?)");
		if (aA != null)
			sqlBuf.append(" AND (AIRPORT_A=?)");
		sqlBuf.append(" ORDER BY SRCLINE");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, src.ordinal());
			ps.setString(2, aD.getIATA());
			if (aA != null)
				ps.setString(3, aA.getIATA());

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads import statistics for a particular schedule source. 
	 * @param src the ScheduleSource
	 * @param ld the effective date
	 * @return a Collection of ImportRoute beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ImportRoute> getImportData(ScheduleSource src, LocalDate ld) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT AIRPORT_D, AIRPORT_A, COUNT(*) AS CNT FROM RAW_SCHEDULE WHERE (SRC=?) AND (STARTDATE<=?) AND (ENDDATE>=?) AND ((DAYS & ?) != 0) GROUP BY AIRPORT_D, AIRPORT_A ORDER BY CNT DESC")) {
			ps.setInt(1, src.ordinal());
			ps.setTimestamp(2, Timestamp.valueOf(ld.atStartOfDay()));
			ps.setTimestamp(3, Timestamp.valueOf(ld.atTime(23, 59, 59)));
			ps.setInt(4, 1 << ld.getDayOfWeek().ordinal());
			
			Collection<ImportRoute> results = new ArrayList<ImportRoute>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ImportRoute ir = new ImportRoute(src, SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
					ir.setFlights(rs.getInt(3));
					results.add(ir);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all raw schedule entries for a particular day of the week.
	 * @param src the ScheduleSource
	 * @param ld the schedule effective date, or null for all
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> load(ScheduleSource src, LocalDate ld) throws DAOException {
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?)");
		if (ld != null)
			sqlBuf.append(" AND (STARTDATE<=?) AND (ENDDATE>=?) AND ((DAYS & ?) != 0)");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, src.ordinal());
			if (ld != null) {
				ps.setTimestamp(2, Timestamp.valueOf(ld.atStartOfDay()));
				ps.setTimestamp(3, Timestamp.valueOf(ld.atTime(23, 59, 59)));
				ps.setInt(4, 1 << ld.getDayOfWeek().ordinal());
			}

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Raw Schedule result sets.
	 */
	private static List<RawScheduleEntry> execute(PreparedStatement ps) throws SQLException {
		List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(rs.getString(6)), rs.getInt(7), rs.getInt(8));
				se.setSource(ScheduleSource.values()[rs.getInt(1)]);
				se.setLineNumber(rs.getInt(2));
				se.setStartDate(rs.getDate(3).toLocalDate());
				se.setEndDate(rs.getDate(4).toLocalDate());
				se.setAirportD(SystemData.getAirport(rs.getString(9)));
				se.setAirportA(SystemData.getAirport(rs.getString(10)));
				se.setEquipmentType(rs.getString(11));
				se.setTimeD(rs.getTimestamp(12).toLocalDateTime());
				se.setTimeA(rs.getTimestamp(13).toLocalDateTime());
				se.setCodeShare(rs.getString(14));
				se.setDayMap(rs.getInt(5));
				results.add(se);
			}
		}
		
		return results;
	}
}