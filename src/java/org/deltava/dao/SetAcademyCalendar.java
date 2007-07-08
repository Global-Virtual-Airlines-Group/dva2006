// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to update Flight Academy Flight reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetAcademyCalendar extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAcademyCalendar(Connection c) {
		super(c);
	}

	/**
	 * Writes an Instruction flight entry to the database.
	 * @param flight the InstructionFlight bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(InstructionFlight flight) throws DAOException {
		try {
			// Prepare the statement
			prepareStatement("REPLACE INTO INSLOG (ID, INSTRUCTOR_ID, COURSE, EQTYPE, STARTDATE, "
					+ "FLIGHT_TIME, REMARKS) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, flight.getID());
			_ps.setInt(2, flight.getInstructorID());
			_ps.setInt(3, flight.getCourseID());
			_ps.setString(4, flight.getEquipmentType());
			_ps.setTimestamp(5, createTimestamp(flight.getDate()));
			_ps.setFloat(6, flight.getLength() / 10.0f);
			_ps.setString(7, flight.getComments());
			executeUpdate(1);
			
			// Update the ID
			if (flight.getID() == 0)
				flight.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an Instruction Calendar entry.
	 * @param s the InstructionSession bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(InstructionSession s) throws DAOException {
		try {
			prepareStatement("REPLACE INTO INSCALENDAR (COURSE, INSTRUCTOR_ID, STARTTIME, ENDTIME, "
					+ "STATUS, NOSHOW, REMARKS, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, s.getCourseID());
			_ps.setInt(2, s.getInstructorID());
			_ps.setTimestamp(3, createTimestamp(s.getStartTime()));
			_ps.setTimestamp(4, createTimestamp(s.getEndTime()));
			_ps.setInt(5, s.getStatus());
			_ps.setBoolean(6, s.getNoShow());
			_ps.setString(7, s.getComments());
			_ps.setInt(8, s.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an Instruction Calendar busy time entry.
	 * @param ib the InstructionBusy bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(InstructionBusy ib) throws DAOException {
		try {
			startTransaction();
			
			// Nuke any existing entries
			prepareStatement("DELETE FROM INSBUSY WHERE (INSTRUCTOR_ID=?) AND (STARTTIME >= ?) AND (STARTTIME <= ?)");
			_ps.setInt(1, ib.getID());
			_ps.setTimestamp(2, createTimestamp(ib.getStartTime()));
			_ps.setTimestamp(3, createTimestamp(ib.getEndTime()));
			executeUpdate(0);
			
			// Add the entry
			prepareStatement("INSERT INTO INSBUSY (INSTRUCTOR_ID, STARTTIME, ENDTIME, COMMENTS) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, ib.getID());
			_ps.setTimestamp(2, createTimestamp(ib.getStartTime()));
			_ps.setTimestamp(3, createTimestamp(ib.getEndTime()));
			_ps.setString(4, ib.getComments());
			executeUpdate(1);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Instruction Flight entry from the database.
	 * @param id the database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM INSLOG WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Instructor busy time entry from the database.
	 * @param instructorID the Instructor's database ID
	 * @param startTime the start time
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteBusy(int instructorID, java.util.Date startTime) throws DAOException {
		try {
			prepareStatement("DELETE FROM INSBUSY WHERE (INSTRUCTOR_ID=?) AND (STARTTIME=?)");
			_ps.setInt(1, instructorID);
			_ps.setTimestamp(2, createTimestamp(startTime));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}