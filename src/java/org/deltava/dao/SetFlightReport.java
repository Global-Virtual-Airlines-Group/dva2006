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
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREPS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepare("DELETE FROM ACARS_PIREPS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE " + dbName + ".PIREPS SET STATUS=?, ATTR=?, DISPOSAL_ID=?, DISPOSED=NOW() WHERE (ID=?)")) {
				ps.setInt(1, status.ordinal());
				ps.setInt(2, pirep.getAttributes());
				ps.setInt(3, (usr == null) ? 0 : usr.getID());
				ps.setInt(4, pirep.getID());
				executeUpdate(ps, 1);
			}
			
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
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, fr.getDatabaseID(DatabaseID.PILOT));
			ps.setString(2, fr.getRank().getName());
			ps.setInt(3, fr.getStatus().ordinal());
			ps.setTimestamp(4, createTimestamp(fr.getDate()));
			ps.setString(5, fr.getAirline().getCode());
			ps.setInt(6, fr.getFlightNumber());
			ps.setInt(7, fr.getLeg());
			ps.setString(8, fr.getAirportD().getIATA());
			ps.setString(9, fr.getAirportA().getIATA());
			ps.setString(10, fr.getEquipmentType());
			ps.setInt(11, fr.getSimulator().getCode());
			ps.setInt(12, fr.getAttributes());
			ps.setInt(13, fr.getDistance());
			ps.setDouble(14, (fr.getLength() / 10.0));
			ps.setTimestamp(15, createTimestamp(fr.getSubmittedOn()));
			ps.setInt(16, fr.getDatabaseID(DatabaseID.EVENT));
			ps.setInt(17, fr.getDatabaseID(DatabaseID.ASSIGN));
			ps.setInt(18, fr.getPassengers());
			ps.setDouble(19, fr.getLoadFactor());
			executeUpdate(ps, 1);
		}
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
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, fr.getStatus().ordinal());
			ps.setTimestamp(2, createTimestamp(fr.getDate()));
			ps.setString(3, fr.getAirline().getCode());
			ps.setInt(4, fr.getFlightNumber());
			ps.setInt(5, fr.getLeg());
			ps.setString(6, fr.getAirportD().getIATA());
			ps.setString(7, fr.getAirportA().getIATA());
			ps.setString(8, fr.getEquipmentType());
			ps.setInt(9, fr.getSimulator().getCode());
			ps.setInt(10, fr.getAttributes());
			ps.setInt(11, fr.getDistance());
			ps.setDouble(12, (fr.getLength() / 10.0));
			ps.setInt(13, fr.getDatabaseID(DatabaseID.DISPOSAL));
			ps.setTimestamp(14, createTimestamp(fr.getSubmittedOn()));
			ps.setTimestamp(15, createTimestamp(fr.getDisposedOn()));
			ps.setInt(16, fr.getDatabaseID(DatabaseID.ASSIGN));
			ps.setInt(17, fr.getDatabaseID(DatabaseID.EVENT));
			ps.setInt(18, fr.getPassengers());
			ps.setDouble(19, fr.getLoadFactor());
			ps.setInt(20, fr.getID());
			executeUpdate(ps, 1);
		}
	}

	/**
	 * Clears whether a Flight Report counts towards promotion to Captain.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearPromoEQ(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PROMO_EQ WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		buf.append(".PIREPS P, common.AIRCRAFT_AIRLINE AA SET P.PAX=ROUND(AA.SEATS*P.LOADFACTOR, 0) WHERE (P.ID=?) AND (P.EQTYPE=AA.NAME) AND (AA.AIRLINE=?) AND (ABS(P.PAX-(AA.SEATS*P.LOADFACTOR))>1)");
		
		try (PreparedStatement ps = prepareWithoutLimits(buf.toString())) {
			ps.setInt(1, pirepID);
			ps.setString(2, ud.getAirlineCode());
			return (executeUpdate(ps, 0) > 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write promotion equipment types.
	 */
	private void writePromoEQ(int id, String dbName, Collection<String> eqTypes) throws SQLException {
		
		// Delete the existing records
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".PROMO_EQ WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		}

		// Queue the new records
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + dbName + ".PROMO_EQ (ID, EQTYPE) VALUES (?, ?)")) {
			ps.setInt(1, id);
			for (String eqType : eqTypes) {
				ps.setString(2, eqType);
				ps.addBatch();
			}

			executeUpdate(ps, 1, eqTypes.size());
		}
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

		// If we are writing a new PIREP get the database ID
		if (fr.getID() == 0)
			fr.setID(getNewID());

		// Write the route into the database
		if (!StringUtils.isEmpty(fr.getRoute())) {
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO " + dbName + ".PIREP_ROUTE (ID, ROUTE) VALUES (?, ?)")) {
				ps.setInt(1, fr.getID());
			
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
			
				ps.setString(2, buf.toString());
				executeUpdate(ps, 1);
			}
		} else {
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".PIREP_ROUTE WHERE (ID=?)")) {
				ps.setInt(1, fr.getID());
				executeUpdate(ps, 0);
			}
		}
	}
	
	/*
	 * Writes flight report comments to the database.
	 */
	private void writeComments(FlightReport fr, String dbName) throws SQLException {
		boolean isEmpty = StringUtils.isEmpty(fr.getRemarks()) && StringUtils.isEmpty(fr.getComments());
		if (!isEmpty) {
			try (PreparedStatement ps = prepare("REPLACE INTO " + dbName + ".PIREP_COMMENT (ID, COMMENTS, REMARKS) VALUES (?, ?, ?)")) {
				ps.setInt(1, fr.getID());
				ps.setString(2, fr.getComments());
				ps.setString(3, fr.getRemarks());
				executeUpdate(ps, 1);
			}
		} else {
			try (PreparedStatement ps = prepare("DELETE FROM " + dbName + ".PIREP_COMMENT WHERE (ID=?)")) {
				ps.setInt(1, fr.getID());
				executeUpdate(ps, 0);
			}
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
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, fr.getID());
				ps.setInt(2, fr.getDatabaseID(DatabaseID.ACARS));
				ps.setTimestamp(3, createTimestamp(fr.getStartTime()));
				ps.setTimestamp(4, createTimestamp(fr.getTaxiTime()));
				ps.setInt(5, fr.getTaxiWeight());
				ps.setInt(6, fr.getTaxiFuel());
				ps.setTimestamp(7, createTimestamp(fr.getTakeoffTime()));
				ps.setInt(8, fr.getTakeoffDistance());
				ps.setInt(9, fr.getTakeoffSpeed());
				ps.setDouble(10, fr.getTakeoffN1());
				ps.setInt(11, fr.getTakeoffHeading());
				ps.setDouble(12, fr.getTakeoffLocation().getLatitude());
				ps.setDouble(13, fr.getTakeoffLocation().getLongitude());
				ps.setInt(14, fr.getTakeoffLocation().getAltitude());
				ps.setInt(15, fr.getTakeoffWeight());
				ps.setInt(16, fr.getTakeoffFuel());
				ps.setTimestamp(17, createTimestamp(fr.getLandingTime()));
				ps.setInt(18, fr.getLandingDistance());
				ps.setInt(19, fr.getLandingSpeed());
				ps.setInt(20, fr.getLandingVSpeed());
				ps.setDouble(21, fr.getLandingN1());
				ps.setInt(22, fr.getLandingHeading());
				ps.setDouble(23, fr.getLandingLocation().getLatitude());
				ps.setDouble(24, fr.getLandingLocation().getLongitude());
				ps.setInt(25, fr.getLandingLocation().getAltitude());
				ps.setInt(26, fr.getLandingWeight());
				ps.setInt(27, fr.getLandingFuel());
				ps.setTimestamp(28, createTimestamp(fr.getEndTime()));
				ps.setInt(29, fr.getGateWeight());
				ps.setInt(30, fr.getGateFuel());
				ps.setInt(31, fr.getTotalFuel());
				
				// ACARS
				if (fr instanceof ACARSFlightReport) {
					ACARSFlightReport afr = (ACARSFlightReport) fr; 
					ps.setInt(32, afr.getTime(0));
					ps.setInt(33, afr.getTime(1));
					ps.setInt(34, afr.getTime(2));
					ps.setInt(35, afr.getTime(4));
					ps.setString(36, afr.getFDE());
					ps.setString(37, afr.getAircraftCode());
					ps.setString(38, afr.getSDK());
					ps.setBoolean(39, afr.getHasReload());
					ps.setInt(40, afr.getClientBuild());
					ps.setInt(41, afr.getBeta());
					ps.setDouble(42, afr.getLandingG());
					ps.setInt(43, afr.getLandingCategory().ordinal());
					ps.setInt(44, (int)(afr.getAverageFrameRate() * 10));
					ps.setInt(45, afr.getPaxWeight());
					ps.setInt(46, afr.getCargoWeight());
					ps.setLong(47, afr.getCapabilities());
				} else if (fr instanceof XACARSFlightReport) {
					XACARSFlightReport xfr = (XACARSFlightReport) fr;
					ps.setInt(32, 0);
					ps.setInt(33, 0);
					ps.setInt(34, 0);
					ps.setInt(35, 0);
					ps.setString(36, null);
					ps.setString(37, null);
					ps.setString(38, null);
					ps.setBoolean(39, false);
					ps.setInt(40, xfr.getMajorVersion());
					ps.setInt(41, xfr.getMinorVersion());
					ps.setDouble(42, 0);
					ps.setInt(43, 0);
					ps.setInt(44, 0);
					ps.setInt(45, 0);
					ps.setInt(46, 0);
					ps.setLong(47, 0);
				}

				executeUpdate(ps, 1);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}