// Copyright 2006, 2007, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Aircraft data.
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class GetAircraft extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAircraft(Connection c) {
		super(c);
	}

	/**
	 * Loads a particular aircraft profile.
	 * @param name the aircraft name
	 * @return the Aircraft bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Aircraft get(String name) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.AIRCRAFT WHERE (NAME=?) LIMIT 1");
			_ps.setString(1, name);
			List<Aircraft> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all aircraft profiles.
	 * @return a Collection of Aircraft beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Aircraft> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRCRAFT");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all aircraft used by the current web application.
	 * @return a Collection of Aircraft beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Aircraft> getAircraftTypes() throws DAOException {
		return getAircraftTypes(SystemData.get("airline.code"));
	}

	/**
	 * Returns all aircraft used by a web application.
	 * @param airlineCode the Airline code
	 * @return a Collection of Aircraft beans
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airlineCode is null
	 */
	public Collection<Aircraft> getAircraftTypes(String airlineCode) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRCRAFT A, common.AIRCRAFT_AIRLINE AA WHERE "
					+ "(A.NAME=AA.NAME) AND (AA.AIRLINE=?)");
			_ps.setString(1, airlineCode.toUpperCase());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to process result sets.
	 */
	private List<Aircraft> execute() throws SQLException {
		Map<String, Aircraft> results = new LinkedHashMap<String, Aircraft>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Aircraft a = new Aircraft(rs.getString(1));
				a.setFullName(rs.getString(2));
				a.setRange(rs.getInt(3));
				a.setIATA(StringUtils.split(rs.getString(4), ","));
				a.setHistoric(rs.getBoolean(5));
				a.setETOPS(rs.getBoolean(6));
				a.setSeats(rs.getInt(7));
				a.setEngines(rs.getByte(8));
				a.setEngineType(rs.getString(9));
				a.setCruiseSpeed(rs.getInt(10));
				a.setFuelFlow(rs.getInt(11));
				a.setBaseFuel(rs.getInt(12));
				a.setTaxiFuel(rs.getInt(13));
				a.setTanks(Aircraft.TankType.PRIMARY, rs.getInt(14));
				a.setPct(Aircraft.TankType.PRIMARY, rs.getInt(15));
				a.setTanks(Aircraft.TankType.SECONDARY, rs.getInt(16));
				a.setPct(Aircraft.TankType.SECONDARY, rs.getInt(17));
				a.setTanks(Aircraft.TankType.OTHER, rs.getInt(18));
				a.setMaxWeight(rs.getInt(19));
				a.setMaxTakeoffWeight(rs.getInt(20));
				a.setMaxLandingWeight(rs.getInt(21));
				a.setTakeoffRunwayLength(rs.getInt(22));
				a.setLandingRunwayLength(rs.getInt(23));
				results.put(a.getName(), a);
			}
		}

		_ps.close();

		// Load the webapp data
		prepareStatementWithoutLimits("SELECT * FROM common.AIRCRAFT_AIRLINE");
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				String alCode = rs.getString(2);
				Aircraft a = results.get(rs.getString(1));
				AirlineInformation al = SystemData.getApp(alCode);
				if ((al != null) && (a != null))
					a.addApp(al);
			}
		}

		// Clean up and return
		_ps.close();
		return new ArrayList<Aircraft>(results.values());
	}
}