// Copyright 2011, 2012, 2014, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object for writing XACARS data to the database.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO xacars.FLIGHTS (PILOT_ID, AIRLINE, FLIGHT, AIRPORT_D, AIRPORT_A, AIRPORT_L, EQTYPE, START_TIME, PHASE, ROUTE, FSVERSION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, inf.getAuthorID());
				ps.setString(2, inf.getAirline().getCode());
				ps.setInt(3, inf.getFlightNumber());
				ps.setString(4, inf.getAirportD().getIATA());
				ps.setString(5, inf.getAirportA().getIATA());
				ps.setString(6, (inf.getAirportL() == null) ? null :  inf.getAirportL().getIATA());
				ps.setString(7, inf.getEquipmentType());
				ps.setTimestamp(8, createTimestamp(inf.getStartTime()));
				ps.setInt(9, inf.getPhase().ordinal());
				ps.setString(10, inf.getRoute());
				ps.setInt(11, inf.getSimulator().getCode());
				executeUpdate(ps, 1);
			}
				
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
		try (PreparedStatement ps = prepare("UPDATE xacars.FLIGHTS SET TAXI_TIME=?, TAXI_WEIGHT=?, TAXI_FUEL=?, TAKEOFF_TIME=?, TAKEOFF_DISTANCE=?, TAKEOFF_SPEED=?, TAKEOFF_N1=?, TAKEOFF_HDG=?, TAKEOFF_LAT=?, TAKEOFF_LNG=?, "
				+ "TAKEOFF_ALT=?, TAKEOFF_WEIGHT=?, TAKEOFF_FUEL=?, LANDING_TIME=?, LANDING_DISTANCE=?, LANDING_SPEED=?, LANDING_N1=?, LANDING_HDG=?, LANDING_LAT=?, LANDING_LNG=?, LANDING_ALT=?, LANDING_WEIGHT=?, "
				+ "LANDING_FUEL=?, END_TIME=?, PHASE=?, CPHASE=?, ZFW=?, ROUTE=?, FSVERSION=? WHERE (ID=?)")) {
			ps.setTimestamp(1, createTimestamp(inf.getTaxiTime()));
			ps.setInt(2, inf.getTaxiWeight());
			ps.setInt(3, inf.getTaxiFuel());
			ps.setTimestamp(4, createTimestamp(inf.getTakeoffTime()));
			ps.setInt(5, inf.getTakeoffDistance());
			ps.setInt(6, inf.getTakeoffSpeed());
			ps.setDouble(7, inf.getTakeoffN1());
			ps.setInt(8, inf.getTakeoffHeading());
			ps.setDouble(9, inf.getTakeoffLocation().getLatitude());
			ps.setDouble(10, inf.getTakeoffLocation().getLongitude());
			ps.setInt(11, inf.getTakeoffLocation().getAltitude());
			ps.setInt(12, inf.getTakeoffWeight());
			ps.setInt(13, inf.getTakeoffFuel());
			ps.setTimestamp(14, createTimestamp(inf.getLandingTime()));
			ps.setInt(15, inf.getLandingDistance());
			ps.setInt(16, inf.getLandingSpeed());
			ps.setDouble(17, inf.getLandingN1());
			ps.setInt(18, inf.getLandingHeading());
			ps.setDouble(19, inf.getLandingLocation().getLatitude());
			ps.setDouble(20, inf.getLandingLocation().getLongitude());
			ps.setInt(21, inf.getLandingLocation().getAltitude());
			ps.setInt(22, inf.getLandingWeight());
			ps.setInt(23, inf.getLandingFuel());
			ps.setTimestamp(24, createTimestamp(inf.getEndTime()));
			ps.setInt(25, inf.getPhase().ordinal());
			ps.setInt(26, inf.getClimbPhase().ordinal());
			ps.setInt(27, inf.getZeroFuelWeight());
			ps.setString(28, inf.getRoute());
			ps.setInt(29, inf.getSimulator().getCode());
			ps.setInt(30, inf.getID());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("UPDATE xacars.FLIGHTS SET END_TIME=NOW(), PHASE=? WHERE (ID=?)")) {
			ps.setInt(1, FlightPhase.COMPLETE.ordinal());
			ps.setInt(2, flightID);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO xacars.POSITIONS (FLIGHT_ID, REPORT_TIME, LAT, LNG, B_ALT, HEADING, ASPEED, GSPEED, VSPEED, MACH, FUEL, PHASE, WIND_HDG, WIND_SPEED, FLAGS, MSGTYPE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, pos.getFlightID());
			ps.setTimestamp(2, createTimestamp(pos.getDate()));
			ps.setDouble(3, pos.getLatitude());
			ps.setDouble(4, pos.getLongitude());
			ps.setInt(5, pos.getAltitude());
			ps.setInt(6, pos.getHeading());
			ps.setInt(7, pos.getAirSpeed());
			ps.setInt(8, pos.getGroundSpeed());
			ps.setInt(9, pos.getVerticalSpeed());
			ps.setDouble(10, pos.getMach());
			ps.setInt(11, pos.getFuelRemaining());
			ps.setInt(12, pos.getPhase().ordinal());
			ps.setInt(13, pos.getWindHeading());
			ps.setInt(14, pos.getWindSpeed());
			ps.setInt(15, pos.getFlags());
			ps.setString(16, pos.getMessageType());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.POSITION_XARCHIVE (SELECT ?, REPORT_TIME, LAT, LNG, B_ALT, HEADING, ASPEED, GSPEED, VSPEED, MACH, FUEL, PHASE, WIND_HDG, WIND_SPEED, FLAGS FROM "
				+ "xacars.POSITIONS WHERE (FLIGHT_ID=?))")) {
			ps.setInt(1, newID);
			ps.setInt(2, oldID);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM xacars.FLIGHTS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM xacars.FLIGHTS WHERE (START_TIME < DATE_SUB(NOW(), INTERVAL ? HOUR)) AND (END_TIME IS NULL)")) {
			ps.setInt(1, hours);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}