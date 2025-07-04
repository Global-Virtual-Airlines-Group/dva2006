// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.AirspaceType;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.file.GetSerializedPosition;

/**
 * A Data Access Object to load ACARS position data.
 * @author Luke
 * @version 11.6
 * @since 4.1
 */

public class GetACARSPositions extends GetACARSData {
	
	private static final Logger log = LogManager.getLogger(GetACARSPositions.class);
	
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
			try (PreparedStatement ps = prepareWithoutLimits("SELECT REPORT_TIME, LAT, LNG, B_ALT, R_ALT, ALTIMETER, HEADING, PITCH, BANK, ASPEED, GSPEED, VSPEED, MACH, N1, N2, FLAPS, WIND_HDG, WIND_SPEED, TEMP, PRESSURE, VIZ, FUEL, "
				+ "FUELFLOW, AOA, CG, GFORCE, FLAGS, FRAMERATE, SIM_RATE, SIM_TIME, PHASE, NAV1, NAV2, ADF1, VAS, WEIGHT, ASTYPE, GNDFLAGS, NET_CONNECTED, ACARS_CONNECTED, RESTORE_COUNT, ENC_N1, ENC_N2 FROM acars.POSITIONS "
				+ "WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME")) {
				ps.setInt(1, flightID);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						Long ts = Long.valueOf(rs.getTimestamp(1).getTime());
						GeospaceLocation pos = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
						ACARSRouteEntry entry = new ACARSRouteEntry(rs.getTimestamp(1).toInstant(), pos);
						entry.setAltitude(rs.getInt(4));
						entry.setRadarAltitude(rs.getInt(5));
						entry.setAltimeter(rs.getInt(6));
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
						entry.setTemperature(rs.getInt(19));
						entry.setPressure(rs.getInt(20));
						entry.setVisibility(rs.getDouble(21));
						entry.setFuelRemaining(rs.getInt(22));
						entry.setFuelFlow(rs.getInt(23));
						entry.setAOA(rs.getDouble(24));
						entry.setCG(rs.getDouble(25));
						entry.setG(rs.getDouble(26));
						entry.setFlags(rs.getInt(27));
						entry.setFrameRate(rs.getInt(28));
						entry.setSimRate(rs.getInt(29));
						entry.setSimUTC(toInstant(rs.getTimestamp(30)));
						entry.setPhase(FlightPhase.values()[rs.getInt(31)]);
						entry.setNAV1(rs.getString(32));
						entry.setNAV2(rs.getString(33));
						entry.setADF1(rs.getString(34));
						entry.setVASFree(rs.getInt(35));
						entry.setWeight(rs.getInt(36));
						entry.setAirspace(AirspaceType.values()[rs.getInt(37)]);
						entry.setGroundOperations(rs.getInt(38));
						entry.setNetworkConnected(rs.getBoolean(39));
						entry.setACARSConnected(rs.getBoolean(40));
						entry.setRestoreCount(rs.getInt(41));
						double[] n1 = EngineSpeedEncoder.decode(rs.getBytes(42));
						double[] n2 = EngineSpeedEncoder.decode(rs.getBytes(43));
						entry.setEngineCount(Math.min(n1.length, n2.length));
						for (int eng = 0; eng < entry.getEngineCount(); eng++) {
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
						if ((loc != null) && (loc instanceof ACARSRouteEntry entry)) {
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
		
		// Get archive metadata
		ArchiveMetadata md = getArchiveInfo(flightID);
		if (md == null) {
			File f = ArchiveHelper.getPositions(flightID);
			throw new ArchiveValidationException(String.format("No metadata for Flight %d (exists=%s)", Integer.valueOf(flightID), Boolean.valueOf(f.exists())));
		}
		
		// Validate and Deserialize
		List<GeospaceLocation> results = new ArrayList<GeospaceLocation>();
		try {
			byte[] data = ArchiveHelper.load(md);
			if (data == null) return Collections.emptyList();
			try (InputStream is = new ByteArrayInputStream(data)) {
				GetSerializedPosition psdao = new GetSerializedPosition(is);
				Collection<? extends RouteEntry> entries = psdao.read();
				for (RouteEntry entry : entries) {
					if (entry.isFlagSet(ACARSFlags.ONGROUND) && !entry.isFlagSet(ACARSFlags.TOUCHDOWN) && !includeOnGround && !entry.isWarning())
						results.add(new GeoPosition(entry));
					else
						results.add(entry);
				}
			}
		} catch (ArchiveValidationException ave) {
			log.warn("Flight {} failed validation - {} [{} / {}]", Integer.valueOf(flightID), ave.getMessage(), md.getBucket(), md.getHexID());
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
	 * @return a FrameRate bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public FrameRates getFrameRate(int flightID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(FRAMERATE), MIN(FRAMERATE), MAX(FRAMERATE), AVG(FRAMERATE) FROM acars.POSITIONS WHERE (FLIGHT_ID=?)")) {
			ps.setInt(1, flightID);
			FrameRates fr = new FrameRates();
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					fr.setSize(rs.getInt(1));
					fr.setMin(rs.getInt(2));
					fr.setMax(rs.getInt(3));
					fr.setAverage(rs.getDouble(4));
				}
			}
			
			return fr;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}