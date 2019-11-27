// Copyright 2005, 2006, 2009, 2011, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
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
			List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM ASSIGNMENTS WHERE (ID=?) LIMIT 1")) {
				ps.setInt(1, id);
				results.addAll(loadInfo(ps));
			}

			// Load the legs
			try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM ASSIGNLEGS WHERE (ID=?)")) {
				ps.setInt(1, id);
				loadLegs(ps, results);
			}

			return results.stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Assignments for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @param st an AssignmentStatus, or null for all
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByPilot(int pilotID, AssignmentStatus st) throws DAOException {
		try {
			// Load the assignment info
			StringBuilder buf = new StringBuilder("SELECT * FROM ASSIGNMENTS WHERE (PILOT_ID=?) ");
			if (st != null)
				buf.append("AND (STATUS=?) ");
			
			buf.append("ORDER BY ASSIGNED_ON DESC");
			
			List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();
			try (PreparedStatement ps = prepare(buf.toString())) {
				ps.setInt(1, pilotID);
				if (st != null)
					ps.setInt(2,  st.ordinal());
			
				results.addAll(loadInfo(ps));
			}

			// Load the legs
			try (PreparedStatement ps = prepareWithoutLimits("SELECT L.* FROM ASSIGNMENTS A, ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.PILOT_ID=?)")) {
				ps.setInt(1, pilotID);
				loadLegs(ps, results);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Assignments with a particular status.
	 * @param status the AssignmentStatus
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByStatus(AssignmentStatus status) throws DAOException {
		try {
			// Load the assignment info
			List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();
			try (PreparedStatement ps = prepare("SELECT * FROM ASSIGNMENTS WHERE (STATUS=?) ORDER BY ASSIGNED_ON DESC")) {
				ps.setInt(1, status.ordinal());
				results.addAll(loadInfo(ps));
			}

			// Load the legs
			try (PreparedStatement ps = prepareWithoutLimits("SELECT L.* FROM ASSIGNMENTS A, ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.STATUS=?)")) {
				ps.setInt(1, status.ordinal());
				loadLegs(ps, results);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Assignments related to a particular Online Event.
	 * @param eventID the Event database ID
	 * @param dbName the database name
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByEvent(int eventID, String dbName) throws DAOException {
		String db = formatDBName(dbName);
		try {
			// Load the assignment info
			List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();
			try (PreparedStatement ps = prepare("SELECT * FROM " + db + ".ASSIGNMENTS WHERE (EVENT_ID=?)")) {
				ps.setInt(1, eventID);
				results.addAll(loadInfo(ps));
			}

			// Load the legs
			try (PreparedStatement ps = prepareWithoutLimits("SELECT L.* FROM " + db + ".ASSIGNMENTS A, " + db + ".ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.EVENT_ID=?)");) {
				ps.setInt(1, eventID);
				loadLegs(ps, results);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Return all Flight Assignment for a particular aircraft type and status.
	 * @param eqType the Equipment type
	 * @param status the AssignmentStatus
	 * @return a List of AssignmentInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AssignmentInfo> getByEquipmentType(String eqType, AssignmentStatus status) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ASSIGNMENTS WHERE (EQTYPE=?)");
		if (status != null)
			sqlBuf.append(" AND (STATUS=?)");
		
		sqlBuf.append(" ORDER BY STATUS, ASSIGNED_ON DESC");
		
		try  {
			// Load the assignment info
			List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();
			try (PreparedStatement ps = prepare(sqlBuf.toString())) {
				ps.setString(1, eqType);
				if (status != null)
					ps.setInt(2, status.ordinal());
			
				results.addAll(loadInfo(ps));
			}

			// Load the legs
			try (PreparedStatement ps = prepareWithoutLimits("SELECT L.* FROM ASSIGNMENTS A, ASSIGNLEGS L WHERE (A.ID=L.ID) AND (A.EQTYPE=?)")) {
				ps.setString(1, eqType);
				loadLegs(ps, results);
			}
			
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
		try (PreparedStatement ps = prepare("SELECT DISTINCT EQTYPE FROM ASSIGNMENTS ORDER BY EQTYPE")) {
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to process the assignment info result set.
	 */
	private static List<AssignmentInfo> loadInfo(PreparedStatement ps) throws SQLException {
		List<AssignmentInfo> results = new ArrayList<AssignmentInfo>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				AssignmentInfo info = new AssignmentInfo(rs.getString(5));
				info.setID(rs.getInt(1));
				info.setStatus(AssignmentStatus.values()[rs.getInt(2)]);
				info.setPilotID(rs.getInt(3));
				info.setEventID(rs.getInt(4));
				info.setAssignDate(toInstant(rs.getTimestamp(6)));
				info.setCompletionDate(toInstant(rs.getTimestamp(7)));
				info.setRepeating(rs.getBoolean(8));
				info.setRandom(rs.getBoolean(9));
				info.setPurgeable(rs.getBoolean(10));
				results.add(info);
			}
		}

		return results;
	}

	/*
	 * Helper method to process the assignment legs result set.
	 */
	private static void loadLegs(PreparedStatement ps, List<AssignmentInfo> assignments) throws SQLException {
		if (assignments.isEmpty()) return;

		// Execute the Query
		Map<Integer, AssignmentInfo> infoMap = CollectionUtils.createMap(assignments, AssignmentInfo::getID);
		try (ResultSet rs = ps.executeQuery()) {
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
		}
	}
}