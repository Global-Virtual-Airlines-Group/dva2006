// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019, 2020, 2021, 2024, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to update Aircraft information.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class SetAircraft extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAircraft(Connection c) {
		super(c);
	}

	/**
	 * Creates a new aircraft profile.
	 * @param a the Aircraft bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Aircraft a) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRCRAFT (NAME, FULLNAME, FAMILY, ICAO, IATA, HISTORIC, ACADEMY_ONLY, ENGINES, ENGINE_TYPE, CRUISE_SPEED, FUEL_FLOW, "
				+ "BASE_FUEL, TAXI_FUEL, PRI_TANKS, PRI_PCT, SEC_TANKS, SEC_PCT, OTHER_TANKS, MAX_WEIGHT, MAX_TWEIGHT, MAX_LWEIGHT, MAX_ZFW) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, a.getName());
				ps.setString(2, a.getFullName());
				ps.setString(3, a.getFamily());
				ps.setString(4, a.getICAO());
				ps.setString(5, StringUtils.listConcat(a.getIATA(), ","));
				ps.setBoolean(6, a.getHistoric());
				ps.setBoolean(7, a.getAcademyOnly());
				ps.setByte(8, a.getEngines());
				ps.setString(9, a.getEngineType());
				ps.setInt(10, a.getCruiseSpeed());
				ps.setInt(11, a.getFuelFlow());
				ps.setInt(12, a.getBaseFuel());
				ps.setInt(13, a.getTaxiFuel());
				ps.setInt(14, a.getTanks(TankType.PRIMARY));
				ps.setInt(15, a.getPct(TankType.PRIMARY));
				ps.setInt(16, a.getTanks(TankType.SECONDARY));
				ps.setInt(17, a.getPct(TankType.SECONDARY));
				ps.setInt(18, a.getTanks(TankType.OTHER));
				ps.setInt(19, a.getMaxWeight());
				ps.setInt(20, a.getMaxTakeoffWeight());
				ps.setInt(21, a.getMaxLandingWeight());
				ps.setInt(22, a.getMaxZeroFuelWeight());
				executeUpdate(ps, 1);
			}
			
			writeAppData(a);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates the aircraft profile.
	 * @param a the Aircraft bean
	 * @param oldName the old aircraft name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Aircraft a, String oldName) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepare("UPDATE common.AIRCRAFT SET IATA=?, ICAO=?, HISTORIC=?, ACADEMY_ONLY=?, ENGINES=?, ENGINE_TYPE=?, CRUISE_SPEED=?, FUEL_FLOW=?, BASE_FUEL=?, TAXI_FUEL=?, "
				+ "PRI_TANKS=?, PRI_PCT=?, SEC_TANKS=?, SEC_PCT=?, OTHER_TANKS=?, MAX_WEIGHT=?, MAX_TWEIGHT=?, MAX_LWEIGHT=?, MAX_ZFW=?, FULLNAME=?, FAMILY=?, NAME=? WHERE (NAME=?)")) {
				ps.setString(1, StringUtils.listConcat(a.getIATA(), ",").replace("\r", ""));
				ps.setString(2, a.getICAO());
				ps.setBoolean(3, a.getHistoric());
				ps.setBoolean(4, a.getAcademyOnly());
				ps.setByte(5, a.getEngines());
				ps.setString(6, a.getEngineType());
				ps.setInt(7, a.getCruiseSpeed());
				ps.setInt(8, a.getFuelFlow());
				ps.setInt(9, a.getBaseFuel());
				ps.setInt(10, a.getTaxiFuel());
				ps.setInt(11, a.getTanks(TankType.PRIMARY));
				ps.setInt(12, a.getPct(TankType.PRIMARY));
				ps.setInt(13, a.getTanks(TankType.SECONDARY));
				ps.setInt(14, a.getPct(TankType.SECONDARY));
				ps.setInt(15, a.getTanks(TankType.OTHER));
				ps.setInt(16, a.getMaxWeight());
				ps.setInt(17, a.getMaxTakeoffWeight());
				ps.setInt(18, a.getMaxLandingWeight());
				ps.setInt(19, a.getMaxZeroFuelWeight());
				ps.setString(20, a.getFullName());
				ps.setString(21, a.getFamily());
				ps.setString(22, a.getName());
				ps.setString(23, oldName);
				executeUpdate(ps, 1);
			}

			// Clean out the webapps
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRCRAFT_AIRLINE WHERE (NAME=?)")) {
				ps.setString(1, a.getName());
				executeUpdate(ps, 0);
			}
			
			// Add the webapps
			writeAppData(a);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("AircraftInfo", oldName);
		}
	}
	
	/*
	 * Helper method to write AircraftPolicyOptions to the database.
	 */
	private void writeAppData(Aircraft a) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRCRAFT_AIRLINE (NAME, AIRLINE, ACRANGE, ETOPS, SEATS, TO_RWLENGTH, LN_RWLENGTH, SOFT_RWY) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setString(1, a.getName());
			for (String appCode : a.getApps()) {
				AircraftPolicyOptions opts = a.getOptions(appCode);
				ps.setString(2, appCode);
				ps.setInt(3, opts.getRange());
				ps.setInt(4, opts.getETOPS().ordinal());
				ps.setInt(5, opts.getSeats());
				ps.setInt(6, opts.getTakeoffRunwayLength());
				ps.setInt(7, opts.getLandingRunwayLength());
				ps.setBoolean(8, opts.getUseSoftRunways());
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, a.getApps().size());
		}
	}
}