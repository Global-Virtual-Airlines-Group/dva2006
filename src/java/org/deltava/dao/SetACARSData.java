// Copyright 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;

import org.deltava.util.CalendarUtils;

/**
 * A Data Access Object to write ACARS data. This is used outside of the ACARS server by classes that need to simulate
 * ACARS server writes without having access to the ACARS server message bean code.
 * @author Luke
 * @version 5.1
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
	 * Writes a Flight Information entry to the database.
	 * @param info the FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void createFlight(FlightInfo info) throws DAOException {
		try {
			// Prepare the statement
			if (info.getID() == 0)
				prepareStatement("INSERT INTO acars.FLIGHTS (FLIGHT_NUM, CREATED, END_TIME, EQTYPE, "
					+ "CRUISE_ALT, AIRPORT_D, AIRPORT_A, AIRPORT_L, ROUTE, REMARKS, FSVERSION, OFFLINE, "
					+ "PIREP, XACARS, REMOTE_HOST, REMOTE_ADDR, CLIENT_BUILD, BETA_BUILD, PILOT_ID) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, INET_ATON(?), ?, ?, ?)");
			else
				prepareStatement("UPDATE acars.FLIGHTS SET FLIGHT_NUM=?, CREATED=?, END_TIME=?, "
					+ "EQTYPE=?, CRUISE_ALT=?, AIRPORT_D=?, AIRPORT_A=?, AIRPORT_L=?, ROUTE=?, REMARKS=?, "
					+ "FSVERSION=?, OFFLINE=?, PIREP=?, XACARS=?, REMOTE_HOST=?, REMOTE_ADDR=INET_ATON(?), "
					+ "CLIENT_BUILD=?, BETA_BUILD=?, PILOT_ID=? WHERE (ID=?)");
			
			// Write the flight info record
			_ps.setString(1, info.getFlightCode());
			_ps.setTimestamp(2, createTimestamp(info.getStartTime()));
			_ps.setTimestamp(3, createTimestamp(info.getEndTime()));
			_ps.setString(4, info.getEquipmentType());
			_ps.setString(5, info.getAltitude());
			_ps.setString(6, info.getAirportD().getIATA());
			_ps.setString(7, info.getAirportA().getIATA());
			_ps.setString(8, (info.getAirportL() == null) ? null : info.getAirportL().getIATA());
			_ps.setString(9, info.getRoute());
			_ps.setString(10, info.getRemarks());
			_ps.setInt(11, info.getFSVersion().getCode());
			_ps.setBoolean(12, info.getOffline());
			_ps.setBoolean(13, true);
			_ps.setBoolean(14, info.isXACARS());
			_ps.setString(15, info.getRemoteHost());
			_ps.setString(16, info.getRemoteAddr());
			_ps.setInt(17, info.getClientBuild());
			_ps.setInt(18, info.getBeta());
			_ps.setInt(19, info.getAuthorID());
			if (info.getID() != 0)
				_ps.setInt(20, info.getID());
			
			executeUpdate(1);

			// Since we're writing a new entry, get the database ID
			if (info.getID() == 0)
				info.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Flags a flight after the fact as being plotted by a Dispatcher.
	 * @param flightID the Flight ID
	 * @param dispatcherID the Dispatcher's database ID (or zero for auto-dispatch)
	 * @param routeID the Route's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeDispatch(int flightID, int dispatcherID, int routeID) throws DAOException {
		try {
			startTransaction();
			
			// Write the dispatcher
			if (dispatcherID != 0) {
				prepareStatementWithoutLimits("REPLACE INTO acars.FLIGHT_DISPATCHER (ID, DISPATCHER_ID) VALUES (?, ?)");
				_ps.setInt(1, flightID);
				_ps.setInt(2, dispatcherID);
				executeUpdate(0);
			}
			
			// Write the route
			if (routeID > 0) {
				prepareStatementWithoutLimits("REPLACE INTO acars.FLIGHT_DISPATCH (ID, ROUTE_ID) VALUES (?, ?)");
				_ps.setInt(1, flightID);
				_ps.setInt(2, routeID);
				executeUpdate(0);
			}
			
			// Update the flight info
			prepareStatementWithoutLimits("UPDATE acars.FLIGHTS SET DISPATCH_PLAN=? WHERE (ID=?) LIMIT 1");
			_ps.setBoolean(1, true);
			_ps.setInt(2, flightID);
			executeUpdate(0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Flight's position entries to the database.
	 * @param flightID the Flight ID
	 * @param entries a Collection of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writePositions(int flightID, Collection<ACARSRouteEntry> entries) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.POSITIONS (FLIGHT_ID, REPORT_TIME, TIME_MS, LAT, LNG, B_ALT, "
					+ "R_ALT, HEADING, ASPEED, GSPEED, VSPEED, N1, N2, MACH, FUEL, PHASE, SIM_RATE, FLAGS, FLAPS, PITCH, BANK, "
					+ "FUELFLOW, WIND_HDG, WIND_SPEED, AOA, GFORCE, FRAMERATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			// Loop through the positions
			_ps.setInt(1, flightID);
			for (Iterator<ACARSRouteEntry> i = entries.iterator(); i.hasNext();) {
				ACARSRouteEntry re = i.next();
				_ps.setTimestamp(2, createTimestamp(re.getDate()));
				_ps.setInt(3, CalendarUtils.getInstance(re.getDate()).get(Calendar.MILLISECOND));
				_ps.setDouble(4, re.getLatitude());
				_ps.setDouble(5, re.getLongitude());
				_ps.setInt(6, re.getAltitude());
				_ps.setInt(7, re.getRadarAltitude());
				_ps.setInt(8, re.getHeading());
				_ps.setInt(9, re.getAirSpeed());
				_ps.setInt(10, re.getGroundSpeed());
				_ps.setInt(11, re.getVerticalSpeed());
				_ps.setDouble(12, re.getN1());
				_ps.setDouble(13, re.getN2());
				_ps.setDouble(14, re.getMach());
				_ps.setInt(15, re.getFuelRemaining());
				_ps.setInt(16, re.getPhase().ordinal());
				_ps.setInt(17, re.getSimRate());
				_ps.setInt(18, re.getFlags());
				_ps.setInt(19, re.getFlaps());
				_ps.setDouble(20, re.getPitch());
				_ps.setDouble(21, re.getBank());
				_ps.setInt(22, re.getFuelFlow());
				_ps.setInt(23, re.getWindHeading());
				_ps.setInt(24, re.getWindSpeed());
				_ps.setDouble(25, re.getAOA());
				_ps.setDouble(26, re.getG());
				_ps.setInt(27, re.getFrameRate());
				_ps.addBatch();
			}

			// Write the batch
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight's SID data from the datbase.
	 * @param id the Flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearSID(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.FLIGHT_SIDSTAR WHERE (ID=?) AND (TYPE=?)");
			_ps.setInt(1, id);
			_ps.setInt(2, TerminalRoute.Type.SID.ordinal());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight's STAR data from the datbase.
	 * @param id the Flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearSTAR(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.FLIGHT_SIDSTAR WHERE (ID=?) AND (TYPE=?)");
			_ps.setInt(1, id);
			_ps.setInt(2, TerminalRoute.Type.STAR.ordinal());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a Flight's SID/STAR data to the database.
	 * @param id the Flight ID
	 * @param tr the TerminalRoute bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeSIDSTAR(int id, TerminalRoute tr) throws DAOException {
		if ((tr == null) || (id == 0))
			return;

		try {
			startTransaction();

			// Write the Route
			prepareStatementWithoutLimits("REPLACE INTO acars.FLIGHT_SIDSTAR (ID, TYPE, NAME, TRANSITION, "
				+ "RUNWAY) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, id);
			_ps.setInt(2, tr.getType().ordinal());
			_ps.setString(3, tr.getName());
			_ps.setString(4, tr.getTransition());
			_ps.setString(5, tr.getRunway());
			executeUpdate(1);

			// Write the route data
			prepareStatementWithoutLimits("REPLACE INTO acars.FLIGHT_SIDSTAR_WP (ID, TYPE, SEQ, CODE, WPTYPE, "
				+ "LATITUDE, LONGITUDE, REGION) VALUES (?, ? ,?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, id);
			_ps.setInt(2, tr.getType().ordinal());
			LinkedList<NavigationDataBean> wps = tr.getWaypoints();
			for (int x = 0; x < wps.size(); x++) {
				NavigationDataBean ai = wps.get(x);
				_ps.setInt(3, x + 1);
				_ps.setString(4, ai.getCode());
				_ps.setInt(5, ai.getType().ordinal());
				_ps.setDouble(6, ai.getLatitude());
				_ps.setDouble(7, ai.getLongitude());
				_ps.setString(8, ai.getRegion());
				_ps.addBatch();
			}

			// Write and clean up
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an ACARS multi-player livery profile.
	 * @param l the Livery bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Livery l) throws DAOException {
		try {
			startTransaction();
			
			// Write the livery
			prepareStatement("REPLACE INTO acars.LIVERIES (AIRLINE, LIVERY, NAME, ISDEFAULT) VALUES (?, ?, ?, ?)");
			_ps.setString(1, l.getAirline().getCode());
			_ps.setString(2, l.getCode());
			_ps.setString(3, l.getDescription());
			_ps.setBoolean(4, l.getDefault());
			executeUpdate(1);
			
			// If we are now the default livery, update the others
			if (l.getDefault()) {
				prepareStatementWithoutLimits("UPDATE acars.LIVERIES SET ISDEFAULT=? WHERE (AIRLINE=?) AND (LIVERY<>?)");
				_ps.setBoolean(1, false);
				_ps.setString(2, l.getAirline().getCode());
				_ps.setString(3, l.getCode());
				executeUpdate(0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an ACARS mutli-player livery profile.
	 * @param aCode the Airline code
	 * @param code the Livery code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteLivery(String aCode, String code) throws DAOException {
		try {
			startTransaction();
			
			// Delete the livery
			prepareStatement("DELETE FROM acars.LIVERIES WHERE (AIRLINE=?) AND (LIVERY=?)");
			_ps.setString(1, aCode);
			_ps.setString(2, code);
			executeUpdate(0);
			
			// Restore the default livery
			prepareStatementWithoutLimits("UPDATE acars.LIVERIES SET ISDEFAULT=? WHERE (AIRLINE=?) "
					+ "ORDER BY ISDEFAULT DESC, LIVERY LIMIT 1");
			_ps.setBoolean(1, true);
			_ps.setString(2, code);
			executeUpdate(0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}