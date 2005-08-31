// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
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
	 * @return a List of StatusUpdate beans, sorted by descending date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByUser(int id) throws DAOException {
		try {
			prepareStatement("SELECT SU.*, P.FIRSTNAME, P.LASTNAME FROM STATUS_UPDATES SU, PILOTS P "
					+ "WHERE (SU.PILOT_ID=?) AND (P.ID=SU.AUTHOR_ID) ORDER BY SU.CREATED DESC");
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Status Updates created by a particular Staff member.
	 * @param id the Pilot ID of the staff member
	 * @return a List of StatusUpdate beans, sorted by descending date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByStaffMember(int id) throws DAOException {
		try {
			prepareStatement("SELECT SU.*, P.FIRSTNAME, P.LASTNAME FROM STATUS_UPDATES SU, PILOTS P "
					+ "WHERE (SU.AUTHOR_ID=?) AND (P.ID=SU.PILOT_ID) ORDER BY SU.CREATED DESC");
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Status Updates created between two dates.
	 * @param sd the start date/time
	 * @param ed the end date/time
	 * @return a List of StatusUpdate beans, sorted by descending date 
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByDate(java.util.Date sd, java.util.Date ed) throws DAOException {
		try {
			prepareStatement("SELECT SU.*, P.FIRSTNAME, P.LASTNAME FROM STATUS_UPDATES SU, PILOTS P WHERE "
					+ "(SU.PILOT_ID=P.ID) AND (SU.CREATED >= ?) AND (SU.CREATED <= ?) ORDER BY SU.CREATED DESC");
			_ps.setTimestamp(1, createTimestamp(sd));
			_ps.setTimestamp(2, createTimestamp(ed));
			return execute();
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
	public List getByType(int updateType) throws DAOException {
		try {
			prepareStatement("SELECT SU.*, P.FIRSTNAME, P.LASTNAME FROM STATUS_UPDATES SU, PILOTS P WHERE"
					+ "(SU.PILOT_ID=P.ID) AND (SU.TYPE=?) ORDER BY SU.CREATED DESC");
			_ps.setInt(1, updateType);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Private helper method to load data from the table.
	 */
	private List execute() throws SQLException {
		
		List results = new ArrayList();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			StatusUpdate upd = new StatusUpdate(rs.getInt(1), rs.getInt(4));
			// FIXME Switch when we go live
			// upd.setCreatedOn(rs.getTimestamp(2));
			upd.setCreatedOn(new java.util.Date(rs.getLong(2)));
			upd.setAuthorID(rs.getInt(3));
			upd.setDescription(rs.getString(5));
			upd.setFirstName(rs.getString(6));
			upd.setLastName(rs.getString(7));
			results.add(upd);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}