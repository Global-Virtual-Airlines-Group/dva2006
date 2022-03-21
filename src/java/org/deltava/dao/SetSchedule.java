// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to update the Flight Schedule.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class SetSchedule extends DAO {
	
	private static final Cache<CacheableCollection<ScheduleSourceInfo>> _srcCache = CacheManager.getCollection(ScheduleSourceInfo.class, "ScheduleSource");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetSchedule(Connection c) {
		super(c);
	}

	/**
	 * Adds an entry to the Flight Schedule.
	 * @param entry the Schedule Entry
	 * @param doReplace TRUE if an existing entry can be replaced, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(ScheduleEntry entry, boolean doReplace) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(doReplace ? "REPLACE" : "INSERT");
		sqlBuf.append(" INTO SCHEDULE (AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, DISTANCE, EQTYPE, FLIGHT_TIME, TIME_D, TIME_A, PLUSDAYS, HISTORIC, ACADEMY, DST_ADJUST, SRC, CODESHARE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, entry.getAirline().getCode());
			ps.setInt(2, entry.getFlightNumber());
			ps.setInt(3, entry.getLeg());
			ps.setString(4, entry.getAirportD().getIATA());
			ps.setString(5, entry.getAirportA().getIATA());
			ps.setInt(6, entry.getDistance());
			ps.setString(7, entry.getEquipmentType());
			ps.setInt(8, entry.getLength());
			ps.setTimestamp(9, Timestamp.valueOf(entry.getTimeD().toLocalDateTime()));
			ps.setTimestamp(10, Timestamp.valueOf(entry.getTimeA().toLocalDateTime()));
			ps.setInt(11, entry.getArrivalPlusDays());
			ps.setBoolean(12, entry.getHistoric());
			ps.setBoolean(13, entry.getAcademy());
			ps.setBoolean(14, entry.getHasDSTAdjustment());
			ps.setInt(15, entry.getSource().ordinal());
			ps.setString(16, entry.getCodeShare());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a raw Schedule Entry from a schedule provider into the storage database.
	 * @param rse a RawScheduleEntry
	 * @param doReplace TRUE if an existing entry can be replaced, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeRaw(RawScheduleEntry rse, boolean doReplace) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(doReplace ? "REPLACE" : "INSERT");
		sqlBuf.append(" INTO RAW_SCHEDULE (SRC, SRCLINE, STARTDATE, ENDDATE, DAYS, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, TIME_D, TIME_A, PLUSDAYS, FORCE_INCLUDE, ISUPDATED, ACADEMY, CODESHARE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, rse.getSource().ordinal());
			ps.setInt(2, rse.getLineNumber());
			ps.setTimestamp(3, Timestamp.valueOf(rse.getStartDate().atStartOfDay()));
			ps.setTimestamp(4, Timestamp.valueOf(rse.getEndDate().atTime(23, 59, 59)));
			ps.setInt(5, rse.getDayMap());
			ps.setString(6, rse.getAirline().getCode());
			ps.setInt(7, rse.getFlightNumber());
			ps.setInt(8, rse.getLeg());
			ps.setString(9, rse.getAirportD().getIATA());
			ps.setString(10, rse.getAirportA().getIATA());
			ps.setString(11, rse.getEquipmentType());
			ps.setTimestamp(12, Timestamp.valueOf(rse.getTimeD().toLocalDateTime()));
			ps.setTimestamp(13, Timestamp.valueOf(rse.getTimeA().toLocalDateTime()));
			ps.setInt(14, rse.getArrivalPlusDays());
			ps.setBoolean(15, rse.getForceInclude());
			ps.setBoolean(16, rse.getUpdated());
			ps.setBoolean(17, rse.getAcademy());
			ps.setString(18, rse.getCodeShare());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an entry from the Flight Schedule.
	 * @param entry the entry
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if entry is null
	 */
	public void delete(RawScheduleEntry entry) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM RAW_SCHEDULE WHERE (SRC=?) AND (SRCLINE=?)")) {
			ps.setInt(1, entry.getSource().ordinal());
			ps.setInt(2, entry.getLineNumber());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private static void purgeSourceCache(ScheduleSource src) {
		if (src != null) 
			_srcCache.remove(src.name());
		else
			CacheManager.invalidate("ScheduleSource", true);
	}
	
	/**
	 * Purges entries from the Flight Schedule.
	 * @param src a ScheduleSource, or null for all
	 * @return the number of deleted entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge(ScheduleSource src) throws DAOException {
	
		// Build the SQL statements
		StringBuilder sbuf = new StringBuilder("DELETE FROM SCHEDULE");
		StringBuilder rsbuf = new StringBuilder("UPDATE RAW_SCHEDULE_DATES SET ISACTIVE=?");
		if (src != null) {
			sbuf.append(" WHERE (SRC=?)");
			rsbuf.append(" WHERE (SRC=?)");
		}

		try {
			int cnt = 0;
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits(sbuf.toString())) {
				if (src != null) ps.setInt(1, src.ordinal());
				cnt = executeUpdate(ps, 0);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits(rsbuf.toString())) {
				ps.setBoolean(1, false);
				if (src != null) ps.setInt(2, src.ordinal());
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
			return cnt;
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			purgeSourceCache(src);
		}
	}
	
	/**
	 * Updates the mapping of Raw Schedules sources to Airlines. 
	 * @param src a ScheduleSourceInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeSourceAirlines(ScheduleSourceHistory src) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM RAW_SCHEDULE_AIRLINES WHERE (SRC=?)")) {
				ps.setInt(1, src.getSource().ordinal());
				executeUpdate(ps, 0);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO RAW_SCHEDULE_DATES (SRC, EFFDATE, IMPORTDATE, ISAUTO, ISACTIVE) VALUES (?, ?, ?, ?, ?)")) {
				ps.setInt(1, src.getSource().ordinal());
				ps.setTimestamp(2, createTimestamp(src.getEffectiveDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
				ps.setTimestamp(3, createTimestamp(src.getDate()));
				ps.setBoolean(4, src.getAutoImport());
				ps.setBoolean(5, src.getActive());
				executeUpdate(ps, 1);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO RAW_SCHEDULE_HISTORY (SRC, EFFDATE, IMPORTDATE, EXEC_TIME, LEGS, SKIPPED, ADJUSTED, PURGED, AIRLINES, USER_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, src.getSource().ordinal());
				ps.setTimestamp(2, createTimestamp(src.getEffectiveDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
				ps.setTimestamp(3, createTimestamp(src.getDate()));
				ps.setInt(4, src.getTime());
				ps.setInt(5, src.getLegs());
				ps.setInt(6, src.getSkipped());
				ps.setInt(7, src.getAdjusted());
				ps.setBoolean(8, src.getPurged());
				ps.setString(9, StringUtils.listConcat(src.getAirlines().stream().map(al -> al.getCode()).collect(Collectors.toList()), ","));
				ps.setInt(10, src.getAuthorID());
				executeUpdate(ps, 1);
			}
		
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO RAW_SCHEDULE_AIRLINES (SRC, AIRLINE) VALUES (?, ?)")) {
				ps.setInt(1, src.getSource().ordinal());
				for (Airline a : src.getAirlines()) {
					ps.setString(2, a.getCode());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, src.getAirlines().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			_srcCache.remove(src.cacheKey());
		}
	}
	
	/**
	 * Purges Raw Schedule source / airline mappings.
	 * @param src a ScheduleSource, or null for all 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeSourceAirlines(ScheduleSource src) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM RAW_SCHEDULE_AIRLINES");
		if (src != null)
			sqlBuf.append(" WHERE (SRC=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			if (src != null) ps.setInt(1,  src.ordinal());
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			purgeSourceCache(src);
		}
	}
	
	/**
	 * Purges entries from the raw Flight Schedule.
	 * @param src the ScheduleSource
	 * @return the number of entries deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeRaw(ScheduleSource src) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM RAW_SCHEDULE WHERE (SRC=?)")) {
			ps.setInt(1, src.ordinal());
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}