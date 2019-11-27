// Copyright 2005, 2006, 2007, 2011, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airline;

/**
 * A Data Access Object to load Airline codes and names.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("SELECT * FROM common.AIRLINES ORDER BY CODE")) {
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT * FROM common.AIRLINES WHERE (ACTIVE=?) ORDER BY CODE")) {
			ps.setBoolean(1, true);
			return execute(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.AIRLINES WHERE (CODE=?) LIMIT 1")) {
			ps.setString(1, code.toUpperCase());

			// Execute the query, if nothing matches return null
			Map<String, Airline> results = execute(ps);
			return results.isEmpty() ? null : results.get(code.toUpperCase());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse Airline result sets.
	 */
	private Map<String, Airline> execute(PreparedStatement ps) throws SQLException {
		Map<String, Airline> results = new LinkedHashMap<String, Airline>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Airline a = new Airline(rs.getString(1), rs.getString(2));
				a.setColor(rs.getString(3));
				a.setActive(rs.getBoolean(4));
				a.setScheduleSync(rs.getBoolean(5));
				a.setHistoric(rs.getBoolean(6));
				results.put(a.getCode(), a);
			}
		}

		// Load alternate codes
		try (PreparedStatement ps2 = prepareWithoutLimits("SELECT * FROM common.AIRLINE_CODES")) {
			try (ResultSet rs = ps2.executeQuery()) {
				while (rs.next()) {
					Airline a = results.get(rs.getString(1).trim());
					if (a != null)
						a.addCode(rs.getString(2));
				}
			}
		}
		
		// Load web app information
		try (PreparedStatement ps2 = prepareWithoutLimits("SELECT UCASE(CODE), UCASE(APPCODE) FROM common.APP_AIRLINES")) {
			try (ResultSet rs = ps2.executeQuery()) {
				while (rs.next()) {
					Airline a = results.get(rs.getString(1).trim());
					if (a != null)
						a.addApp(rs.getString(2));
				}
			}
		}

		return results;
	}
}