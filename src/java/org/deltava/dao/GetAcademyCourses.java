// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
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
			setQueryMax(1);
			prepareStatement("SELECT C.*, CR.STAGE FROM COURSES C, CERTS CR WHERE (C.CERTNAME=CR.NAME) AND "
					+ "(C.ID=?)");
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
			prepareStatement("SELECT C.*, CR.STAGE FROM COURSES C, CERTS CR WHERE (C.CERTNAME=CR.NAME) AND "
					+ "(C.NAME=?)");
			_ps.setString(1, name);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Course profiles for a particular Pilot.
	 * @param pilotID the Pilot's databae ID
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByPilot(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT C.*, CR.STAGE FROM COURSES C, CERTS CR WHERE (C.CERTNAME=CR.NAME) "
					+ "AND (C.PILOT_ID=?) ORDER BY C.STARTDATE");
			_ps.setInt(1, pilotID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all completed Flight Academy Course profiles for a particular Pilot.
	 * @param pilotID the Pilot's databae ID
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getCompletedByPilot(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT C.*, CR.STAGE FROM COURSES C, CERTS CR WHERE (C.CERTNAME=CR.NAME) "
					+ "AND (C.PILOT_ID=?) AND (C.STATUS=?) ORDER BY C.STARTDATE");
			_ps.setInt(1, pilotID);
			_ps.setInt(2, Course.COMPLETE);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Flight Academy Course profiles.
	 * @param sortBy the sort column SQL
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getActive(String sortBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, MAX(CC.CREATED) AS LC FROM COURSES C, "
				+ "CERTS CR LEFT JOIN COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) "
				+ "AND ((C.STATUS=?) OR (C.STATUS=?)) GROUP BY C.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, Course.STARTED);
			_ps.setInt(2, Course.PENDING);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Certifications obtained by a group of Pilots.
	 * @param ids a Collection of database IDs
	 * @param dbName the database name
	 * @return a Map of comma-delimited certifications, indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Collection<String>> getCertifications(Collection ids, String dbName) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();
		
		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT C.PILOT_ID, CR.ABBR FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".COURSES C, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".CERTS CR WHERE (CR.NAME=C.CERTNAME) AND (C.STATUS=?) AND (C.PILOT_ID IN (");
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
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, MAX(CC.CREATED) AS LC FROM COURSES C, CERTS CR LEFT "
				+ "JOIN COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) GROUP BY C.ID ORDER BY ");
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
		boolean hasLastChat = (rs.getMetaData().getColumnCount() > 7);
		
		// Iterate through the results
		List<Course> results = new ArrayList<Course>();
		while (rs.next()) {
			Course c = new Course(rs.getString(2), rs.getInt(3));
			c.setID(rs.getInt(1));
			c.setStatus(rs.getInt(4));
			c.setStartDate(rs.getTimestamp(5));
			c.setEndDate(rs.getTimestamp(6));
			c.setStage(rs.getInt(7));
			if (hasLastChat)
				c.setLastComment(rs.getTimestamp(8));
			
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
		prepareStatementWithoutLimits("SELECT * FROM COURSECHAT WHERE (COURSE_ID=?)");
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
		prepareStatementWithoutLimits("SELECT * FROM COURSEPROGRESS WHERE (ID=?) ORDER BY SEQ");
		_ps.setInt(1, c.getID());
		
		// Load the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			CourseProgress cp = new CourseProgress(c.getID(), rs.getInt(2));
			cp.setText(rs.getString(3));
			cp.setComplete(rs.getBoolean(4));
			cp.setCompletedOn(rs.getTimestamp(5));
			c.addProgress(cp);
		}
		
		// Clean up after ourselves
		rs.close();
		_ps.close();
	}
}