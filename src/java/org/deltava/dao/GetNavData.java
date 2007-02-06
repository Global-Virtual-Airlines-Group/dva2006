// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 1.0
 * @since 1.0
 */

public class GetNavData extends DAO {
	
	protected static final Cache<Cacheable> _cache = new AgingCache<Cacheable>(256);

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
		NavigationDataMap result = (NavigationDataMap) _cache.get(code);
		if (result != null)
			return result;
		
		try {
			prepareStatement("SELECT * FROM common.NAVDATA WHERE (UPPER(CODE)=?)");
			_ps.setString(1, code.toUpperCase());
			result = new NavigationDataMap(execute());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Add to the cache and return
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
		for (Iterator i = results.getAll().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = (NavigationDataBean) i.next();
			if (nd instanceof AirportLocation)
				return (AirportLocation) nd;
		}
		
		return null;
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
	 * @return a NavigationDataMap bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public NavigationDataMap getByID(Collection<String> ids) throws DAOException {

		// Check for empty id set
		if (ids.isEmpty())
			return new NavigationDataMap();
		
		// Check the cache
		NavigationDataMap results = (NavigationDataMap) _cache.get(ids);
		if (results != null)
			return results;

		// Build the SQL Statement
		Collection<String> orderedIDs = new LinkedHashSet<String>(ids);
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.NAVDATA WHERE CODE IN (");
		for (Iterator<String> i = orderedIDs.iterator(); i.hasNext();) {
			i.next();
			sqlBuf.append('?');
			if (i.hasNext())
			sqlBuf.append(',');
		}

		sqlBuf.append(')');
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			int ofs = 0;
			for (Iterator<String> i = orderedIDs.iterator(); i.hasNext(); ) {
				String id = i.next().toUpperCase();
				_ps.setString(++ofs, id);
			}
			
			results = new NavigationDataMap(execute());
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache and return
		results.setCacheKey(ids);
		_cache.add(results);
		return results;
	}

	/**
	 * Returns all Intersections within a set number of miles from a point.
	 * @param loc the central location
	 * @param distance the distance in miles
	 * @return a Map of Intersections, with the code as the key
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if distance is negative or > 300
	 */
	public Map<String, NavigationDataBean> getIntersections(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 300))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Calculate the height/width of the square in degrees (use 70% of the value of a lon degree)
		double height = (distance / GeoLocation.DEGREE_MILES) / 2;
		double width = (height * 0.7);

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
	 * @throws IllegalArgumentException if distance is negative or > 300
	 */
	public Map<String, NavigationDataBean> getObjects(GeoLocation loc, int distance) throws DAOException {
		if ((distance < 0) || (distance > 300))
			throw new IllegalArgumentException("Invalid distance -  " + distance);

		// Calculate the height/width of the square in degrees (use 70% of the value of a lon degree)
		double height = (distance / GeoLocation.DEGREE_MILES) / 2;
		double width = (height * 0.7);

		Collection<NavigationDataBean> results = null;
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
	private void distanceFilter(Collection entries, GeoLocation loc, int distance) {
		for (Iterator i = entries.iterator(); i.hasNext();) {
			NavigationDataBean ndb = (NavigationDataBean) i.next();
			if (ndb.getPosition().distanceTo(loc) > distance)
				i.remove();
		}
	}
}