// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.schedule.*;
import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Airport data.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetAirport extends DAO {

	private String _appCode;

	/**
	 * Creates the DAO with a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetAirport(Connection c) {
		super(c);
		setAppCode(SystemData.get("airline.code"));
	}
	
	/**
	 * Overrides the application code (for use by the ACARS server).
	 * @param code the new application code
	 * @throws NullPointerException if code is null
	 */
	public void setAppCode(String code) {
		_appCode = code.toUpperCase();
	}

	/**
	 * Returns an airport object by its IATA or ICAO code.
	 * @param code the airport IATA or ICAO code
	 * @return an Airport object matching the requested code, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public Airport get(String code) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT A.*, ND.ALTITUDE, ND.REGION FROM common.AIRPORTS A "
					+ "LEFT JOIN common.NAVDATA ND ON (ND.CODE=A.ICAO) AND (ND.ITEMTYPE=?) WHERE "
					+ "((A.ICAO=?) OR (A.IATA=?)) LIMIT 1");
			_ps.setInt(1, NavigationDataBean.AIRPORT);
			_ps.setString(2, code.toUpperCase());
			_ps.setString(3, code.toUpperCase());

			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				return null;
			}

			// Create the airport object
			Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
			a.setTZ(rs.getString(3));
			a.setLocation(rs.getDouble(5), rs.getDouble(6));
			a.setADSE(rs.getBoolean(7));
			a.setAltitude(rs.getInt(8));
			a.setRegion(rs.getString(9));

			// Close JDBC resources
			rs.close();
			_ps.close();

			// Init the prepared statement to pull in the airline data
			prepareStatementWithoutLimits("SELECT CODE FROM common.AIRPORT_AIRLINE WHERE (IATA=?) AND (APPCODE=?)");
			_ps.setString(1, a.getIATA());
			_ps.setString(2, _appCode);

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
	 * @param sortBy the SORT BY column
	 * @return a List of Airport objects
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if al is null
	 */
	public Collection<Airport> getByAirline(Airline al, String sortBy) throws DAOException {
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT A.*, ND.ALTITUDE, ND.REGION FROM common.AIRPORT_AIRLINE AA, "
				+ "common.AIRPORTS A LEFT JOIN common.NAVDATA ND ON (ND.CODE=A.ICAO) AND (ND.ITEMTYPE=?) ");
		if (al != null)
			sqlBuf.append("WHERE (A.IATA=AA.IATA) AND (AA.CODE=?) AND (AA.APPCODE=?)");
		sqlBuf.append(" ORDER BY A.");
		sqlBuf.append(sortBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, NavigationDataBean.AIRPORT);
			if (al != null) {
				_ps.setString(2, al.getCode());
				_ps.setString(3, _appCode);
			}

			// Execute the query
			List<Airport> results = new ArrayList<Airport>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
				a.setTZ(rs.getString(3));
				a.setLocation(rs.getDouble(5), rs.getDouble(6));
				a.setADSE(rs.getBoolean(7));
				a.setAltitude(rs.getInt(8));
				a.setRegion(rs.getString(9));
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
	 * Returns all airports visited by a particular Pilot.
	 * @param id the Pilot's database ID
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getByPilot(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT AIRPORT_D, AIRPORT_A FROM PIREPS P WHERE (PILOT_ID=?) ORDER BY ID");
			_ps.setInt(1, id);
			
			// Execute the query
			Collection<Airport> results = new LinkedHashSet<Airport>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				results.add(SystemData.getAirport(rs.getString(1)));
				results.add(SystemData.getAirport(rs.getString(2)));
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
	 * Returns all Airports with Terminal Routes.
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getWithTerminalRoutes() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT ICAO FROM common.SID_STAR");
			Collection<Airport> results = new LinkedHashSet<Airport>();
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
	 * Returns all airports.
	 * @return a Map of Airports, keyed by IATA/ICAO codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Airport> getAll() throws DAOException {
		Map<String, Airport> results = new HashMap<String, Airport>();
		try {
			prepareStatementWithoutLimits("SELECT A.*, ND.ALTITUDE, ND.REGION FROM common.AIRPORTS A "
					+ "LEFT JOIN common.NAVDATA ND ON (ND.CODE=A.ICAO) AND (ND.ITEMTYPE=?)");
			_ps.setInt(1, NavigationDataBean.AIRPORT);
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			while (rs.next()) {
				Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
				a.setTZ(rs.getString(3));
				a.setLocation(rs.getDouble(5), rs.getDouble(6));
				a.setADSE(rs.getBoolean(7));
				a.setAltitude(rs.getInt(8));
				a.setRegion(rs.getString(9));

				// Save in the map
				results.put(a.getIATA(), a);
				results.put(a.getICAO(), a);
			}

			// Clean up the first query
			rs.close();
			_ps.close();
			
			// Load the airlines for each airport and execute the query
			if (!"ALL".equals(_appCode)) {
				prepareStatementWithoutLimits("SELECT * FROM common.AIRPORT_AIRLINE WHERE (APPCODE=?)");
				_ps.setString(1, _appCode);
			} else
				prepareStatementWithoutLimits("SELECT * FROM common.AIRPORT_AIRLINE");
			
			// Iterate through the results
			rs = _ps.executeQuery();
			while (rs.next()) {
				String code = rs.getString(2);
				Airport a = results.get(code);
				if (a != null)
					a.addAirlineCode(rs.getString(1));
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
	 * Returns the ICAO code for a particular airport from the DAFIF database.
	 * @param iata the IATA code
	 * @return the ICAO code, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getICAO(String iata) throws DAOException {
		if ((iata == null) || (iata.length() == 4))
			return iata;
		
		try {
			prepareStatementWithoutLimits("SELECT ICAO FROM common.AIRPORT_CODES WHERE (IATA=?) LIMIT 1");
			_ps.setString(1, iata.toUpperCase());
			
			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			String code = rs.next() ? rs.getString(1) : null;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return code;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}