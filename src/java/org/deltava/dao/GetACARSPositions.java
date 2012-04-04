// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import static org.gvagroup.acars.ACARSFlags.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.servinfo.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load ACARS position data.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class GetACARSPositions extends GetACARSData {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSPositions(Connection c) {
		super(c);
	}

	/**
	 * Loads completed route data for a particular ACARS filght ID, including data when on the ground.
	 * @param flightID the ACARS flight ID
	 * @param isArchived TRUE if the positions should be read from the archive, otherwise FALSE
	 * @return a List of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetACARSPositions#getRouteEntries(int, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<ACARSRouteEntry> getRouteEntries(int flightID, boolean isArchived) throws DAOException {
		List<?> results = getRouteEntries(flightID, true, isArchived);
		return (List<ACARSRouteEntry>) results;
	}

	/**
	 * Loads complete route data for a particular ACARS flight ID.
	 * @param flightID the ACARS flight ID
	 * @param includeOnGround TRUE if entries on the ground are RouteEntry beans, otherwise FALSE
	 * @param isArchived TRUE if the positions should be read from the archive, otherwise FALSE
	 * @return a List of GeoLocation beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetACARSPositions#getRouteEntries(int, boolean)
	 */
	public List<GeoLocation> getRouteEntries(int flightID, boolean includeOnGround, boolean isArchived) throws DAOException {
		return isArchived ? getArchivedEntries(flightID, includeOnGround) : getLiveEntries(flightID, includeOnGround);
	}
	
	private List<GeoLocation> getLiveEntries(int flightID, boolean includeOnGround) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT P.REPORT_TIME, P.TIME_MS, P.LAT, P.LNG, P.B_ALT, P.R_ALT, P.HEADING, "
				+ "P.PITCH, P.BANK, P.ASPEED, P.GSPEED, P.VSPEED, P.MACH, P.N1, P.N2, P.FLAPS, P.WIND_HDG, P.WIND_SPEED, P.FUEL, "
				+ "P.FUELFLOW, P.AOA, P.GFORCE, P.FLAGS, P.FRAMERATE, P.SIM_RATE, P.PHASE, PA.COM1, PA.CALLSIGN, PA.LAT, PA.LNG, "
				+ "PA.NETWORK_ID FROM acars.POSITIONS P LEFT JOIN acars.POSITION_ATC PA ON (PA.FLIGHT_ID=P.FLIGHT_ID) AND "
				+ "(PA.REPORT_TIME=P.REPORT_TIME) AND (PA.TIME_MS=P.TIME_MS) WHERE (P.FLIGHT_ID=?) ORDER BY P.REPORT_TIME, P.TIME_MS");
			_ps.setInt(1, flightID);

			// Execute the query
			List<GeoLocation> results = new ArrayList<GeoLocation>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					java.util.Date dt = new java.util.Date(rs.getTimestamp(1).getTime() + rs.getInt(2));
					ACARSRouteEntry entry = new ACARSRouteEntry(dt, new GeoPosition(rs.getDouble(3), rs.getDouble(4)));
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
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private List<GeoLocation> getArchivedEntries(int flightID, boolean includeOnGround) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DATA FROM acars.POS_ARCHIVE WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, flightID);
			
			// Load the data
			InputStream in = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					in = new ByteArrayInputStream(rs.getBytes(1));
			}
			
			_ps.close();
			if (in == null)
				return Collections.emptyList();
			
			// Deserialize and validate
			GetSerializedPosition psdao = new GetSerializedPosition(in);
			Collection<? extends RouteEntry> entries = psdao.read();

			// Deserialize
			List<GeoLocation> results = new ArrayList<GeoLocation>();
			for (RouteEntry entry : entries) {
				if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN) && !includeOnGround)
					results.add(new GeoPosition(entry));
				else
					results.add(entry);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves XACARS position entries from the database.
	 * @param flightID the flight ID
	 * @return a List of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<XARouteEntry> getXACARSEntries(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME, TIME_MS");
			_ps.setInt(1, flightID);
			
			// Execute the query
			List<XARouteEntry> results = new ArrayList<XARouteEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					java.util.Date dt = new java.util.Date(rs.getTimestamp(2).getTime() + rs.getInt(3));
					XARouteEntry re = new XARouteEntry(new GeoPosition(rs.getDouble(4), rs.getDouble(5)), dt);
					re.setAltitude(rs.getInt(6));
					re.setHeading(rs.getInt(7));
					re.setAirSpeed(rs.getInt(8));
					re.setGroundSpeed(rs.getInt(9));
					re.setVerticalSpeed(rs.getInt(10));
					re.setMach(rs.getDouble(11));
					re.setFuelRemaining(rs.getInt(12));
					re.setPhase(rs.getInt(13));
					re.setFlags(rs.getInt(14));
					re.setWindHeading(rs.getInt(15));
					re.setWindSpeed(rs.getInt(16));
					results.add(re);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the average frame rate for a Flight.
	 * @param flightID the ACARS flight ID
	 * @return the rate in frames per second
	 * @throws DAOException if a JDBC error occurs
	 */
	public double getFrameRate(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT AVG(FRAMERATE) FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			
			double frameRate = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					frameRate = rs.getDouble(1);
			}
			
			_ps.close();
			return frameRate;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}