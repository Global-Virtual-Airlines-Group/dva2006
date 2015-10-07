// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import static org.gvagroup.acars.ACARSFlags.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.file.GetSerializedPosition;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load ACARS position data.
 * @author Luke
 * @version 6.2
 * @since 4.1
 */

public class GetACARSPositions extends GetACARSData {
	
	private static final Logger log = Logger.getLogger(GetACARSPositions.class);

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
			prepareStatementWithoutLimits("SELECT P.REPORT_TIME, P.LAT, P.LNG, P.B_ALT, P.R_ALT, P.HEADING, P.PITCH, P.BANK, "
				+ "P.ASPEED, P.GSPEED, P.VSPEED, P.MACH, P.N1, P.N2, P.FLAPS, P.WIND_HDG, P.WIND_SPEED, P.FUEL, P.FUELFLOW, "
				+ "P.AOA, P.GFORCE, P.FLAGS, P.FRAMERATE, P.SIM_RATE, P.PHASE, P.NAV1, P.NAV2 FROM acars.POSITIONS P WHERE "
				+ "(P.FLIGHT_ID=?) ORDER BY P.REPORT_TIME");
			_ps.setInt(1, flightID);

			// Execute the query
			Map<Long, GeoLocation> results = new LinkedHashMap<Long, GeoLocation>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Long ts = Long.valueOf(rs.getTimestamp(1).getTime());
					GeoLocation pos = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
					ACARSRouteEntry entry = new ACARSRouteEntry(rs.getTimestamp(1), pos);
					entry.setFlags(rs.getInt(22));

					// Add to results - or just log a GeoPosition if we're on the ground
					if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN) && !includeOnGround)
						results.put(ts, pos);
					else {
						entry.setAltitude(rs.getInt(4));
						entry.setRadarAltitude(rs.getInt(5));
						entry.setHeading(rs.getInt(6));
						entry.setPitch(rs.getDouble(7));
						entry.setBank(rs.getDouble(8));
						entry.setAirSpeed(rs.getInt(9));
						entry.setGroundSpeed(rs.getInt(10));
						entry.setVerticalSpeed(rs.getInt(11));
						entry.setMach(rs.getDouble(12));
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
						entry.setPhase(rs.getInt(25));
						entry.setNAV1(rs.getString(26));
						entry.setNAV2(rs.getString(27));
						results.put(ts, entry);
					}
				}
			}

			_ps.close();
			
			// Load ATC data
			prepareStatementWithoutLimits("SELECT REPORT_TIME, IDX, COM1, CALLSIGN, LAT, LNG, NETWORK_ID FROM acars.POSITION_ATC "
				+ "WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Long ts = Long.valueOf(rs.getTimestamp(1).getTime());
					GeoLocation loc = results.get(ts);
					if ((loc != null) && (loc instanceof ACARSRouteEntry)) {
						ACARSRouteEntry entry = (ACARSRouteEntry) loc;
						int idx = rs.getInt(2);
						if (idx == 2)
							entry.setCOM2(rs.getString(3));
						else
							entry.setCOM1(rs.getString(3));

						// Set controller
						String atcID = rs.getString(4);
						Controller ctr = new Controller(rs.getInt(7));
						ctr.setPosition(rs.getDouble(5), rs.getDouble(6));
						ctr.setCallsign(atcID);
						
						// Load ATC info
						try {
							ctr.setFacility(Facility.valueOf(atcID.substring(atcID.lastIndexOf('_') + 1)));
						} catch (IllegalArgumentException iae) {
							ctr.setFacility(Facility.CTR);
						} finally {
							if (idx == 2)
								entry.setATC2(ctr);
							else
								entry.setATC1(ctr);
						}
					}
				}
			}
			
			_ps.close();
			return new ArrayList<GeoLocation>(results.values());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private List<GeoLocation> getArchivedEntries(int flightID, boolean includeOnGround) throws DAOException {
		
		// Load from file system if present
		String hash = Integer.toHexString(flightID % 2048);
		File path = new File(SystemData.get("path.archive"), hash);
		File f = new File(path, Integer.toHexString(flightID) + ".dat");
		if (f.exists()) {
			Collection<? extends RouteEntry> entries = null;
			try (InputStream in = new FileInputStream(f)) {
				try (InputStream gi = new GZIPInputStream(in, 8192)) {
					GetSerializedPosition psdao = new GetSerializedPosition(gi);	
					entries = psdao.read();
				}
				
				// Deserialize
				List<GeoLocation> results = new ArrayList<GeoLocation>();
				for (RouteEntry entry : entries) {
					if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN) && !includeOnGround)
						results.add(new GeoPosition(entry));
					else
						results.add(entry);
				}

				return results;
			} catch (IOException ie) {
				log.warn("Error reading " + f.getAbsolutePath() + " - " + ie.getMessage());
			}
		}
		
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
			prepareStatementWithoutLimits("SELECT * FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);
			
			// Execute the query
			List<XARouteEntry> results = new ArrayList<XARouteEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					XARouteEntry re = new XARouteEntry(new GeoPosition(rs.getDouble(3), rs.getDouble(4)), rs.getTimestamp(2));
					re.setAltitude(rs.getInt(5));
					re.setHeading(rs.getInt(6));
					re.setAirSpeed(rs.getInt(7));
					re.setGroundSpeed(rs.getInt(8));
					re.setVerticalSpeed(rs.getInt(9));
					re.setMach(rs.getDouble(10));
					re.setFuelRemaining(rs.getInt(11));
					re.setPhase(rs.getInt(12));
					re.setFlags(rs.getInt(13));
					re.setWindHeading(rs.getInt(14));
					re.setWindSpeed(rs.getInt(15));
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