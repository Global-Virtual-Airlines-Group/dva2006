// Copyright 2006, 2007, 2008, 2011, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Aircraft data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetAircraft extends DAO {
	
	private static final Cache<Aircraft> _cache = CacheManager.get(Aircraft.class, "AircraftInfo");

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
		Aircraft a = _cache.get(name);
		if (a != null)
			return a;
		
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
	 * Loads a particular aircraft by IATA code.
	 * @param iataCode the IATA code
	 * @return the Aircraft bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Aircraft getIATA(String iataCode) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRCRAFT WHERE (INSTR(IATA, ?) > 0)");
			_ps.setString(1, iataCode);
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
	public List<Aircraft> getAll() throws DAOException {
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
	 */
	public Collection<Aircraft> getAircraftTypes(String airlineCode) throws DAOException {
		try {
			prepareStatement("SELECT A.* FROM common.AIRCRAFT A, common.AIRCRAFT_AIRLINE AA WHERE (A.NAME=AA.NAME) AND (AA.AIRLINE=?)");
			_ps.setString(1, airlineCode);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns aircraft used by a particular pilot.
	 * @param pilotID the Pilot database ID
	 * @return a Collection of Aircraft beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Aircraft> getAircraftTypes(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT A.* FROM common.AIRCRAFT A, PIREPS PR WHERE (A.NAME=PR.EQTYPE) AND (PR.PILOT_ID=?) ORDER BY A.NAME");
			_ps.setInt(1, pilotID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to process result sets.
	 */
	private List<Aircraft> execute() throws SQLException {
		Map<String, Aircraft> results = new LinkedHashMap<String, Aircraft>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Aircraft a = new Aircraft(rs.getString(1));
				a.setFullName(rs.getString(2));
				a.setFamily(rs.getString(3));
				a.setRange(rs.getInt(4));
				a.setIATA(StringUtils.split(rs.getString(5), ","));
				a.setHistoric(rs.getBoolean(6));
				a.setETOPS(rs.getBoolean(7));
				a.setSeats(rs.getInt(8));
				a.setEngines(rs.getByte(9));
				a.setEngineType(rs.getString(10));
				a.setCruiseSpeed(rs.getInt(11));
				a.setFuelFlow(rs.getInt(12));
				a.setBaseFuel(rs.getInt(13));
				a.setTaxiFuel(rs.getInt(14));
				a.setTanks(Aircraft.TankType.PRIMARY, rs.getInt(15));
				a.setPct(Aircraft.TankType.PRIMARY, rs.getInt(16));
				a.setTanks(Aircraft.TankType.SECONDARY, rs.getInt(17));
				a.setPct(Aircraft.TankType.SECONDARY, rs.getInt(18));
				a.setTanks(Aircraft.TankType.OTHER, rs.getInt(19));
				a.setMaxWeight(rs.getInt(20));
				a.setMaxTakeoffWeight(rs.getInt(21));
				a.setMaxLandingWeight(rs.getInt(22));
				a.setMaxZeroFuelWeight(rs.getInt(23));
				a.setTakeoffRunwayLength(rs.getInt(24));
				a.setLandingRunwayLength(rs.getInt(25));
				a.setUseSoftRunways(rs.getBoolean(26));
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

		_ps.close();
		
		// Add to cache and return
		List<Aircraft> ac = new ArrayList<Aircraft>(results.values());
		for (Aircraft a : ac)
			_cache.add(a);
		
		return ac;
	}
}