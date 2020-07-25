// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

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
 * @version 9.1
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
			Map<Long, GeospaceLocation> results = new LinkedHashMap<Long, GeospaceLocation>();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT REPORT_TIME, LAT, LNG, B_ALT, R_ALT, HEADING, PITCH, BANK, ASPEED, GSPEED, VSPEED, MACH, N1, N2, FLAPS, WIND_HDG, WIND_SPEED, TEMP, PRESSURE, VIZ, FUEL, "
				+ "FUELFLOW, AOA, CG, GFORCE, FLAGS, FRAMERATE, SIM_RATE, SIM_TIME, PHASE, NAV1, NAV2, ADF1, VAS, WEIGHT, ASTYPE, GNDFLAGS, NET_CONNECTED, ENC_N1, ENC_N2 FROM acars.POSITIONS WHERE (FLIGHT_ID=?) "
				+ "ORDER BY REPORT_TIME")) {
				ps.setInt(1, flightID);
				try (ResultSet rs = ps.executeQuery()) {
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
						entry.setCG(rs.getDouble(24));
						entry.setG(rs.getDouble(25));
						entry.setFlags(rs.getInt(26));
						entry.setFrameRate(rs.getInt(27));
						entry.setSimRate(rs.getInt(28));
						entry.setSimUTC(toInstant(rs.getTimestamp(29)));
						entry.setPhase(FlightPhase.values()[rs.getInt(30)]);
						entry.setNAV1(rs.getString(31));
						entry.setNAV2(rs.getString(32));
						entry.setADF1(rs.getString(33));
						entry.setVASFree(rs.getInt(34));
						entry.setWeight(rs.getInt(35));
						entry.setAirspace(AirspaceType.values()[rs.getInt(36)]);
						entry.setGroundOperations(rs.getInt(37));
						entry.setNetworkConnected(rs.getBoolean(38));
						double[] n1 = EngineSpeedEncoder.decode(rs.getBytes(39));
						double[] n2 = EngineSpeedEncoder.decode(rs.getBytes(40));
						for (int eng = 0; eng < n1.length; eng++) {
							entry.setN1(eng, n1[eng]);
							entry.setN2(eng, n2[eng]);
						}
						
						// Add to results - or just log a GeoPosition if we're on the ground
						if (entry.isFlagSet(ACARSFlags.ONGROUND) && !entry.isFlagSet(ACARSFlags.TOUCHDOWN) && !includeOnGround && !entry.isWarning())
							results.put(ts, pos);
						else
							results.put(ts, entry);
					}
				}
			}

			// Load ATC data
			try (PreparedStatement ps = prepareWithoutLimits("SELECT REPORT_TIME, IDX, COM1, CALLSIGN, LAT, LNG, NETWORK_ID FROM acars.POSITION_ATC WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME")) {
				ps.setInt(1, flightID);
				try (ResultSet rs = ps.executeQuery()) {
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
			}
			
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
				if (entry.isFlagSet(ACARSFlags.ONGROUND) && !entry.isFlagSet(ACARSFlags.TOUCHDOWN) && !includeOnGround && !entry.isWarning())
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME")) {
			ps.setInt(1, flightID);
			
			// Execute the query
			List<XARouteEntry> results = new ArrayList<XARouteEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					XARouteEntry re = new XARouteEntry(new GeoPosition(rs.getDouble(3), rs.getDouble(4)), rs.getTimestamp(2).toInstant());
					re.setAltitude(rs.getInt(5));
					re.setHeading(rs.getInt(6));
					re.setAirSpeed(rs.getInt(7));
					re.setGroundSpeed(rs.getInt(8));
					re.setVerticalSpeed(rs.getInt(9));
					re.setMach(rs.getDouble(10));
					re.setFuelRemaining(rs.getInt(11));
					re.setPhase(FlightPhase.values()[rs.getInt(12)]);
					re.setFlags(rs.getInt(13));
					re.setWindHeading(rs.getInt(14));
					re.setWindSpeed(rs.getInt(15));
					results.add(re);
				}
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AVG(FRAMERATE) FROM acars.POSITIONS WHERE (FLIGHT_ID=?)")){
			ps.setInt(1, flightID);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getDouble(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}