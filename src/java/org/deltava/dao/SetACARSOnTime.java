// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.flight.ACARSFlightReport;
import org.deltava.beans.schedule.ScheduleEntry;

/**
 * A Data Access Object to write ACARS on-time data to the database.
 * @author Luke
 * @version 9.0
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
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, afr.getID());
			ps.setInt(2, afr.getOnTime().ordinal());
			ps.setString(3, entry.getAirline().getCode());
			ps.setInt(4, entry.getFlightNumber());
			ps.setInt(5, entry.getLeg());
			ps.setTimestamp(6, Timestamp.valueOf(entry.getTimeD().toLocalDateTime()));
			ps.setTimestamp(7, Timestamp.valueOf(entry.getTimeA().toLocalDateTime()));
			ps.setTimestamp(8, createTimestamp(afr.getTimeD().toInstant()));
			ps.setTimestamp(9, createTimestamp(afr.getTimeA().toInstant()));
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}