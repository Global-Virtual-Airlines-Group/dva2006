// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airline;

/**
 * A Data Access Object to load Airline codes and names.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAirline extends DAO {

	/**
	 * Creates the DAO with a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetAirline(Connection c) {
		super(c);
	}

	/**
	 * Returns all Airlines from the database.
	 * @return a Map of Airline objects, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Airline> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRLINES ORDER BY CODE");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Airlines from the database.
	 * @return a Map of Airline objects where isActive() == TRUE with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Airline> getActive() throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRLINES WHERE (ACTIVE=?) ORDER BY CODE");
			_ps.setBoolean(1, true);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns an Airline object.
	 * @param code the Airline code to get
	 * @return the Airline, or null if the code was not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public Airline get(String code) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRLINES WHERE (CODE=?)");
			_ps.setString(1, code.toUpperCase());
			_ps.setMaxRows(1);

			// Execute the query, if nothing matches return null
			Map<String, Airline> results = execute();
			return results.isEmpty() ? null : results.get(code.toUpperCase());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Airline result sets.
	 */
	private Map<String, Airline> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		Map<String, Airline> results = new LinkedHashMap<String, Airline>();
		while (rs.next()) {
			Airline a = new Airline(rs.getString(1), rs.getString(2));
			a.setColor(rs.getString(3));
			a.setActive(rs.getBoolean(4));
			results.put(a.getCode(), a);
		}

		// Clean up
		rs.close();
		_ps.close();
		
		// Load alternate codes
		prepareStatementWithoutLimits("SELECT * FROM common.AIRLINE_CODES");
		rs = _ps.executeQuery();
		while (rs.next()) {
			Airline a = results.get(rs.getString(1).trim());
			if (a != null)
				a.addCode(rs.getString(2));
		}
		
		// Clean up
		rs.close();
		_ps.close();

		// Load web app information
		prepareStatementWithoutLimits("SELECT UCASE(CODE), UCASE(APPCODE) FROM common.APP_AIRLINES");
		rs = _ps.executeQuery();
		while (rs.next()) {
			Airline a = results.get(rs.getString(1).trim());
			if (a != null)
				a.addApp(rs.getString(2));
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}