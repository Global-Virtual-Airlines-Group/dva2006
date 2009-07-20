// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.assign.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Flight Assignments. All calls in this DAO will populate the legs for any returned Flight
 * Assignments, but will <i>not </i> populate any Flight Reports.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetAssignment extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAssignment(Connection c) {
		super(c);
	}

	/**
	 * Returns a Flight Assignment from the database.
	 * @param id the Assignment Database ID
	 * @return an Assignment bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AssignmentInfo get(int id) throws DAOException {
		try {
			// Load the assignment info
			prepareStatement("SELECT * FROM ASSIGNMENTS WHERE (ID=?)");
			_ps.setInt(1, id);
			List<AssignmentInfo> results = loadInfo();

			// Load the legs
			prepareStatementWithoutLimits("SELECT * FROM ASSIGNLEGS WHERE (ID=?)");
			_ps.setInt(1, id);
			loadLegs(results);

			// Return results, or null if none found
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Assignments for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByPilot(int pilotID) throws DAOException {
		try {
			// Load the assignment info
			prepareStatement("SELECT * FROM ASSIGNMENTS WHERE (PILOT_ID=?) ORDER BY ASSIGNED_ON DESC");
			_ps.setInt(1, pilotID);
			List<AssignmentInfo> results = loadInfo();

			// Load the legs
			prepareStatementWithoutLimits("SELECT L.* FROM ASSIGNMENTS A, ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.PILOT_ID=?)");
			_ps.setInt(1, pilotID);
			loadLegs(results);

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Assignments with a particular status.
	 * @param status the status code
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByStatus(int status) throws DAOException {
		try {
			// Load the assignment info
			prepareStatement("SELECT * FROM ASSIGNMENTS WHERE (STATUS=?) ORDER BY ASSIGNED_ON DESC");
			_ps.setInt(1, status);
			List<AssignmentInfo> results = loadInfo();

			// Load the legs
			prepareStatementWithoutLimits("SELECT L.* FROM ASSIGNMENTS A, ASSIGNLEGS L WHERE (A.ID=L.ID) AND " + "(A.STATUS=?)");
			_ps.setInt(1, status);
			loadLegs(results);

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Assignments related to a particular Online Event.
	 * @param eventID the Event database ID
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByEvent(int eventID, String dbName) throws DAOException {
		
		dbName = formatDBName(dbName);
		try {
			// Load the assignment info
			prepareStatement("SELECT * FROM " + dbName + ".ASSIGNMENTS WHERE (EVENT_ID=?)");
			_ps.setInt(1, eventID);
			List<AssignmentInfo> results = loadInfo();

			// Load the legs
			prepareStatementWithoutLimits("SELECT L.* FROM " + dbName + ".ASSIGNMENTS A, " + dbName + 
					".ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.EVENT_ID=?)");
			_ps.setInt(1, eventID);
			loadLegs(results);

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Return all Flight Assignment for a particular aircraft type and status.
	 * @param eqType the Equipment type
	 * @param status the status code, or -1 if none
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByEquipmentType(String eqType, int status) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ASSIGNMENTS WHERE (EQTYPE=?)");
		if (status != -1)
			sqlBuf.append(" AND (STATUS=?)");
		
		sqlBuf.append(" ORDER BY STATUS, ASSIGNED_ON DESC");
		
		try {
			// Load the assignment info
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, eqType);
			if (status != -1)
				_ps.setInt(2, status);
			
			List<AssignmentInfo> results = loadInfo();

			// Load the legs
			prepareStatementWithoutLimits("SELECT L.* FROM ASSIGNMENTS A, ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.EQTYPE=?)");
			_ps.setString(1, eqType);
			loadLegs(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the equipment types with Flight Assignments.
	 * @return a List of Equipment type codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getEquipmentTypes() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT EQTYPE FROM ASSIGNMENTS ORDER BY EQTYPE");
			
			// Execute the statement
			List<String> results = new ArrayList<String>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(rs.getString(1));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to process the assignment info result set.
	 */
	private List<AssignmentInfo> loadInfo() throws SQLException {
		List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			AssignmentInfo info = new AssignmentInfo(rs.getString(5));
			info.setID(rs.getInt(1));
			info.setStatus(rs.getInt(2));
			info.setPilotID(rs.getInt(3));
			info.setEventID(rs.getInt(4));
			info.setAssignDate(rs.getTimestamp(6));
			info.setCompletionDate(rs.getTimestamp(7));
			info.setRepeating(rs.getBoolean(8));
			info.setRandom(rs.getBoolean(9));
			info.setPurgeable(rs.getBoolean(10));

			// Add to results
			results.add(info);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to process the assignment legs result set.
	 */
	private void loadLegs(List<AssignmentInfo> assignments) throws SQLException {
		if (assignments.isEmpty())
			return;

		// Execute the Query
		Map<Integer, AssignmentInfo> infoMap = CollectionUtils.createMap(assignments, "ID");
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			int assignID = rs.getInt(1);
			AssignmentInfo info = infoMap.get(Integer.valueOf(assignID));
			if (info != null) {
				AssignmentLeg leg = new AssignmentLeg(SystemData.getAirline(rs.getString(2)), rs.getInt(3), rs.getInt(4));
				leg.setID(assignID);
				leg.setEquipmentType(info.getEquipmentType());
				leg.setAirportD(SystemData.getAirport(rs.getString(5)));
				leg.setAirportA(SystemData.getAirport(rs.getString(6)));
				info.addAssignment(leg);
			}
		}

		// Clean up and return
		rs.close();
		_ps.close();
	}
}