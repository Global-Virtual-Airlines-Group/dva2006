// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Schedule route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetScheduleAirport extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleAirport(Connection c) {
		super(c);
	}

	/**
	 * Returns all Airports for an Airline with at least one flight departing.
	 * @param al the Airline bean or null
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getOriginAirports(Airline al) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AIRPORT_D FROM SCHEDULE");
		if (al != null)
			sqlBuf.append(" WHERE (AIRLINE=?)");

		try {
			prepareStatement(sqlBuf.toString());
			if (al != null)
				_ps.setString(1, al.getCode());

			// Execute the query
			Collection<Airport> results = new HashSet<Airport>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Airport a = SystemData.getAirport(rs.getString(1));
				if (a != null)
					results.add(a);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Airports with flights departing or arriving at a particular Airport.
	 * @param a the Airport bean
	 * @param from TRUE if returning destination airports for flights originating at a, otherwise FALSE
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getConnectingAirports(Airport a, boolean from) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT ");
		sqlBuf.append(from ? "AIRPORT_A" : "AIRPORT_D");
		sqlBuf.append(" AS CODE FROM SCHEDULE WHERE (");
		sqlBuf.append(from ? "AIRPORT_D" : "AIRPORT_A");
		sqlBuf.append("=?) ORDER BY CODE");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, a.getIATA());

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			Collection<Airport> results = new LinkedHashSet<Airport>();
			while (rs.next()) {
				Airport ap = SystemData.getAirport(rs.getString(1));
				if (ap != null)
					results.add(ap);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Airports with flights departing or arriving at a particular Airport for a particular Airline.
	 * @param a the Airport bean
	 * @param from TRUE if returning destination airports for flights originating at a, otherwise FALSE
	 * @param al the Airline bean
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getConnectingAirports(Airport a, boolean from, Airline al) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT ");
		sqlBuf.append(from ? "AIRPORT_A" : "AIRPORT_D");
		sqlBuf.append(" AS CODE FROM SCHEDULE WHERE (AIRLINE=?) AND (");
		sqlBuf.append(from ? "AIRPORT_D" : "AIRPORT_A");
		sqlBuf.append("=?) ORDER BY CODE");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, al.getCode());
			_ps.setString(2, a.getIATA());
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			Collection<Airport> results = new LinkedHashSet<Airport>();
			while (rs.next()) {
				Airport ap = SystemData.getAirport(rs.getString(1));
				if (ap != null)
					results.add(ap);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}