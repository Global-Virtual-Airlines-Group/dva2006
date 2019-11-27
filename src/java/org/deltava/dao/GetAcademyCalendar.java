// Copyright 2006, 2007, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DateRange;
import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy calendars.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM exams.COURSES C, exams.INSCALENDAR I WHERE (C.ID=I.COURSE) AND (I.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return executeCalendar(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Instructor busy time entries for the Calendar.
	 * @param insID the database ID of the instructor, or zero if all selected
	 * @param dr the DateRange
	 * @return a Collection of InstructionBusy beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionBusy> getBusyCalendar(int insID, DateRange dr) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM exams.INSBUSY ");
		if ((dr != null) || (insID > 0))
			sqlBuf.append("WHERE ");
		if (dr != null) {
			sqlBuf.append("(STARTTIME >=?) AND (STARTTIME < ?) ");
			if (insID > 0)
				sqlBuf.append("AND ");
		}
		
		if (insID > 0)
			sqlBuf.append("(INSTRUCTOR_ID=?) ");
		
		sqlBuf.append("ORDER BY STARTTIME");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int param = 0;
			if (dr != null) {
				ps.setTimestamp(++param, createTimestamp(dr.getStartDate()));
				ps.setTimestamp(++param, createTimestamp(dr.getEndDate()));
			}
			
			if (insID > 0)
				ps.setInt(++param, insID);
			
			return executeBusy(ps);
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
		try (PreparedStatement ps = prepare("SELECT * FROM exams.INSBUSY WHERE (INSTRUCTOR_ID=?) ORDER BY STARTTIME")) {
			ps.setInt(1, instructorID);
			return executeBusy(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT I.*, C.CERTNAME, C.PILOT_ID FROM exams.INSLOG I, exams.COURSES C WHERE (I.COURSE=C.ID) AND (I.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return executeFlightCalendar(ps).stream().findFirst().orElse(null);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM exams.INSLOG ");
		if (courseID != 0)
			sqlBuf.append("WHERE (COURSE=?) ");

		sqlBuf.append("ORDER BY STARTDATE");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (courseID != 0) ps.setInt(1, courseID);
			return executeFlightCalendar(ps);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM exams.INSCALENDAR I, "
				+ "exams.COURSES C WHERE (C.ID=I.COURSE) ");
		if (courseID != 0)
			sqlBuf.append("AND (I.COURSE=?) ");

		sqlBuf.append("ORDER BY I.STARTTIME");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (courseID != 0) ps.setInt(1, courseID);
			return executeCalendar(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads the Flight Academy Instruction Flight calendar.
	 * @param pilotID the database ID of the instructor/student pilot, or zero if all selected 
	 * @param dr the DateRange
	 * @return a Collection of InstructionFlight beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionFlight> getFlightCalendar(int pilotID, DateRange dr) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.*, C.CERTNAME, C.PILOT_ID FROM exams.INSLOG I, exams.COURSES C WHERE (C.ID=I.COURSE) ");
		if (dr != null)
			sqlBuf.append("AND (I.STARTTIME >=?) AND (I.STARTTIME < ?) ");
		if (pilotID != 0)
			sqlBuf.append("AND ((C.PILOT_ID=?) OR (I.INSTRUCTOR_ID=?)) ");

		sqlBuf.append("ORDER BY I.STARTDATE DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int param = 0;
			if (dr != null) {
				ps.setTimestamp(++param, createTimestamp(dr.getStartDate()));
				ps.setTimestamp(++param, createTimestamp(dr.getEndDate()));
			}
			
			if (pilotID != 0) {
				ps.setInt(++param, pilotID);
				ps.setInt(++param, pilotID);
			}
			
			return executeFlightCalendar(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads the Flight Academy Instruction Calendar.
	 * @param pilotID the Pilot ID to display, or 0 if none
	 * @param dr the DateRange
	 * @return a Collection of InstructionSession beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<InstructionSession> getSessionCalendar(int pilotID, DateRange dr) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.CERTNAME, C.PILOT_ID, I.* FROM exams.COURSES C, exams.INSCALENDAR I WHERE (C.ID=I.COURSE) AND (I.STARTTIME >=?) AND (I.STARTTIME < ?) AND (I.STATUS != ?) ");
		if (pilotID != 0)
			sqlBuf.append("AND ((C.PILOT_ID=?) OR (I.INSTRUCTOR_ID=?)) ");
				
		sqlBuf.append("ORDER BY I.STARTTIME");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
			ps.setInt(3, InstructionSession.CANCELED);
			if (pilotID != 0) {
				ps.setInt(4, pilotID);
				ps.setInt(5, pilotID);
			}
			
			return executeCalendar(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse InstructionSession result sets.
	 */
	private static List<InstructionSession> executeCalendar(PreparedStatement ps) throws SQLException {
		List<InstructionSession> results = new ArrayList<InstructionSession>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				InstructionSession s = new InstructionSession(rs.getInt(3), rs.getInt(4));
				s.setName(rs.getString(1));
				s.setPilotID(rs.getInt(2));
				s.setInstructorID(rs.getInt(5));
				s.setStartTime(toInstant(rs.getTimestamp(6)));
				s.setEndTime(toInstant(rs.getTimestamp(7)));
				s.setStatus(rs.getInt(8));
				s.setNoShow(rs.getBoolean(9));
				s.setComments(rs.getString(10));
				results.add(s);
			}
		}
	
		return results;
	}
	
	/*
	 * Helper method to parse InstructionFlight result sets.
	 */
	private static List<InstructionFlight> executeFlightCalendar(PreparedStatement ps) throws SQLException {
		List<InstructionFlight> results = new ArrayList<InstructionFlight>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasCourseInfo = (rs.getMetaData().getColumnCount() > 7);
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
			
				results.add(entry);
			}
		}

		return results;
	}
	
	/*
	 * Helper method to parse InstructionBusy result sets.
	 */
	private static List<InstructionBusy> executeBusy(PreparedStatement ps) throws SQLException {
		List<InstructionBusy> results = new ArrayList<InstructionBusy>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				InstructionBusy ib = new InstructionBusy(rs.getInt(1));
				ib.setStartTime(toInstant(rs.getTimestamp(2)));
				ib.setEndTime(toInstant(rs.getTimestamp(3)));
				ib.setComments(rs.getString(4));
				results.add(ib);
			}
		}
		
		return results;
	}
}