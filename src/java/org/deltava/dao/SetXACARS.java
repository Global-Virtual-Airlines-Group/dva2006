// Copyright 2011, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object for writing XACARS data to the database.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class SetXACARS extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetXACARS(Connection c) {
		super(c);
	}

	/**
	 * Writes a new XACARS flight information bean to the database.
	 * @param inf a FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(XAFlightInfo inf) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO xacars.FLIGHTS (PILOT_ID, AIRLINE, FLIGHT, AIRPORT_D, AIRPORT_A, "
				+ "AIRPORT_L, EQTYPE, START_TIME, PHASE, ROUTE, FSVERSION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, inf.getAuthorID());
			_ps.setString(2, inf.getAirline().getCode());
			_ps.setInt(3, inf.getFlightNumber());
			_ps.setString(4, inf.getAirportD().getIATA());
			_ps.setString(5, inf.getAirportA().getIATA());
			_ps.setString(6, (inf.getAirportL() == null) ? null :  inf.getAirportL().getIATA());
			_ps.setString(7, inf.getEquipmentType());
			_ps.setTimestamp(8, createTimestamp(inf.getStartTime()));
			_ps.setInt(9, inf.getPhase().ordinal());
			_ps.setString(10, inf.getRoute());
			_ps.setInt(11, inf.getSimulator().getCode());
			executeUpdate(1);
			inf.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an existing XACARS flight information bean in the database.
	 * @param inf a FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(XAFlightInfo inf) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE xacars.FLIGHTS SET TAXI_TIME=?, TAXI_WEIGHT=?, TAXI_FUEL=?, TAKEOFF_TIME=?, "
				+ "TAKEOFF_DISTANCE=?, TAKEOFF_SPEED=?, TAKEOFF_N1=?, TAKEOFF_HDG=?, TAKEOFF_LAT=?, TAKEOFF_LNG=?, "
				+ "TAKEOFF_ALT=?, TAKEOFF_WEIGHT=?, TAKEOFF_FUEL=?, LANDING_TIME=?, LANDING_DISTANCE=?, LANDING_SPEED=?, "
				+ "LANDING_N1=?, LANDING_HDG=?, LANDING_LAT=?, LANDING_LNG=?, LANDING_ALT=?, LANDING_WEIGHT=?, "
				+ "LANDING_FUEL=?, END_TIME=?, PHASE=?, CPHASE=?, ZFW=?, ROUTE=?, FSVERSION=? WHERE (ID=?)");
			_ps.setTimestamp(1, createTimestamp(inf.getTaxiTime()));
			_ps.setInt(2, inf.getTaxiWeight());
			_ps.setInt(3, inf.getTaxiFuel());
			_ps.setTimestamp(4, createTimestamp(inf.getTakeoffTime()));
			_ps.setInt(5, inf.getTakeoffDistance());
			_ps.setInt(6, inf.getTakeoffSpeed());
			_ps.setDouble(7, inf.getTakeoffN1());
			_ps.setInt(8, inf.getTakeoffHeading());
			_ps.setDouble(9, inf.getTakeoffLocation().getLatitude());
			_ps.setDouble(10, inf.getTakeoffLocation().getLongitude());
			_ps.setInt(11, inf.getTakeoffLocation().getAltitude());
			_ps.setInt(12, inf.getTakeoffWeight());
			_ps.setInt(13, inf.getTakeoffFuel());
			_ps.setTimestamp(14, createTimestamp(inf.getLandingTime()));
			_ps.setInt(15, inf.getLandingDistance());
			_ps.setInt(16, inf.getLandingSpeed());
			_ps.setDouble(17, inf.getLandingN1());
			_ps.setInt(18, inf.getLandingHeading());
			_ps.setDouble(19, inf.getLandingLocation().getLatitude());
			_ps.setDouble(20, inf.getLandingLocation().getLongitude());
			_ps.setInt(21, inf.getLandingLocation().getAltitude());
			_ps.setInt(22, inf.getLandingWeight());
			_ps.setInt(23, inf.getLandingFuel());
			_ps.setTimestamp(24, createTimestamp(inf.getEndTime()));
			_ps.setInt(25, inf.getPhase().ordinal());
			_ps.setInt(26, inf.getClimbPhase().ordinal());
			_ps.setInt(27, inf.getZeroFuelWeight());
			_ps.setString(28, inf.getRoute());
			_ps.setInt(29, inf.getSimulator().getCode());
			_ps.setInt(30, inf.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a flight as complete.
	 * @param flightID the XACARS flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void endFlight(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE xacars.FLIGHTS SET END_TIME=NOW(), PHASE=? WHERE (ID=?)");
			_ps.setInt(1, FlightPhase.COMPLETE.ordinal());
			_ps.setInt(2, flightID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an XACARS position report to the database.
	 * @param pos a PositionInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(XARouteEntry pos) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO xacars.POSITIONS (FLIGHT_ID, REPORT_TIME, LAT, LNG, B_ALT, "
				+ "HEADING, ASPEED, GSPEED, VSPEED, MACH, FUEL, PHASE, WIND_HDG, WIND_SPEED, FLAGS, MSGTYPE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, pos.getFlightID());
			_ps.setTimestamp(2, createTimestamp(pos.getDate()));
			_ps.setDouble(3, pos.getLatitude());
			_ps.setDouble(4, pos.getLongitude());
			_ps.setInt(5, pos.getAltitude());
			_ps.setInt(6, pos.getHeading());
			_ps.setInt(7, pos.getAirSpeed());
			_ps.setInt(8, pos.getGroundSpeed());
			_ps.setInt(9, pos.getVerticalSpeed());
			_ps.setDouble(10, pos.getMach());
			_ps.setInt(11, pos.getFuelRemaining());
			_ps.setInt(12, pos.getPhase().ordinal());
			_ps.setInt(13, pos.getWindHeading());
			_ps.setInt(14, pos.getWindSpeed());
			_ps.setInt(15, pos.getFlags());
			_ps.setString(16, pos.getMessageType());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Archives a set of XACARS position reports.
	 * @param oldID the temporary XACARS flight ID
	 * @param newID the permanent ACARS flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void archive(int oldID, int newID) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.POSITION_XARCHIVE (SELECT ?, REPORT_TIME, LAT, LNG, "
				+ "B_ALT, HEADING, ASPEED, GSPEED, VSPEED, MACH, FUEL, PHASE, WIND_HDG, WIND_SPEED, FLAGS FROM "
				+ "xacars.POSITIONS WHERE (FLIGHT_ID=?))");
			_ps.setInt(1, newID);
			_ps.setInt(2, oldID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an XACARS flight from the database.
	 * @param id the flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM xacars.FLIGHTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges old XACARS flights from the database.
	 * @param hours the purge interval in hours
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(int hours) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM xacars.FLIGHTS WHERE (START_TIME < DATE_SUB(NOW(), "
				+ "INTERVAL ? HOUR)) AND (END_TIME IS NULL)");
			_ps.setInt(1, hours);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}