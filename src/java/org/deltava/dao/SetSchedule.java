// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;

import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to update the Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetSchedule extends DAO {

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
		sqlBuf.append(" INTO SCHEDULE (AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, DISTANCE, EQTYPE, FLIGHT_TIME, TIME_D, TIME_A, HISTORIC, ACADEMY, SRC, CODESHARE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
			ps.setBoolean(11, entry.getHistoric());
			ps.setBoolean(12, entry.getAcademy());
			ps.setInt(13, (entry.getSource() == null) ? -1 : entry.getSource().ordinal());
			ps.setString(14, entry.getCodeShare());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a raw Schedule Entry from a schedule provider into the storage database.
	 * @param rse a RawScheduleEntry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeRaw(RawScheduleEntry rse) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO RAW_SCHEDULE (SRC, SRCLINE, STARTDATE, ENDDATE, DAYS, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, TIME_D, TIME_A, CODESHARE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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
			ps.setString(14, rse.getCodeShare());
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
	public void delete(ScheduleEntry entry) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?) AND (LEG=?)")) {
			ps.setString(1, entry.getAirline().getCode());
			ps.setInt(2, entry.getFlightNumber());
			ps.setInt(3, entry.getLeg());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges entries from the Flight Schedule.
	 * @return the number of deleted entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM SCHEDULE")) {
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates the mapping of Raw Schedules sources to Airlines. 
	 * @param src a ScheduleSourceInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeSourceAirlines(ScheduleSourceInfo src) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM RAW_SCHEDULE_AIRLINES WHERE (SRC=?)")) {
				ps.setInt(1, src.getSource().ordinal());
				executeUpdate(ps, 0);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO RAW_SCHEDULE_DATES (SRC, EFFDATE, IMPORTDATE) VALUES (?, ?, ?)")) {
				ps.setInt(1, src.getSource().ordinal());
				ps.setTimestamp(2, createTimestamp(src.getEffectiveDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
				ps.setTimestamp(3, createTimestamp(src.getImportDate()));
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
		}
	}
	
	/**
	 * Purges all Raw Schedule source / airline mappings.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeSourceAirlines() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM RAW_SCHEDULE_AIRLINES")) {
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
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