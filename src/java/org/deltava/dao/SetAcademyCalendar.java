// Copyright 2006, 2007, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to update Flight Academy Flight reports.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO exams.INSLOG (ID, INSTRUCTOR_ID, COURSE, EQTYPE, STARTDATE, FLIGHT_TIME, REMARKS) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, flight.getID());
				ps.setInt(2, flight.getInstructorID());
				ps.setInt(3, flight.getCourseID());
				ps.setString(4, flight.getEquipmentType());
				ps.setTimestamp(5, createTimestamp(flight.getDate()));
				ps.setFloat(6, flight.getLength() / 10.0f);
				ps.setString(7, flight.getComments());
				executeUpdate(ps, 1);
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO exams.INSCALENDAR (COURSE, INSTRUCTOR_ID, STARTTIME, ENDTIME, STATUS, NOSHOW, REMARKS, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, s.getCourseID());
			ps.setInt(2, s.getInstructorID());
			ps.setTimestamp(3, createTimestamp(s.getStartTime()));
			ps.setTimestamp(4, createTimestamp(s.getEndTime()));
			ps.setInt(5, s.getStatus());
			ps.setBoolean(6, s.getNoShow());
			ps.setString(7, s.getComments());
			ps.setInt(8, s.getID());
			executeUpdate(ps, 1);
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
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.INSBUSY WHERE (INSTRUCTOR_ID=?) AND (STARTTIME >= ?) AND (STARTTIME <= ?)")) {
				ps.setInt(1, ib.getID());
				ps.setTimestamp(2, createTimestamp(ib.getStartTime()));
				ps.setTimestamp(3, createTimestamp(ib.getEndTime()));
				executeUpdate(ps, 0);
			}
			
			// Add the entry
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.INSBUSY (INSTRUCTOR_ID, STARTTIME, ENDTIME, COMMENTS) VALUES (?, ?, ?, ?)")) {
				ps.setInt(1, ib.getID());
				ps.setTimestamp(2, createTimestamp(ib.getStartTime()));
				ps.setTimestamp(3, createTimestamp(ib.getEndTime()));
				ps.setString(4, ib.getComments());
				executeUpdate(ps, 1);
			}
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
		try (PreparedStatement ps = prepare("DELETE FROM exams.INSLOG WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
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
	public void deleteBusy(int instructorID, java.time.Instant startTime) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM exams.INSBUSY WHERE (INSTRUCTOR_ID=?) AND (STARTTIME=?)")) {
			ps.setInt(1, instructorID);
			ps.setTimestamp(2, createTimestamp(startTime));
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}