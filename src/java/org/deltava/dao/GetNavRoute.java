// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.comparators.GeoComparator;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load navigation route and airway data.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class GetNavRoute extends GetNavData {
	
	private static final Cache<Route> _rCache = new AgingCache<Route>(640);
	private static final Cache<CacheableList<Airway>> _aCache = new AgingCache<CacheableList<Airway>>(640); 

	private class CacheableRoute implements Route {

		private String _route;
		private final LinkedList<NavigationDataBean> _waypoints = new LinkedList<NavigationDataBean>();

		CacheableRoute(String route, LinkedList<NavigationDataBean> waypoints) {
			super();
			_route = route.toUpperCase();
			_waypoints.addAll(waypoints);
		}
		
		public void addWaypoint(NavigationDataBean nd) {
			_waypoints.add(nd);
		}

		public LinkedList<NavigationDataBean> getWaypoints() {
			return new LinkedList<NavigationDataBean>(_waypoints);
		}
		
		public String getRoute() {
			return _route;
		}
		
		public int getSize() {
			return _waypoints.size();
		}

		public Object cacheKey() {
			return new Integer(_route.hashCode());
		}
	}

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavRoute(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public final int getRequests() {
		return _rCache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public final int getHits() {
		return _rCache.getHits();
	}

	/**
	 * Loads a SID/STAR from the navigation database.
	 * @param name the name of the Terminal Route, as NAME.TRANSITION
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(String name) throws DAOException {

		// Chceck the cache
		Route result = _rCache.get(name);
		if ((result != null) && (result instanceof TerminalRoute))
			return (TerminalRoute) result;

		// Split the name
		StringTokenizer tkns = new StringTokenizer(name, ".");
		if (tkns.countTokens() != 2)
			return null;

		try {
			prepareStatementWithoutLimits("SELECT * FROM common.SID_STAR WHERE (NAME=?) AND (TRANSITION=?) ORDER BY SEQ");
			_ps.setString(1, tkns.nextToken().toUpperCase());
			_ps.setString(2, tkns.nextToken().toUpperCase());

			// Execute the query
			List<TerminalRoute> results = executeSIDSTAR();
			result = results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		_rCache.add(result);
		return (TerminalRoute) result;
	}
	
	/**
	 * Retrieves a specifc SID/STAR.
	 * @param a the Airport
	 * @param type the route type
	 * @param name the name of the Terminal Route, as NAME.TRANSITION.RWY
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(Airport a, int type, String name) throws DAOException {

		// Chceck the cache
		Route result = _rCache.get(name);
		if ((result != null) && (result instanceof TerminalRoute)) {
			TerminalRoute tr = (TerminalRoute) result;
			if ((tr.getType() == type) && (tr.getICAO().equals(a.getICAO())))
				return tr;
		} else if (name == null)
			return null;
			
		// Split the name
		StringTokenizer tkns = new StringTokenizer(name, ".");
		if (tkns.countTokens() != 3)
			return null;

		try {
			prepareStatementWithoutLimits("SELECT * FROM common.SID_STAR WHERE (ICAO=?) AND (NAME=?) "
					+ "AND (TRANSITION=?) AND (RUNWAY=?) ORDER BY SEQ");
			_ps.setString(1, a.getICAO());
			_ps.setString(2, tkns.nextToken().toUpperCase());
			_ps.setString(3, tkns.nextToken().toUpperCase());
			_ps.setString(4, tkns.nextToken().toUpperCase());

			// Execute the query
			List<TerminalRoute> results = executeSIDSTAR();
			result = results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		_rCache.add(result);
		return (TerminalRoute) result;
	}
	
	/**
	 * Returns all SIDs/STARs in the database. <i>This does not populate waypoint data.</i>
	 * @return a Collection of TerminalRoute beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TerminalRoute> getRouteNames() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT ICAO, TYPE, NAME, TRANSITION, RUNWAY FROM common.SID_STAR "
					+ "ORDER BY ICAO, NAME, TRANSITION");
			List<TerminalRoute> results = executeSIDSTAR();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all SIDs/STARs in the database.
	 * @return a Collection of TerminalRoute beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TerminalRoute> getAll() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.SID_STAR ORDER BY ICAO, NAME, TRANSITION, RUNWAY, SEQ");
			List<TerminalRoute> results = executeSIDSTAR();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
			prepareStatement("SELECT * FROM common.SID_STAR WHERE (ICAO=?) AND (TYPE=?) ORDER BY NAME, TRANSITION, RUNWAY, SEQ");
			_ps.setString(1, code.toUpperCase());
			_ps.setInt(2, type);
			results = executeSIDSTAR();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache and return
		_rCache.addAll(results);
		return new LinkedHashSet<TerminalRoute>(results);
	}

	/**
	 * Loads a Airway definitions from the database.
	 * @param name the airway code
	 * @return a Collection of Airway beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airway> getAirways(String name) throws DAOException {

		// Check the cache
		CacheableList<Airway> results = _aCache.get(name);
		if (results != null)
			return results;

		results = new CacheableList<Airway>(name);
		try {
			prepareStatement("SELECT ID, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, REGION, HIGH, LOW "
					+ "FROM common.AIRWAYS WHERE (NAME=?) ORDER BY ID, SEQ");
			_ps.setString(1, name.toUpperCase());

			// Execute the query
			Airway a = null; int lastID = -1;
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				if (id != lastID) {
					lastID = id;
					a = new Airway(name, id);
					a.setHighLevel(rs.getBoolean(7));
					a.setLowLevel(rs.getBoolean(8));
					results.add(a);
				}
				
				NavigationDataBean nd = NavigationDataBean.create(rs.getInt(3), rs.getDouble(4), rs.getDouble(5));
				nd.setCode(rs.getString(2));
				nd.setRegion(rs.getString(6));
				a.addWaypoint(nd);
			}

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to cache and return
		_aCache.add(results);
		return results;
	}
	
	/**
	 * Loads all Airways from the database.
	 * @return a Collection of Airways
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airway> getAirways() throws DAOException {
		try {
			Collection<Airway> results = new ArrayList<Airway>();
			prepareStatementWithoutLimits("SELECT * FROM common.AIRWAYS ORDER BY NAME, ID, SEQ");
			
			// Execute the query
			Airway a = null; int lastID = -1; String lastCode = "";
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(2);
				String code = rs.getString(1).toUpperCase();
				if ((lastID != id) || (!lastCode.equals(code))) {
					lastID = id;
					lastCode = code;
					a = new Airway(code, id);
					a.setHighLevel(rs.getBoolean(9));
					a.setLowLevel(rs.getBoolean(10));
					results.add(a);
				}
				
				NavigationDataBean nd = NavigationDataBean.create(rs.getInt(5), rs.getDouble(6), rs.getDouble(7));
				nd.setCode(rs.getString(4));
				nd.setRegion(rs.getString(8));
				a.addWaypoint(nd);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all waypoints for a route, expanding Airways but <i>NOT</i> SID/STARs.
	 * @param route the space-delimited route
	 * @param start the starting point
	 * @return an ordered List of NavigationDataBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	public LinkedList<NavigationDataBean> getRouteWaypoints(String route, GeoLocation start) throws DAOException {
		if (route == null)
			return new LinkedList<NavigationDataBean>();

		// Check the cache
		Route obj = _rCache.get(route);
		if (obj instanceof CacheableRoute) {
			CacheableRoute cr = (CacheableRoute) obj;
			return cr.getWaypoints();
		} else if (obj != null)
			obj = null;

		// Get the route text
		List<String> tkns = StringUtils.split(route, " ");
		GeoLocation lastPosition = start;
		Set<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		for (int x = 0; x < tkns.size(); x++) {
			String wp = tkns.get(x);
			
			// Check if we're referencing an airway
			Collection<Airway> aws = getAirways(wp); 
			if (aws.size() > 0) {
				Airway aw = null;
				
				// Find the closest one if there's more than one
				if (aws.size() > 1) {
					GeoComparator gp = new GeoComparator(lastPosition);
					SortedSet<Airway> airways = new TreeSet<Airway>(gp);
					airways.addAll(aws);
					aw = airways.first();
				} else
					aw = aws.iterator().next();
				
				// Get the waypoints
				String endPoint = (x < (tkns.size() - 1)) ? tkns.get(x + 1) : "";
				Collection<NavigationDataBean> awPoints = aw.getWaypoints((x == 0) ? wp : tkns.get(x - 1), endPoint);
				routePoints.addAll(awPoints);
			} else {
				NavigationDataMap navaids = get(wp);
				NavigationDataBean nd = navaids.get(wp, lastPosition);
				if (nd != null) {
					routePoints.add(nd);
					lastPosition = nd;
				}
			}
		}

		// Get the points, and the start/distance
		LinkedList<NavigationDataBean> points = new LinkedList<NavigationDataBean>(routePoints);
		if (points.size() > 2) {
			GeoLocation lastP = points.getFirst();
			int distance = GeoUtils.distance(lastP, points.getLast());

			// Add a check to ensure that this point isn't crazily out of the way
			for (Iterator<? extends GeoLocation> i = points.iterator(); i.hasNext();) {
				GeoLocation gl = i.next();
				if (GeoUtils.distance(lastP, gl) > distance)
					i.remove();
				else
					lastP = gl;
			}
		}

		// Add to the cache and return the waypoints
		if (tkns.size() > 1) {
			CacheableRoute cr = new CacheableRoute(route, points);
			_rCache.add(cr);
		}
		
		return points;
	}

	/**
	 * Helper method to iterate through a SID_STAR result set.
	 */
	private List<TerminalRoute> executeSIDSTAR() throws SQLException {
		
		// Execute the Query
		TerminalRoute tr = null;
		ResultSet rs = _ps.executeQuery();
		int columnCount = rs.getMetaData().getColumnCount();
		List<TerminalRoute> results = new ArrayList<TerminalRoute>();
		while (rs.next()) {
			TerminalRoute tr2 = new TerminalRoute(rs.getString(1), rs.getString(3), rs.getInt(2));
			tr2.setTransition(rs.getString(4));
			tr2.setRunway(rs.getString(5));
			if (columnCount > 10)
				tr2.setCanPurge(rs.getBoolean(11));
			if ((tr == null) || (tr2.hashCode() != tr.hashCode())) {
				results.add(tr2);
				tr = tr2;
			}
			
			// Add the waypoint if present
			if (columnCount > 10) {
				NavigationDataBean nd = NavigationDataBean.create(rs.getInt(8), rs.getDouble(9), rs.getDouble(10));
				nd.setCode(rs.getString(7));
				nd.setRegion(rs.getString(11));
				tr.addWaypoint(nd);
			}
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}