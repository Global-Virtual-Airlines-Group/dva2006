// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy calendars.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAcademyCalendar extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyCalendar(Connection c) {
		super(c);
	}

	/**
	 * Returns a specific Flight Academy Instruction Session record.
	 * @param id the Session database ID
	 * @return an InstructionSession bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public InstructionSession getSession(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM COURSES C, INSCALENDAR I "
					+ "WHERE (C.ID=I.COURSE) AND (I.ID=?) LIMIT 1");
			_ps.setInt(1, id);
			
			// Execute the query, if empty return null
			List<InstructionSession> results = executeCalendar();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Instructor busy time entries for the Calendar.
	 * @param startDate the start date/time, or null if all values should be displayed
	 * @param days the number of days to display (ignored if startDate is null)
	 * @param pilotID the database ID of the instructor, or zero if all selected
	 * @return a Collection of InstructionBusy beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionBusy> getBusyCalendar(java.util.Date startDate, int days, int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM INSBUSY ");
		if ((startDate != null) || (pilotID > 0))
			sqlBuf.append("WHERE ");
		if (startDate != null) {
			sqlBuf.append("(STARTTIME >=?) AND (STARTTIME < DATE_ADD(?, INTERVAL ? DAY)) ");
			if (pilotID > 0)
				sqlBuf.append("AND ");
		}
		
		if (pilotID > 0)
			sqlBuf.append("(INSTRUCTOR_ID=?) ");
		
		sqlBuf.append("ORDER BY STARTTIME");
		
		int param = 0;
		try {
			prepareStatement(sqlBuf.toString());
			if (startDate != null) {
				_ps.setTimestamp(++param, createTimestamp(startDate));
				_ps.setTimestamp(++param, createTimestamp(startDate));
				_ps.setInt(++param, days);
			}
			
			if (pilotID > 0)
				_ps.setInt(++param, pilotID);
			
			// Execute the query
			return executeBusy();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all busy time for a particular Instructor.
	 * @param instructorID the Instructor database ID
	 * @return a Collection of InstructionBusy beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionBusy> getBusyTime(int instructorID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM INSBUSY WHERE (INSTRUCTOR_ID=?) ORDER BY STARTTIME");
			_ps.setInt(1, instructorID);
			return executeBusy();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns a specific Flight Academy Instruction flight record.
	 * @param id the flight database ID
	 * @return an InstructionFlight bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public InstructionFlight getFlight(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT I.*, C.CERTNAME, C.PILOT_ID FROM INSLOG I, COURSES C "
				+ "WHERE (I.COURSE=C.ID) AND (I.ID=?) LIMIT 1");
			_ps.setInt(1, id);
			
			// Execute the query, if empty return null
			List<InstructionFlight> results = executeFlightCalendar();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Instruction log entries.
	 * @param courseID the database ID of the Course
	 * @return a Collection of InstructionFlight beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionFlight> getFlights(int courseID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM INSLOG ");
		if (courseID != 0)
			sqlBuf.append("WHERE (COURSE=?) ");

		sqlBuf.append("ORDER BY STARTDATE");

		try {
			prepareStatement(sqlBuf.toString());
			if (courseID != 0)
				_ps.setInt(1, courseID);

			// Execute the query
			return executeFlightCalendar();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Instruction Sessions for a particular Flight Academy Course.
	 * @param courseID the database ID of the Course
	 * @return a Collection of InstructionSession beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionSession> getSessions(int courseID) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM INSCALENDAR I, COURSES C "
				+ "WHERE (C.ID=I.COURSE) ");
		if (courseID != 0)
			sqlBuf.append("AND (I.COURSE=?) ");

		sqlBuf.append("ORDER BY I.STARTTIME");

		try {
			prepareStatement(sqlBuf.toString());
			if (courseID != 0)
				_ps.setInt(1, courseID);

			return executeCalendar();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads the Flight Academy Instruction Flight calendar. 
	 * @param startDate the start date/time, or null if all values should be displayed
	 * @param days the number of days to display (ignored if startDate is null)
	 * @param pilotID the database ID of the instructor/student pilot, or zero if all selected
	 * @return a Collection of InstructionFlight beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionFlight> getFlightCalendar(java.util.Date startDate, int days, int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, C.CERTNAME, C.PILOT_ID FROM INSLOG I, COURSES C WHERE "
				+ "(C.ID=I.COURSE) ");
		if (startDate != null)
			sqlBuf.append("AND (I.STARTTIME >=?) AND (I.STARTTIME < DATE_ADD(?, INTERVAL ? DAY)) ");
		if (pilotID != 0)
			sqlBuf.append("AND ((C.PILOT_ID=?) OR (I.INSTRUCTOR_ID=?)) ");

		sqlBuf.append("ORDER BY I.STARTDATE");
		
		int param = 0;
		try {
			prepareStatement(sqlBuf.toString());
			if (startDate != null) {
				_ps.setTimestamp(++param, createTimestamp(startDate));
				_ps.setTimestamp(++param, createTimestamp(startDate));
				_ps.setInt(++param, days);
			}
			
			if (pilotID != 0) {
				_ps.setInt(++param, pilotID);
				_ps.setInt(++param, pilotID);
			}
			
			// Execute the query
			return executeFlightCalendar();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads the Flight Academy Instruction Calendar.
	 * @param startDate the start date/time
	 * @param days the number of days to display
	 * @param pilotID the Pilot ID to display, or 0 if none
	 * @return a Collection of InstructionSession beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionSession> getSessionCalendar(java.util.Date startDate, int days, int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM COURSES C, "
				+ "INSCALENDAR I WHERE (C.ID=I.COURSE) AND (I.STARTTIME >=?) AND (I.STARTTIME "
				+ "< DATE_ADD(?, INTERVAL ? DAY)) AND (I.STATUS != ?) ");
		if (pilotID != 0)
			sqlBuf.append("AND ((C.PILOT_ID=?) OR (I.INSTRUCTOR_ID=?)) ");
				
		sqlBuf.append("ORDER BY I.STARTTIME");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setTimestamp(1, createTimestamp(startDate));
			_ps.setTimestamp(2, createTimestamp(startDate));
			_ps.setInt(3, days);
			_ps.setInt(4, InstructionSession.CANCELED);
			if (pilotID != 0) {
				_ps.setInt(5, pilotID);
				_ps.setInt(6, pilotID);
			}
			
			// Execute the query
			return executeCalendar();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse InstructionSession result sets.
	 */
	private List<InstructionSession> executeCalendar() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<InstructionSession> results = new ArrayList<InstructionSession>();
		while (rs.next()) {
			InstructionSession s = new InstructionSession(rs.getInt(3), rs.getInt(4));
			s.setName(rs.getString(1));
			s.setPilotID(rs.getInt(2));
			s.setInstructorID(rs.getInt(5));
			s.setStartTime(rs.getTimestamp(6));
			s.setEndTime(rs.getTimestamp(7));
			s.setStatus(rs.getInt(8));
			s.setNoShow(rs.getBoolean(9));
			s.setComments(rs.getString(10));
			
			// Add to results
			results.add(s);
		}
	
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to parse InstructionFlight result sets.
	 */
	private List<InstructionFlight> executeFlightCalendar() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasCourseInfo = (rs.getMetaData().getColumnCount() > 7);

		// Iterate through the results
		List<InstructionFlight> results = new ArrayList<InstructionFlight>();
		while (rs.next()) {
			InstructionFlight entry = new InstructionFlight(rs.getInt(2), rs.getInt(3));
			entry.setID(rs.getInt(1));
			entry.setEquipmentType(rs.getString(4));
			entry.setDate(expandDate(rs.getDate(5)));
			entry.setLength(Math.round(rs.getFloat(6) * 10));
			entry.setComments(rs.getString(7));
			if (hasCourseInfo) {
				entry.setCourseName(rs.getString(8));
				entry.setPilotID(rs.getInt(9));
			}
			
			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to parse InstructionBusy result sets.
	 */
	private List<InstructionBusy> executeBusy() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<InstructionBusy> results = new ArrayList<InstructionBusy>();
		while (rs.next()) {
			InstructionBusy ib = new InstructionBusy(rs.getInt(1));
			ib.setStartTime(rs.getTimestamp(2));
			ib.setEndTime(rs.getTimestamp(3));
			ib.setComments(rs.getString(4));
			results.add(ib);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}