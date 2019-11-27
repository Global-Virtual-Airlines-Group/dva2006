// Copyright 2006, 2007, 2008, 2011, 2012, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.ETOPS;
import org.deltava.beans.schedule.*;

import org.deltava.util.cache.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Aircraft data.
 * @author Luke
 * @version 9.0
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
		if (a != null) return a;

		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.AIRCRAFT WHERE (NAME=?) LIMIT 1")) {
			ps.setString(1, name);
			return execute(ps).stream().findFirst().orElse(null);
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
		try (PreparedStatement ps = prepare("SELECT * FROM common.AIRCRAFT WHERE (INSTR(IATA, ?) > 0)")) {
			ps.setString(1, iataCode);
			return execute(ps).stream().findFirst().orElse(null);
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
		try (PreparedStatement ps = prepare("SELECT * FROM common.AIRCRAFT")) {
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT A.* FROM common.AIRCRAFT A, common.AIRCRAFT_AIRLINE AA WHERE (A.NAME=AA.NAME) AND (AA.AIRLINE=?)")) {
			ps.setString(1, airlineCode);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT DISTINCT A.* FROM common.AIRCRAFT A, PIREPS PR WHERE (A.NAME=PR.EQTYPE) AND (PR.PILOT_ID=?) ORDER BY A.NAME")) {
			ps.setInt(1, pilotID);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to process result sets.
	 */
	private List<Aircraft> execute(PreparedStatement ps) throws SQLException {
		Map<String, Aircraft> results = new LinkedHashMap<String, Aircraft>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Aircraft a = new Aircraft(rs.getString(1));
				a.setFullName(rs.getString(2));
				a.setFamily(rs.getString(3));
				a.setIATA(StringUtils.split(rs.getString(4), ","));
				a.setICAO(rs.getString(5));
				a.setHistoric(rs.getBoolean(6));
				a.setEngines(rs.getByte(7));
				a.setEngineType(rs.getString(8));
				a.setCruiseSpeed(rs.getInt(9));
				a.setFuelFlow(rs.getInt(10));
				a.setBaseFuel(rs.getInt(11));
				a.setTaxiFuel(rs.getInt(12));
				a.setTanks(TankType.PRIMARY, rs.getInt(13));
				a.setPct(TankType.PRIMARY, rs.getInt(14));
				a.setTanks(TankType.SECONDARY, rs.getInt(15));
				a.setPct(TankType.SECONDARY, rs.getInt(16));
				a.setTanks(TankType.OTHER, rs.getInt(17));
				a.setMaxWeight(rs.getInt(18));
				a.setMaxTakeoffWeight(rs.getInt(19));
				a.setMaxLandingWeight(rs.getInt(20));
				a.setMaxZeroFuelWeight(rs.getInt(21));
				results.put(a.getName(), a);
			}
		}

		// Load the webapp data
		try (PreparedStatement ps2 = prepareWithoutLimits("SELECT * FROM common.AIRCRAFT_AIRLINE")) {
			try (ResultSet rs = ps2.executeQuery()) {
				while (rs.next()) {
					Aircraft a = results.get(rs.getString(1));
					if (a == null)
						continue;
					AircraftPolicyOptions opts = new AircraftPolicyOptions(a.getName(), rs.getString(2));
					opts.setRange(rs.getInt(3));
					opts.setETOPS(ETOPS.fromCode(rs.getInt(4)));
					opts.setSeats(rs.getInt(5));
					opts.setTakeoffRunwayLength(rs.getInt(6));
					opts.setLandingRunwayLength(rs.getInt(7));
					opts.setUseSoftRunways(rs.getBoolean(8));
					a.addApp(opts);
				}
			}
		}

		// Add to cache and return
		List<Aircraft> ac = new ArrayList<Aircraft>(results.values());
		ac.forEach(_cache::add);
		return ac;
	}
}