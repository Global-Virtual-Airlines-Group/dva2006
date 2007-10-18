// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.StatusUpdate;

/**
 * A Data Access Object to read Status Update log entries.
 * @author Luke
 * @version 1.0
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
	 * @see StatusUpdate#SR_CAPTAIN
	 */
	public boolean isSeniorCaptain(int id) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM STATUS_UPDATES WHERE (PILOT_ID=?) AND (TYPE=?)");
			_ps.setInt(1, id);
			_ps.setInt(2, StatusUpdate.SR_CAPTAIN);
			
			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			boolean isSC = rs.next() ? (rs.getInt(1) > 0) : false;
			
			// Clean up and return
			rs.close();
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
	public List<StatusUpdate> getByType(int updateType) throws DAOException {
		try {
			prepareStatement("SELECT * FROM STATUS_UPDATES WHERE (TYPE=?) ORDER BY CREATED DESC");
			_ps.setInt(1, updateType);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Private helper method to load data from the table.
	 */
	private List<StatusUpdate> execute() throws SQLException {
		
		List<StatusUpdate> results = new ArrayList<StatusUpdate>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			StatusUpdate upd = new StatusUpdate(rs.getInt(1), rs.getInt(4));
			upd.setCreatedOn(rs.getDate(2));
			upd.setAuthorID(rs.getInt(3));
			upd.setDescription(rs.getString(5));
			results.add(upd);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}