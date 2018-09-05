// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to update the Flight Schedule.
 * @author Luke
 * @version 8.3
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
		sqlBuf.append(" INTO SCHEDULE (AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, DISTANCE, EQTYPE, FLIGHT_TIME, TIME_D, TIME_A, HISTORIC, CAN_PURGE, ACADEMY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, entry.getAirline().getCode());
			_ps.setInt(2, entry.getFlightNumber());
			_ps.setInt(3, entry.getLeg());
			_ps.setString(4, entry.getAirportD().getIATA());
			_ps.setString(5, entry.getAirportA().getIATA());
			_ps.setInt(6, entry.getDistance());
			_ps.setString(7, entry.getEquipmentType());
			_ps.setInt(8, entry.getLength());
			_ps.setTimestamp(9, Timestamp.valueOf(entry.getTimeD().toLocalDateTime()));
			_ps.setTimestamp(10, Timestamp.valueOf(entry.getTimeA().toLocalDateTime()));
			_ps.setBoolean(11, entry.getHistoric());
			_ps.setBoolean(12, entry.getCanPurge());
			_ps.setBoolean(13, entry.getAcademy());
			executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("REPLACE INTO RAW_SCHEDULE (DAY, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, TIME_D, TIME_A, CODESHARE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, rse.getDay().ordinal());
			_ps.setString(2, rse.getAirline().getCode());
			_ps.setInt(3, rse.getFlightNumber());
			_ps.setInt(4, rse.getLeg());
			_ps.setString(5, rse.getAirportD().getIATA());
			_ps.setString(6, rse.getAirportA().getIATA());
			_ps.setString(7, rse.getEquipmentType());
			_ps.setTimestamp(8, Timestamp.valueOf(rse.getTimeD().toLocalDateTime()));
			_ps.setTimestamp(9, Timestamp.valueOf(rse.getTimeA().toLocalDateTime()));
			_ps.setString(10, rse.getCodeShare());
			executeUpdate(1);
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
		try {
			prepareStatement("DELETE FROM SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?) AND (LEG=?)");
			_ps.setString(1, entry.getAirline().getCode());
			_ps.setInt(2, entry.getFlightNumber());
			_ps.setInt(3, entry.getLeg());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges entries from the Flight Schedule.
	 * @param force TRUE if all entries should be purged, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(boolean force) throws DAOException {

		StringBuilder sqlBuf = new StringBuilder("DELETE FROM SCHEDULE");
		if (!force)
			sqlBuf.append(" WHERE (CAN_PURGE=?)");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			if (!force)
				_ps.setBoolean(1, true);

			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges entries from the raw Flight Schedule.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeRaw() throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM RAW_SCHEDULE");
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}