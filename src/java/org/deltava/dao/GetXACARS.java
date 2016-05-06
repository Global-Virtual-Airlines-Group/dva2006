// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.Flight;
import org.deltava.beans.Simulator;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for reading XACARS data from the database.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class GetXACARS extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetXACARS(Connection c) {
		super(c);
	}

	/**
	 * Retrieves an XACARS flight ID from a flight number and user. 
	 * @param userID the user's database ID
	 * @param f the Flight information
	 * @return a Flight ID, or zero if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getID(int userID, Flight f) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ID FROM xacars.FLIGHTS WHERE (PILOT_ID=?) AND (AIRLINE=?) AND "
				+ "(FLIGHT=?) ORDER BY START_TIME DESC LIMIT 1");
			_ps.setInt(1, userID);
			_ps.setString(2, f.getAirline().getCode());
			_ps.setInt(3, f.getFlightNumber());
			
			// Do the query
			int flightID = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					flightID = rs.getInt(1);
			}

			_ps.close();
			return flightID;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves an XACARS flight record.
	 * @param id the flight ID
	 * @return a FlightInfo bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public XAFlightInfo getFlight(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM xacars.FLIGHTS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			
			// Execute the query
			List<XAFlightInfo> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns XACARS flights with a position report submitted within the last 30 minutes.
	 * @return a Collection of XAFlightInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<XAFlightInfo> getActive() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT F.*, MAX(P.REPORT_TIME) AS LATPOS FROM xacars.FLIGHTS F, "
				+ "xacars.POSITIONS P WHERE (F.ID=P.FLIGHT_ID) GROUP BY F.ID HAVING (LASTPOS > DATE_SUB(NOW(), "
				+ "INTERVAL ? MINUTE))");
			_ps.setInt(1, 30);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves all position entries associated with a particular flight.
	 * @param flightID the XACARS flight ID
	 * @return a Collection of PositionInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<XARouteEntry> getPositions(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM xacars.POSITIONS WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);
			
			// Execute the query
			Collection<XARouteEntry> results = new ArrayList<XARouteEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Instant dt = Instant.ofEpochMilli(rs.getTimestamp(2).getTime() + rs.getInt(3));
					XARouteEntry pos = new XARouteEntry(new GeoPosition(rs.getDouble(4), rs.getDouble(5), rs.getInt(6)), dt);
					pos.setFlightID(rs.getInt(1));
					pos.setHeading(rs.getInt(7));
					pos.setAirSpeed(rs.getInt(8));
					pos.setGroundSpeed(rs.getInt(9));
					pos.setVerticalSpeed(rs.getInt(10));
					pos.setMach(rs.getDouble(11));
					pos.setFuelRemaining(rs.getInt(12));
					pos.setPhase(rs.getInt(13));
					pos.setWindHeading(rs.getInt(14));
					pos.setWindSpeed(rs.getInt(15));
					results.add(pos);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Flight Info result sets.
	 */
	private List<XAFlightInfo> execute() throws SQLException {

		List<XAFlightInfo> results = new ArrayList<XAFlightInfo>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				XAFlightInfo inf = new XAFlightInfo(SystemData.getAirline(rs.getString(3)), rs.getInt(4));
				inf.setID(rs.getInt(1));
				inf.setAuthorID(rs.getInt(2));
				inf.setAirportD(SystemData.getAirport(rs.getString(5)));
				inf.setAirportA(SystemData.getAirport(rs.getString(6)));
				inf.setAirportL(SystemData.getAirport(rs.getString(7)));
				inf.setEquipmentType(rs.getString(8));
				inf.setStartTime(toInstant(rs.getTimestamp(9)));
				inf.setTaxiTime(toInstant(rs.getTimestamp(10)));
				inf.setTaxiWeight(rs.getInt(11));
				inf.setTaxiFuel(rs.getInt(12));
				inf.setTakeoffTime(toInstant(rs.getTimestamp(13)));
				inf.setTakeoffDistance(rs.getInt(14));
				inf.setTakeoffSpeed(rs.getInt(15));
				inf.setTakeoffN1(rs.getDouble(16));
				inf.setTakeoffHeading(rs.getInt(17));
				inf.setTakeoffLocation(new GeoPosition(rs.getDouble(18), rs.getDouble(19), rs.getInt(20)));
				inf.setTakeoffWeight(rs.getInt(21));
				inf.setTakeoffFuel(rs.getInt(22));
				inf.setLandingTime(toInstant(rs.getTimestamp(23)));
				inf.setLandingDistance(rs.getInt(24));
				inf.setLandingSpeed(rs.getInt(25));
				inf.setLandingN1(rs.getDouble(26));
				inf.setLandingHeading(rs.getInt(27));
				inf.setLandingLocation(new GeoPosition(rs.getDouble(28), rs.getDouble(29), rs.getInt(30)));
				inf.setLandingWeight(rs.getInt(31));
				inf.setLandingFuel(rs.getInt(32));
				inf.setEndTime(toInstant(rs.getTimestamp(33)));
				inf.setPhase(FlightPhase.values()[rs.getInt(34)]);
				inf.setClimbPhase(XAFlightInfo.ClimbPhase.values()[rs.getInt(35)]);
				inf.setZeroFuelWeight(rs.getInt(36));
				inf.setRoute(rs.getString(37));
				inf.setSimulator(Simulator.fromVersion(rs.getInt(38)));
				results.add(inf);
			}
		}
		
		_ps.close();
		return results;
	}
}