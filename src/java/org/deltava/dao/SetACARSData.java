// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.util.CalendarUtils;

/**
 * A Data Access Object to write ACARS data. This is used outside of the ACARS server by classes that need to simulate ACARS server
 * writes without having access to the ACARS server message bean code.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetACARSData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSData(Connection c) {
		super(c);
	}

	/**
	 * Writes an ACARS connection entry to the database. 
	 * @param ce the ConnectionEntry bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void createConnection(ConnectionEntry ce) throws DAOException {
		try {
			prepareStatement("INSERT INTO acars.CONS (ID, PILOT_ID, DATE, REMOTE_ADDR, REMOTE_HOST, CLIENT_BUILD) "
				      + "VALUES (?, ?, ?, INET_ATON(?), ?, ?)");
			_ps.setLong(1, ce.getID());
			_ps.setInt(2, ce.getPilotID());
			_ps.setTimestamp(3, createTimestamp(ce.getStartTime()));
			_ps.setString(4, ce.getRemoteAddr());
			_ps.setString(5, ce.getRemoteHost());
			_ps.setInt(6, ce.getClientBuild());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Flight Information entry to the database.
	 * @param info the FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void createFlight(FlightInfo info) throws DAOException {
		try {
			prepareStatement("INSERT INTO acars.FLIGHTS (CON_ID, FLIGHT_NUM, CREATED, END_TIME, EQTYPE, CRUISE_ALT, "
					+ "AIRPORT_D, AIRPORT_A, ROUTE, REMARKS, FSVERSION, OFFLINE, PIREP) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setLong(1, info.getConnectionID());
			_ps.setString(2, info.getFlightCode());
			_ps.setTimestamp(3, createTimestamp(info.getStartTime()));
			_ps.setTimestamp(4, createTimestamp(info.getEndTime()));
			_ps.setString(5, info.getEquipmentType());
			_ps.setString(6, info.getAltitude());
			_ps.setString(7, info.getAirportD().getIATA());
			_ps.setString(8, info.getAirportA().getIATA());
			_ps.setString(9, info.getRoute());
			_ps.setString(10, info.getRemarks());
			_ps.setInt(11, info.getFSVersion());
			_ps.setBoolean(12, info.getOffline());
			_ps.setBoolean(13, true);
			executeUpdate(1);
			
			// Since we're writing a new entry, get the database ID
			if (info.getID() == 0)
				info.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Flight's position entries to the database.
	 * @param cid the Connection ID
	 * @param flightID the Flight ID
	 * @param entries a Collection of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writePositions(long cid, int flightID, Collection<RouteEntry> entries) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.POSITIONS (CON_ID, FLIGHT_ID, REPORT_TIME, TIME_MS, LAT, LNG, B_ALT, "
					+ "R_ALT, HEADING, ASPEED, GSPEED, VSPEED, N1, N2, MACH, FUEL, PHASE, SIM_RATE, FLAGS, FLAPS, PITCH, BANK, "
					+ "FUELFLOW, WIND_HDG, WIND_SPEED, AOA, GFORCE, FRAMERATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			// Loop through the positions
			_ps.setLong(1, cid);
			_ps.setInt(2, flightID);
			for (Iterator<RouteEntry> i = entries.iterator(); i.hasNext(); ) {
				RouteEntry re = i.next();
				_ps.setTimestamp(3, createTimestamp(re.getDate()));
				_ps.setInt(4, CalendarUtils.getInstance(re.getDate()).get(Calendar.MILLISECOND));
				_ps.setDouble(5, re.getLatitude());
				_ps.setDouble(6, re.getLongitude());
				_ps.setInt(7, re.getAltitude());
				_ps.setInt(8, re.getRadarAltitude());
				_ps.setInt(9, re.getHeading());
				_ps.setInt(10, re.getAirSpeed());
				_ps.setInt(11, re.getGroundSpeed());
				_ps.setInt(12, re.getVerticalSpeed());
				_ps.setDouble(13, re.getN1());
				_ps.setDouble(14, re.getN2());
				_ps.setDouble(15, re.getMach());
				_ps.setInt(16, re.getFuelRemaining());
				_ps.setInt(17, re.getPhase());
				_ps.setInt(18, re.getSimRate());
				_ps.setInt(19, re.getFlags());
				_ps.setInt(20, re.getFlaps());
				_ps.setDouble(21, re.getPitch());
				_ps.setDouble(22, re.getBank());
				_ps.setInt(23, re.getFuelFlow());
				_ps.setInt(24, re.getWindHeading());
				_ps.setInt(25, re.getWindSpeed());
				_ps.setDouble(26, re.getAOA());
				_ps.setDouble(27, re.getG());
				_ps.setInt(28, re.getFrameRate());
				_ps.addBatch();
			}
			
			// Write the batch
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}