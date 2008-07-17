// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.util.CalendarUtils;

/**
 * A Data Access Object to write ACARS data. This is used outside of the ACARS server by classes that need to simulate
 * ACARS server writes without having access to the ACARS server message bean code.
 * @author Luke
 * @version 2.1
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
			// Write the flight info record
			prepareStatement("INSERT INTO acars.FLIGHTS (FLIGHT_NUM, CREATED, END_TIME, EQTYPE, CRUISE_ALT, AIRPORT_D, "
					+ "AIRPORT_A, ROUTE, REMARKS, FSVERSION, OFFLINE, PIREP, CON_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, info.getFlightCode());
			_ps.setTimestamp(2, createTimestamp(info.getStartTime()));
			_ps.setTimestamp(3, createTimestamp(info.getEndTime()));
			_ps.setString(4, info.getEquipmentType());
			_ps.setString(5, info.getAltitude());
			_ps.setString(6, info.getAirportD().getIATA());
			_ps.setString(7, info.getAirportA().getIATA());
			_ps.setString(8, info.getRoute());
			_ps.setString(9, info.getRemarks());
			_ps.setInt(10, info.getFSVersion());
			_ps.setBoolean(11, info.getOffline());
			_ps.setBoolean(12, true);
			_ps.setLong(13, info.getConnectionID());
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
	 * @param flightID the Flight ID
	 * @param entries a Collection of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writePositions(int flightID, Collection<RouteEntry> entries) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.POSITIONS (FLIGHT_ID, REPORT_TIME, TIME_MS, LAT, LNG, B_ALT, "
					+ "R_ALT, HEADING, ASPEED, GSPEED, VSPEED, N1, N2, MACH, FUEL, PHASE, SIM_RATE, FLAGS, FLAPS, PITCH, BANK, "
					+ "FUELFLOW, WIND_HDG, WIND_SPEED, AOA, GFORCE, FRAMERATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			// Loop through the positions
			_ps.setInt(1, flightID);
			for (Iterator<RouteEntry> i = entries.iterator(); i.hasNext();) {
				RouteEntry re = i.next();
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
				_ps.setInt(16, re.getPhase().getPhase());
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
	 * Deletes a saved route from the database.
	 * @param id the route ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteRoute(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM acars.ROUTES WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight's SID/STAR data from the datbase.
	 * @param id the Flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearSIDSTAR(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.FLIGHT_SIDSTAR WHERE (ID=?)");
			_ps.setInt(1, id);
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
			_ps.setInt(2, tr.getType());
			_ps.setString(3, tr.getName());
			_ps.setString(4, tr.getTransition());
			_ps.setString(5, tr.getRunway());
			executeUpdate(1);

			// Write the route data
			prepareStatementWithoutLimits("REPLACE INTO acars.FLIGHT_SIDSTAR_WP (ID, TYPE, SEQ, CODE, LATITUDE, "
					+ "LONGITUDE) VALUES (?, ? ,?, ?, ?, ?)");
			_ps.setInt(1, id);
			_ps.setInt(2, tr.getType());
			LinkedList<NavigationDataBean> wps = tr.getWaypoints();
			for (int x = 0; x < wps.size(); x++) {
				NavigationDataBean ai = wps.get(x);
				_ps.setInt(3, x + 1);
				_ps.setString(4, ai.getCode());
				_ps.setDouble(5, ai.getLatitude());
				_ps.setDouble(6, ai.getLongitude());
				_ps.addBatch();
			}

			// Write and clean up
			_ps.executeBatch();
			_ps.close();

			// Commit
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
			prepareStatement("REPLACE INTO acars.LIVERIES (AIRLINE, CODE, NAME, ISDEFAULT) VALUES (?, ?, ?, ?)");
			_ps.setString(1, l.getAirline().getCode());
			_ps.setString(2, l.getCode());
			_ps.setString(3, l.getDescription());
			_ps.setBoolean(4, l.getDefault());
			executeUpdate(1);
			
			// If we are now the default livery, update the others
			if (l.getDefault()) {
				prepareStatementWithoutLimits("UPDATE acars.LIVERIES SET ISDEFAULT=? WHERE (AIRLINE=?) "
						+ "AND (CODE<>?)");
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
}