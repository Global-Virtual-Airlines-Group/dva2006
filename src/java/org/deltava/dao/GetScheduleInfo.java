// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

/**
 * A Data Access Object to extract Flight Schedule data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetScheduleInfo extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleInfo(Connection c) {
		super(c);
	}

	/**
	 * Returns all flight numbers in a particular range. 
	 * @param start the start of the range, or zero if none specified
	 * @param end the end of the range, or zero if none specified
	 * @return a Collection of Integers with flight numbers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getFlightNumbers(int start, int end) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT FLIGHT FROM SCHEDULE");
		Collection<String> params = new ArrayList<String>();
		if (start > 0)
			params.add("(FLIGHT > ?)");
		if (end > 0)
			params.add("(FLIGHT < ?)");
		
		// Add parameters
		if (!params.isEmpty()) {
			sqlBuf.append(" WHERE ");
			for (Iterator<String> i = params.iterator(); i.hasNext(); ) {
				String p = i.next();
				sqlBuf.append(p);
				if (i.hasNext())
					sqlBuf.append(" AND ");
			}
		}
		
		sqlBuf.append(" ORDER BY FLIGHT");
		
		try {
			prepareStatement(sqlBuf.toString());
			if (start > 0)
				_ps.setInt(1, start);
			if (end > 0)
				_ps.setInt(2, end);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			Collection<Integer> results = new LinkedHashSet<Integer>();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the next available Leg number for a Flight. 
	 * @param flightNumber the flight number
	 * @return the next available leg number
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if flightNumber is zero or negative
	 */
	public int getNextLeg(int flightNumber) throws DAOException {
		if (flightNumber < 1)
			throw new IllegalArgumentException("Invalid Flight Number -  " + flightNumber);
		
		try {
			prepareStatement("SELECT MAX(LEG) FROM SCHEDULE WHERE (FLIGHT=?)");
			_ps.setInt(1, flightNumber);
			
			// Do the query
			ResultSet rs = _ps.executeQuery();
			int leg = (rs.next()) ? rs.getInt(1) : 0;
			
			// Clean up, increment and return
			rs.close();
			_ps.close();
			return ++leg;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}