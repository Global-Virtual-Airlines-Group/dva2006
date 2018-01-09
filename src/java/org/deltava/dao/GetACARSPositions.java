// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import static org.gvagroup.acars.ACARSFlags.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.AirspaceType;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.file.GetSerializedPosition;

/**
 * A Data Access Object to load ACARS position data.
 * @author Luke
 * @version 8.1
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
	public List<GeospaceLocation> getRouteEntries(int flightID, boolean includeOnGround, boolean isArchived) throws DAOException {
		return isArchived ? getArchivedEntries(flightID, includeOnGround) : getLiveEntries(flightID, includeOnGround);
	}
	
	private List<GeospaceLocation> getLiveEntries(int flightID, boolean includeOnGround) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT REPORT_TIME, LAT, LNG, B_ALT, R_ALT, HEADING, PITCH, BANK, ASPEED, GSPEED, VSPEED, MACH, N1, N2, FLAPS, "
				+ "WIND_HDG, WIND_SPEED, TEMP, PRESSURE, VIZ, FUEL, FUELFLOW, AOA, GFORCE, FLAGS, FRAMERATE, SIM_RATE, SIM_TIME, PHASE, NAV1, NAV2, VAS, "
				+ "ASTYPE FROM acars.POSITIONS WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);

			// Execute the query
			Map<Long, GeospaceLocation> results = new LinkedHashMap<Long, GeospaceLocation>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Long ts = Long.valueOf(rs.getTimestamp(1).getTime());
					GeospaceLocation pos = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
					ACARSRouteEntry entry = new ACARSRouteEntry(rs.getTimestamp(1).toInstant(), pos);
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
					entry.setTemperature(rs.getInt(18));
					entry.setPressure(rs.getInt(19));
					entry.setVisibility(rs.getDouble(20));
					entry.setFuelRemaining(rs.getInt(21));
					entry.setFuelFlow(rs.getInt(22));
					entry.setAOA(rs.getDouble(23));
					entry.setG(rs.getDouble(24));
					entry.setFlags(rs.getInt(25));
					entry.setFrameRate(rs.getInt(26));
					entry.setSimRate(rs.getInt(27));
					entry.setSimUTC(toInstant(rs.getTimestamp(28)));
					entry.setPhase(rs.getInt(29));
					entry.setNAV1(rs.getString(30));
					entry.setNAV2(rs.getString(31));
					entry.setVASFree(rs.getInt(32));
					entry.setAirspace(AirspaceType.values()[rs.getInt(33)]);

					// Add to results - or just log a GeoPosition if we're on the ground
					if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN) && !includeOnGround && !entry.isWarning())
						results.put(ts, pos);
					else
						results.put(ts, entry);
				}
			}

			_ps.close();
			
			// Load ATC data
			prepareStatementWithoutLimits("SELECT REPORT_TIME, IDX, COM1, CALLSIGN, LAT, LNG, NETWORK_ID FROM acars.POSITION_ATC WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
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
						Controller ctr = new Controller(rs.getInt(7), null);
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
			return new ArrayList<GeospaceLocation>(results.values());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private List<GeospaceLocation> getArchivedEntries(int flightID, boolean includeOnGround) throws DAOException {
		
		// Get archive metadata and file pointer
		ArchiveMetadata md = getArchiveInfo(flightID);
		File f = ArchiveHelper.getPositions(flightID);
		if ((md == null) || !f.exists())
			return Collections.emptyList();
		
		// Validate size
		if ((md.getSize() > 0) && (f.length() != md.getSize()))
			throw new DAOException("Flight " + flightID + " Invalid file size, expected " + md.getSize() + ", got " + f.length() + " bytes");
		
		// Validate CRC-32
		byte[] rawData = null; CRC32 crc = new CRC32();
		try (InputStream is = ArchiveHelper.getStream(f)) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream(8192)) {
				byte[] buffer = new byte[8192];
				int bytesRead = is.read(buffer);
				while (bytesRead > 0) {
					crc.update(buffer, 0, bytesRead);
					out.write(buffer, 0, bytesRead);
					bytesRead = is.read(buffer);
				}
				
				rawData = out.toByteArray();
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		if ((md.getCRC32() != 0) && (md.getCRC32() != crc.getValue()))
			throw new DAOException("Flight " + flightID + " Invalid CRC32, expected " + Long.toHexString(md.getCRC32()) + ", got " + Long.toHexString(crc.getValue()));
		
		// Deserialize and validate
		List<GeospaceLocation> results = new ArrayList<GeospaceLocation>();
		try (GZIPInputStream gi = new GZIPInputStream(new ByteArrayInputStream(rawData))) {
			GetSerializedPosition psdao = new GetSerializedPosition(gi);
			Collection<? extends RouteEntry> entries = psdao.read();
			for (RouteEntry entry : entries) {
				if (entry.isFlagSet(FLAG_ONGROUND) && !entry.isFlagSet(FLAG_TOUCHDOWN) && !includeOnGround && !entry.isWarning())
					results.add(new GeoPosition(entry));
				else
					results.add(entry);
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}

		return results;
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
					XARouteEntry re = new XARouteEntry(new GeoPosition(rs.getDouble(3), rs.getDouble(4)), rs.getTimestamp(2).toInstant());
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