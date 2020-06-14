// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.UserData;
import org.deltava.beans.academy.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load Flight Academy course data. 
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT C.*, CR.STAGE, CR.ABBR FROM exams.COURSES C, exams.CERTS CR WHERE (C.CERTNAME=CR.NAME) AND (C.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			Course c = execute(ps).stream().findFirst().orElse(null);
			if (c == null) return null;
			
			// Get the course and load from the child tables
			loadComments(c);
			loadProgress(c);
			return c;
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
		try (PreparedStatement ps = prepare("SELECT C.*, CR.STAGE, CR.ABBR FROM exams.COURSES C, exams.CERTS CR WHERE (C.CERTNAME=CR.NAME) AND (C.NAME=?)")) {
			ps.setString(1, name);
			return execute(ps);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, CR.ABBR FROM exams.COURSES C, exams.CERTS CR, exams.COURSERIDES CRR WHERE (C.CERTNAME=CR.NAME) AND (CRR.COURSE=C.ID) AND (CRR.CHECKRIDE IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append("))");
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT C.*, CR.STAGE, CR.ABBR, MAX(CC.CREATED) FROM exams.CERTS CR, exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) "
			+ "WHERE (C.CERTNAME=CR.NAME) AND (C.PILOT_ID=?) GROUP BY C.ID ORDER BY C.STARTDATE")) {
			ps.setInt(1, pilotID);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT C.*, CR.STAGE, CR.ABBR, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) "
			+ "WHERE (C.CERTNAME=CR.NAME) AND (C.INSTRUCTOR_ID=?) AND ((C.STATUS=?) OR (C.STATUS=?)) GROUP BY C.ID ORDER BY " + sortBy)) {
			ps.setInt(1, instructorID);
			ps.setInt(2, Status.PENDING.ordinal());
			ps.setInt(3, Status.STARTED.ordinal());
			return execute(ps);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, CR.ABBR, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON "
			+ "(C.ID=CC.COURSE_ID) WHERE (C.STATUS=?) AND (C.CERTNAME=CR.NAME)");
		if (pilotID != 0)
			sqlBuf.append(" AND (C.PILOT_ID=?)");
		
		sqlBuf.append(" GROUP BY C.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, Status.COMPLETE.ordinal());
			if (pilotID != 0)
				ps.setInt(2, pilotID);
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Academy Course profiles with a particular status.
	 * @param s the Status
	 * @param sortBy the sort column SQL
	 * @param c the Certification, or null for all
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getByStatus(Status s, String sortBy, Certification c) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, CR.ABBR, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON "
			+ "(C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) AND (C.STATUS=?) ");
		if (c != null)
			sqlBuf.append("AND (C.CERTNAME=?) ");
		sqlBuf.append("GROUP BY C.ID ");
		if (!StringUtils.isEmpty(sortBy)) {
			sqlBuf.append("ORDER BY ");
			sqlBuf.append(sortBy);
		}
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, s.ordinal());
			if (c != null)
				ps.setString(2, c.getName());
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Courses ready for approval.
	 * @return a Collection of Course beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Course> getCompletionQueue() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT C.*, CRT.STAGE, CRT.ABBR, MAX(CC.CREATED) AS LC, COUNT(CP.SEQ) AS REQCNT, SUM(IF(CP.COMPLETE,1,0)) AS CREQCNT, SUM(CR.PASS) AS CRCNT "
			+ "FROM (exams.CERTS CRT, exams.COURSES C) LEFT JOIN exams.COURSEPROGRESS CP ON (C.ID=CP.ID) LEFT JOIN exams.COURSERIDES CCR ON (C.ID=CCR.COURSE) LEFT JOIN exams.CHECKRIDES CR "
			+ "ON (CR.ID=CCR.CHECKRIDE) LEFT JOIN exams.COURSECHAT CC ON (C.ID=CC.COURSE_ID) WHERE (CRT.NAME=C.CERTNAME) AND (C.STATUS=?) GROUP BY C.ID HAVING (REQCNT=CREQCNT) AND "
			+ "(C.CHECKRIDES=CRCNT) ORDER BY LC DESC")) {
			ps.setInt(1, Status.STARTED.ordinal());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Certifications obtained by a group of Pilots.
	 * @param ids a Collection of database IDs
	 * @param visibleOnly TRUE if only visible certifications should be returned, otherwise FALSE
	 * @return a Map of comma-delimited certifications, indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Collection<String>> getCertifications(Collection<?> ids, boolean visibleOnly) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();
		
		// Build the SQL statement
		Map<Integer, Integer> xdbIDs = new HashMap<Integer, Integer>();
		StringBuilder sqlBuf = new StringBuilder("SELECT C.PILOT_ID, CR.ABBR FROM exams.COURSES C, exams.CERTS CR WHERE (CR.NAME=C.CERTNAME) AND (C.STATUS=?) AND (C.PILOT_ID IN (");
		for (Iterator<?> i = ids.iterator(); i.hasNext(); ) {
			Object rawID = i.next();
			if (rawID instanceof Integer)
				sqlBuf.append(rawID.toString());
			else if (rawID instanceof UserData) {
				UserData ud = (UserData) rawID;
				sqlBuf.append(ud.getID());
				for (Integer id : ud.getIDs()) {
					xdbIDs.put(id, Integer.valueOf(ud.getID()));
					sqlBuf.append(',');
					sqlBuf.append(id.toString());
				}
			} else
				sqlBuf.append(((DatabaseBean) rawID).getID());	
			
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(")) ");
		if (visibleOnly)
			sqlBuf.append("AND (CR.VISIBLE=1) ");
		sqlBuf.append("ORDER BY C.PILOT_ID, CR.STAGE");
		
		Map<Integer, Collection<String>> results = new HashMap<Integer, Collection<String>>();
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, Status.COMPLETE.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Integer id = Integer.valueOf(rs.getInt(1));
					String cert = rs.getString(2);
					CollectionUtils.addMapCollection(results, id, cert);
					
					Integer xdbID = xdbIDs.get(id);
					if (xdbID != null)
						CollectionUtils.addMapCollection(results, xdbID, cert);
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
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
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CR.STAGE, CR.ABBR, MAX(CC.CREATED) AS LC FROM exams.CERTS CR, exams.COURSES C LEFT JOIN exams.COURSECHAT CC ON "
			+ "(C.ID=CC.COURSE_ID) WHERE (C.CERTNAME=CR.NAME) GROUP BY C.ID ORDER BY ");
		sqlBuf.append(sortBy);
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Course result sets.
	 */
	private static List<Course> execute(PreparedStatement ps) throws SQLException {
		List<Course> results = new ArrayList<Course>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasLastChat = (rs.getMetaData().getColumnCount() > 10);
			while (rs.next()) {
				Course c = new Course(rs.getString(2), rs.getInt(3));
				c.setID(rs.getInt(1));
				c.setInstructorID(rs.getInt(4));
				c.setStatus(Status.values()[rs.getInt(5)]);
				c.setStartDate(toInstant(rs.getTimestamp(6)));
				c.setEndDate(toInstant(rs.getTimestamp(7)));
				c.setRideCount(rs.getInt(8));
				c.setStage(rs.getInt(9));
				c.setCode(rs.getString(10));
				if (hasLastChat)
					c.setLastComment(toInstant(rs.getTimestamp(11)));
			
				results.add(c);
			}
		}
		
		return results;
	}
	
	/*
	 * Helper method to load comments for a Course.
	 */
	private void loadComments(Course c) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM exams.COURSECHAT WHERE (COURSE_ID=?)")) {
			ps.setInt(1, c.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CourseComment cc = new CourseComment(c.getID(), rs.getInt(2));
					cc.setCreatedOn(rs.getTimestamp(3).toInstant());
					cc.setBody(rs.getString(4));
					c.addComment(cc);
				}
			}
		}
	}
	
	/*
	 * Helper method to load progress for a Course.
	 */
	private void loadProgress(Course c) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM exams.COURSEPROGRESS WHERE (ID=?) ORDER BY SEQ")) {
			ps.setInt(1, c.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CourseProgress cp = new CourseProgress(c.getID(), rs.getInt(2));
					cp.setAuthorID(rs.getInt(3));
					cp.setText(rs.getString(4));
					cp.setExamName(rs.getString(5));
					cp.setComplete(rs.getBoolean(6));
					cp.setCompletedOn(toInstant(rs.getTimestamp(7)));
					c.addProgress(cp);
				}
			}
		}
	}
}