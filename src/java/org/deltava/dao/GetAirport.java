// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.navdata.NavigationDataBean;

/**
 * A Data Access Object to load Airport data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAirport extends DAO {

	private static final Logger log = Logger.getLogger(GetAirport.class);

	/**
	 * Creates the DAO with a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetAirport(Connection c) {
		super(c);
	}

	/**
	 * Returns an airport object by its IATA or ICAO code.
	 * @param code the airport IATA or ICAO code
	 * @return an Airport object matching the requested code, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public Airport get(String code) throws DAOException {

		// Init the prepared statement in such a way that we can search for ICAO or IATA
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM common.AIRPORTS WHERE ((ICAO=?) OR (IATA=?))");
			_ps.setString(1, code.toUpperCase());
			_ps.setString(2, code.toUpperCase());

			ResultSet rs = _ps.executeQuery();
			if (!rs.next())
				return null;

			// Create the airport object
			Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
			a.setTZ(rs.getString(3));
			a.setLocation(rs.getDouble(5), rs.getDouble(6));

			// Close JDBC resources
			rs.close();
			_ps.close();

			// Init the prepared statement to pull in altitude data
			prepareStatement("SELECT ALTITUDE FROM common.NAVDATA WHERE (CODE=?) AND (ITEMTYPE=?)");
			_ps.setString(1, a.getICAO());
			_ps.setInt(2, NavigationDataBean.AIRPORT);

			// Do the query
			rs = _ps.executeQuery();
			if (rs.next())
				a.setAltitude(rs.getInt(1));
			
			// Close JDBC resources
			rs.close();
			_ps.close();

			// Init the prepared statement to pull in the airline data
			prepareStatementWithoutLimits("SELECT CODE FROM common.AIRPORT_AIRLINE WHERE (IATA=?)");
			_ps.setString(1, a.getIATA());

			// Iterate through the results
			rs = _ps.executeQuery();
			while (rs.next())
				a.addAirlineCode(rs.getString(1));

			// Close and return the airport
			rs.close();
			_ps.close();
			return a;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all airports served by a particular airline.
	 * @param al the Airline to query with
	 * @return a List of Airport objects
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if al is null
	 */
	public Collection<Airport> getByAirline(Airline al) throws DAOException {
		try {
			prepareStatement("SELECT A.* FROM common.AIRPORTS A, common.AIRPORT_AIRLINE AA WHERE "
					+ "(A.IATA=AA.IATA) AND (AA.CODE=?) ORDER BY A.IATA");
			_ps.setString(1, al.getCode());

			// Execute the query
			List<Airport> results = new ArrayList<Airport>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
				a.setTZ(rs.getString(3));
				a.setLocation(rs.getDouble(5), rs.getDouble(6));

				// Add to the results
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
	 * Returns all airports.
	 * @return a Map of Airports, keyed by IATA/ICAO codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Airport> getAll() throws DAOException {
		Map<String, Airport> results = new HashMap<String, Airport>();
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.AIRPORTS");
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			while (rs.next()) {
				Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
				a.setTZ(rs.getString(3));
				a.setLocation(rs.getDouble(5), rs.getDouble(6));

				// Save in the map
				results.put(a.getIATA(), a);
				results.put(a.getICAO(), a);
			}

			// Clean up the first query
			rs.close();
			_ps.close();
			
			// Load the altitudes for the airports
			loadAltitude(results);

			// Load the airlines for each airport and execute the query
			prepareStatementWithoutLimits("SELECT * FROM common.AIRPORT_AIRLINE");
			rs = _ps.executeQuery();

			// Iterate through the results
			while (rs.next()) {
				String code = rs.getString(2);
				Airport a = results.get(code);
				if (a != null) {
					a.addAirlineCode(rs.getString(1));
				} else {
					log.warn("Cannot find Airport " + code);
				}
			}

			// Clean up the second query and return results
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load airport altitudes.
	 */
	private void loadAltitude(Map<String, Airport> airports) throws SQLException {
		if (airports.isEmpty())
			return;
		
		// Convert the airports into a set
		Set<Airport> apSet = new HashSet<Airport>(airports.values());
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT CODE, ALTITUDE FROM common.NAVDATA WHERE (ITEMTYPE=?) "
				+ "AND CODE IN (");
		for (Iterator i = apSet.iterator(); i.hasNext(); ) {
			Airport a = (Airport) i.next();
			sqlBuf.append('\"');
			sqlBuf.append(a.getICAO());
			sqlBuf.append(i.hasNext() ? "\"," : "\")");
		}

		// Prepare the SQL statement
		prepareStatementWithoutLimits(sqlBuf.toString());
		_ps.setInt(1, NavigationDataBean.AIRPORT);
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Airport a = airports.get(rs.getString(1));
			if (a != null)
				a.setAltitude(rs.getInt(2));
		}
		
		// Clean up after ourselves
		rs.close();
		_ps.close();
	}
}