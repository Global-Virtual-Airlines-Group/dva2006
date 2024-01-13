// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.Recorder;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.OperatingSystem;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load ACARS information.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class GetACARSData extends DAO {
	
	private static final Cache<ArchiveMetadata> _mdCache = CacheManager.get(ArchiveMetadata.class, "ArchiveMeta");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSData(Connection c) {
		super(c);
	}

	/**
	 * Retrieves position archive metadata about a flight.
	 * @param flightID the flight ID
	 * @return an ArchiveMetadata bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	@Deprecated
	public ArchiveMetadata getArchiveInfo(int flightID) throws DAOException {
		
		// Check the cache
		ArchiveMetadata md = _mdCache.get(Integer.valueOf(flightID));
		if (md != null)
			return md;
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT CNT, SIZE, CRC, ARCHIVED, FMT FROM acars.ARCHIVE WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, flightID);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					md = new ArchiveMetadata(flightID);
					md.setPositionCount(rs.getInt(1));
					md.setSize(rs.getInt(2));
					md.setCRC32(rs.getLong(3));
					md.setArchivedOn(rs.getTimestamp(4).toInstant());
					md.setFormat(SerializedDataVersion.fromCode(rs.getInt(5)));
					md.setBucket(ArchiveHelper.getBucket(flightID));
				}
				
				_mdCache.add(md);
				return md;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves the takeoff and landing coordinates for a particular flight. More than two results may be returned, if
	 * the aircraft bounced on takeoff and/or landing.
	 * @param flightID the flight ID
	 * @return a List of RouteEntry beans, ordered by time
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ACARSRouteEntry> getTakeoffLanding(int flightID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT REPORT_TIME, LAT, LNG, B_ALT, HEADING, VSPEED FROM acars.POSITIONS WHERE (FLIGHT_ID=?) AND ((FLAGS & ?) > 0) ORDER BY REPORT_TIME")) {
			ps.setInt(1, flightID);
			ps.setInt(2, ACARSFlags.TOUCHDOWN.getMask());
			
			// Execute the query
			try (ResultSet rs = ps.executeQuery()) {
				List<ACARSRouteEntry> results = new ArrayList<ACARSRouteEntry>();
				while (rs.next()) {
					ACARSRouteEntry entry = new ACARSRouteEntry(rs.getTimestamp(1).toInstant(), new GeoPosition(rs.getDouble(2), rs.getDouble(3)));
					entry.setAltitude(rs.getInt(4));
					entry.setHeading(rs.getInt(5));
					entry.setVerticalSpeed(rs.getInt(6));
					results.add(entry);
				}
				
				return results;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns landing runway distance for a particular flight.
	 * @param flightID the ACARS flight ID
	 * @return a RunwayDistance bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public RunwayDistance getLandingRunway(int flightID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT R.*, IFNULL(ND.HDG, 0), ND.FREQ, RR.OLDCODE FROM acars.RWYDATA R LEFT JOIN common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND "
			+ "((R.RUNWAY=RR.OLDCODE) OR (R.RUNWAY=RR.NEWCODE))) LEFT JOIN common.NAVDATA ND ON (R.ICAO=ND.CODE) AND (IFNULL(RR.OLDCODE,R.RUNWAY)=ND.NAME) AND (ND.ITEMTYPE=?) AND "
			+ "(R.ISTAKEOFF=?) WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, Navaid.RUNWAY.ordinal());
			ps.setBoolean(2, false);
			ps.setInt(3, flightID);
			
			// Execute the query
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
					r.setCode(rs.getString(2));
					r.setName(rs.getString(3));
					r.setLength(rs.getInt(6));
					r.setHeading(rs.getInt(9));
					r.setFrequency(rs.getString(10));
					r.setAlternateCode(rs.getString(11), false);
					return new RunwayDistance(r, rs.getInt(7));
				}
			}

			return null;
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ROUTE from acars.FLIGHTS WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, flightID);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString(1) : null;
			}
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
			FlightInfo info = null;
			try (PreparedStatement ps = prepareWithoutLimits("SELECT F.*, INET6_NTOA(F.REMOTE_ADDR), FD.ROUTE_ID, FDR.DISPATCHER_ID, FDL.LOG_ID, FL.PAX, FL.SEATS, FL.LOADTYPE, FL.LOADFACTOR FROM "
				+ "acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID) LEFT JOIN acars.FLIGHT_DISPATCH_LOG FDL ON (F.ID=FDL.ID) "
				+ "LEFT JOIN acars.FLIGHT_LOAD FL ON (F.ID=FL.ID) WHERE (F.ID=?) LIMIT 1")) {
				ps.setInt(1, flightID);

				// Get the first entry, or null
				info = executeFlightInfo(ps).stream().findFirst().orElse(null);
				if (info == null)
					return null;
			}

			// Get the terminal routes
			Map<TerminalRoute.Type, TerminalRoute> routes = getTerminalRoutes(info.getID());
			info.setSID(routes.get(TerminalRoute.Type.SID));
			info.setSTAR(routes.get(TerminalRoute.Type.STAR));
			
			// Fetch the takeoff and landing runways
			if (info.getHasPIREP()) {
				try (PreparedStatement ps = prepareWithoutLimits("SELECT R.*, IFNULL(ND.HDG, 0), ND.FREQ, RW.MAGVAR, RW.WIDTH, RW.THRESHOLD, IFNULL(RW.SURFACE, ?), RR.OLDCODE FROM acars.RWYDATA R LEFT JOIN "
					+ "common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND (R.RUNWAY=RR.NEWCODE)) LEFT JOIN common.RUNWAYS RW ON ((RW.ICAO=R.ICAO) AND ((RW.NAME=R.RUNWAY) OR (RW.NAME=RR.OLDCODE)) "
					+ "AND (RW.SIMVERSION=?)) LEFT JOIN common.NAVDATA ND ON ((R.ICAO=ND.CODE) AND (R.RUNWAY=ND.NAME) AND (ND.ITEMTYPE=?)) WHERE (R.ID=?) LIMIT 2")) {
					ps.setInt(1, Surface.UNKNOWN.ordinal());
					ps.setInt(2, Math.max(2004, info.getSimulator().getCode()));
					ps.setInt(3, Navaid.RUNWAY.ordinal());
					ps.setInt(4, flightID);
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
							r.setCode(rs.getString(2));
							r.setName(rs.getString(3));
							r.setLength(rs.getInt(6));
							r.setHeading(rs.getInt(9));
							r.setFrequency(rs.getString(10));
							r.setMagVar(rs.getDouble(11));
							r.setWidth(rs.getInt(12));
							r.setThresholdLength(rs.getInt(13));
							r.setSurface(Surface.values()[rs.getInt(14)]);
							r.setSimulator(info.getSimulator());
							r.setAlternateCode(rs.getString(15), false);
							if (rs.getBoolean(8))
								info.setRunwayD(new RunwayDistance(r, rs.getInt(7)));
							else
								info.setRunwayA(new RunwayDistance(r, rs.getInt(7)));
						}
					}
				}
			}
			
			// Count the number of position records
			String sql = null;
			if (info.getArchived())
				sql = "SELECT CNT FROM acars.ARCHIVE WHERE (ID=?)";
			else if (info.getFDR() == Recorder.XACARS)
				sql = "SELECT COUNT(*) FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?)";
			else
				sql = "SELECT COUNT(*) FROM acars.POSITIONS WHERE (FLIGHT_ID=?)";
			
			try (PreparedStatement ps = prepareWithoutLimits(sql)) {
				ps.setInt(1, flightID);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						info.setPositionCount(rs.getInt(1));
				}
				
				return info;
			}
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET6_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD FROM acars.CONS C WHERE (C.ID=CONV(?,10,16)) LIMIT 1")) {
			ps.setLong(1, conID);
			return executeConnectionInfo(ps).stream().findFirst().orElse(null);
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
	protected Map<TerminalRoute.Type, TerminalRoute> getTerminalRoutes(int id) throws DAOException {
		try {
			Map<TerminalRoute.Type, TerminalRoute> results = new HashMap<TerminalRoute.Type, TerminalRoute>();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT IF(FS.TYPE=?, F.AIRPORT_D, F.AIRPORT_A), FS.* FROM acars.FLIGHTS F, acars.FLIGHT_SIDSTAR FS WHERE (F.ID=?) AND (F.ID=FS.ID)")) {
				ps.setInt(1, TerminalRoute.Type.SID.ordinal());
				ps.setInt(2, id);

				// Execute the query
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						TerminalRoute.Type rt = TerminalRoute.Type.values()[rs.getInt(3)];
						TerminalRoute tr = new TerminalRoute(SystemData.getAirport(rs.getString(1)), rs.getString(4), rt);
						tr.setTransition(rs.getString(5));
						tr.setRunway(rs.getString(6));
						results.put(tr.getType(), tr);
					}
				}
			}

			// Load the waypoint data
			try (PreparedStatement ps = prepareWithoutLimits("SELECT TYPE, CODE, WPTYPE, LATITUDE, LONGITUDE, REGION FROM acars.FLIGHT_SIDSTAR_WP WHERE (ID=?) ORDER BY TYPE, SEQ")) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						TerminalRoute.Type rt = TerminalRoute.Type.values()[rs.getInt(1)];
						TerminalRoute tr = results.get(rt);
						if (tr != null) {
							Navaid nt = Navaid.values()[rs.getInt(3)];
							NavigationDataBean nd = NavigationDataBean.create(nt, rs.getDouble(4), rs.getDouble(5));
							nd.setCode(rs.getString(2));
							nd.setRegion(rs.getString(6));
							tr.addWaypoint(nd);
						}
					}
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Flight Info result sets.
	 * @param ps a PreparedStatement
	 * @return a List of FlightInfo beans
	 * @throws SQLException if an error occurs
	 */
	protected static List<FlightInfo> executeFlightInfo(PreparedStatement ps) throws SQLException {
		List<FlightInfo> results = new ArrayList<FlightInfo>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				FlightInfo info = new FlightInfo(rs.getInt(1));
				info.setAuthorID(rs.getInt(2));
				info.setStartTime(rs.getTimestamp(3).toInstant());
				info.setEndTime(toInstant(rs.getTimestamp(4)));
				info.setAirline(SystemData.getAirline(rs.getString(5)));
				info.setFlight(rs.getInt(6));
				info.setEquipmentType(rs.getString(7));
				info.setAltitude(rs.getString(8));
				info.setAirportD(SystemData.getAirport(rs.getString(9)));
				info.setAirportA(SystemData.getAirport(rs.getString(10)));
				info.setAirportL(SystemData.getAirport(rs.getString(11)));
				// skip 12 - decode in 25
				info.setRemoteHost(rs.getString(13));
				info.setRoute(rs.getString(14));
				info.setRemarks(rs.getString(15));
				info.setSimulator(Simulator.fromVersion(rs.getInt(16), Simulator.UNKNOWN));
				info.setOffline(rs.getBoolean(17));
				info.setHasPIREP(rs.getBoolean(18));
				info.setArchived(rs.getBoolean(19));
				info.setScheduleValidated(rs.getBoolean(20));
				info.setDispatcher(DispatchType.values()[rs.getInt(21)]);
				info.setIsMP(rs.getBoolean(22));
				info.setClientBuild(rs.getInt(23));
				info.setBeta(rs.getInt(24));
				info.setFDR(Recorder.values()[rs.getInt(25)]);
				info.setSimulatorVersion(rs.getInt(26), rs.getInt(27));
				info.setTXCode(rs.getInt(28));
				info.setAutopilotType(AutopilotType.values()[rs.getInt(29)]);
				info.setPlatform(OperatingSystem.values()[rs.getInt(30)]);
				info.setIsSim64Bit(rs.getBoolean(31));
				info.setIsACARS64Bit(rs.getBoolean(32));
				info.setRemoteAddr(rs.getString(33));
				info.setRouteID(rs.getInt(34));
				info.setDispatcherID(rs.getInt(35));
				info.setDispatchLogID(rs.getInt(36));
				info.setPassengers(rs.getInt(37));
				info.setSeats(rs.getInt(38));
				info.setLoadType(LoadType.values()[rs.getInt(39)]);
				info.setLoadFactor(rs.getDouble(40));
				results.add(info);
			}
		}

		return results;
	}

	/**
	 * Helper method to parse Connection result sets.
	 * @param ps a PreparedStatement
	 * @return a List of ConnectionEntry beans
	 * @throws SQLException if an error occurs
	 */
	protected static List<ConnectionEntry> executeConnectionInfo(PreparedStatement ps) throws SQLException {
		List<ConnectionEntry> results = new ArrayList<ConnectionEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ConnectionEntry entry = new DispatchConnectionEntry(Long.parseLong(rs.getString(1), 16));
				entry.setAuthorID(rs.getInt(2));
				entry.setStartTime(rs.getTimestamp(3).toInstant());
				entry.setEndTime(toInstant(rs.getTimestamp(4)));
				entry.setRemoteAddr(rs.getString(5));
				entry.setRemoteHost(rs.getString(6));
				entry.setClientBuild(rs.getInt(7));
				entry.setBeta(rs.getInt(8));
				results.add(entry);
			}
		}

		return results;
	}
}