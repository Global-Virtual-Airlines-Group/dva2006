// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to update the Flight Schedule.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class SetSchedule extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetSchedule(Connection c) {
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
			prepareStatement("INSERT INTO common.AIRLINES (CODE, NAME, COLOR, ACTIVE) VALUES (?, ?, ?, ?)");
			_ps.setString(1, al.getCode());
			_ps.setString(2, al.getName());
			_ps.setString(3, al.getColor());
			_ps.setBoolean(4, al.getActive());
			executeUpdate(1);
			
			// Write the alternate codes
			prepareStatement("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (Iterator<String> i = al.getCodes().iterator(); i.hasNext(); ) {
				String code = i.next();
				if (!code.equals(al.getCode())) {
					_ps.setString(2, code);
					_ps.addBatch();
				}
			}
			
			// Execute update
			_ps.executeBatch();
			_ps.close();
			
			// Write the webapp data
			prepareStatement("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (Iterator<String> i = al.getApplications().iterator(); i.hasNext(); ) {
				_ps.setString(2, i.next());
				_ps.addBatch();
			}
			
			// Write and commit
			_ps.executeBatch();
			_ps.close();
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
			prepareStatement("UPDATE common.AIRLINES SET NAME=?, COLOR=?, ACTIVE=?, CODE=? WHERE (CODE=?)");
			_ps.setString(1, al.getName());
			_ps.setString(2, al.getColor());
			_ps.setBoolean(3, al.getActive());
			_ps.setString(4, al.getCode());
			_ps.setString(5, oldCode);
			executeUpdate(1);
			
			// Write the alternate codes
			prepareStatement("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (Iterator<String> i = al.getCodes().iterator(); i.hasNext(); ) {
				String code = i.next();
				if (!code.equals(al.getCode())) {
					_ps.setString(2, code);
					_ps.addBatch();
				}
			}
			
			// Execute update
			_ps.executeBatch();
			_ps.close();
			
			// Write the webapp data
			prepareStatement("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?, ?)");
			_ps.setString(1, al.getCode());
			for (Iterator<String> i = al.getApplications().iterator(); i.hasNext(); ) {
				_ps.setString(2, i.next());
				_ps.addBatch();
			}

			// Write and commit
			_ps.executeBatch();
			_ps.close();
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
			prepareStatement("INSERT INTO common.AIRPORTS (IATA, ICAO, TZ, NAME, LATITUDE, LONGITUDE, ADSE) "
					+ "VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setString(1, a.getIATA());
			_ps.setString(2, a.getICAO());
			_ps.setString(3, a.getTZ().getID());
			_ps.setString(4, a.getName());
			_ps.setDouble(5, a.getLatitude());
			_ps.setDouble(6, a.getLongitude());
			_ps.setBoolean(7, a.getADSE());
			executeUpdate(1);

			// Write the airline data
			prepareStatement("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA, APPCODE) VALUES (?, ?, ?)");
			_ps.setString(2, a.getIATA());
			_ps.setString(3, SystemData.get("airline.code"));
			for (Iterator<String> i = a.getAirlineCodes().iterator(); i.hasNext();) {
				String aCode = i.next();
				_ps.setString(1, aCode);
				_ps.addBatch();
			}

			// Execute and commit
			_ps.executeBatch();
			_ps.close();
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
		if (oldCode == null)
			oldCode = a.getIATA();
		
		try {
			startTransaction();

			// Update the airport data
			prepareStatement("UPDATE common.AIRPORTS SET ICAO=?, TZ=?, NAME=?, LATITUDE=?, LONGITUDE=?, "
					+ "IATA=?, ADSE=? WHERE (IATA=?)");
			_ps.setString(1, a.getICAO());
			_ps.setString(2, a.getTZ().getID());
			_ps.setString(3, a.getName());
			_ps.setDouble(4, a.getLatitude());
			_ps.setDouble(5, a.getLongitude());
			_ps.setString(6, a.getIATA());
			_ps.setBoolean(7, a.getADSE());
			_ps.setString(8, oldCode);
			executeUpdate(1);

			// Clear out the airlines
			prepareStatement("DELETE FROM common.AIRPORT_AIRLINE WHERE (IATA=?) AND (APPCODE=?)");
			_ps.setString(1, oldCode);
			_ps.setString(2, SystemData.get("airline.code"));
			executeUpdate(0);

			// Write the airline data
			prepareStatement("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA, APPCODE) VALUES (?, ?, ?)");
			_ps.setString(2, a.getIATA());
			_ps.setString(3, SystemData.get("airline.code"));
			for (Iterator<String> i = a.getAirlineCodes().iterator(); i.hasNext();) {
				String aCode = i.next();
				_ps.setString(1, aCode);
				_ps.addBatch();
			}

			// Execute and commit
			_ps.executeBatch();
			_ps.close();
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
	 * Adds an entry to the Flight Schedule.
	 * @param entry the Schedule Entry
	 * @param doReplace TRUE if an existing entry can be replaced, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(ScheduleEntry entry, boolean doReplace) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(doReplace ? "REPLACE" : "INSERT");
		sqlBuf.append(" INTO SCHEDULE (AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, DISTANCE, EQTYPE, FLIGHT_TIME, "
				+ "TIME_D, TIME_A, HISTORIC, CAN_PURGE, ACADEMY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, entry.getAirline().getCode());
			_ps.setInt(2, entry.getFlightNumber());
			_ps.setInt(3, entry.getLeg());
			_ps.setString(4, entry.getAirportD().getIATA());
			_ps.setString(5, entry.getAirportA().getIATA());
			_ps.setInt(6, entry.getDistance());
			_ps.setString(7, entry.getEquipmentType());
			_ps.setInt(8, entry.getLength());
			_ps.setTimestamp(9, createTimestamp(entry.getTimeD()));
			_ps.setTimestamp(10, createTimestamp(entry.getTimeA()));
			_ps.setBoolean(11, entry.getHistoric());
			_ps.setBoolean(12, entry.getCanPurge());
			_ps.setBoolean(13, entry.getAcademy());

			// Update the database
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
			prepareStatement("INSERT INTO common.AIRCRAFT (NAME, FULLNAME, ACRANGE, IATA, HISTORIC, ETOPS, ENGINES, "
					+ "ENGINE_TYPE, CRUISE_SPEED, FUEL_FLOW, BASE_FUEL, TAXI_FUEL, PRI_TANKS, PRI_PCT, SEC_TANKS, "
					+ "SEC_PCT, OTHER_TANKS, MAX_WEIGHT, MAX_TWEIGHT, MAX_LWEIGHT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, a.getName());
			_ps.setString(2, a.getFullName());
			_ps.setInt(3, a.getRange());
			_ps.setString(4, StringUtils.listConcat(a.getIATA(), ","));
			_ps.setBoolean(5, a.getHistoric());
			_ps.setBoolean(6, a.getETOPS());
			_ps.setByte(7, a.getEngines());
			_ps.setString(8, a.getEngineType());
			_ps.setInt(9, a.getCruiseSpeed());
			_ps.setInt(10, a.getFuelFlow());
			_ps.setInt(11, a.getBaseFuel());
			_ps.setInt(12, a.getTaxiFuel());
			_ps.setInt(13, a.getTanks(Aircraft.PRIMARY));
			_ps.setInt(14, a.getPct(Aircraft.PRIMARY));
			_ps.setInt(15, a.getTanks(Aircraft.SECONDARY));
			_ps.setInt(16, a.getPct(Aircraft.SECONDARY));
			_ps.setInt(17, a.getTanks(Aircraft.OTHER));
			_ps.setInt(18, a.getMaxWeight());
			_ps.setInt(19, a.getMaxTakeoffWeight());
			_ps.setInt(20, a.getMaxLandingWeight());
			executeUpdate(1);
			
			// Add the webapps
			prepareStatement("INSERT INTO common.AIRCRAFT_AIRLINE (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, a.getName());
			for (Iterator<AirlineInformation> i = a.getApps().iterator(); i.hasNext(); ) {
				AirlineInformation ai = i.next();
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}
			
			// Execute and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates the aircraft profile.
	 * @param a the Aircraft bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Aircraft a) throws DAOException {
		try {
			startTransaction();
			prepareStatement("UPDATE common.AIRCRAFT SET ACRANGE=?, IATA=?, HISTORIC=?, ENGINES=?, ENGINE_TYPE=?, "
					+ "CRUISE_SPEED=?, FUEL_FLOW=?, BASE_FUEL=?, TAXI_FUEL=?, PRI_TANKS=?, PRI_PCT=?, SEC_TANKS=?, "
					+ "SEC_PCT=?, OTHER_TANKS=?, ETOPS=?, MAX_WEIGHT=?, MAX_TWEIGHT=?, MAX_LWEIGHT=?, FULLNAME=? "
					+ "WHERE (NAME=?)");
			_ps.setInt(1, a.getRange());
			_ps.setString(2, StringUtils.listConcat(a.getIATA(), ",").replace("\r", ""));
			_ps.setBoolean(3, a.getHistoric());
			_ps.setByte(4, a.getEngines());
			_ps.setString(5, a.getEngineType());
			_ps.setInt(6, a.getCruiseSpeed());
			_ps.setInt(7, a.getFuelFlow());
			_ps.setInt(8, a.getBaseFuel());
			_ps.setInt(9, a.getTaxiFuel());
			_ps.setInt(10, a.getTanks(Aircraft.PRIMARY));
			_ps.setInt(11, a.getPct(Aircraft.PRIMARY));
			_ps.setInt(12, a.getTanks(Aircraft.SECONDARY));
			_ps.setInt(13, a.getPct(Aircraft.SECONDARY));
			_ps.setInt(14, a.getTanks(Aircraft.OTHER));
			_ps.setBoolean(15, a.getETOPS());
			_ps.setInt(16, a.getMaxWeight());
			_ps.setInt(17, a.getMaxTakeoffWeight());
			_ps.setInt(18, a.getMaxLandingWeight());
			_ps.setString(19, a.getFullName());
			_ps.setString(20, a.getName());
			executeUpdate(1);

			// Clean out the webapps
			prepareStatementWithoutLimits("DELETE FROM common.AIRCRAFT_AIRLINE WHERE (NAME=?)");
			_ps.setString(1, a.getName());
			executeUpdate(0);
			
			// Add the webapps
			prepareStatement("INSERT INTO common.AIRCRAFT_AIRLINE (NAME, AIRLINE) VALUES (?, ?)");
			_ps.setString(1, a.getName());
			for (Iterator<AirlineInformation> i = a.getApps().iterator(); i.hasNext(); ) {
				AirlineInformation ai = i.next();
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}
			
			// Execute and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an entry from the Flight Schedule.
	 * @param entry the entry
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if entry is null
	 */
	public void delete(ScheduleEntry entry) throws DAOException {
		try {
			prepareStatement("DELETE FROM SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?) AND (LEG=?)");
			_ps.setString(1, entry.getAirline().getCode());
			_ps.setInt(2, entry.getFlightNumber());
			_ps.setInt(3, entry.getLeg());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges entries from the Flight Schedule.
	 * @param force TRUE if all entries should be purged, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(boolean force) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM SCHEDULE");
		if (!force)
			sqlBuf.append(" WHERE (CAN_PURGE=?)");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			if (!force)
				_ps.setBoolean(1, true);

			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}