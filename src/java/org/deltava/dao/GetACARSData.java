// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.Recorder;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A Data Access Object to load ACARS information.
 * @author Luke
 * @version 7.5
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
	 * Retrieves position archive metadata about a flight.
	 * @param flightID the flight ID
	 * @return an ArchiveMetadata bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ArchiveMetadata getArchiveInfo(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT CNT, SIZE, CRC, ARCHIVED FROM acars.ARCHIVE WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, flightID);
			
			ArchiveMetadata md = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					md = new ArchiveMetadata(flightID);
					md.setPositionCount(rs.getInt(1));
					md.setSize(rs.getInt(2));
					md.setCRC32(rs.getLong(3));
					md.setArchivedOn(rs.getTimestamp(4).toInstant());
				}
			}
			
			_ps.close();
			return md;
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
	public List<? extends RouteEntry> getTakeoffLanding(int flightID) throws DAOException {
		try {
			prepareStatement("SELECT REPORT_TIME, LAT, LNG, B_ALT, HEADING, VSPEED FROM acars.POSITIONS WHERE (FLIGHT_ID=?) AND ((FLAGS & ?) > 0) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);
			_ps.setInt(2, FLAG_TOUCHDOWN);
			
			// Execute the query
			List<ACARSRouteEntry> results = new ArrayList<ACARSRouteEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					ACARSRouteEntry entry = new ACARSRouteEntry(rs.getTimestamp(1).toInstant(), new GeoPosition(rs.getDouble(2), rs.getDouble(3)));
					entry.setAltitude(rs.getInt(4));
					entry.setHeading(rs.getInt(5));
					entry.setVerticalSpeed(rs.getInt(6));
					results.add(entry);
				}
			}
			
			_ps.close();
			return results;
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
		try {
			prepareStatementWithoutLimits("SELECT R.*, IFNULL(ND.HDG, 0), ND.FREQ FROM acars.RWYDATA R LEFT JOIN "
				+ "common.NAVDATA ND ON (R.ICAO=ND.CODE) AND (R.RUNWAY=ND.NAME) AND (ND.ITEMTYPE=?) AND "
				+ "(R.ISTAKEOFF=?) WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, Navaid.RUNWAY.ordinal());
			_ps.setBoolean(2, false);
			_ps.setInt(3, flightID);
			
			// Execute the query
			RunwayDistance rd = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
					r.setCode(rs.getString(2));
					r.setName(rs.getString(3));
					r.setLength(rs.getInt(6));
					r.setHeading(rs.getInt(9));
					r.setFrequency(rs.getString(10));
					rd = new RunwayDistance(r, rs.getInt(7));
				}
			}

			_ps.close();
			return rd;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Checks if in-flight refueling was used on a Flight.
	 * @param flightID the ACARS Flight ID
	 * @return a {@link FuelUse} bean with fuel data
	 * @throws DAOException if a JDBC error occurs
	 */
	public FuelUse checkRefuel(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT FUEL, FLAGS FROM acars.POSITIONS WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);
			
			// Execute the query
			FuelUse use = new FuelUse();
			try (ResultSet rs = _ps.executeQuery()) {
				 int lastFuel = 0;
				 while (rs.next()) {
					 int fuel = rs.getInt(1);
					 if (lastFuel != 0) {
						 int fuelDelta = (lastFuel - fuel); 
						 if (fuelDelta < -FuelUse.MAX_DELTA) {
							 boolean isAirborne = ((rs.getInt(2) & FLAG_ONGROUND) != 0);
							 if (!isAirborne)
								 use.setRefuel(true);
						 } else if (fuelDelta > 0)
							 use.addFuelUse(fuelDelta);
					 }
				
					 lastFuel = fuel;
				 }
			}
			
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
			String result = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getString(1);
			}

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
			prepareStatementWithoutLimits("SELECT F.ID FROM acars.FLIGHTS F WHERE (F.CREATED=(SELECT CREATED FROM acars.FLIGHTS WHERE (ID=?) LIMIT 1)) AND "
				+ "(F.PILOT_ID=(SELECT PILOT_ID FROM acars.FLIGHTS WHERE (ID=?) LIMIT 1)) AND (F.ID<>?) ORDER BY F.ID LIMIT 1");
			_ps.setInt(1, id);
			_ps.setInt(2, id);
			_ps.setInt(3, id);
			
			// Execute the query
			int dupeID = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					dupeID = rs.getInt(1);
			}
			
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
			prepareStatementWithoutLimits("SELECT F.*, INET6_NTOA(F.REMOTE_ADDR), FD.ROUTE_ID, "
				+ "FDR.DISPATCHER_ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON "
				+ "(F.ID=FD.ID) LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID) WHERE (F.ID=?) LIMIT 1");
			_ps.setInt(1, flightID);

			// Get the first entry, or null
			List<FlightInfo> results = executeFlightInfo();
			FlightInfo info = results.isEmpty() ? null : results.get(0);
			if (info == null)
				return null;

			// Get the terminal routes
			Map<TerminalRoute.Type, TerminalRoute> routes = getTerminalRoutes(info.getID());
			info.setSID(routes.get(TerminalRoute.Type.SID));
			info.setSTAR(routes.get(TerminalRoute.Type.STAR));
			
			// Fetch the takeoff and landing runways
			if (info.getHasPIREP()) {
				prepareStatementWithoutLimits("SELECT R.*, IFNULL(ND.HDG, 0), ND.FREQ, RW.MAGVAR, IFNULL(RW.SURFACE, ?) FROM acars.RWYDATA R LEFT JOIN "
					+ "common.NAVDATA ND ON ((R.ICAO=ND.CODE) AND (R.RUNWAY=ND.NAME) AND (ND.ITEMTYPE=?)) LEFT JOIN common.RUNWAYS RW "
					+ "ON ((RW.ICAO=ND.CODE) AND (RW.NAME=ND.NAME) AND (RW.SIMVERSION=?)) WHERE (R.ID=?) LIMIT 2");
				_ps.setInt(1, Surface.UNKNOWN.ordinal());
				_ps.setInt(2, Navaid.RUNWAY.ordinal());
				_ps.setInt(3, Math.max(2004, info.getSimulator().getCode()));
				_ps.setInt(4, flightID);
				try (ResultSet rs = _ps.executeQuery()) {
					while (rs.next()) {
						Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
						r.setCode(rs.getString(2));
						r.setName(rs.getString(3));
						r.setLength(rs.getInt(6));
						r.setHeading(rs.getInt(9));
						r.setFrequency(rs.getString(10));
						r.setMagVar(rs.getDouble(11));
						r.setSurface(Surface.values()[rs.getInt(12)]);
						if (rs.getBoolean(8))
							info.setRunwayD(new RunwayDistance(r, rs.getInt(7)));
						else
							info.setRunwayA(new RunwayDistance(r, rs.getInt(7)));
					}
				}

				_ps.close();
			}
			
			// Count the number of position records
			String sql = null;
			if (info.getArchived())
				sql = "SELECT CNT FROM acars.ARCHIVE WHERE (ID=?)";
			else if (info.getFDR() == Recorder.XACARS)
				sql = "SELECT COUNT(*) FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?)";
			else
				sql = "SELECT COUNT(*) FROM acars.POSITIONS WHERE (FLIGHT_ID=?)";
			
			prepareStatement(sql);
			_ps.setInt(1, flightID);
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					info.setPositionCount(rs.getInt(1));
			}
			
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
			prepareStatementWithoutLimits("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET6_NTOA(C.REMOTE_ADDR), "
				+ "C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD FROM acars.CONS C WHERE (C.ID=CONV(?,10,16)) LIMIT 1");
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
	protected Map<TerminalRoute.Type, TerminalRoute> getTerminalRoutes(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT IF(FS.TYPE=?, F.AIRPORT_D, F.AIRPORT_A), FS.* FROM acars.FLIGHTS F, acars.FLIGHT_SIDSTAR FS WHERE (F.ID=?) AND (F.ID=FS.ID)");
			_ps.setInt(1, TerminalRoute.Type.SID.ordinal());
			_ps.setInt(2, id);

			// Execute the query
			Map<TerminalRoute.Type, TerminalRoute> results = new HashMap<TerminalRoute.Type, TerminalRoute>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					TerminalRoute.Type rt = TerminalRoute.Type.values()[rs.getInt(3)];
					TerminalRoute tr = new TerminalRoute(SystemData.getAirport(rs.getString(1)), rs.getString(4), rt);
					tr.setTransition(rs.getString(5));
					tr.setRunway(rs.getString(6));
					results.put(tr.getType(), tr);
				}
			}

			_ps.close();

			// Load the waypoint data
			prepareStatementWithoutLimits("SELECT TYPE, CODE, WPTYPE, LATITUDE, LONGITUDE, REGION FROM acars.FLIGHT_SIDSTAR_WP WHERE (ID=?) ORDER BY TYPE, SEQ");
			_ps.setInt(1, id);
			try (ResultSet rs = _ps.executeQuery()) {
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

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Flight Info result sets.
	 * @return a List of FlightInfo beans
	 * @throws SQLException if an error occurs
	 */
	protected List<FlightInfo> executeFlightInfo() throws SQLException {
		List<FlightInfo> results = new ArrayList<FlightInfo>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				FlightInfo info = new FlightInfo(rs.getInt(1));
				info.setAuthorID(rs.getInt(2));
				info.setStartTime(rs.getTimestamp(3).toInstant());
				info.setEndTime(toInstant(rs.getTimestamp(4)));
				info.setFlightCode(rs.getString(5));
				info.setEquipmentType(rs.getString(6));
				info.setAltitude(rs.getString(7));
				info.setAirportD(SystemData.getAirport(rs.getString(8)));
				info.setAirportA(SystemData.getAirport(rs.getString(9)));
				info.setAirportL(SystemData.getAirport(rs.getString(10)));
				// skip 11 - decode in 25
				info.setRemoteHost(rs.getString(12));
				info.setRoute(rs.getString(13));
				info.setRemarks(rs.getString(14));
				info.setSimulator(Simulator.fromVersion(rs.getInt(15)));
				info.setOffline(rs.getBoolean(16));
				info.setHasPIREP(rs.getBoolean(17));
				info.setArchived(rs.getBoolean(18));
				info.setScheduleValidated(rs.getBoolean(19));
				info.setDispatchPlan(rs.getBoolean(20));
				info.setIsMP(rs.getBoolean(21));
				info.setClientBuild(rs.getInt(22));
				info.setBeta(rs.getInt(23));
				info.setFDR(Recorder.values()[rs.getInt(24)]);
				info.setSimulatorVersion(rs.getInt(25), rs.getInt(26));
				info.setTXCode(rs.getInt(27));
				info.setLoadFactor(rs.getDouble(28));
				info.setRemoteAddr(rs.getString(29));
				info.setRouteID(rs.getInt(30));
				info.setDispatcherID(rs.getInt(31));
				results.add(info);
			}
		}

		_ps.close();
		return results;
	}

	/**
	 * Helper method to parse Connection result sets.
	 * @return a List of ConnectionEntry beans
	 * @throws SQLException if an error occurs
	 */
	protected List<ConnectionEntry> executeConnectionInfo() throws SQLException {
		List<ConnectionEntry> results = new ArrayList<ConnectionEntry>();
		try (ResultSet rs = _ps.executeQuery()) {
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

		_ps.close();
		return results;
	}
}