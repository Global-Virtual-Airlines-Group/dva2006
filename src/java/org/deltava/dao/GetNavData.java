// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;

import org.deltava.util.CollectionUtils;

import org.deltava.util.cache.AgingCache;
import org.deltava.util.cache.Cache;

/**
 * A Data Access Object to read Navigation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetNavData extends DAO {

	private static final Logger log = Logger.getLogger(GetNavData.class);
	private static Cache _cache = new AgingCache(192);

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
	 * @return a NavigationDataBean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public NavigationDataBean get(String code) throws DAOException {

		// Check if we're in the cache
		code = code.toUpperCase();
		if (_cache.contains(code))
			return (NavigationDataBean) _cache.get(code);

		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (UPPER(CODE)=?)");
			_ps.setString(1, code.toUpperCase());
			setQueryMax(1);

			// Execute the query
			List results = execute();
			return results.isEmpty() ? null : (NavigationDataBean) results.get(0);
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
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND (UPPER(CODE)=?) "
					+ "AND (UPPER(NAME)=?)");
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
	 * @return a Map of NavigationDataBeans, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map getByID(Collection ids) throws DAOException {

		// Build the results
		List results = new ArrayList();

		// Build the SQL Statement
		int querySize = 0;
		StringBuffer sqlBuf = new StringBuffer("SELECT * FROM common.NAVDATA WHERE CODE IN (");
		for (Iterator i = ids.iterator(); i.hasNext();) {
			String code = ((String) i.next()).toUpperCase();
			if (_cache.contains(code)) {
				results.add(_cache.get(code));
			} else {
				querySize++;
				sqlBuf.append('\'');
				sqlBuf.append(code);
				sqlBuf.append("\',");
			}
		}

		// Only execute the prepared statement if we haven't gotten anything from the cache
		log.debug("Uncached set size = " + querySize);
		if (querySize > 0) {
			if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
				sqlBuf.setLength(sqlBuf.length() - 1);

			sqlBuf.append(')');
			List uncached = null;
			try {
				prepareStatementWithoutLimits(sqlBuf.toString());
				uncached = execute();
			} catch (SQLException se) {
				throw new DAOException(se);
			}

			// Add to results
			results.addAll(uncached);
		}

		// Convert to a Map for easy lookup
		return CollectionUtils.createMap(results, "code");
	}
	
	/**
	 * Returns all Intersections within a set number of miles from a point.
	 * @param loc the central location
	 * @param distance the distance in miles
	 * @return a Map of Intersections, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if distance is negative or > 300
	 */
	public Map getIntersections(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 300))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Calculate the height/width of the square in degrees (use 70% of the value of a lon degree)
		double height = (distance / GeoLocation.DEGREE_MILES) / 2;
		double width = (height * 0.7);

		Collection results = null;
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
	 * @throws IllegalArgumentException if distance is negative or > 300
	 */
	public Map getObjects(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 300))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Calculate the height/width of the square in degrees (use 70% of the value of a lon degree)
		double height = (distance / GeoLocation.DEGREE_MILES) / 2;
		double width = (height * 0.7);
		
		Collection results = null;
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE <> ?) AND (ITEMTYPE <> ?) AND "
					+ "((LATITUDE > ?) AND (LATITUDE < ?)) AND ((LONGITUDE > ?) AND (LONGITUDE < ?))");
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
	 * Helper method to iterate through the result set.
	 */
	private List execute() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List results = new ArrayList();
		while (rs.next()) {
			NavigationDataBean obj = null;
			switch (rs.getInt(1)) {
				case NavigationDataBean.AIRPORT:
					AirportLocation a = new AirportLocation(rs.getDouble(3), rs.getDouble(4));
					a.setCode(rs.getString(2));
					a.setAltitude(rs.getInt(6));
					a.setName(rs.getString(7));
					obj = a;
					break;

				case NavigationDataBean.INT:
					Intersection i = new Intersection(rs.getDouble(3), rs.getDouble(4));
					i.setCode(rs.getString(2));
					obj = i;
					break;

				case NavigationDataBean.VOR:
					VOR vor = new VOR(rs.getDouble(3), rs.getDouble(4));
					vor.setCode(rs.getString(2));
					vor.setFrequency(rs.getString(5));
					vor.setName(rs.getString(7));
					obj = vor;
					break;

				case NavigationDataBean.NDB:
					NDB ndb = new NDB(rs.getDouble(3), rs.getDouble(4));
					ndb.setCode(rs.getString(2));
					ndb.setFrequency(rs.getString(5));
					ndb.setName(rs.getString(7));
					obj = ndb;
					break;

				case NavigationDataBean.RUNWAY:
					Runway rwy = new Runway(rs.getDouble(3), rs.getDouble(4));
					rwy.setCode(rs.getString(2));
					rwy.setFrequency(rs.getString(5));
					rwy.setLength(rs.getInt(6));
					rwy.setName(rs.getString(7));
					rwy.setHeading(rs.getInt(8));
					obj = rwy;
					break;

				default:
			}

			// Add to results and cache
			_cache.add(obj);
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
	private void distanceFilter(Collection entries, GeoLocation loc, int distance) {
		for (Iterator i = entries.iterator(); i.hasNext();) {
			NavigationDataBean ndb = (NavigationDataBean) i.next();
			if (ndb.getPosition().distanceTo(loc) > distance)
				i.remove();
		}
	}
}