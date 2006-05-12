// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
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
			setQueryMax(1);
			prepareStatement("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM COURSES C, INSCALENDAR I WHERE "
					+ "(C.ID=I.COURSE) AND (I.ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query, if empty return null
			List<InstructionSession> results = executeCalendar();
			return results.isEmpty() ? null : results.get(0);
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
			setQueryMax(1);
			prepareStatement("SELECT * FROM INSLOG WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query, if empty return null
			List<InstructionFlight> results = executeFlightCalendar();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	public Collection<InstructionFlight> getFlightCalendar(java.util.Date startDate, int days, int pilotID) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.* FROM INSLOG I, COURSES C WHERE (C.ID=I.COURSE) "
				+ "AND (I.STARTTIME >=?) AND (I.STARTTIME < DATE_ADD(?, INTERVAL ? DAY)) ");
		if (pilotID != 0)
			sqlBuf.append("AND ((C.PILOT_ID=?) OR (I.INSTRUCTOR_ID=?)) ");

		sqlBuf.append("ORDER BY I.DATE");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setTimestamp(1, createTimestamp(startDate));
			_ps.setTimestamp(2, createTimestamp(startDate));
			_ps.setInt(3, days);
			if (pilotID != 0) {
				_ps.setInt(4, pilotID);
				_ps.setInt(5, pilotID);
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
			s.setRemarks(rs.getString(10));
			
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

		// Iterate through the results
		List<InstructionFlight> results = new ArrayList<InstructionFlight>();
		while (rs.next()) {
			InstructionFlight entry = new InstructionFlight(rs.getInt(2), rs.getInt(3));
			entry.setID(rs.getInt(1));
			entry.setEquipmentType(rs.getString(4));
			entry.setDate(rs.getTimestamp(5));
			entry.setLength(Math.round(rs.getFloat(6) * 10));
			entry.setComments(rs.getString(7));
			
			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}