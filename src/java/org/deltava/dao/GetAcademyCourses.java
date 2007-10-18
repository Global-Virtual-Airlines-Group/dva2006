// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy course data. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAcademyCourses extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyCourses(Connection c) {
		super(c);
	}

	/**
	 * Returns a Flight Academy Course profile.
	 * @param id the database ID
	 * @return a Course bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Course get(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.*, CR.STAGE FROM exams.COURSES C, exams.CERTS CR WHERE "
					+ "(C.CERTNAME=CR.NAME) AND (C.ID=?) LIMIT 1");
			_ps.setInt(1, id);
			
			// Execute the query
			List<Course> results = execute();
			if (results.isEmpty())
				return null;
			
			// Get the course and load from the child tables
			Course result = results.get(0);
			loadComments(result);
			loadProgress(result);
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Course profiles for a particular Certification.
	 * @param name the Certification name
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByName(String name) throws DAOException {
		try {
			prepareStatement("SELECT C.*, CR.STAGE FROM exams.COURSES C, exams.CERTS CR WHERE (C.CERTNAME=CR.NAME) "
					+ "AND (C.NAME=?)");
			_ps.setString(1, name);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Course profiles for particular Check Rides.
	 * @param ids a Collection of Check Ride database IDs
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByCheckRide(Collection<Integer> ids) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyList();
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE FROM exams.COURSES C, exams.CERTS CR, "
				+ "exams.COURSERIDES CRR WHERE (C.CERTNAME=CR.NAME) AND (CRR.COURSE=C.ID) AND (CRR.CHECKRIDE IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append("))");
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Course profiles for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByPilot(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT C.*, CR.STAGE FROM exams.COURSES C, exams.CERTS CR WHERE "
					+ "(C.CERTNAME=CR.NAME) AND (C.PILOT_ID=?) ORDER BY C.STARTDATE");
			_ps.setInt(1, pilotID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all active or pending Flight Academy Course profiles for a particular Instructor.
	 * @param instructorID the Instructor's database ID
	 * @param sortBy the sort column SQL
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByInstructor(int instructorID, String sortBy) throws DAOException {
		try {
			prepareStatement("SELECT C.*, CR.STAGE, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, exams.COURSES C "
					+ "LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) AND "
					+ "(C.INSTRUCTOR_ID=?) AND ((C.STATUS=?) OR (C.STATUS=?)) GROUP BY C.ID ORDER BY " + sortBy);
			_ps.setInt(1, instructorID);
			_ps.setInt(2, Course.PENDING);
			_ps.setInt(3, Course.STARTED);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all completed Flight Academy Course profiles for a particular Pilot.
	 * @param pilotID the Pilot's database ID, or zero if all completed courses should be returned
	 * @param sortBy the sort column SQL
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getCompleted(int pilotID, String sortBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, "
				+ "exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (C.STATUS=?) "
				+ "AND (C.CERTNAME=CR.NAME)");
		if (pilotID != 0)
			sqlBuf.append(" AND (C.PILOT_ID=?)");
		
		sqlBuf.append(" GROUP BY C.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, Course.COMPLETE);
			if (pilotID != 0)
				_ps.setInt(2, pilotID);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Academy Course profiles with a particular status.
	 * @param sortBy the sort column SQL
	 * @param status the Course status code
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByStatus(String sortBy, int status) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, "
				+ "exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) "
				+ "AND (C.STATUS=?) GROUP BY C.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, status);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Certifications obtained by a group of Pilots.
	 * @param ids a Collection of database IDs
	 * @return a Map of comma-delimited certifications, indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Collection<String>> getCertifications(Collection ids) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.PILOT_ID, CR.ABBR FROM exams.COURSES C, exams.CERTS CR WHERE "
				+ "(CR.NAME=C.CERTNAME) AND (C.STATUS=?) AND (C.PILOT_ID IN (");
		for (Iterator i = ids.iterator(); i.hasNext(); ) {
			Object rawID = i.next();
			Integer id = (rawID instanceof Integer) ? (Integer) rawID : new Integer(((DatabaseBean) rawID).getID());
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(")) ORDER BY C.PILOT_ID, CR.STAGE");
		
		Map<Integer, Collection<String>> results = new HashMap<Integer, Collection<String>>();
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, Course.COMPLETE);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Integer id = new Integer(rs.getInt(1));
				Collection<String> certs = results.get(id);
				if (certs == null) {
					certs = new LinkedHashSet<String>();
					results.put(id, certs);
				}
				
				// Add the certification
				certs.add(rs.getString(2));
			}
			
			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Return results
		return results;
	}
	
	/**
	 * Returns all Flight Academy Course profiles.
	 * @param sortBy the sort column SQL
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getAll(String sortBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, "
				+ "exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) "
				+ "GROUP BY C.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Course result sets.
	 */
	private List<Course> execute() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();
		boolean hasLastChat = (rs.getMetaData().getColumnCount() > 8);
		
		// Iterate through the results
		List<Course> results = new ArrayList<Course>();
		while (rs.next()) {
			Course c = new Course(rs.getString(2), rs.getInt(3));
			c.setID(rs.getInt(1));
			c.setInstructorID(rs.getInt(4));
			c.setStatus(rs.getInt(5));
			c.setStartDate(rs.getTimestamp(6));
			c.setEndDate(rs.getTimestamp(7));
			c.setStage(rs.getInt(8));
			if (hasLastChat)
				c.setLastComment(rs.getTimestamp(9));
			
			// Add to results
			results.add(c);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to load comments for a Course.
	 */
	private void loadComments(Course c) throws SQLException {
		
		// Prepare the statement
		prepareStatementWithoutLimits("SELECT * FROM exams.COURSECHAT WHERE (COURSE_ID=?)");
		_ps.setInt(1, c.getID());

		// Load the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			CourseComment cc = new CourseComment(c.getID(), rs.getInt(2));
			cc.setCreatedOn(rs.getTimestamp(3));
			cc.setText(rs.getString(4));
			c.addComment(cc);
		}
		
		// Clean up after ourselves
		rs.close();
		_ps.close();
	}
	
	/**
	 * Helper method to load progress for a Course.
	 */
	private void loadProgress(Course c) throws SQLException {

		// Prepare the statement
		prepareStatementWithoutLimits("SELECT * FROM exams.COURSEPROGRESS WHERE (ID=?) ORDER BY SEQ");
		_ps.setInt(1, c.getID());
		
		// Load the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			CourseProgress cp = new CourseProgress(c.getID(), rs.getInt(2));
			cp.setAuthorID(rs.getInt(3));
			cp.setText(rs.getString(4));
			cp.setComplete(rs.getBoolean(5));
			cp.setCompletedOn(rs.getTimestamp(6));
			c.addProgress(cp);
		}
		
		// Clean up after ourselves
		rs.close();
		_ps.close();
	}
}