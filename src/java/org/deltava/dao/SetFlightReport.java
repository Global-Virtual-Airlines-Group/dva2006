// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access object to write Flight Reports to the database.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class SetFlightReport extends DAO {
	
	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetFlightReport(Connection c) {
		super(c);
	}

	/**
	 * Deletes a Flight Report from the database.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM PIREPS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes ACARS flight data from the database.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteACARS(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM ACARS_PIREPS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Disposes of a Flight Report, by setting its status to Approved, Rejected or Held.
	 * @param db the database name
	 * @param usr the Person updating the Flight Report
	 * @param pirep the Flight Report
	 * @param status the new FlightStatus
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if pirep is null
	 */
	public void dispose(String db, Person usr, FlightReport pirep, FlightStatus status) throws DAOException {
		String dbName = formatDBName(db);
		try {
			startTransaction();

			// Write the PIREP
			prepareStatementWithoutLimits("UPDATE " + dbName + ".PIREPS SET STATUS=?, ATTR=?, DISPOSAL_ID=?, DISPOSED=NOW() WHERE (ID=?)");
			_ps.setInt(1, status.ordinal());
			_ps.setInt(2, pirep.getAttributes());
			_ps.setInt(3, (usr == null) ? 0 : usr.getID());
			_ps.setInt(4, pirep.getID());
			executeUpdate(1);
			
			// Save the promotion equipment types and comments
			writePromoEQ(pirep.getID(), dbName, pirep.getCaptEQType());
			writeComments(pirep, dbName);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(pirep.getDatabaseID(DatabaseID.PILOT)));
		}
	}

	/*
	 * Private helper method to prepare an INSERT statement for a new Flight Report.
	 */
	private void insert(FlightReport fr, String db) throws SQLException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS (PILOT_ID, RANKING, STATUS, DATE, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, FSVERSION, ATTR, DISTANCE, FLIGHT_TIME, "
			+ "SUBMITTED, EVENT_ID, ASSIGN_ID, PAX, LOADFACTOR) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		// Set the prepared statement parameters
		prepareStatement(sqlBuf.toString());
		_ps.setInt(1, fr.getDatabaseID(DatabaseID.PILOT));
		_ps.setString(2, fr.getRank().getName());
		_ps.setInt(3, fr.getStatus().ordinal());
		_ps.setTimestamp(4, createTimestamp(fr.getDate()));
		_ps.setString(5, fr.getAirline().getCode());
		_ps.setInt(6, fr.getFlightNumber());
		_ps.setInt(7, fr.getLeg());
		_ps.setString(8, fr.getAirportD().getIATA());
		_ps.setString(9, fr.getAirportA().getIATA());
		_ps.setString(10, fr.getEquipmentType());
		_ps.setInt(11, fr.getSimulator().getCode());
		_ps.setInt(12, fr.getAttributes());
		_ps.setInt(13, fr.getDistance());
		_ps.setDouble(14, (fr.getLength() / 10.0));
		_ps.setTimestamp(15, createTimestamp(fr.getSubmittedOn()));
		_ps.setInt(16, fr.getDatabaseID(DatabaseID.EVENT));
		_ps.setInt(17, fr.getDatabaseID(DatabaseID.ASSIGN));
		_ps.setInt(18, fr.getPassengers());
		_ps.setDouble(19, fr.getLoadFactor());
	}

	/*
	 * Private helper method to prepare an UPDATE statement for an existing Flight Report.
	 */
	private void update(FlightReport fr, String db) throws SQLException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS SET STATUS=?, DATE=?, AIRLINE=?, FLIGHT=?, LEG=?, AIRPORT_D=?, AIRPORT_A=?, EQTYPE=?, FSVERSION=?, ATTR=?, DISTANCE=?, FLIGHT_TIME=?, DISPOSAL_ID=?, SUBMITTED=?, "
			+ "DISPOSED=?, ASSIGN_ID=?, EVENT_ID=?, PAX=?, LOADFACTOR=? WHERE (ID=?)");

		// Set the prepared statement parameters
		prepareStatement(sqlBuf.toString());
		_ps.setInt(1, fr.getStatus().ordinal());
		_ps.setTimestamp(2, createTimestamp(fr.getDate()));
		_ps.setString(3, fr.getAirline().getCode());
		_ps.setInt(4, fr.getFlightNumber());
		_ps.setInt(5, fr.getLeg());
		_ps.setString(6, fr.getAirportD().getIATA());
		_ps.setString(7, fr.getAirportA().getIATA());
		_ps.setString(8, fr.getEquipmentType());
		_ps.setInt(9, fr.getSimulator().getCode());
		_ps.setInt(10, fr.getAttributes());
		_ps.setInt(11, fr.getDistance());
		_ps.setDouble(12, (fr.getLength() / 10.0));
		_ps.setInt(13, fr.getDatabaseID(DatabaseID.DISPOSAL));
		_ps.setTimestamp(14, createTimestamp(fr.getSubmittedOn()));
		_ps.setTimestamp(15, createTimestamp(fr.getDisposedOn()));
		_ps.setInt(16, fr.getDatabaseID(DatabaseID.ASSIGN));
		_ps.setInt(17, fr.getDatabaseID(DatabaseID.EVENT));
		_ps.setInt(18, fr.getPassengers());
		_ps.setDouble(19, fr.getLoadFactor());
		_ps.setInt(20, fr.getID());
	}

	/**
	 * Clears whether a Flight Report counts towards promotion to Captain.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearPromoEQ(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM PROMO_EQ WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Sets whether a Flight Report counts towards promotion to Captain.
	 * @param id the Flight Report database ID
	 * @param eqTypes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setPromoEQ(int id, Collection<String> eqTypes) throws DAOException {
		try {
			startTransaction();
			writePromoEQ(id, SystemData.get("airline.db"), eqTypes);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates Flight Report comments.
	 * @param fr the FlightReport bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeComments(FlightReport fr) throws DAOException {
		try {
			writeComments(fr, SystemData.get("airline.db"));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates passenger count for a flight report in the current airline.
	 * @param pirepID the Flight Report database ID
	 * @return TRUE if the passenger count was updated, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean updatePaxCount(int pirepID) throws DAOException {
		UserData ud = new UserData(SystemData.get("airline.db"), "PILOTS", SystemData.get("airline.domain"));
		return updatePaxCount(pirepID, ud);
	}
	
	/**
	 * Updates passenger count based on load factor.
	 * @param pirepID the Flight Report database ID
	 * @param ud the Pilot's UserData object
	 * @return TRUE if the passenger count was updated, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean updatePaxCount(int pirepID, UserData ud) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("UPDATE ");
		buf.append(formatDBName(ud.getDB()));
		buf.append(".PIREPS P, common.AIRCRAFT_AIRLINE A SET P.PAX=ROUND(AA.SEATS*P.LOADFACTOR, 0) WHERE (P.ID=?) AND (P.EQTYPE=AA.NAME) AND (AA.AIRLINE=?) AND (ABS(P.PAX-(AA.SEATS*P.LOADFACTOR))>1)");
		
		try {
			prepareStatementWithoutLimits(buf.toString());
			_ps.setInt(1, pirepID);
			_ps.setString(2, ud.getAirlineCode());
			return (executeUpdate(0) > 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write promotion equipment types.
	 */
	private void writePromoEQ(int id, String dbName, Collection<String> eqTypes) throws SQLException {
		
		// Delete the existing records
		prepareStatementWithoutLimits("DELETE FROM " + dbName + ".PROMO_EQ WHERE (ID=?)");
		_ps.setInt(1, id);
		executeUpdate(0);

		// Queue the new records
		prepareStatementWithoutLimits("INSERT INTO " + dbName + ".PROMO_EQ (ID, EQTYPE) VALUES (?, ?)");
		_ps.setInt(1, id);
		for (String eqType : eqTypes) {
			_ps.setString(2, eqType);
			_ps.addBatch();
		}

		executeBatchUpdate(1, eqTypes.size());
	}

	/*
	 * Helper method to write a Flight Report's core data to the database.
	 */
	private void writeCore(FlightReport fr, String dbName) throws SQLException {

		// Initialize the prepared statement
		if (fr.getID() == 0)
			insert(fr, dbName);
		else
			update(fr, dbName);

		// Write the PIREP data into the database; if we are writing a new PIREP get the database ID
		executeUpdate(1);
		if (fr.getID() == 0)
			fr.setID(getNewID());

		// Write the route into the database
		if (!StringUtils.isEmpty(fr.getRoute())) {
			prepareStatementWithoutLimits("REPLACE INTO " + dbName + ".PIREP_ROUTE (ID, ROUTE) VALUES (?, ?)");
			_ps.setInt(1, fr.getID());
			
			// Strip out anything not a letter or digit
			String rt = fr.getRoute().toUpperCase();
			StringBuilder buf = new StringBuilder(); char lastChar = ' ';
			for (int x = 0; x < rt.length(); x++) {
				char c = rt.charAt(x);
				if (Character.isDigit(c) || Character.isLetter(c)) {
					buf.append(c);
					lastChar = c;
				} else if ((Character.isWhitespace(c) || (c == '.')) && !Character.isWhitespace(lastChar)) {
					buf.append(' ');
					lastChar = ' ';
				}
			}
			
			_ps.setString(2, buf.toString());
			executeUpdate(1);
		} else {
			prepareStatementWithoutLimits("DELETE FROM " + dbName + ".PIREP_ROUTE WHERE (ID=?)");
			_ps.setInt(1, fr.getID());
			executeUpdate(0);
		}
	}
	
	/*
	 * Writes flight report comments to the database.
	 */
	private void writeComments(FlightReport fr, String dbName) throws SQLException {
		boolean isEmpty = StringUtils.isEmpty(fr.getRemarks()) && StringUtils.isEmpty(fr.getComments());
		if (!isEmpty) {
			prepareStatement("REPLACE INTO " + dbName + ".PIREP_COMMENT (ID, COMMENTS, REMARKS) VALUES (?, ?, ?)");
			_ps.setInt(1, fr.getID());
			_ps.setString(2, fr.getComments());
			_ps.setString(3, fr.getRemarks());
			executeUpdate(1);
		} else {
			prepareStatement("DELETE FROM " + dbName + ".PIREP_COMMENT WHERE (ID=?)");
			_ps.setInt(1, fr.getID());
			executeUpdate(0);
		}
	}

	/**
	 * Write a Flight Report to the default database.
	 * @param fr the Flight Report
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(FlightReport fr) throws DAOException {
		write(fr, SystemData.get("airline.db"));
	}

	/**
	 * Writes a Flight Report to the database.
	 * @param fr the Flight Report
	 * @param db the Database to write to
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(FlightReport fr, String db) throws DAOException {
		String dbName = formatDBName(db);
		try {
			startTransaction();
			writeCore(fr, dbName);
			writeComments(fr, dbName);
			writePromoEQ(fr.getID(), dbName, fr.getCaptEQType());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an ACARS-enabled Flight Report into the database. This can handle INSERT and UPDATE operations.
	 * @param fr the Flight Report
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeACARS(FDRFlightReport fr, String dbName) throws DAOException {
		String db = formatDBName(dbName);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS (ID, ACARS_ID, START_TIME, TAXI_TIME, TAXI_WEIGHT, TAXI_FUEL, TAKEOFF_TIME, TAKEOFF_DISTANCE, TAKEOFF_SPEED, TAKEOFF_N1, TAKEOFF_HDG, TAKEOFF_LAT, TAKEOFF_LNG, "
			+ "TAKEOFF_ALT, TAKEOFF_WEIGHT, TAKEOFF_FUEL, LANDING_TIME, LANDING_DISTANCE, LANDING_SPEED, LANDING_VSPEED, LANDING_N1, LANDING_HDG, LANDING_LAT, LANDING_LNG, LANDING_ALT, LANDING_WEIGHT, "
			+ "LANDING_FUEL, END_TIME, GATE_WEIGHT, GATE_FUEL, TOTAL_FUEL, TIME_0X, TIME_1X, TIME_2X, TIME_4X, FDE, CODE, SDK, RELOAD, CLIENT_BUILD, BETA_BUILD, LANDING_G, LANDING_CAT, FRAMERATE, PAX_WEIGHT, "
			+ "CARGO_WEIGHT, CAPABILITIES) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			startTransaction();

			// Write the regular fields
			writeCore(fr, db);
			writeComments(fr, db);
			writePromoEQ(fr.getID(), db, fr.getCaptEQType());

			// Write the ACARS fields
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, fr.getID());
			_ps.setInt(2, fr.getDatabaseID(DatabaseID.ACARS));
			_ps.setTimestamp(3, createTimestamp(fr.getStartTime()));
			_ps.setTimestamp(4, createTimestamp(fr.getTaxiTime()));
			_ps.setInt(5, fr.getTaxiWeight());
			_ps.setInt(6, fr.getTaxiFuel());
			_ps.setTimestamp(7, createTimestamp(fr.getTakeoffTime()));
			_ps.setInt(8, fr.getTakeoffDistance());
			_ps.setInt(9, fr.getTakeoffSpeed());
			_ps.setDouble(10, fr.getTakeoffN1());
			_ps.setInt(11, fr.getTakeoffHeading());
			_ps.setDouble(12, fr.getTakeoffLocation().getLatitude());
			_ps.setDouble(13, fr.getTakeoffLocation().getLongitude());
			_ps.setInt(14, fr.getTakeoffLocation().getAltitude());
			_ps.setInt(15, fr.getTakeoffWeight());
			_ps.setInt(16, fr.getTakeoffFuel());
			_ps.setTimestamp(17, createTimestamp(fr.getLandingTime()));
			_ps.setInt(18, fr.getLandingDistance());
			_ps.setInt(19, fr.getLandingSpeed());
			_ps.setInt(20, fr.getLandingVSpeed());
			_ps.setDouble(21, fr.getLandingN1());
			_ps.setInt(22, fr.getLandingHeading());
			_ps.setDouble(23, fr.getLandingLocation().getLatitude());
			_ps.setDouble(24, fr.getLandingLocation().getLongitude());
			_ps.setInt(25, fr.getLandingLocation().getAltitude());
			_ps.setInt(26, fr.getLandingWeight());
			_ps.setInt(27, fr.getLandingFuel());
			_ps.setTimestamp(28, createTimestamp(fr.getEndTime()));
			_ps.setInt(29, fr.getGateWeight());
			_ps.setInt(30, fr.getGateFuel());
			_ps.setInt(31, fr.getTotalFuel());
			
			// ACARS
			if (fr instanceof ACARSFlightReport) {
				ACARSFlightReport afr = (ACARSFlightReport) fr; 
				_ps.setInt(32, afr.getTime(0));
				_ps.setInt(33, afr.getTime(1));
				_ps.setInt(34, afr.getTime(2));
				_ps.setInt(35, afr.getTime(4));
				_ps.setString(36, afr.getFDE());
				_ps.setString(37, afr.getAircraftCode());
				_ps.setString(38, afr.getSDK());
				_ps.setBoolean(39, afr.getHasReload());
				_ps.setInt(40, afr.getClientBuild());
				_ps.setInt(41, afr.getBeta());
				_ps.setDouble(42, afr.getLandingG());
				_ps.setInt(43, afr.getLandingCategory().ordinal());
				_ps.setInt(44, (int)(afr.getAverageFrameRate() * 10));
				_ps.setInt(45, afr.getPaxWeight());
				_ps.setInt(46, afr.getCargoWeight());
				_ps.setLong(47, afr.getCapabilities());
			} else if (fr instanceof XACARSFlightReport) {
				XACARSFlightReport xfr = (XACARSFlightReport) fr;
				_ps.setInt(32, 0);
				_ps.setInt(33, 0);
				_ps.setInt(34, 0);
				_ps.setInt(35, 0);
				_ps.setString(36, null);
				_ps.setString(37, null);
				_ps.setString(38, null);
				_ps.setBoolean(39, false);
				_ps.setInt(40, xfr.getMajorVersion());
				_ps.setInt(41, xfr.getMinorVersion());
				_ps.setDouble(42, 0);
				_ps.setInt(43, 0);
				_ps.setInt(44, 0);
				_ps.setInt(45, 0);
				_ps.setInt(46, 0);
				_ps.setLong(47, 0);
			}

			executeUpdate(1);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}