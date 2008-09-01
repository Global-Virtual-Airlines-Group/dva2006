// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;

import org.deltava.util.CollectionUtils;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Navigation data.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class GetNavData extends DAO implements CachingDAO {
	
	protected static final Cache<NavigationDataMap> _cache = new AgingCache<NavigationDataMap>(6144);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavData(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getRequests() {
		return _cache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _cache.getHits();
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
		NavigationDataMap result = _cache.get(code);
		if (result != null)
			return result;
		
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (CODE=?) ORDER BY ITEMTYPE");
			_ps.setString(1, code.toUpperCase());
			NavigationDataMap ndmap = new NavigationDataMap(execute());
			ndmap.setCacheKey(code);
			_cache.add(ndmap);
			result = ndmap;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Add to the cache and return
		return  result;
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
	 * @param type the Navigation aid code
	 * @return a Collection of NavigationDataBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<NavigationDataBean> getAll(int type) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?)");
			_ps.setInt(1, type);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns information about a particular airport Runway.
	 * @param airportCode the airport ICAO code
	 * @param rwyCode the runway name/number
	 * @return a Runway bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Runway getRunway(String airportCode, String rwyCode) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND (CODE=?) AND (NAME=?)");
			_ps.setInt(1, NavigationDataBean.RUNWAY);
			_ps.setString(2, airportCode.toUpperCase());
			_ps.setString(3, rwyCode.toUpperCase());

			// Execute the query
			List results = execute();
			return results.isEmpty() ? null : (Runway) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
		for (Iterator<String> i = ids.iterator(); i.hasNext(); ) {
			String id = i.next();
			results.addAll(get(id).getAll());
		}
		
		// Add to the cache and return
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
	public Map<String, NavigationDataBean> getIntersections(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 1000))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Calculate the height/width of the square in degrees (use 85% of the value of a lon degree)
		double height = (distance / GeoLocation.DEGREE_MILES) / 2;
		double width = (height * 0.85);

		Collection<NavigationDataBean> results = null;
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND ((LATITUDE > ?) AND (LATITUDE < ?)) "
					+ "AND ((LONGITUDE > ?) AND (LONGITUDE < ?))");
			_ps.setInt(1, NavigationDataBean.INT);
			_ps.setDouble(2, loc.getLatitude() - height);
			_ps.setDouble(3, loc.getLatitude() + height);
			_ps.setDouble(4, loc.getLongitude() - width);
			_ps.setDouble(5, loc.getLongitude() + width);
			results = execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Ensure that we are within the correct distance and convert to a Map for easy lookup
		distanceFilter(results, loc, distance);
		return CollectionUtils.createMap(results, "code");
	}

	/**
	 * Returns all Navigation objects (except Intersections/Runways) within a set number of miles from a point.
	 * @param loc the central location
	 * @param distance the distance in miles
	 * @return a Map of NavigationDataBeans, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if distance is negative or > 1000
	 */
	public Map<String, NavigationDataBean> getObjects(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 1000))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Calculate the height/width of the square in degrees (use 85% of the value of a lon degree)
		double height = (distance / GeoLocation.DEGREE_MILES) / 2;
		double width = (height * 0.85);

		Collection<NavigationDataBean> results = null;
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE <> ?) AND (ITEMTYPE <> ?) AND "
					+ "((LATITUDE > ?) AND (LATITUDE < ?)) AND ((LONGITUDE > ?) AND (LONGITUDE < ?)) ORDER BY ITEMTYPE");
			_ps.setInt(1, NavigationDataBean.INT);
			_ps.setInt(2, NavigationDataBean.RUNWAY);
			_ps.setDouble(3, loc.getLatitude() - height);
			_ps.setDouble(4, loc.getLatitude() + height);
			_ps.setDouble(5, loc.getLongitude() - width);
			_ps.setDouble(6, loc.getLongitude() + width);
			results = execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Ensure that we are within the correct distance and convert to a Map for easy lookup
		distanceFilter(results, loc, distance);
		return CollectionUtils.createMap(results, "code");
	}

	/**
	 * Helper method to iterate through a NAVDATA result set.
	 */
	private List<NavigationDataBean> execute() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<NavigationDataBean> results = new ArrayList<NavigationDataBean>();
		while (rs.next()) {
			NavigationDataBean obj = null;
			switch (rs.getInt(1)) {
				case NavigationDataBean.AIRPORT:
					AirportLocation a = new AirportLocation(rs.getDouble(3), rs.getDouble(4));
					a.setCode(rs.getString(2));
					a.setAltitude(rs.getInt(6));
					a.setName(rs.getString(7));
					a.setRegion(rs.getString(9));
					obj = a;
					break;

				case NavigationDataBean.INT:
					obj = new Intersection(rs.getString(2), rs.getDouble(3), rs.getDouble(4));
					obj.setRegion(rs.getString(9));
					break;

				case NavigationDataBean.VOR:
					VOR vor = new VOR(rs.getDouble(3), rs.getDouble(4));
					vor.setCode(rs.getString(2));
					vor.setFrequency(rs.getString(5));
					vor.setName(rs.getString(7));
					vor.setRegion(rs.getString(9));
					obj = vor;
					break;

				case NavigationDataBean.NDB:
					NDB ndb = new NDB(rs.getDouble(3), rs.getDouble(4));
					ndb.setCode(rs.getString(2));
					ndb.setFrequency(rs.getString(5));
					ndb.setName(rs.getString(7));
					ndb.setRegion(rs.getString(9));
					obj = ndb;
					break;

				case NavigationDataBean.RUNWAY:
					Runway rwy = new Runway(rs.getDouble(3), rs.getDouble(4));
					rwy.setCode(rs.getString(2));
					rwy.setFrequency(rs.getString(5));
					rwy.setLength(rs.getInt(6));
					rwy.setName(rs.getString(7));
					rwy.setHeading(rs.getInt(8));
					rwy.setRegion(rs.getString(9));
					obj = rwy;
					break;

				default:
			}

			// Add to results
			results.add(obj);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to filter objects based on distance from a certain point
	 */
	private void distanceFilter(Collection<NavigationDataBean> entries, GeoLocation loc, int distance) {
		for (Iterator<NavigationDataBean> i = entries.iterator(); i.hasNext();) {
			NavigationDataBean ndb = i.next();
			if (ndb.getPosition().distanceTo(loc) > distance)
				i.remove();
		}
	}
}