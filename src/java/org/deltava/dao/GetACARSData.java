// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.system.SystemData;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A Data Access Object to load ACARS information.
 * @author Luke
 * @version 1.0
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
		List results = getRouteEntries(flightID, true, isArchived);
		return results;
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
	public List<GeoLocation> getRouteEntries(int flightID, boolean includeOnGround, boolean isArchived)
			throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT REPORT_TIME, TIME_MS, LAT, LNG, B_ALT, R_ALT, HEADING, "
				+ "PITCH, BANK, ASPEED, GSPEED, VSPEED, N1, N2, FLAPS, WIND_HDG, WIND_SPEED, FUEL, FUELFLOW, "
				+ "AOA, GFORCE, FLAGS, FRAMERATE, SIM_RATE FROM acars.");
		sqlBuf.append(isArchived ? "POSITION_ARCHIVE" : "POSITIONS");
		sqlBuf.append(" WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME, TIME_MS");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, flightID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			List<GeoLocation> results = new ArrayList<GeoLocation>();
			while (rs.next()) {
				java.util.Date dt = new java.util.Date(rs.getTimestamp(1).getTime() + rs.getInt(2));
				RouteEntry entry = new RouteEntry(dt, rs.getDouble(3), rs.getDouble(4));
				entry.setFlags(rs.getInt(22));

				// Add to results - or just log a GeoPosition if we're on the ground
				if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN)
						&& !includeOnGround) {
					results.add(new GeoPosition(entry));
				} else {
					entry.setAltitude(rs.getInt(5));
					entry.setRadarAltitude(rs.getInt(6));
					entry.setHeading(rs.getInt(7));
					entry.setPitch(rs.getDouble(8));
					entry.setBank(rs.getDouble(9));
					entry.setAirSpeed(rs.getInt(10));
					entry.setGroundSpeed(rs.getInt(11));
					entry.setVerticalSpeed(rs.getInt(12));
					entry.setN1(rs.getDouble(13));
					entry.setN2(rs.getDouble(14));
					entry.setFlaps(rs.getInt(15));
					entry.setWindHeading(rs.getInt(16));
					entry.setWindSpeed(rs.getInt(17));
					entry.setFuelRemaining(rs.getInt(18));
					entry.setFuelFlow(rs.getInt(19));
					entry.setAOA(rs.getDouble(20));
					entry.setG(rs.getDouble(21));
					entry.setFrameRate(rs.getInt(23));
					entry.setSimRate(rs.getInt(24));
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
	 * Returns the filed route for a particular ACARS flight.
	 * @param flightID the ACARS flight ID
	 * @return the filed route
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getRoute(int flightID) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT ROUTE from acars.FLIGHTS WHERE (ID=?)");
			_ps.setInt(1, flightID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			String result = (rs.next()) ? rs.getString(1) : null;
			setQueryMax(0);

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
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
			setQueryMax(1);
			prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.FLIGHTS F, acars.CONS C WHERE (F.CON_ID=C.ID) "
					+ "AND (F.ID=?)");
			_ps.setInt(1, flightID);

			// Get the first entry, or null
			List<FlightInfo> results = executeFlightInfo();
			setQueryMax(0);
			FlightInfo info = results.isEmpty() ? null : results.get(0);
			if (info == null)
				return null;

			// Count the number of position records
			prepareStatement("SELECT COUNT(*) FROM acars." + (info.getArchived() ? "POSITION_ARCHIVE" : "POSITIONS")
					+ " WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);

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
	 * @return the last FlightInfo bean associated with a particular connection, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightInfo getInfo(long conID) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.FLIGHTS F, acars.CONS C WHERE (F.CON_ID=C.ID) "
					+ "AND (C.ID=?) ORDER BY F.CREATED DESC");
			_ps.setLong(1, conID);

			// Get the first entry, or null
			List<FlightInfo> results = executeFlightInfo();
			setQueryMax(0);
			FlightInfo info = results.isEmpty() ? null : results.get(0);

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
			setQueryMax(1);
			prepareStatement("SELECT C.ID, C.PILOT_ID, C.DATE, INET_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, "
					+ "C.CLIENT_BUILD, COUNT(DISTINCT F.ID), COUNT(P.REPORT_TIME) FROM acars.CONS C LEFT JOIN acars.FLIGHTS F "
					+ "ON (C.ID=F.CON_ID) LEFT JOIN acars.POSITIONS P ON (F.ID=P.FLIGHT_ID) WHERE (C.ID=?) GROUP BY C.ID");
			_ps.setLong(1, conID);

			// Get the first entry, or null
			List<ConnectionEntry> results = executeConnectionInfo();
			setQueryMax(0);
			return results.isEmpty() ? null : results.get(0);
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

		// Iterate through the results
		List<FlightInfo> results = new ArrayList<FlightInfo>();
		while (rs.next()) {
			FlightInfo info = new FlightInfo(rs.getInt(1), rs.getLong(2));
			info.setStartTime(rs.getTimestamp(3));
			info.setEndTime(rs.getTimestamp(4));
			info.setFlightCode(rs.getString(5));
			info.setEquipmentType(rs.getString(6));
			info.setAltitude(rs.getString(7));
			info.setAirportD(SystemData.getAirport(rs.getString(8)));
			info.setAirportA(SystemData.getAirport(rs.getString(9)));
			info.setRoute(rs.getString(11));
			info.setRemarks(rs.getString(12));
			info.setFSVersion(rs.getInt(13));
			info.setOffline(rs.getBoolean(14));
			info.setHasPIREP(rs.getBoolean(15));
			info.setArchived(rs.getBoolean(16));
			info.setPilotID(rs.getInt(17));

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
		ResultSetMetaData md = rs.getMetaData();
		boolean hasMessageCounts = (md.getColumnCount() > 7);

		// Iterate through the results
		List<ConnectionEntry> results = new ArrayList<ConnectionEntry>();
		while (rs.next()) {
			ConnectionEntry entry = new ConnectionEntry(rs.getLong(1));
			entry.setPilotID(rs.getInt(2));
			entry.setStartTime(rs.getTimestamp(3));
			entry.setRemoteAddr(rs.getString(4));
			entry.setRemoteHost(rs.getString(5));
			entry.setClientBuild(rs.getInt(6));
			if (hasMessageCounts) {
				entry.setFlightInfoCount(rs.getInt(7));
				entry.setPositionCount(rs.getInt(8));
			}

			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}