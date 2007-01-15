// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.beans.navdata.*;

import org.deltava.util.GeoUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A Data Access Object to load navigation route and airway data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetNavRoute extends GetNavData {

	private class CacheableRoute implements Cacheable {

		private String _route;
		private LinkedList<NavigationDataBean> _waypoints;

		CacheableRoute(String route, LinkedList<NavigationDataBean> waypoints) {
			super();
			_route = route;
			_waypoints = waypoints;
		}

		public LinkedList<NavigationDataBean> getWaypoints() {
			return _waypoints;
		}

		public Object cacheKey() {
			return _route;
		}
	}

	/**
	 * Initializes the Data Access Object
	 * @param c the JDBC connection to use
	 */
	public GetNavRoute(Connection c) {
		super(c);
	}

	/**
	 * Loads a SID/STAR from the navigation database.
	 * @param name the name of the Terminal Route, as NAME.TRANSITION
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(String name) throws DAOException {

		// Chceck the cache
		TerminalRoute result = (TerminalRoute) _cache.get(name);
		if (result != null)
			return result;

		// Split the name
		StringTokenizer tkns = new StringTokenizer(name, ".");
		if (tkns.countTokens() != 2)
			return null;

		try {
			prepareStatement("SELECT * FROM common.SID_STAR WHERE (NAME=?) AND (TRANSITION=?)");
			_ps.setString(1, tkns.nextToken().toUpperCase());
			_ps.setString(2, tkns.nextToken().toUpperCase());
			_ps.setMaxRows(1);

			// Execute the query
			List results = executeSIDSTAR();
			result = results.isEmpty() ? null : (TerminalRoute) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		_cache.add(result);
		return result;
	}

	/**
	 * Loads all SIDs/STARs for a particular Airport.
	 * @param code the Airport ICAO code
	 * @param type the SID/STAR type
	 * @return a List of TerminalRoutes
	 * @throws DAOException
	 * @see TerminalRoute#SID
	 * @see TerminalRoute#STAR
	 */
	public Collection<TerminalRoute> getRoutes(String code, int type) throws DAOException {
		List<TerminalRoute> results = null;
		try {
			prepareStatement("SELECT * FROM common.SID_STAR WHERE (ICAO=?) AND (TYPE=?) ORDER BY NAME, TRANSITION");
			_ps.setString(1, code.toUpperCase());
			_ps.setInt(2, type);
			results = executeSIDSTAR();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache and return
		_cache.addAll(results);
		return new LinkedHashSet<TerminalRoute>(results);
	}

	/**
	 * Loads an Airway definition from the database.
	 * @param name the airway code
	 * @return an Airway bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Airway getAirway(String name) throws DAOException {

		// Check the cache
		Cacheable result = _cache.get(name);
		if (result instanceof Airway) {
			return (Airway) result;
		} else if (result != null) {
			result = null;
		}

		try {
			prepareStatement("SELECT * FROM common.AIRWAYS WHERE (NAME=?)");
			_ps.setString(1, name.toUpperCase());
			_ps.setMaxRows(1);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Populate the airway bean
			if (rs.next())
				result = new Airway(rs.getString(1), rs.getString(2));

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to cache and return
		_cache.add(result);
		return (Airway) result;
	}

	/**
	 * Loads multiple Airway definitions from the database.
	 * @param names a Collection of airway names
	 * @return a Map of Airways, indexed by name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Airway> getAirways(Collection<String> names) throws DAOException {

		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT * FROM common.AIRWAYS WHERE (NAME IN (");
		for (Iterator i = names.iterator(); i.hasNext();) {
			String code = (String) i.next();
			buf.append(code.toUpperCase());
			if (i.hasNext())
				buf.append(',');
		}

		// Close the SQL statement
		buf.append("))");

		Map<String, Airway> results = new HashMap<String, Airway>();
		try {
			prepareStatement(buf.toString());

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Airway a = new Airway(rs.getString(1), rs.getString(2));
				results.put(a.getCode(), a);
			}

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Return results
		return results;
	}

	/**
	 * Returns all waypoints for a route, expanding SIDs/STARs and Airways.
	 * @param route the space-delimited route
	 * @return an ordered List of NavigationDataBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	public LinkedList<NavigationDataBean> getRouteWaypoints(String route) throws DAOException {

		// Check the cache
		Cacheable obj = _cache.get(route);
		if (obj instanceof CacheableRoute) {
			CacheableRoute cr = (CacheableRoute) obj;
			return cr.getWaypoints();
		} else if (obj != null) {
			obj = null;
		}

		// Get the route text
		List tkns = Collections.list(new StringTokenizer(route, " "));
		GeoLocation lastPosition = null;
		Set<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		for (int x = 0; x < tkns.size(); x++) {
			String wp = (String) tkns.get(x);

			// Check for an SID/STAR
			if (wp.indexOf('.') != -1) {
				TerminalRoute tr = getRoute(wp); // Load the SID/STAR
				if (tr != null) {
					NavigationDataMap ndMap = getByID(tr.getWaypoints());
					routePoints.addAll(tr.getWaypoints(ndMap));
				}
			} else {
				Airway aw = getAirway(wp); // Check if we're referencing an airway
				if (aw != null) {
					String endPoint = (x < (tkns.size() - 1)) ? (String) tkns.get(x + 1) : "";
					Collection<String> awPoints = aw.getWaypoints((x == 0) ? wp : (String) tkns.get(x - 1), endPoint);
					NavigationDataMap ndMap = getByID(awPoints);
					for (Iterator i = awPoints.iterator(); i.hasNext();) {
						String awp = (String) i.next();
						NavigationDataBean nd = ndMap.get(awp, lastPosition);
						if (nd != null) {
							routePoints.add(nd);
							lastPosition = nd;
						}
					}
				} else {
					NavigationDataMap navaids = get(wp);
					NavigationDataBean nd = navaids.get(wp, lastPosition);
					if (nd != null) {
						routePoints.add(nd);
						lastPosition = nd;
					}
				}
			}
		}

		// Get the points, and the start/distance
		LinkedList<NavigationDataBean> points = new LinkedList<NavigationDataBean>(routePoints);
		if (points.size() > 2) {
			GeoLocation lastP = points.getFirst();
			int distance = GeoUtils.distance(lastP, points.getLast());

			// Add a check to ensure that this point isn't crazily out of the way
			for (Iterator i = points.iterator(); i.hasNext();) {
				GeoLocation gl = (GeoLocation) i.next();
				if (GeoUtils.distance(lastP, gl) > distance)
					i.remove();
			}
		}

		// Add to the cache and return the waypoints
		if (tkns.size() > 1) {
			CacheableRoute cr = new CacheableRoute(route, points);
			_cache.add(cr);
		}
		
		return points;
	}

	/**
	 * Helper method to iterate through a SID_STAR result set.
	 */
	private List<TerminalRoute> executeSIDSTAR() throws SQLException {

		// Execute the Query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<TerminalRoute> results = new ArrayList<TerminalRoute>();
		while (rs.next()) {
			TerminalRoute tr = new TerminalRoute(rs.getString(1), rs.getString(3), rs.getInt(2) + 1);
			tr.setTransition(rs.getString(4));
			tr.setRunway(rs.getString(5));
			tr.setRoute(rs.getString(6));

			// Add to results
			results.add(tr);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}