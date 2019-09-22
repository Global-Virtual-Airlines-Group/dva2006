// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to update Airport and Airline information.
 * @author Luke
 * @version 8.7
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
			prepareStatement("INSERT INTO common.AIRLINES (CODE, NAME, COLOR, ACTIVE, SYNC, HISTORIC) VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setString(1, al.getCode());
			_ps.setString(2, al.getName());
			_ps.setString(3, al.getColor());
			_ps.setBoolean(4, al.getActive());
			_ps.setBoolean(5, al.getScheduleSync());
			_ps.setBoolean(6, al.getHistoric());
			executeUpdate(1);
			
			// Write the alternate codes
			prepareStatement("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (String code : al.getCodes()) {
				if (!code.equals(al.getCode())) {
					_ps.setString(2, code);
					_ps.addBatch();
				}
			}
			
			// Execute update
			executeBatchUpdate(1, 0);
			
			// Write the webapp data
			prepareStatement("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (Iterator<String> i = al.getApplications().iterator(); i.hasNext(); ) {
				_ps.setString(2, i.next());
				_ps.addBatch();
			}
			
			// Write and commit
			executeBatchUpdate(1, al.getApplications().size());
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
			prepareStatementWithoutLimits("DELETE FROM common.AIRLINE_CODES WHERE (CODE=?)");
			_ps.setString(1, oldCode);
			executeUpdate(0);
			
			// Clear the webapp data
			prepareStatementWithoutLimits("DELETE FROM common.APP_AIRLINES WHERE (CODE=?)");
			_ps.setString(1, oldCode);
			executeUpdate(0);
			
			// Write the airline data
			prepareStatement("UPDATE common.AIRLINES SET NAME=?, COLOR=?, ACTIVE=?, CODE=?, SYNC=?, HISTORIC=? WHERE (CODE=?)");
			_ps.setString(1, al.getName());
			_ps.setString(2, al.getColor());
			_ps.setBoolean(3, al.getActive());
			_ps.setString(4, al.getCode());
			_ps.setBoolean(5, al.getScheduleSync());
			_ps.setBoolean(6, al.getHistoric());
			_ps.setString(7, oldCode);
			executeUpdate(1);
			
			// Write the alternate codes
			prepareStatement("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (String code : al.getCodes()) {
				if (!code.equals(al.getCode())) {
					_ps.setString(2, code);
					_ps.addBatch();
				}
			}
			
			executeBatchUpdate(1, 0);
			
			// Write the webapp data
			prepareStatement("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (String code : al.getApplications()) {
				_ps.setString(2, code);
				_ps.addBatch();
			}

			executeBatchUpdate(1, al.getApplications().size());
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
			prepareStatement("INSERT INTO common.AIRPORTS (IATA, ICAO, TZ, NAME, COUNTRY, LATITUDE, LONGITUDE, ADSE, OLDCODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, a.getIATA());
			_ps.setString(2, a.getICAO());
			_ps.setString(3, a.getTZ().getID());
			_ps.setString(4, a.getName());
			_ps.setString(5, a.getCountry().getCode());
			_ps.setDouble(6, a.getLatitude());
			_ps.setDouble(7, a.getLongitude());
			_ps.setBoolean(8, a.getADSE());
			_ps.setString(9, a.getSupercededAirport());
			executeUpdate(1);
			
			// Write superceded airport
			if (a.getSupercededAirport() != null) {
				prepareStatement("UPDATE common.AIRPORTS SET OLDCODE=? WHERE (IATA=?)");
				_ps.setString(1, a.getIATA());
				_ps.setString(2, a.getSupercededAirport());
				executeUpdate(1);
			}

			// Write the airline data
			prepareStatement("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA, APPCODE) VALUES (?, ?, ?)");
			_ps.setString(2, a.getIATA());
			_ps.setString(3, SystemData.get("airline.code"));
			for (String aCode : a.getAirlineCodes()) {
				_ps.setString(1, aCode);
				_ps.addBatch();
			}

			// Execute and commit
			executeBatchUpdate(1, a.getAirlineCodes().size());
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
			prepareStatement("UPDATE common.AIRPORTS SET ICAO=?, TZ=?, NAME=?, LATITUDE=?, LONGITUDE=?, IATA=?, ADSE=?, COUNTRY=?, OLDCODE=? WHERE (IATA=?)");
			_ps.setString(1, a.getICAO());
			_ps.setString(2, a.getTZ().getID());
			_ps.setString(3, a.getName());
			_ps.setDouble(4, a.getLatitude());
			_ps.setDouble(5, a.getLongitude());
			_ps.setString(6, a.getIATA());
			_ps.setBoolean(7, a.getADSE());
			_ps.setString(8, a.getCountry().getCode());
			_ps.setString(9, a.getSupercededAirport());
			_ps.setString(10, oc);
			executeUpdate(1);
			
			// Ensure the superceded airports are interchangeable
			if (a.getSupercededAirport() != null) {
				prepareStatement("UPDATE common.AIRPORTS SET OLDCODE=? WHERE (IATA=?)");
				_ps.setString(2, a.getSupercededAirport());
			} else
				prepareStatement("UPDATE common.AIRPORTS SET OLDCODE=NULL WHERE (OLDCODE=?)");

			_ps.setString(1, a.getIATA());
			executeUpdate(0);
			
			// Clear out the airlines
			prepareStatement("DELETE FROM common.AIRPORT_AIRLINE WHERE (IATA=?) AND (APPCODE=?)");
			_ps.setString(1, oc);
			_ps.setString(2, SystemData.get("airline.code"));
			executeUpdate(0);

			// Write the airline data
			prepareStatement("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA, APPCODE) VALUES (?, ?, ?)");
			_ps.setString(2, a.getIATA());
			_ps.setString(3, SystemData.get("airline.code"));
			for (String aCode : a.getAirlineCodes()) {
				_ps.setString(1, aCode);
				_ps.addBatch();
			}

			// Execute and commit
			executeBatchUpdate(1, a.getAirlineCodes().size());
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
		try {
			prepareStatement("DELETE FROM common.AIRPORTS WHERE (IATA=?)");
			_ps.setString(1, a.getIATA());
			executeUpdate(1);
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
		try {
			prepareStatement("DELETE FROM common.AIRLINES WHERE (CODE=?)");
			_ps.setString(1, a.getCode());
			executeUpdate(1);
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
			prepareStatement("INSERT INTO common.AIRCRAFT (NAME, FULLNAME, FAMILY, ACRANGE, ICAO, IATA, HISTORIC, ETOPS, SEATS, ENGINES, ENGINE_TYPE, CRUISE_SPEED, FUEL_FLOW, BASE_FUEL, TAXI_FUEL, PRI_TANKS, PRI_PCT, "
				+ "SEC_TANKS, SEC_PCT, OTHER_TANKS, MAX_WEIGHT, MAX_TWEIGHT, MAX_LWEIGHT, MAX_ZFW, TO_RWLENGTH, LN_RWLENGTH, SOFT_RWY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, a.getName());
			_ps.setString(2, a.getFullName());
			_ps.setString(3, a.getFamily());
			_ps.setInt(4, a.getRange());
			_ps.setString(5, a.getICAO());
			_ps.setString(6, StringUtils.listConcat(a.getIATA(), ","));
			_ps.setBoolean(7, a.getHistoric());
			_ps.setBoolean(8, a.getETOPS());
			_ps.setInt(9, a.getSeats());
			_ps.setByte(10, a.getEngines());
			_ps.setString(11, a.getEngineType());
			_ps.setInt(12, a.getCruiseSpeed());
			_ps.setInt(13, a.getFuelFlow());
			_ps.setInt(14, a.getBaseFuel());
			_ps.setInt(15, a.getTaxiFuel());
			_ps.setInt(16, a.getTanks(TankType.PRIMARY));
			_ps.setInt(17, a.getPct(TankType.PRIMARY));
			_ps.setInt(18, a.getTanks(TankType.SECONDARY));
			_ps.setInt(19, a.getPct(TankType.SECONDARY));
			_ps.setInt(20, a.getTanks(TankType.OTHER));
			_ps.setInt(21, a.getMaxWeight());
			_ps.setInt(22, a.getMaxTakeoffWeight());
			_ps.setInt(23, a.getMaxLandingWeight());
			_ps.setInt(24, a.getMaxZeroFuelWeight());
			_ps.setInt(25, a.getTakeoffRunwayLength());
			_ps.setInt(26, a.getLandingRunwayLength());
			_ps.setBoolean(27, a.getUseSoftRunways());
			executeUpdate(1);
			
			// Add the webapps
			prepareStatement("INSERT INTO common.AIRCRAFT_AIRLINE (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, a.getName());
			for (AirlineInformation ai : a.getApps()) {
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}
			
			// Execute and commit
			executeBatchUpdate(1, a.getApps().size());
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
			prepareStatement("UPDATE common.AIRCRAFT SET ACRANGE=?, IATA=?, ICAO=?, HISTORIC=?, ENGINES=?, ENGINE_TYPE=?, CRUISE_SPEED=?, FUEL_FLOW=?, BASE_FUEL=?, TAXI_FUEL=?, "
				+ "PRI_TANKS=?, PRI_PCT=?, SEC_TANKS=?, SEC_PCT=?, OTHER_TANKS=?, ETOPS=?, MAX_WEIGHT=?, MAX_TWEIGHT=?, MAX_LWEIGHT=?, MAX_ZFW=?, SEATS=?, TO_RWLENGTH=?, "
				+ "LN_RWLENGTH=?, FULLNAME=?, FAMILY=?, SOFT_RWY=?, NAME=? WHERE (NAME=?)");
			_ps.setInt(1, a.getRange());
			_ps.setString(2, StringUtils.listConcat(a.getIATA(), ",").replace("\r", ""));
			_ps.setString(3, a.getICAO());
			_ps.setBoolean(4, a.getHistoric());
			_ps.setByte(5, a.getEngines());
			_ps.setString(6, a.getEngineType());
			_ps.setInt(7, a.getCruiseSpeed());
			_ps.setInt(8, a.getFuelFlow());
			_ps.setInt(9, a.getBaseFuel());
			_ps.setInt(10, a.getTaxiFuel());
			_ps.setInt(11, a.getTanks(TankType.PRIMARY));
			_ps.setInt(12, a.getPct(TankType.PRIMARY));
			_ps.setInt(13, a.getTanks(TankType.SECONDARY));
			_ps.setInt(14, a.getPct(TankType.SECONDARY));
			_ps.setInt(15, a.getTanks(TankType.OTHER));
			_ps.setBoolean(16, a.getETOPS());
			_ps.setInt(17, a.getMaxWeight());
			_ps.setInt(18, a.getMaxTakeoffWeight());
			_ps.setInt(19, a.getMaxLandingWeight());
			_ps.setInt(20, a.getMaxZeroFuelWeight());
			_ps.setInt(21, a.getSeats());
			_ps.setInt(22, a.getTakeoffRunwayLength());
			_ps.setInt(23, a.getLandingRunwayLength());
			_ps.setString(24, a.getFullName());
			_ps.setString(25, a.getFamily());
			_ps.setBoolean(26, a.getUseSoftRunways());
			_ps.setString(27, a.getName());
			_ps.setString(28, oldName);
			executeUpdate(1);

			// Clean out the webapps
			prepareStatementWithoutLimits("DELETE FROM common.AIRCRAFT_AIRLINE WHERE (NAME=?)");
			_ps.setString(1, a.getName());
			executeUpdate(0);
			
			// Add the webapps
			prepareStatement("INSERT INTO common.AIRCRAFT_AIRLINE (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, a.getName());
			for (AirlineInformation ai : a.getApps()) {
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}
			
			// Execute and commit
			executeBatchUpdate(1, a.getApps().size());
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
			prepareStatementWithoutLimits("DELETE FROM common.AIRPORT_AIRLINE WHERE (APPCODE=?)");
			_ps.setString(1, SystemData.get("airline.code"));
			executeUpdate(0);
			
			prepareStatementWithoutLimits("INSERT INTO common.AIRPORT_AIRLINE (SELECT DISTINCT AIRLINE, AIRPORT_D, ? FROM SCHEDULE)");
			_ps.setString(1, SystemData.get("airline.code"));
			executeUpdate(0);
			
			prepareStatementWithoutLimits("REPLACE INTO common.AIRPORT_AIRLINE (SELECT DISTINCT AIRLINE, AIRPORT_D, ? FROM SCHEDULE)");
			_ps.setString(1, SystemData.get("airline.code"));
			executeUpdate(0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}