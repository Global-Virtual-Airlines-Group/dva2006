// Copyright 2008, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.DispatchScheduleEntry;

/**
 * A Data Access Object to write entries to the ACARS Dispatcher service calendar.
 * @author Luke
 * @version 9.0
 * @since 2.2
 */

public class SetDispatchCalendar extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetDispatchCalendar(Connection c) {
		super(c);
	}

	/**
	 * Writes a schedule entry to the calendar. <i>This can handle INSERT and UPDATE operations.</i>
	 * @param dse the DispatchScheduleEntry to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(DispatchScheduleEntry dse) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.DSP_SCHEDULE (ID, DISPATCHER, STARTTIME, ENDTIME, REMARKS) VALUES (?, ?, ?, ?, ?)")) {
			ps.setInt(1, dse.getID());
			ps.setInt(2, dse.getAuthorID());
			ps.setTimestamp(3, createTimestamp(dse.getStartTime()));
			ps.setTimestamp(4, createTimestamp(dse.getEndTime()));
			ps.setString(5, dse.getComments());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a schedule entry from the calendar.
	 * @param id the entry database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM acars.DSP_SCHEDULE WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}