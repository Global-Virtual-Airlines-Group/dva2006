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
	 * @return a Collection of Airport beans, sorted by airport name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getOriginAirports(Airline al) throws DAOException {
		return getAirports("AIRPORT_D", al);
	}
	
	/**
	 * Returns all Airports for an Airline with at least one flight arriving.
	 * @param al the Airline bean or null
	 * @return a Collection of Airport beans, sorted by airport name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getDestinationAirports(Airline al) throws DAOException {
		return getAirports("AIRPORT_A", al);
	}

	/**
	 * Returns origin/departure airports for an airline.
	 */
	private Collection<Airport> getAirports(String fName, Airline al) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT S.");
		sqlBuf.append(fName);
		sqlBuf.append(" FROM SCHEDULE S, common.AIRPORTS A WHERE (S.");
		sqlBuf.append(fName);
		sqlBuf.append("=A.IATA)");
		if (al != null)
			sqlBuf.append(" AND (S.AIRLINE=?)");
		
		sqlBuf.append(" ORDER BY A.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			if (al != null)
				_ps.setString(1, al.getCode());

			return execute();
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
		if (a == null)
			return from ? getDestinationAirports(al) : getOriginAirports(al);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT S.");
		sqlBuf.append(from ? "AIRPORT_A" : "AIRPORT_D");
		sqlBuf.append(" AS CODE FROM SCHEDULE S, common.AIRPORTS A WHERE (S.");
		sqlBuf.append(from ? "AIRPORT_A" : "AIRPORT_D");
		sqlBuf.append("=A.IATA) AND (S.");
		sqlBuf.append(from ? "AIRPORT_D" : "AIRPORT_A");
		sqlBuf.append("=?)");
		if (al != null)
			sqlBuf.append(" AND (S.AIRLINE=?)");
		
		sqlBuf.append(" ORDER BY A.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, a.getIATA());
			if (al != null)
				_ps.setString(2, al.getCode());

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Airport result sets.
	 */
	private List<Airport> execute() throws SQLException {
		List<Airport> results = new ArrayList<Airport>();
		
		// Do the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Airport ap = SystemData.getAirport(rs.getString(1));
			if (ap != null)
				results.add(ap);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}