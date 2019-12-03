// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Navigation data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetNavData extends DAO {
	
	private static final Cache<NavigationDataMap> _cache = CacheManager.get(NavigationDataMap.class, "NavData");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavData(Connection c) {
		super(c);
	}

	/**
	 * Returns a Navigation object.
	 * @param code the object code
	 * @return a NavigationDataMap
	 * @throws DAOException if a JDBC error occurs
	 */
	public NavigationDataMap get(String code) throws DAOException {
		if (code == null)
			return new NavigationDataMap();
		
		// Check the cache
		String c = code.toUpperCase();
		NavigationDataMap result = _cache.get(c);
		if (result != null)
			return result;
		
		// If we're too long, then try looking for a bearing and range
		if (c.length() > 7) {
			result = getBearingRange(c);
			if (!result.isEmpty())
				return result;
			
			c = code.substring(0, 7);
		}
		
		// Build the navigation data map
		NavigationDataMap ndmap = new NavigationDataMap();
		ndmap.setCacheKey(c);
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.NAVDATA WHERE (CODE=?) ORDER BY ITEMTYPE")) {
			ps.setString(1, c);
			ndmap.addAll(execute(ps));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Check for a lat/long pair
		if (NavigationDataBean.isCoordinates(c) != CodeType.CODE) {
			try {
				Intersection i = Intersection.parse(c);
				ndmap.add(i);
			} catch (Exception e) {
				// empty
			}
		}
		
		_cache.add(ndmap);
		return ndmap;
	}
	
	/**
	 * Retrieves "special" navaids such as CODE[bearing][distance].
	 * @param code the code/bearing/distance
	 * @return a NavigationDataMap
	 * @throws DAOException if a JDBC error occurs
	 */
	public NavigationDataMap getBearingRange(String code) throws DAOException {
		if (code == null)
			return new NavigationDataMap();
		
		// Check the cache
		NavigationDataMap result = _cache.get(code);
		if (result != null)
			return result;
		
		// Get the code
		StringBuilder cBuf = new StringBuilder();
		for (int x = 0; x < code.length(); x++) {
			char c = code.charAt(x);
			if (Character.isLetter(c))
				cBuf.append(c);
			else
				break;
		}
		
		// If there are no digits, abort
		if ((code.length() - cBuf.length()) < 3)
			return new NavigationDataMap();
		
		// Find the navaid
		NavigationDataMap codeResults = get(cBuf.toString());
		if (codeResults.isEmpty())
			return codeResults;
		
		// Get the bearing and the distance
		int hdg = StringUtils.parse(code.substring(cBuf.length(), cBuf.length() + 3), 0);
		int distance = StringUtils.parse(code.substring(cBuf.length() + 3), 0);
		
		// Convert the results
		result = new NavigationDataMap();
		for (NavigationDataBean nd : codeResults.getAll()) {
			GeoLocation loc = GeoUtils.bearingPoint(nd, distance, hdg);
			NavigationDataBean nd2 = new Intersection(code, loc.getLatitude(), loc.getLongitude());
			nd2.setRegion(nd.getRegion());
			result.add(nd2);
		}
		
		result.setCacheKey(code);
		_cache.add(result);
		return result;
	}
	
	/**
	 * Returns an Airport location from the database.
	 * @param code the airport ICAO code
	 * @return an AirportLocation bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AirportLocation getAirport(String code) throws DAOException {
		
		// Get all entries with the code
		NavigationDataMap results = get(code);
		for (NavigationDataBean nd : results.getAll()) {
			if (nd instanceof AirportLocation)
				return (AirportLocation) nd;
		}
		
		return null;
	}
	
	/**
	 * Returns all navaids of a particular type in the database.
	 * @param type the Navaid
	 * @return a Collection of NavigationDataBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<NavigationDataBean> getAll(Navaid type) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?)")) {
			ps.setInt(1, type.ordinal());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns information about a particular airport Runway.
	 * @param a the ICAOAirport
	 * @param rwyCode the runway name/number
	 * @param sim the Simulator to select
	 * @return a Runway bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Runway getRunway(ICAOAirport a, String rwyCode, Simulator sim) throws DAOException {
		if (rwyCode == null) return null;
		Simulator s = (sim == null) ? Simulator.FSX : sim;
		String rw = rwyCode.toUpperCase();
		if (rw.startsWith("RW"))
			rw = rw.substring(2);
		
		try (PreparedStatement ps = prepare("SELECT ?, R.ICAO, R.LATITUDE, R.LONGITUDE, N.FREQ, R.LENGTH, R.NAME, R.HDG, N.REGION, NULL AS LL, R.MAGVAR, IFNULL(R.SURFACE, ?), RR.NEWCODE, "
			+ "R.SIMVERSION, R.WIDTH FROM common.RUNWAYS R LEFT JOIN common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND (R.NAME=RR.OLDCODE)) LEFT JOIN common.NAVDATA N ON ((N.CODE=R.ICAO) "
			+ "AND (N.NAME=IFNULL(RR.NEWCODE,R.NAME)) AND (N.ITEMTYPE=?)) WHERE (R.ICAO=?) AND (R.NAME=?) AND (R.SIMVERSION=?)")) {
			ps.setInt(1, Navaid.RUNWAY.ordinal());
			ps.setInt(2, Surface.UNKNOWN.ordinal());
			ps.setInt(3, Navaid.RUNWAY.ordinal());
			ps.setString(4, a.getICAO());
			ps.setString(5, rw);
			ps.setInt(6, s.getCode());
			List<NavigationDataBean> results = execute(ps);
			return results.isEmpty() ? null : (Runway) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Runways for a particular Airport.
	 * @param a the ICAOAirport
	 * @param sim the Simulator to select
	 * @return a Collection of Runway beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Runway> getRunways(ICAOAirport a, Simulator sim) throws DAOException {
		Simulator s = (sim == null) ? Simulator.FSX : sim;
		try (PreparedStatement ps = prepare("SELECT ?, R.ICAO, R.LATITUDE, R.LONGITUDE, N.FREQ, R.LENGTH, R.NAME, R.HDG, N.REGION, NULL AS LL, R.MAGVAR, IFNULL(R.SURFACE, ?), RR.NEWCODE, "
			+ "R.SIMVERSION, R.WIDTH FROM common.RUNWAYS R LEFT JOIN common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND (R.NAME=RR.OLDCODE)) LEFT JOIN common.NAVDATA N ON "
			+ "((N.CODE=R.ICAO) AND (N.NAME=IFNULL(RR.NEWCODE,R.NAME)) AND (N.ITEMTYPE=?)) WHERE (R.ICAO=?) AND (R.SIMVERSION=?)")) {
			ps.setInt(1, Navaid.RUNWAY.ordinal());
			ps.setInt(2, Surface.UNKNOWN.ordinal());
			ps.setInt(3, Navaid.RUNWAY.ordinal());
			ps.setString(4, a.getICAO());
			ps.setInt(5, s.getCode());
			List<NavigationDataBean> results = execute(ps);
			return results.stream().filter(Runway.class::isInstance).map(Runway.class::cast).collect(Collectors.toList());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the likeliest runway for a takeoff or landing. 
	 * @param a the Airport
	 * @param sim the Simulator
	 * @param loc the takeoff/landing location
	 * @param hdg the takeoff/landing heading in degrees 
	 * @return a Runway, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public LandingRunways getBestRunway(ICAOAirport a, Simulator sim, GeoLocation loc, int hdg) throws DAOException {
		
		Collection<Runway> results = new HashSet<Runway>();
		try {
			if (sim != Simulator.UNKNOWN) {
				Simulator s = (sim.ordinal() < Simulator.FS9.ordinal()) ? Simulator.FS9 : sim;
				try (PreparedStatement ps = prepareWithoutLimits("SELECT R.*, RR.NEWCODE FROM common.RUNWAYS R LEFT JOIN common.RUNWAY_RENUMBER RR ON ((R.ICAO=RR.ICAO) AND (R.NAME=RR.OLDCODE)) WHERE (R.ICAO=?) AND (R.SIMVERSION=?)")) {	
					ps.setString(1, a.getICAO());
					ps.setInt(2, s.getCode());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
							r.setCode(rs.getString(1));
							r.setName(rs.getString(2));
							r.setHeading(rs.getInt(6));
							r.setLength(rs.getInt(7));
							r.setWidth(rs.getInt(8));
							r.setMagVar(rs.getDouble(9));
							r.setSurface(Surface.values()[rs.getInt(10)]);
							// LL
							r.setNewCode(rs.getString(12));
							r.setSimulator(s);
							results.add(r);
						}
					}
				}
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND (CODE=?)")) {
				ps.setInt(1, Navaid.RUNWAY.ordinal());
				ps.setString(2, a.getICAO());
				execute(ps).stream().filter(Runway.class::isInstance).map(Runway.class::cast).forEach(results::add);
			}
		} catch (SQLException se) { 
			throw new DAOException(se);
		}
		
		LandingRunways lr = new LandingRunways(loc, hdg);
		lr.addAll(results);
		return lr;
	}

	/**
	 * Returns a group of Navigation objects.
	 * @param ids a Collection of navigation object codes
	 * @return a NavigationDataMap bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public NavigationDataMap getByID(Collection<String> ids) throws DAOException {
		if (CollectionUtils.isEmpty(ids))
			return new NavigationDataMap();
		
		// Check the cache
		NavigationDataMap results = new NavigationDataMap();
		for (String id : ids)
			results.addAll(get(id).getAll());
		
		results.setCacheKey(ids);
		return results;
	}

	/**
	 * Returns all Intersections within a set number of miles from a point.
	 * @param loc the central location
	 * @param distance the distance in miles
	 * @return a Map of Intersections, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if distance is negative or > 1000
	 */
	public Collection<NavigationDataBean> getIntersections(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 1000))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Convert to meters
		int dstM = (int)(distance * DistanceUnit.KM.getFactor() * 1000);
		try (PreparedStatement ps = prepare("SELECT ND.*, ST_Distance_Sphere(LL, ST_PointFromText(?,?)) AS DST FROM common.NAVDATA ND WHERE (ND.ITEMTYPE=?) HAVING (DST<?) ORDER BY DST")) {
			ps.setString(1, formatLocation(loc));
			ps.setInt(2, WGS84_SRID);
			ps.setInt(3, Navaid.INT.ordinal());
			ps.setInt(4, dstM);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Navigation objects (except Intersections/Runways/Gates) within a set number of miles from a point.
	 * @param loc the central location
	 * @param distance the distance in miles
	 * @return a Map of NavigationDataBeans, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if distance is negative or > 1500
	 */
	public Collection<NavigationDataBean> getObjects(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 1500))
			throw new IllegalArgumentException("Invalid distance -  " + distance);
		
		// Convert to meters
		int dstM = (int)(distance * DistanceUnit.KM.getFactor() * 1000);
		try (PreparedStatement ps = prepare("SELECT ND.*, ST_Distance_Sphere(LL, ST_PointFromText(?,?)) AS DST FROM common.NAVDATA ND WHERE (ND.ITEMTYPE<=?) HAVING (DST<?) ORDER BY DST")) {
			ps.setString(1, formatLocation(loc));
			ps.setInt(2, WGS84_SRID);
			ps.setInt(3, Navaid.NDB.ordinal());
			ps.setInt(4, dstM);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to iterate through a NAVDATA result set.
	 */
	private static List<NavigationDataBean> execute(PreparedStatement ps) throws SQLException {
		List<NavigationDataBean> results = new ArrayList<NavigationDataBean>();
		try (ResultSet rs = ps.executeQuery()) {
			ResultSetMetaData md = rs.getMetaData();
			while (rs.next()) {
				NavigationDataBean obj = null;
				Navaid nt = Navaid.values()[rs.getInt(1)];
				switch (nt) {
				case AIRPORT:
						AirportLocation a = new AirportLocation(rs.getDouble(3), rs.getDouble(4));
						a.setCode(rs.getString(2));
						a.setAltitude(rs.getInt(6));
						a.setName(rs.getString(7));
						a.setRegion(rs.getString(9));
						obj = a;
						break;

					case INT:
						obj = new Intersection(rs.getString(2), rs.getDouble(3), rs.getDouble(4));
						obj.setRegion(rs.getString(9));
						break;

					case VOR:
						VOR vor = new VOR(rs.getDouble(3), rs.getDouble(4));
						vor.setCode(rs.getString(2));
						vor.setFrequency(rs.getString(5));
						vor.setName(rs.getString(7));
						vor.setRegion(rs.getString(9));
						obj = vor;
						break;

					case NDB:
						NDB ndb = new NDB(rs.getDouble(3), rs.getDouble(4));
						ndb.setCode(rs.getString(2));
						ndb.setFrequency(rs.getString(5));
						ndb.setName(rs.getString(7));
						ndb.setRegion(rs.getString(9));
						obj = ndb;
						break;

					case RUNWAY:
						int cc = md.getColumnCount();
						Runway rwy = new Runway(rs.getDouble(3), rs.getDouble(4));
						rwy.setCode(rs.getString(2));
						rwy.setFrequency(rs.getString(5));
						rwy.setLength(rs.getInt(6));
						rwy.setName(rs.getString(7));
						rwy.setHeading(rs.getInt(8));
						rwy.setRegion(rs.getString(9));
						if (cc > 11) {
							rwy.setMagVar(rs.getDouble(11));
							rwy.setSurface(Surface.values()[rs.getInt(12)]);
							if (cc > 12) {
								rwy.setNewCode(rs.getString(13));
								rwy.setSimulator(Simulator.fromVersion(rs.getInt(14), Simulator.UNKNOWN));
								rwy.setWidth(rs.getInt(15));
							}
						}
						
						obj = rwy;
						break;
						
					default:
				}

				results.add(obj);
			}
		}

		return results;
	}
}