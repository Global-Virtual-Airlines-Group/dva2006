// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.SelectCall;

/**
 * A Data Access Object to read aircraft SELCAL data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSELCAL extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSELCAL(Connection c) {
		super(c);
	}
	
	/**
	 * Returns SELCAL data for a particular code.
	 * @param code the SELCAL code
	 * @return a SelectCall or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public SelectCall get(String code) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM SELCAL WHERE (UPPER(CODE)=?)");
			_ps.setString(1, code.toUpperCase());
			
			// Return result or null if empty
			List results = execute();
			setQueryMax(0);
			return results.isEmpty() ? null : (SelectCall) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all aircraft SELCAL codes.
	 * @param orderBy the column to sort results with
	 * @return a Collection of SelectCall beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SelectCall> getCodes(String orderBy) throws DAOException {
		try {
			prepareStatement("SELECT SC.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) AS PNAME FROM "
					+ "SELCAL SC LEFT JOIN PILOTS P ON (P.ID=SC.PILOT_ID) ORDER BY " + orderBy);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all reserved aircraft SELCAL codes.
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of SelectCall beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SelectCall> getReserved(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM SELCAL WHERE (PILOT_ID=?) ORDER BY CODE");
			_ps.setInt(1, pilotID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all aircraft SELCAL codes for a particular aircraft type.
	 * @param eqType the equipment type
	 * @return a Collection of SelectCall beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SelectCall> getByEquipmentType(String eqType) throws DAOException {
		try {
			prepareStatement("SELECT * FROM SELCAL WHERE (EQTYPE=?) ORDER BY CODE");
			_ps.setString(1, eqType);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<SelectCall> execute() throws SQLException {
		
		// Execute the query
		List<SelectCall> results = new ArrayList<SelectCall>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			SelectCall sc = new SelectCall(rs.getString(1), rs.getString(2));
			sc.setEquipmentType(rs.getString(3));
			sc.setReservedBy(rs.getInt(4));
			sc.setReservedOn(rs.getTimestamp(5));
			
			// Add to results
			results.add(sc);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}