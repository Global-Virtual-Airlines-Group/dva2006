// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Navigation data.
 * @author Luke
 * @version 7.2
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
		
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (CODE=?) ORDER BY ITEMTYPE");
			_ps.setString(1, c);
			ndmap.addAll(execute());
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
		for (Iterator<NavigationDataBean> i = codeResults.getAll().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
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
		for (Iterator<NavigationDataBean> i = results.getAll().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
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
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?)");
			_ps.setInt(1, type.ordinal());
			return execute();
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
		
		try {
			prepareStatement("SELECT N.*, R.MAGVAR, IFNULL(R.SURFACE, ?) FROM common.NAVDATA N LEFT JOIN common.RUNWAYS R ON "
				+ "((N.CODE=R.ICAO) AND (N.NAME=R.NAME) AND (R.SIMVERSION=?)) WHERE (N.ITEMTYPE=?) AND (N.CODE=?) AND (N.NAME=?)");
			_ps.setInt(1, Surface.UNKNOWN.ordinal());
			_ps.setInt(2, s.getCode());
			_ps.setInt(3, Navaid.RUNWAY.ordinal());
			_ps.setString(4, a.getICAO());
			_ps.setString(5, rw);
			List<NavigationDataBean> results = execute();
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
		try {
			prepareStatement("SELECT N.*, R.MAGVAR, IFNULL(R.SURFACE, ?) FROM common.NAVDATA N LEFT JOIN common.RUNWAYS R ON "
				+ "((N.CODE=R.ICAO) AND (N.NAME=R.NAME) AND (R.SIMVERSION=?)) WHERE (N.ITEMTYPE=?) AND (N.CODE=?)");
			_ps.setInt(1, Surface.UNKNOWN.ordinal());
			_ps.setInt(2, s.getCode());
			_ps.setInt(3, Navaid.RUNWAY.ordinal());
			_ps.setString(4, a.getICAO());
			List<NavigationDataBean> results = execute();
			List<Runway> runways = new ArrayList<Runway>();
			for (NavigationDataBean nd : results) {
				if (nd instanceof Runway)
					runways.add((Runway) nd);
			}
			
			return runways;
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
				prepareStatementWithoutLimits("SELECT * FROM common.RUNWAYS WHERE (ICAO=?) AND (SIMVERSION=?)");	
				_ps.setString(1, a.getICAO());
				_ps.setInt(2, Math.max(2004, sim.getCode()));
				try (ResultSet rs = _ps.executeQuery()) {
					while (rs.next()) {
						Runway r = new Runway(rs.getDouble(4), rs.getDouble(5));
						r.setCode(rs.getString(1));
						r.setName(rs.getString(2));
						r.setHeading(rs.getInt(6));
						r.setLength(rs.getInt(7));
						r.setMagVar(rs.getDouble(8));
						r.setSurface(Surface.values()[rs.getInt(9)]);
						results.add(r);
					}
				}
				
				_ps.close();
			}
			
			prepareStatementWithoutLimits("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND (CODE=?)");
			_ps.setInt(1, Navaid.RUNWAY.ordinal());
			_ps.setString(2, a.getICAO());
			Collection<NavigationDataBean> r2 = execute();
			for (NavigationDataBean nd : r2) {
				if (nd.getType() == Navaid.RUNWAY)
					results.add((Runway) nd);
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

		try {
			prepareStatement("SELECT ND.*, ST_Distance(LL, ST_PointFromText(?,?)) AS DST FROM common.NAVDATA ND WHERE (ND.ITEMTYPE=?) HAVING (DST<?) ORDER BY DST");
			_ps.setString(1, formatLocation(loc));
			_ps.setInt(2, GEO_SRID);
			_ps.setInt(3, Navaid.INT.ordinal());
			_ps.setDouble(4, distance / GeoLocation.DEGREE_MILES);
			return execute();
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

		try {
			prepareStatement("SELECT ND.*, ST_Distance(LL, ST_PointFromText(?,?)) AS DST FROM common.NAVDATA ND WHERE (ND.ITEMTYPE<=?) HAVING (DST<?) ORDER BY DST");
			_ps.setString(1, formatLocation(loc));
			_ps.setInt(2, GEO_SRID);
			_ps.setInt(3, Navaid.NDB.ordinal());
			_ps.setDouble(4, distance / GeoLocation.DEGREE_MILES); // convert to degrees
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to iterate through a NAVDATA result set.
	 */
	private List<NavigationDataBean> execute() throws SQLException {
		List<NavigationDataBean> results = new ArrayList<NavigationDataBean>();
		try (ResultSet rs = _ps.executeQuery()) {
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
						Runway rwy = new Runway(rs.getDouble(3), rs.getDouble(4));
						rwy.setCode(rs.getString(2));
						rwy.setFrequency(rs.getString(5));
						rwy.setLength(rs.getInt(6));
						rwy.setName(rs.getString(7));
						rwy.setHeading(rs.getInt(8));
						rwy.setRegion(rs.getString(9));
						if (md.getColumnCount() > 11) {
							rwy.setMagVar(rs.getDouble(11));
							rwy.setSurface(Surface.values()[rs.getInt(12)]);
						}
						
						obj = rwy;
						break;
						
					default:
				}

				results.add(obj);
			}
		}

		_ps.close();
		return results;
	}
}