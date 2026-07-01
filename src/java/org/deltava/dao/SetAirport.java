// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019, 2020, 2021, 2024, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to update Airport information.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class SetAirport extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAirport(Connection c) {
		super(c);
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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRPORTS (IATA, ICAO, TZ, NAME, COUNTRY, LATITUDE, LONGITUDE, ADSE, HAS_USPFI, IS_SCHENGEN, HAS_FICTIONAL_CODE, OLDCODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, a.getIATA());
				ps.setString(2, a.getICAO());
				ps.setString(3, a.getTZ().getID());
				ps.setString(4, a.getName());
				ps.setString(5, a.getCountry().getCode());
				ps.setDouble(6, a.getLatitude());
				ps.setDouble(7, a.getLongitude());
				ps.setBoolean(8, a.getASDE());
				ps.setBoolean(9, a.getHasPFI());
				ps.setBoolean(10, a.getIsSchengen());
				ps.setBoolean(11, a.getHasFictionalCode());
				ps.setString(12, a.getSupercededAirport());
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
			
			// Clear out the airlines
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRPORT_AIRLINE WHERE (IATA=?) AND (APPCODE=?)")) {
				ps.setString(1, oc);
				ps.setString(2, SystemData.get("airline.code"));
				executeUpdate(ps, 0);
			}

			// Update the airport data
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.AIRPORTS SET ICAO=?, TZ=?, NAME=?, LATITUDE=?, LONGITUDE=?, IATA=?, ADSE=?, COUNTRY=?, HAS_USPFI=?, IS_SCHENGEN=?, HAS_FICTIONAL_CODE=?, OLDCODE=? WHERE (IATA=?)")) {
				ps.setString(1, a.getICAO());
				ps.setString(2, a.getTZ().getID());
				ps.setString(3, a.getName());
				ps.setDouble(4, a.getLatitude());
				ps.setDouble(5, a.getLongitude());
				ps.setString(6, a.getIATA());
				ps.setBoolean(7, a.getASDE());
				ps.setString(8, a.getCountry().getCode());
				ps.setBoolean(9,  a.getHasPFI());
				ps.setBoolean(10, a.getIsSchengen());
				ps.setBoolean(11, a.getHasFictionalCode());
				ps.setString(12, a.getSupercededAirport());
				ps.setString(13, oc);
				executeUpdate(ps, 1);
			}
			
			// Ensure the superceded airports are interchangeable
			if (a.getSupercededAirport() != null) {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.AIRPORTS SET OLDCODE=? WHERE (IATA=?)")) {
					ps.setString(1, a.getIATA());	
					ps.setString(2, a.getSupercededAirport());
					executeUpdate(ps, 0);
				}
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.AIRPORTS SET OLDCODE=NULL WHERE (OLDCODE=?)")) {
					ps.setString(1, a.getIATA());
					executeUpdate(ps, 0);
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
}