// Copyright 2005, 2006, 2007, 2008, 2011, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

/**
 * A Data Access Object to read Status Update log entries.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class GetStatusUpdate extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetStatusUpdate(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves all Status Updates for a particular Pilot. <i>The firstName and lastName properties will
	 * be populated by the <b>Status Update's author name</b>, not the pilot name.</i>
	 * @param id the Pilot ID
	 * @param dbName the database name
	 * @return a List of StatusUpdate beans, sorted by descending date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<StatusUpdate> getByUser(int id, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".STATUS_UPDATES WHERE (PILOT_ID=?) ORDER BY CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns if a particular Pilot has been promoted to Senior Captain.
	 * @param id the Pilot's database ID
	 * @return TRUE if the Pilot has a StatusUpdate entry promoting to Senior Captain, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 * @see UpdateType#SR_CAPTAIN
	 */
	public boolean isSeniorCaptain(int id) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM STATUS_UPDATES WHERE (PILOT_ID=?) AND (TYPE=?)");
			_ps.setInt(1, id);
			_ps.setInt(2, UpdateType.SR_CAPTAIN.ordinal());
			
			// Execute the Query
			boolean isSC = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					isSC = (rs.getInt(1) > 0);
			}
			
			_ps.close();
			return isSC;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Status Updates with a given type.
	 * @param updateType the Status Update type
	 * @return a List of StatusUpdate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<StatusUpdate> getByType(UpdateType updateType) throws DAOException {
		return getByType(updateType, 0);
	}
	
	/**
	 * Returns all Status Updates with a given type and time period.
	 * @param updateType the Status Update type
	 * @param maxHours the maximum elapsed time in hours, or zero for all 
	 * @return a List of StatusUpdate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<StatusUpdate> getByType(UpdateType updateType, int maxHours) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM STATUS_UPDATES WHERE (TYPE=?)");
		if (maxHours > 0)
			sqlBuf.append(" AND (CREATED > DATE_SUB(NOW(), INTERVAL ? HOUR))");
		sqlBuf.append(" ORDER BY CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, updateType.ordinal());
			if (maxHours > 0)
				_ps.setInt(2, maxHours);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Private helper method to load data from the table.
	 */
	private List<StatusUpdate> execute() throws SQLException {
		List<StatusUpdate> results = new ArrayList<StatusUpdate>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				StatusUpdate upd = new StatusUpdate(rs.getInt(1), UpdateType.values()[rs.getInt(4)]);
				upd.setDate(rs.getTimestamp(2).toInstant());
				upd.setAuthorID(rs.getInt(3));
				upd.setDescription(rs.getString(5));
				results.add(upd);
			}
		}
		
		_ps.close();
		return results;
	}
}