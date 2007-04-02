// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access object to write Flight Reports to the database.
 * @author Luke
 * @version 1.0
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
	 * @param usr the Person updating the Flight Report
	 * @param pirep the Flight Report
	 * @param statusCode the new Flight Report status code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void dispose(Person usr, FlightReport pirep, int statusCode) throws DAOException {
		try {
			startTransaction();

			// Write the PIREP
			prepareStatementWithoutLimits("UPDATE PIREPS SET STATUS=?, DISPOSAL_ID=?, DISPOSED=NOW() WHERE (ID=?)");
			_ps.setInt(1, statusCode);
			_ps.setInt(2, usr.getID());
			_ps.setInt(3, pirep.getID());
			executeUpdate(1);
			
			// Save the promotion equipment types
			writePromoEQ(pirep.getID(), SystemData.get("airline.db"), pirep.getCaptEQType());

			// Write the comments into the database
			if (!StringUtils.isEmpty(pirep.getComments())) {
				prepareStatement("REPLACE INTO PIREP_COMMENT (ID, COMMENTS) VALUES (?, ?)");
				_ps.setInt(1, pirep.getID());
				_ps.setString(2, pirep.getComments());
				executeUpdate(1);
			} else {
				prepareStatement("DELETE FROM PIREP_COMMENT WHERE (ID=?)");
				_ps.setInt(1, pirep.getID());
				executeUpdate(0);
			}

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}

		// Invalidate the entry in the Pilot cache
		PilotDAO.invalidate(pirep.getDatabaseID(FlightReport.DBID_PILOT));
	}

	/**
	 * Private helper method to prepare an INSERT statement for a new Flight Report.
	 */
	private void insert(FlightReport fr, String db) throws SQLException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS (PILOT_ID, RANK, STATUS, DATE, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, "
				+ "FSVERSION, ATTR, DISTANCE, FLIGHT_TIME, REMARKS, SUBMITTED, EVENT_ID, ASSIGN_ID) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		// Set the prepared statement parameters
		prepareStatement(sqlBuf.toString());
		_ps.setInt(1, fr.getDatabaseID(FlightReport.DBID_PILOT));
		_ps.setString(2, fr.getRank());
		_ps.setInt(3, fr.getStatus());
		_ps.setTimestamp(4, createTimestamp(fr.getDate()));
		_ps.setString(5, fr.getAirline().getCode());
		_ps.setInt(6, fr.getFlightNumber());
		_ps.setInt(7, fr.getLeg());
		_ps.setString(8, fr.getAirportD().getIATA());
		_ps.setString(9, fr.getAirportA().getIATA());
		_ps.setString(10, fr.getEquipmentType());
		_ps.setInt(11, fr.getFSVersion());
		_ps.setInt(12, fr.getAttributes());
		_ps.setInt(13, fr.getDistance());
		_ps.setDouble(14, (fr.getLength() / 10.0));
		_ps.setString(15, fr.getRemarks());
		_ps.setTimestamp(16, createTimestamp(fr.getSubmittedOn()));
		_ps.setInt(17, fr.getDatabaseID(FlightReport.DBID_EVENT));
		_ps.setInt(18, fr.getDatabaseID(FlightReport.DBID_ASSIGN));
	}

	/**
	 * Private helper method to prepare an UPDATE statement for an existing Flight Report.
	 */
	private void update(FlightReport fr, String db) throws SQLException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS SET STATUS=?, DATE=?, AIRLINE=?, FLIGHT=?, LEG=?, AIRPORT_D=?, AIRPORT_A=?, "
				+ "EQTYPE=?, FSVERSION=?, ATTR=?, DISTANCE=?, FLIGHT_TIME=?, REMARKS=?, DISPOSAL_ID=?, "
				+ "SUBMITTED=?, DISPOSED=?, ASSIGN_ID=?, EVENT_ID=? WHERE (ID=?)");

		// Set the prepared statement parameters
		prepareStatement(sqlBuf.toString());
		_ps.setInt(1, fr.getStatus());
		_ps.setTimestamp(2, createTimestamp(fr.getDate()));
		_ps.setString(3, fr.getAirline().getCode());
		_ps.setInt(4, fr.getFlightNumber());
		_ps.setInt(5, fr.getLeg());
		_ps.setString(6, fr.getAirportD().getIATA());
		_ps.setString(7, fr.getAirportA().getIATA());
		_ps.setString(8, fr.getEquipmentType());
		_ps.setInt(9, fr.getFSVersion());
		_ps.setInt(10, fr.getAttributes());
		_ps.setInt(11, fr.getDistance());
		_ps.setDouble(12, (fr.getLength() / 10.0));
		_ps.setString(13, fr.getRemarks());
		_ps.setInt(14, fr.getDatabaseID(FlightReport.DBID_DISPOSAL));
		_ps.setTimestamp(15, createTimestamp(fr.getSubmittedOn()));
		_ps.setTimestamp(16, createTimestamp(fr.getDisposedOn()));
		_ps.setInt(17, fr.getDatabaseID(FlightReport.DBID_ASSIGN));
		_ps.setInt(18, fr.getDatabaseID(FlightReport.DBID_EVENT));
		_ps.setInt(19, fr.getID());
	}

	/**
	 * Helper method to write promotion equipment types.
	 */
	private void writePromoEQ(int id, String dbName, Collection<String> eqTypes) throws SQLException {
		dbName = formatDBName(dbName);
		
		// Delete the existing records
		prepareStatementWithoutLimits("DELETE FROM " + dbName + ".PROMO_EQ WHERE (ID=?)");
		_ps.setInt(1, id);
		executeUpdate(0);

		// Queue the new records
		prepareStatementWithoutLimits("INSERT INTO " + dbName + ".PROMO_EQ (ID, EQTYPE) VALUES (?, ?)");
		_ps.setInt(1, id);
		for (Iterator<String> i = eqTypes.iterator(); i.hasNext();) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}

		// Write the entries
		_ps.executeBatch();
		_ps.close();
	}

	/**
	 * Helper method to write a Flight Report's core data to the database.
	 */
	private void writeCore(FlightReport fr, String dbName) throws SQLException {
		dbName = formatDBName(dbName);
		
		// Initialize the prepared statement
		if (fr.getID() == 0)
			insert(fr, dbName);
		else
			update(fr, dbName);

		// Write the PIREP data into the database; if we are writing a new PIREP get the database ID
		executeUpdate(1);
		if (fr.getID() == 0)
			fr.setID(getNewID());

		// Write the comments into the database
		if (!StringUtils.isEmpty(fr.getComments())) {
			prepareStatement("REPLACE INTO " + dbName + ".PIREP_COMMENT (ID, COMMENTS) VALUES (?, ?)");
			_ps.setInt(1, fr.getID());
			_ps.setString(2, fr.getComments());
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
	 * Write a Flight Report to the database.
	 * @param fr the Flight Report
	 * @param db the Database to write to
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(FlightReport fr, String db) throws DAOException {
		try {
			startTransaction();

			// Write the fields and the captain equipment types
			writeCore(fr, db);
			writePromoEQ(fr.getID(), db, fr.getCaptEQType());

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an ACARS-enabled Flight Report into the database. This can handle INSERT and UPDATE operations.
	 * @param afr the Flight Report
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeACARS(ACARSFlightReport afr, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".ACARS_PIREPS (ID, ACARS_ID, START_TIME, TAXI_TIME, TAXI_WEIGHT, TAXI_FUEL, "
						+ "TAKEOFF_TIME, TAKEOFF_DISTANCE, TAKEOFF_SPEED, TAKEOFF_N1, TAKEOFF_WEIGHT, "
						+ "TAKEOFF_FUEL, LANDING_TIME, LANDING_DISTANCE, LANDING_SPEED, LANDING_VSPEED, "
						+ "LANDING_N1, LANDING_WEIGHT, LANDING_FUEL, END_TIME, GATE_WEIGHT, GATE_FUEL, "
						+ "TIME_0X, TIME_1X, TIME_2X, TIME_4X) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			// Since we are writing to multiple tables, this is designd as a transaction
			startTransaction();

			// Write the regular fields
			writeCore(afr, dbName);
			writePromoEQ(afr.getID(), dbName, afr.getCaptEQType());

			// Write the ACARS fields
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, afr.getID());
			_ps.setInt(2, afr.getDatabaseID(FlightReport.DBID_ACARS));
			_ps.setTimestamp(3, createTimestamp(afr.getStartTime()));
			_ps.setTimestamp(4, createTimestamp(afr.getTaxiTime()));
			_ps.setInt(5, afr.getTaxiWeight());
			_ps.setInt(6, afr.getTaxiFuel());
			_ps.setTimestamp(7, createTimestamp(afr.getTakeoffTime()));
			_ps.setInt(8, afr.getTakeoffDistance());
			_ps.setInt(9, afr.getTakeoffSpeed());
			_ps.setDouble(10, afr.getTakeoffN1());
			_ps.setInt(11, afr.getTakeoffWeight());
			_ps.setInt(12, afr.getTakeoffFuel());
			_ps.setTimestamp(13, createTimestamp(afr.getLandingTime()));
			_ps.setInt(14, afr.getLandingDistance());
			_ps.setInt(15, afr.getLandingSpeed());
			_ps.setInt(16, afr.getLandingVSpeed());
			_ps.setDouble(17, afr.getLandingN1());
			_ps.setInt(18, afr.getLandingWeight());
			_ps.setInt(19, afr.getLandingFuel());
			_ps.setTimestamp(20, createTimestamp(afr.getEndTime()));
			_ps.setInt(21, afr.getGateWeight());
			_ps.setInt(22, afr.getGateFuel());
			_ps.setInt(23, afr.getTime(0));
			_ps.setInt(24, afr.getTime(1));
			_ps.setInt(25, afr.getTime(2));
			_ps.setInt(26, afr.getTime(4));

			// Write to the database
			executeUpdate(1);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}