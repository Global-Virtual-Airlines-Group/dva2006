// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2016, 2017, 2018, 2019, 2021, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write ACARS data. This is used outside of the ACARS server by classes that need to simulate
 * ACARS server writes without having access to the ACARS server message bean code.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public class SetACARSData extends DAO {
	
	private static final String ISQL = "INSERT INTO acars.FLIGHTS (AIRLINE, FLIGHT, CREATED, END_TIME, EQTYPE, CRUISE_ALT, AIRPORT_D, AIRPORT_A, AIRPORT_L, ROUTE, REMARKS, FSVERSION, OFFLINE, PIREP, FDR, "
			+ "REMOTE_HOST, REMOTE_ADDR, CLIENT_BUILD, BETA_BUILD, SIM_MAJOR, SIM_MINOR, IS64, ACARS64, APTYPE, PILOT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, INET6_ATON(?), ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String USQL = "UPDATE acars.FLIGHTS SET AIRLINE=?, FLIGHT=?, CREATED=?, END_TIME=?, EQTYPE=?, CRUISE_ALT=?, AIRPORT_D=?, AIRPORT_A=?, AIRPORT_L=?, ROUTE=?, REMARKS=?, FSVERSION=?, "
			+ "OFFLINE=?, PIREP=?, FDR=?, REMOTE_HOST=?, REMOTE_ADDR=INET6_ATON(?), CLIENT_BUILD=?, BETA_BUILD=?, SIM_MAJOR=?, SIM_MINOR=?, IS64=?, ACARS64=?, APTYPE=?, PILOT_ID=? WHERE (ID=?)";
	
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
		boolean isNew = (info.getID() == 0);
		try {
			try (PreparedStatement ps = prepare(isNew ? ISQL : USQL)) {
				ps.setString(1, info.getAirline().getCode());
				ps.setInt(2, info.getFlightNumber());
				ps.setTimestamp(3, createTimestamp(info.getStartTime()));
				ps.setTimestamp(4, createTimestamp(info.getEndTime()));
				ps.setString(5, info.getEquipmentType());
				ps.setString(6, info.getAltitude());
				ps.setString(7, info.getAirportD().getIATA());
				ps.setString(8, info.getAirportA().getIATA());
				ps.setString(9, (info.getAirportL() == null) ? null : info.getAirportL().getIATA());
				ps.setString(10, info.getRoute());
				ps.setString(11, info.getRemarks());
				ps.setInt(12, info.getSimulator().getCode());
				ps.setBoolean(13, info.getOffline());
				ps.setBoolean(14, true);
				ps.setInt(15, info.getFDR().ordinal());
				ps.setString(16, info.getRemoteHost());
				ps.setString(17, info.getRemoteAddr());
				ps.setInt(18, info.getClientBuild());
				ps.setInt(19, info.getBeta());
				ps.setInt(20, info.getSimMajor());
				ps.setInt(21, info.getSimMinor());
				ps.setBoolean(22, info.getIsSim64Bit());
				ps.setBoolean(23, info.getIsACARS64Bit());
				ps.setInt(24, info.getAutopilotType().ordinal());
				ps.setInt(25, info.getAuthorID());
				if (!isNew)
					ps.setInt(26, info.getID());
			
				executeUpdate(ps, 1);
			}

			// Since we're writing a new entry, get the database ID
			if (isNew) info.setID(getNewID());
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
				try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.FLIGHT_DISPATCHER (ID, DISPATCHER_ID) VALUES (?,?)")) {
					ps.setInt(1, flightID);
					ps.setInt(2, dispatcherID);
					executeUpdate(ps, 0);
				}
			}
			
			// Write the route
			if (routeID > 0) {
				try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.FLIGHT_DISPATCH (ID, ROUTE_ID) VALUES (?,?)")) {
					ps.setInt(1, flightID);
					ps.setInt(2, routeID);
					executeUpdate(ps, 0);
				}
			}
			
			// Update the flight info
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE acars.FLIGHTS SET DISPATCHER=? WHERE (ID=?) LIMIT 1")) {
				ps.setInt(1, DispatchType.DISPATCH.ordinal());
				ps.setInt(2, flightID);
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes ACARS load data to the database.
	 * @param info a FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeLoad(FlightInfo info) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.FLIGHT_LOAD (ID, PAX, SEATS, LOADTYPE, LOADFACTOR) VALUES (?,?,?,?,?)")) {
			ps.setInt(1, info.getID());
			ps.setInt(2, info.getPassengers());
			ps.setInt(3, info.getSeats());
			ps.setInt(4, info.getLoadType().ordinal());
			ps.setDouble(5, info.getLoadFactor());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes taxi time data for a flight to the database.
	 * @param inf the FlightData object
	 * @param taxiIn the inbound taxi time in seconds
	 * @param taxiOut the outbound taxi time in seconds
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeTaxi(FlightInfo inf, int taxiIn, int taxiOut) throws DAOException {
		int year = inf.getDate().atOffset(ZoneOffset.UTC).getYear();
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.TAXI_TIMES (ID, IS_DEPARTURE, IATA, YEAR, TAXITIME) VALUES (?,?,?,?,?)")) {
			ps.setInt(1, inf.getID());
			ps.setInt(4, year);
			int cnt = 0;
				
			// Write outbound
			if ((taxiOut > 40) && (taxiOut < 2700)) {
				ps.setBoolean(2, true);
				ps.setString(3, inf.getAirportD().getIATA());
				ps.setInt(5, taxiOut);
				ps.addBatch();
				cnt++;
			}
				
			// Write inbound
			if ((taxiIn > 40) && (taxiOut < 2700)) {
				ps.setBoolean(2, false);
				ps.setString(3, inf.getAirportA().getIATA());
				ps.setInt(5, taxiIn);
				ps.addBatch();
				cnt++;
			}
			
			if (cnt > 0)
				executeUpdate(ps, 1, cnt);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TaxiTime", new TaxiTime(inf.getAirportD().getICAO(), year).cacheKey());
			CacheManager.invalidate("TaxiTime", new TaxiTime(inf.getAirportA().getICAO(), year).cacheKey());
		}
	}
	
	/**
	 * Writes a Flight's position entries to the database.
	 * @param flightID the Flight ID
	 * @param entries a Collection of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writePositions(int flightID, Collection<ACARSRouteEntry> entries) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.POSITIONS (FLIGHT_ID, REPORT_TIME, LAT, LNG, B_ALT, R_ALT, HEADING, ASPEED, GSPEED, VSPEED, N1, N2, MACH, FUEL, PHASE, SIM_RATE, FLAGS, FLAPS, PITCH, BANK, "
				+ "FUELFLOW, WIND_HDG, WIND_SPEED, TEMP, PRESSURE, VIZ, AOA, CG, GFORCE, FRAMERATE, NET_CONNECTED, SIM_TIME, VAS, WEIGHT, GNDFLAGS) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
			ps.setInt(1, flightID);
			for (ACARSRouteEntry re: entries) {
				ps.setTimestamp(2, createTimestamp(re.getDate()));
				ps.setDouble(3, re.getLatitude());
				ps.setDouble(4, re.getLongitude());
				ps.setInt(5, re.getAltitude());
				ps.setInt(6, re.getRadarAltitude());
				ps.setInt(7, re.getHeading());
				ps.setInt(8, re.getAirSpeed());
				ps.setInt(9, re.getGroundSpeed());
				ps.setInt(10, re.getVerticalSpeed());
				ps.setDouble(11, re.getN1());
				ps.setDouble(12, re.getN2());
				ps.setDouble(13, re.getMach());
				ps.setInt(14, re.getFuelRemaining());
				ps.setInt(15, re.getPhase().ordinal());
				ps.setInt(16, re.getSimRate());
				ps.setInt(17, re.getFlags());
				ps.setInt(18, re.getFlaps());
				ps.setDouble(19, re.getPitch());
				ps.setDouble(20, re.getBank());
				ps.setInt(21, re.getFuelFlow());
				ps.setInt(22, re.getWindHeading());
				ps.setInt(23, re.getWindSpeed());
				ps.setInt(24, re.getTemperature());
				ps.setInt(25, re.getPressure());
				ps.setDouble(26, re.getVisibility());
				ps.setDouble(27, re.getAOA());
				ps.setDouble(28, re.getCG());
				ps.setDouble(29, re.getG());
				ps.setInt(30, re.getFrameRate());
				ps.setBoolean(31, re.getNetworkConnected());
				ps.setTimestamp(32, createTimestamp(re.getSimUTC()));
				ps.setInt(33, re.getVASFree());
				ps.setInt(34, re.getWeight());
				ps.setInt(35, re.getGroundOperations());
				ps.addBatch();
			}

			executeUpdate(ps, 1, entries.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight's SID/STAR data from the database.
	 * @param id the Flight ID
	 * @param t the TerminalRoute Type
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearTerminalRoutes(int id, TerminalRoute.Type t) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM acars.FLIGHT_SIDSTAR WHERE (ID=?) AND (TYPE=?)")) {
			ps.setInt(1, id);
			ps.setInt(2, t.ordinal());
			executeUpdate(ps, 0);
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
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.FLIGHT_SIDSTAR (ID, TYPE, NAME, TRANSITION, RUNWAY) VALUES (?,?,?,?,?)")) {
				ps.setInt(1, id);
				ps.setInt(2, tr.getType().ordinal());
				ps.setString(3, tr.getName());
				ps.setString(4, tr.getTransition());
				ps.setString(5, tr.getRunway());
				executeUpdate(ps, 1);
			}

			// Write the route data
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.FLIGHT_SIDSTAR_WP (ID, TYPE, SEQ, CODE, WPTYPE, LATITUDE, LONGITUDE, REGION) VALUES (?,?,?,?,?,?,?,?)")) {
				ps.setInt(1, id);
				ps.setInt(2, tr.getType().ordinal());
				LinkedList<NavigationDataBean> wps = tr.getWaypoints();
				for (int x = 0; x < wps.size(); x++) {
					NavigationDataBean ai = wps.get(x);
					ps.setInt(3, x + 1);
					ps.setString(4, ai.getCode());
					ps.setInt(5, ai.getType().ordinal());
					ps.setDouble(6, ai.getLatitude());
					ps.setDouble(7, ai.getLongitude());
					ps.setString(8, ai.getRegion());
					ps.addBatch();
				}

				executeUpdate(ps, 1, wps.size());
			}
			
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
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.LIVERIES (AIRLINE, LIVERY, NAME, ISDEFAULT) VALUES (?,?,?,?)")) {
				ps.setString(1, l.getAirline().getCode());
				ps.setString(2, l.getCode());
				ps.setString(3, l.getDescription());
				ps.setBoolean(4, l.getDefault());
				executeUpdate(ps, 1);
			}
			
			// If we are now the default livery, update the others
			if (l.getDefault()) {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE acars.LIVERIES SET ISDEFAULT=? WHERE (AIRLINE=?) AND (LIVERY<>?)")) {
					ps.setBoolean(1, false);
					ps.setString(2, l.getAirline().getCode());
					ps.setString(3, l.getCode());
					executeUpdate(ps, 0);
				}
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
			try (PreparedStatement ps = prepare("DELETE FROM acars.LIVERIES WHERE (AIRLINE=?) AND (LIVERY=?)")) {
				ps.setString(1, aCode);
				ps.setString(2, code);
				executeUpdate(ps, 0);
			}
			
			// Restore the default livery
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE acars.LIVERIES SET ISDEFAULT=? WHERE (AIRLINE=?) ORDER BY ISDEFAULT DESC, LIVERY LIMIT 1")) {
				ps.setBoolean(1, true);
				ps.setString(2, code);
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}