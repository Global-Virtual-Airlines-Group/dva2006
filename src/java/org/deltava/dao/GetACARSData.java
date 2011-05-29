// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.servinfo.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;
import org.gvagroup.acars.ACARSFlags;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A Data Access Object to load ACARS information.
 * @author Luke
 * @version 3.7
 * @since 1.0
 */

public class GetACARSData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSData(Connection c) {
		super(c);
	}

	/**
	 * Loads route position data for a particular ACARS flight ID.
	 * @param flightID the ACARS flight ID
	 * @param isArchived TRUE if the positions should be read from the archive
	 * @return a List of GeoPosition beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<GeoPosition> getRoutePositions(int flightID, boolean isArchived) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT LAT, LNG FROM acars.");
		sqlBuf.append(isArchived ? "POSITION_ARCHIVE" : "POSITIONS");
		sqlBuf.append(" WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, flightID);

			// Execute the query
			List<GeoPosition> results = new ArrayList<GeoPosition>();
			ResultSet rs = _ps.executeQuery();

			// Iterate through the result set
			while (rs.next())
				results.add(new GeoPosition(rs.getDouble(1), rs.getDouble(2)));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads completed route data for a particular ACARS filght ID, including data when on the ground.
	 * @param flightID the ACARS flight ID
	 * @param isArchived TRUE if the positions should be read from the archive, otherwise FALSE
	 * @return a List of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetACARSData#getRouteEntries(int, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<RouteEntry> getRouteEntries(int flightID, boolean isArchived) throws DAOException {
		List<?> results = getRouteEntries(flightID, true, isArchived);
		return (List<RouteEntry>) results;
	}

	/**
	 * Loads complete route data for a particular ACARS flight ID.
	 * @param flightID the ACARS flight ID
	 * @param includeOnGround TRUE if entries on the ground are RouteEntry beans, otherwise FALSE
	 * @param isArchived TRUE if the positions should be read from the archive, otherwise FALSE
	 * @return a List of GeoLocation beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetACARSData#getRouteEntries(int, boolean)
	 */
	public List<GeoLocation> getRouteEntries(int flightID, boolean includeOnGround, boolean isArchived) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.REPORT_TIME, P.TIME_MS, P.LAT, P.LNG, P.B_ALT, P.R_ALT, P.HEADING, "
				+ "P.PITCH, P.BANK, P.ASPEED, P.GSPEED, P.VSPEED, P.MACH, P.N1, P.N2, P.FLAPS, P.WIND_HDG, P.WIND_SPEED, P.FUEL, "
				+ "P.FUELFLOW, P.AOA, P.GFORCE, P.FLAGS, P.FRAMERATE, P.SIM_RATE, P.PHASE, PA.COM1, PA.CALLSIGN, PA.LAT, PA.LNG, "
				+ "PA.NETWORK_ID FROM acars.");
		sqlBuf.append(isArchived ? "POSITION_ARCHIVE" : "POSITIONS");
		sqlBuf.append(" P LEFT JOIN acars.POSITION_ATC PA ON (PA.FLIGHT_ID=P.FLIGHT_ID) AND (PA.REPORT_TIME=P.REPORT_TIME) "
				+ "AND (PA.TIME_MS=P.TIME_MS) WHERE (P.FLIGHT_ID=?) ORDER BY P.REPORT_TIME, P.TIME_MS");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, flightID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			List<GeoLocation> results = new ArrayList<GeoLocation>();
			while (rs.next()) {
				java.util.Date dt = new java.util.Date(rs.getTimestamp(1).getTime() + rs.getInt(2));
				RouteEntry entry = new RouteEntry(dt, new GeoPosition(rs.getDouble(3), rs.getDouble(4)));
				entry.setFlags(rs.getInt(23));

				// Add to results - or just log a GeoPosition if we're on the ground
				if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN) && !includeOnGround)
					results.add(new GeoPosition(entry));
				else {
					entry.setAltitude(rs.getInt(5));
					entry.setRadarAltitude(rs.getInt(6));
					entry.setHeading(rs.getInt(7));
					entry.setPitch(rs.getDouble(8));
					entry.setBank(rs.getDouble(9));
					entry.setAirSpeed(rs.getInt(10));
					entry.setGroundSpeed(rs.getInt(11));
					entry.setVerticalSpeed(rs.getInt(12));
					entry.setMach(rs.getDouble(13));
					entry.setN1(rs.getDouble(14));
					entry.setN2(rs.getDouble(15));
					entry.setFlaps(rs.getInt(16));
					entry.setWindHeading(rs.getInt(17));
					entry.setWindSpeed(rs.getInt(18));
					entry.setFuelRemaining(rs.getInt(19));
					entry.setFuelFlow(rs.getInt(20));
					entry.setAOA(rs.getDouble(21));
					entry.setG(rs.getDouble(22));
					entry.setFrameRate(rs.getInt(24));
					entry.setSimRate(rs.getInt(25));
					entry.setPhase(rs.getInt(26));
					
					// Load ATC info
					String atcID = rs.getString(28);
					if (!StringUtils.isEmpty(atcID)) {
						entry.setCOM1(rs.getString(27));
						Controller ctr = new Controller(rs.getInt(31));
						ctr.setPosition(rs.getDouble(29), rs.getDouble(30));
						ctr.setCallsign(atcID);
						try {
							ctr.setFacility(Facility.valueOf(atcID.substring(atcID.lastIndexOf('_') + 1)));
						} catch (IllegalArgumentException iae) {
							ctr.setFacility(Facility.CTR);
						} finally {
							entry.setController(ctr);
						}
					}
					
					results.add(entry);
				}
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the takeoff and landing coordinates for a particular flight. More than two results may be returned, if
	 * the aircraft bounced on takeoff and/or landing.
	 * @param flightID the flight ID
	 * @param isArchived TRUE if the flight data is archived, otherwise FALSE 
	 * @return a List of RouteEntry beans, ordered by time
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RouteEntry> getTakeoffLanding(int flightID, boolean isArchived) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT REPORT_TIME, TIME_MS, LAT, LNG, B_ALT, HEADING, VSPEED FROM acars.");
		buf.append(isArchived ? "POSITION_ARCHIVE" : "POSITIONS");
		buf.append(" WHERE (FLIGHT_ID=?) AND ((FLAGS & ?) > 0) ORDER BY REPORT_TIME, TIME_MS");
		
		try {
			prepareStatement(buf.toString());
			_ps.setInt(1, flightID);
			_ps.setInt(2, FLAG_TOUCHDOWN);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			List<RouteEntry> results = new ArrayList<RouteEntry>();
			while (rs.next()) {
				java.util.Date dt = new java.util.Date(rs.getTimestamp(1).getTime() + rs.getInt(2));
				RouteEntry entry = new RouteEntry(dt, new GeoPosition(rs.getDouble(3), rs.getDouble(4)));
				entry.setAltitude(rs.getInt(5));
				entry.setHeading(rs.getInt(6));
				entry.setVerticalSpeed(rs.getInt(7));
				results.add(entry);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Checks if in-flight refueling was used on a Flight.
	 * @param flightID the ACARS Flight ID
	 * @param isArchived TRUE if the flight data is archived, otherwise FALSE
	 * @return a {@link FuelUse} bean with fuel data
	 * @throws DAOException if a JDBC error occurs
	 */
	public FuelUse checkRefuel(int flightID, boolean isArchived) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT FUEL, FLAGS FROM acars.");
		sqlBuf.append(isArchived ? "POSITION_ARCHIVE" : "POSITIONS");
		sqlBuf.append(" WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME, TIME_MS");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, flightID);
			
			// Execute the query
			FuelUse use = new FuelUse(); int lastFuel = 0;
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				int fuel = rs.getInt(1);
				if (lastFuel != 0) {
					int fuelDelta = (lastFuel - fuel); 
					if (fuelDelta < -FuelUse.MAX_DELTA) {
						boolean isAirborne = ((rs.getInt(2) & ACARSFlags.FLAG_ONGROUND) != 0);
						if (!isAirborne)
							use.setRefuel(true);
					} else if (fuelDelta > 0)
						use.addFuelUse(fuelDelta);
				}
				
				lastFuel = fuel;
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return use;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the filed route for a particular ACARS flight.
	 * @param flightID the ACARS flight ID
	 * @return the filed route
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getRoute(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ROUTE from acars.FLIGHTS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, flightID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			String result = rs.next() ? rs.getString(1) : null;

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Searches for a duplicate flight ID created at the same time by the same user.
	 * @param id the ACARS flight ID
	 * @return the duplicate flight ID, or zero if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getDuplicateID(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT F.ID, C.PILOT_ID FROM acars.FLIGHTS F, acars.CONS C WHERE (F.CON_ID=C.ID) AND "
				+ "(F.CREATED=(SELECT CREATED FROM acars.FLIGHTS WHERE (ID=?) LIMIT 1)) AND (C.PILOT_ID=(SELECT C.PILOT_ID "
				+ "FROM acars.CONS C, acars.FLIGHTS F WHERE (C.ID=F.CON_ID) AND (F.ID=?) LIMIT 1)) AND (F.ID<>?) ORDER BY F.ID LIMIT 1");
			_ps.setInt(1, id);
			_ps.setInt(2, id);
			_ps.setInt(3, id);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			int dupeID = rs.next() ? rs.getInt(1) : 0;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return dupeID;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns information about a particular ACARS flight.
	 * @param flightID the ACARS flight ID
	 * @return the Flight Information, nor null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightInfo getInfo(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT F.*, FD.ROUTE_ID, FDR.DISPATCHER_ID, C.PILOT_ID FROM "
					+ "acars.FLIGHTS F LEFT JOIN acars.CONS C ON (F.CON_ID=C.ID) LEFT JOIN acars.FLIGHT_DISPATCH FD "
					+ "ON (F.ID=FD.ID) LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID) WHERE (F.ID=?) LIMIT 1");
			_ps.setInt(1, flightID);

			// Get the first entry, or null
			List<FlightInfo> results = executeFlightInfo();
			FlightInfo info = results.isEmpty() ? null : results.get(0);
			if (info == null)
				return null;

			// Get the terminal routes
			Map<Integer, TerminalRoute> routes = getTerminalRoutes(info.getID());
			info.setSID(routes.get(Integer.valueOf(TerminalRoute.SID)));
			info.setSTAR(routes.get(Integer.valueOf(TerminalRoute.STAR)));
			
			// Fetch the takeoff and landing runways
			if (info.getHasPIREP()) {
				prepareStatementWithoutLimits("SELECT R.*, IFNULL(ND.HDG, 0), ND.FREQ FROM acars.RWYDATA R LEFT JOIN "
						+ "common.NAVDATA ND ON (R.ICAO=ND.CODE) AND (R.RUNWAY=ND.NAME) AND (ND.ITEMTYPE=?) "
						+ "WHERE (ID=?) LIMIT 2");
				_ps.setInt(1, NavigationDataBean.RUNWAY);
				_ps.setInt(2, flightID);
				
				// Execute the query
				ResultSet rs = _ps.executeQuery();
				while (rs.next()) {
					Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
					r.setCode(rs.getString(2));
					r.setName(rs.getString(3));
					r.setLength(rs.getInt(6));
					r.setHeading(rs.getInt(9));
					r.setFrequency(rs.getString(10));
					if (rs.getBoolean(8))
						info.setRunwayD(new RunwayDistance(r, rs.getInt(7)));
					else
						info.setRunwayA(new RunwayDistance(r, rs.getInt(7)));
				}

				rs.close();
				_ps.close();
			}

			// Count the number of position records
			prepareStatement("SELECT COUNT(*), AVG(FRAMERATE) FROM acars." + (info.getArchived() ? "POSITION_ARCHIVE" : "POSITIONS")
					+ " WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				info.setPositionCount(rs.getInt(1));
				info.setAverageFrameRate(rs.getDouble(2));
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return info;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns information about a particular ACARS connection.
	 * @param conID the ACARS connection ID
	 * @return the last FlightInfo bean associated with a particular connection, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightInfo getInfo(long conID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT F.*, FD.ROUTE_ID, FDR.DISPATCHER_ID, C.PILOT_ID FROM "
					+ "acars.CONS C, acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) LEFT JOIN "
					+ "acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID) WHERE (F.CON_ID=C.ID) AND (C.ID=CONV(?,10,16)) "
					+ "ORDER BY F.CREATED DESC LIMIT 1");
			_ps.setLong(1, conID);

			// Get the first entry, or null
			List<FlightInfo> results = executeFlightInfo();
			FlightInfo info = results.isEmpty() ? null : results.get(0);
			if (info == null)
				return null;

			// Get the terminal routes
			Map<Integer, TerminalRoute> routes = getTerminalRoutes(info.getID());
			info.setSID(routes.get(Integer.valueOf(TerminalRoute.SID)));
			info.setSTAR(routes.get(Integer.valueOf(TerminalRoute.STAR)));

			// Count the number of position records
			prepareStatement("SELECT COUNT(*) FROM acars." + (info.getArchived() ? "POSITION_ARCHIVE" : "POSITIONS")
					+ " WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, info.getID());

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			info.setPositionCount(rs.next() ? rs.getInt(1) : 0);

			// Clean up and return
			rs.close();
			_ps.close();
			return info;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns information about a particular ACARS connection.
	 * @param conID the ACARS connection ID
	 * @return the Connection information, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ConnectionEntry getConnection(long conID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET_NTOA(C.REMOTE_ADDR), "
					+ "C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD, C.DISPATCH, COUNT(DISTINCT F.ID), COUNT(P.REPORT_TIME) "
					+ "FROM acars.CONS C LEFT JOIN acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.POSITIONS P ON "
					+ "(F.ID=P.FLIGHT_ID) WHERE (C.ID=CONV(?,10,16)) GROUP BY C.ID LIMIT 1");
			_ps.setLong(1, conID);

			// Get the first entry, or null
			List<ConnectionEntry> results = executeConnectionInfo();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads the Terminal Routes used on a particular ACARS flight.
	 * @param id the ACARS flight ID
	 * @return a Map of TerminalRoutes, keyed by route type
	 * @throws DAOException if a JDBC error occurs
	 */
	protected Map<Integer, TerminalRoute> getTerminalRoutes(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT IF(FS.TYPE=?, F.AIRPORT_D, F.AIRPORT_A), FS.* FROM acars.FLIGHTS F, "
					+ "acars.FLIGHT_SIDSTAR FS WHERE (F.ID=?) AND (F.ID=FS.ID)");
			_ps.setInt(1, TerminalRoute.SID);
			_ps.setInt(2, id);

			// Execute the query
			Map<Integer, TerminalRoute> results = new HashMap<Integer, TerminalRoute>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				TerminalRoute tr = new TerminalRoute(SystemData.getAirport(rs.getString(1)), rs.getString(4), rs
						.getInt(3));
				tr.setTransition(rs.getString(5));
				tr.setRunway(rs.getString(6));
				results.put(Integer.valueOf(tr.getType()), tr);
			}

			// Clean up
			rs.close();
			_ps.close();

			// Load the waypoint data
			prepareStatementWithoutLimits("SELECT TYPE, CODE, WPTYPE, LATITUDE, LONGITUDE, REGION "
				+ "FROM acars.FLIGHT_SIDSTAR_WP WHERE (ID=?) ORDER BY TYPE, SEQ");
			_ps.setInt(1, id);
			rs = _ps.executeQuery();
			while (rs.next()) {
				TerminalRoute tr = results.get(Integer.valueOf(rs.getInt(1)));
				if (tr != null) {
					NavigationDataBean nd = NavigationDataBean.create(rs.getInt(3), rs.getDouble(4), rs.getDouble(5));
					nd.setCode(rs.getString(2));
					nd.setRegion(rs.getString(6));
					tr.addWaypoint(nd);
				}
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Flight Info result sets.
	 */
	protected List<FlightInfo> executeFlightInfo() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		List<FlightInfo> results = new ArrayList<FlightInfo>();
		while (rs.next()) {
			long conID = Long.parseLong(rs.getString(2), 16);
			FlightInfo info = new FlightInfo(rs.getInt(1), conID);
			info.setStartTime(rs.getTimestamp(3));
			info.setEndTime(rs.getTimestamp(4));
			info.setFlightCode(rs.getString(5));
			info.setEquipmentType(rs.getString(6));
			info.setAltitude(rs.getString(7));
			info.setAirportD(SystemData.getAirport(rs.getString(8)));
			info.setAirportA(SystemData.getAirport(rs.getString(9)));
			info.setAirportL(SystemData.getAirport(rs.getString(10)));
			info.setRoute(rs.getString(11));
			info.setRemarks(rs.getString(12));
			info.setFSVersion(rs.getInt(13));
			info.setOffline(rs.getBoolean(14));
			info.setHasPIREP(rs.getBoolean(15));
			info.setArchived(rs.getBoolean(16));
			info.setScheduleValidated(rs.getBoolean(17));
			info.setDispatchPlan(rs.getBoolean(18));
			info.setIsMP(rs.getBoolean(19));
			info.setRouteID(rs.getInt(20));
			info.setDispatcherID(rs.getInt(21));
			info.setPilotID(rs.getInt(22));

			// Add to results
			results.add(info);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to parse Connection result sets.
	 */
	protected List<ConnectionEntry> executeConnectionInfo() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasMessageCounts = (rs.getMetaData().getColumnCount() > 10);
		List<ConnectionEntry> results = new ArrayList<ConnectionEntry>();
		while (rs.next()) {
			boolean isDispatch = rs.getBoolean(9);
			long id = Long.parseLong(rs.getString(1), 16);
			ConnectionEntry entry = isDispatch ? new DispatchConnectionEntry(id) : new ConnectionEntry(id);
			entry.setAuthorID(rs.getInt(2));
			entry.setStartTime(rs.getTimestamp(3));
			entry.setEndTime(rs.getTimestamp(4));
			entry.setRemoteAddr(rs.getString(5));
			entry.setRemoteHost(rs.getString(6));
			entry.setClientBuild(rs.getInt(7));
			entry.setBeta(rs.getInt(8));
			if (hasMessageCounts) {
				entry.setFlightInfoCount(rs.getInt(10));
				entry.setPositionCount(rs.getInt(11));
			}

			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}