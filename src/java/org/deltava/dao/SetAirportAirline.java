// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to update Airport and Airline information.
 * @author Luke
 * @version 9.0
 * @since 8.0
 */

public class SetAirportAirline extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAirportAirline(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Airline to the Schedule.
	 * @param al the Airline bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Airline al) throws DAOException {
		try {
			startTransaction();
			
			// Write the airline data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINES (CODE, NAME, COLOR, ACTIVE, SYNC, HISTORIC) VALUES (?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, al.getCode());
				ps.setString(2, al.getName());
				ps.setString(3, al.getColor());
				ps.setBoolean(4, al.getActive());
				ps.setBoolean(5, al.getScheduleSync());
				ps.setBoolean(6, al.getHistoric());
				executeUpdate(ps, 1);
			}
			
			// Write the alternate codes
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?, ?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getCodes()) {
					if (!code.equals(al.getCode())) {
						ps.setString(2, code);
						ps.addBatch();
					}
				}
			
				executeUpdate(ps, 1, 0);
			}
			
			// Write the webapp data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?, ?)")) {
				ps.setString(1, al.getCode());
				for (Iterator<String> i = al.getApplications().iterator(); i.hasNext(); ) {
					ps.setString(2, i.next());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, al.getApplications().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Airline in the Schedule.
	 * @param al the Airline bean
	 * @param oldCode the old airline code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Airline al, String oldCode) throws DAOException {
		try {
			startTransaction();
			
			// Clear the alternate code data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRLINE_CODES WHERE (CODE=?)")) {
				ps.setString(1, oldCode);
				executeUpdate(ps, 0);
			}
			
			// Clear the webapp data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.APP_AIRLINES WHERE (CODE=?)")) {
				ps.setString(1, oldCode);
				executeUpdate(ps, 0);
			}
			
			// Write the airline data
			try (PreparedStatement ps = prepare("UPDATE common.AIRLINES SET NAME=?, COLOR=?, ACTIVE=?, CODE=?, SYNC=?, HISTORIC=? WHERE (CODE=?)")) {
				ps.setString(1, al.getName());
				ps.setString(2, al.getColor());
				ps.setBoolean(3, al.getActive());
				ps.setString(4, al.getCode());
				ps.setBoolean(5, al.getScheduleSync());
				ps.setBoolean(6, al.getHistoric());
				ps.setString(7, oldCode);
				executeUpdate(ps, 1);
			}
			
			// Write the alternate codes
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?, ?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getCodes()) {
					if (!code.equals(al.getCode())) {
						ps.setString(2, code);
						ps.addBatch();
					}
				}
			
				executeUpdate(ps, 1, 0);
			}
			
			// Write the webapp data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?, ?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getApplications()) {
					ps.setString(2, code);
					ps.addBatch();
				}

				executeUpdate(ps, 1, al.getApplications().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Airport to the Schedule.
	 * @param a the Airport bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Airport a) throws DAOException {
		try {
			startTransaction();

			// Write the airport data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRPORTS (IATA, ICAO, TZ, NAME, COUNTRY, LATITUDE, LONGITUDE, ADSE, OLDCODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, a.getIATA());
				ps.setString(2, a.getICAO());
				ps.setString(3, a.getTZ().getID());
				ps.setString(4, a.getName());
				ps.setString(5, a.getCountry().getCode());
				ps.setDouble(6, a.getLatitude());
				ps.setDouble(7, a.getLongitude());
				ps.setBoolean(8, a.getASDE());
				ps.setString(9, a.getSupercededAirport());
				executeUpdate(ps, 1);
			}
			
			// Write superceded airport
			if (a.getSupercededAirport() != null) {
				try (PreparedStatement ps = prepare("UPDATE common.AIRPORTS SET OLDCODE=? WHERE (IATA=?)")) {
					ps.setString(1, a.getIATA());
					ps.setString(2, a.getSupercededAirport());
					executeUpdate(ps, 1);
				}
			}

			// Write the airline data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA, APPCODE) VALUES (?, ?, ?)")) {
				ps.setString(2, a.getIATA());
				ps.setString(3, SystemData.get("airline.code"));
				for (String aCode : a.getAirlineCodes()) {
					ps.setString(1, aCode);
					ps.addBatch();
				}

				executeUpdate(ps, 1, a.getAirlineCodes().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Airport in the Schedule.
	 * @param a the Airport bean
	 * @param oldCode the old IATA code, or null if no change
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Airport a, String oldCode) throws DAOException {
		String oc = (oldCode == null) ? a.getIATA() : oldCode;
		try {
			startTransaction();

			// Update the airport data
			try (PreparedStatement ps = prepare("UPDATE common.AIRPORTS SET ICAO=?, TZ=?, NAME=?, LATITUDE=?, LONGITUDE=?, IATA=?, ADSE=?, COUNTRY=?, OLDCODE=? WHERE (IATA=?)")) {
				ps.setString(1, a.getICAO());
				ps.setString(2, a.getTZ().getID());
				ps.setString(3, a.getName());
				ps.setDouble(4, a.getLatitude());
				ps.setDouble(5, a.getLongitude());
				ps.setString(6, a.getIATA());
				ps.setBoolean(7, a.getASDE());
				ps.setString(8, a.getCountry().getCode());
				ps.setString(9, a.getSupercededAirport());
				ps.setString(10, oc);
				executeUpdate(ps, 1);
			}
			
			// Ensure the superceded airports are interchangeable
			if (a.getSupercededAirport() != null) {
				try (PreparedStatement ps = prepare("UPDATE common.AIRPORTS SET OLDCODE=? WHERE (IATA=?)")) {
					ps.setString(1, a.getIATA());	
					ps.setString(2, a.getSupercededAirport());
					executeUpdate(ps, 0);
				}
			} else {
				try (PreparedStatement ps = prepare("UPDATE common.AIRPORTS SET OLDCODE=NULL WHERE (OLDCODE=?)")) {
					ps.setString(1, a.getIATA());
					executeUpdate(ps, 0);
				}
			}
			
			// Clear out the airlines
			try (PreparedStatement ps = prepare("DELETE FROM common.AIRPORT_AIRLINE WHERE (IATA=?) AND (APPCODE=?)")) {
				ps.setString(1, oc);
				ps.setString(2, SystemData.get("airline.code"));
				executeUpdate(ps, 0);
			}

			// Write the airline data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA, APPCODE) VALUES (?, ?, ?)")) {
				ps.setString(2, a.getIATA());
				ps.setString(3, SystemData.get("airline.code"));
				for (String aCode : a.getAirlineCodes()) {
					ps.setString(1, aCode);
					ps.addBatch();
				}

				executeUpdate(ps, 1, a.getAirlineCodes().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Airport from the Schedule. This operation may fail if there are any Flight Reports or Events that
	 * reference this Airport. In such a case, it is best to remove all Airlines from the Airport.
	 * @param a the Airport bean
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if a is null
	 */
	public void delete(Airport a) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.AIRPORTS WHERE (IATA=?)")) {
			ps.setString(1, a.getIATA());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Airline from the Schedule. This operation may fail if there are any Flight Reports that reference
	 * this Airline. In such a case, it is best to disable the Airline.
	 * @param a the Airline bean
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if a is null
	 */
	public void delete(Airline a) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.AIRLINES WHERE (CODE=?)")) {
			ps.setString(1, a.getCode());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
	
	/**
	 * Regenerates the mapping of airports to airlines.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void remapAirportAirlines() throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRPORT_AIRLINE WHERE (APPCODE=?)")) {
				ps.setString(1, SystemData.get("airline.code"));
				executeUpdate(ps, 0);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRPORT_AIRLINE (SELECT DISTINCT AIRLINE, AIRPORT_D, ? FROM SCHEDULE)")) {
				ps.setString(1, SystemData.get("airline.code"));
				executeUpdate(ps, 0);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.AIRPORT_AIRLINE (SELECT DISTINCT AIRLINE, AIRPORT_D, ? FROM SCHEDULE)")) {
				ps.setString(1, SystemData.get("airline.code"));
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
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