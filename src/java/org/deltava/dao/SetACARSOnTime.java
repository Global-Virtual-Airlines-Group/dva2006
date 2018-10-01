// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.flight.ACARSFlightReport;
import org.deltava.beans.schedule.ScheduleEntry;

/**
 * A Data Access Object to write ACARS on-time data to the database.
 * @author Luke
 * @version 8.4
 * @since 8.4
 */

public class SetACARSOnTime extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public SetACARSOnTime(Connection c) {
		super(c);
	}

	/**
	 * Writes ACARS on-time data to the database.
	 * @param db the database name
	 * @param afr the ACARSFlightReport
	 * @param entry the matched ScheduleEntry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(String db, ACARSFlightReport afr, ScheduleEntry entry) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".ACARS_ONTIME VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, afr.getID());
			_ps.setInt(2, afr.getOnTime().ordinal());
			_ps.setString(3, entry.getAirline().getCode());
			_ps.setInt(4, entry.getFlightNumber());
			_ps.setInt(5, entry.getLeg());
			_ps.setTimestamp(6, Timestamp.valueOf(entry.getTimeD().toLocalDateTime()));
			_ps.setTimestamp(7, Timestamp.valueOf(entry.getTimeA().toLocalDateTime()));
			_ps.setTimestamp(8, createTimestamp(afr.getTimeD().toInstant()));
			_ps.setTimestamp(9, createTimestamp(afr.getTimeA().toInstant()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}