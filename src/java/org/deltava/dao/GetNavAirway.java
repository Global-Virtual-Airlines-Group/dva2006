// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;

import com.enterprisedt.util.debug.Logger;

/**
 * A Data Access Object to load navigation route and airway data.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetNavAirway extends GetNavData {
	
	private static final Logger log = Logger.getLogger(GetNavAirway.class);
	
	private static final Cache<TerminalRoute> _rCache = new AgingCache<TerminalRoute>(640);
	private static final Cache<CacheableList<Airway>> _aCache = new AgingCache<CacheableList<Airway>>(1024);
	private static final Cache<CacheableSet<String>> _rwCache = new AgingCache<CacheableSet<String>>(256);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavAirway(Connection c) {
		super(c);
	}
	
	/**
	 * Returns information about the caches.
	 */
	public CacheInfo getCacheInfo() {
		CacheInfo info = new CacheInfo(_rCache);
		info.add(_aCache);
		info.add(_rwCache);
		return info;
	}
	
	/**
	 * Clears the caches.
	 */
	public void clear() {
		_rCache.clear();
		_aCache.clear();
		_rwCache.clear();
		super.clear();
	}
	
	/**
	 * Loads a SID/STAR from the navigation database.
	 * @param name the name of the Terminal Route, as NAME.TRANSITION
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(String name) throws DAOException {
		if (StringUtils.isEmpty(name)) return null;

		// Check the cache
		TerminalRoute result = _rCache.get(name);
		if (result != null)
			return result;

		// Split the name
		StringTokenizer tkns = new StringTokenizer(name, ".");
		int tkCount = tkns.countTokens();
		if ((tkCount < 1) || (tkCount > 4))
			return null;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.SID_STAR WHERE ");
		if (name.contains("%"))
			sqlBuf.append("(NAME LIKE ?)");
		else
			sqlBuf.append("(NAME=?)");
		
		sqlBuf.append(" AND (TRANSITION=?)");
		if (tkCount > 2)
			sqlBuf.append(" AND (RUNWAY=?)");
		if (tkCount > 3)
			sqlBuf.append(" AND (ICAO=?)");
		sqlBuf.append(" ORDER BY SEQ");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			if (tkCount < 4) {
				_ps.setString(1, tkns.nextToken().toUpperCase());
				_ps.setString(2, tkns.nextToken().toUpperCase());
				if (tkns.hasMoreTokens())
					_ps.setString(3, tkns.nextToken().toUpperCase());
			} else {
				_ps.setString(4, tkns.nextToken().toUpperCase());
				_ps.setString(1, tkns.nextToken().toUpperCase());
				_ps.setString(2, tkns.nextToken().toUpperCase());
				_ps.setString(3, tkns.nextToken().toUpperCase());
			}

			// Execute the query
			List<TerminalRoute> results = executeSIDSTAR();
			result = results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		_rCache.add(result);
		return result;
	}
	
	/**
	 * Retrieves a specifc SID/STAR.
	 * @param a the Airport
	 * @param type the route type
	 * @param name the name of the Terminal Route, as NAME.TRANSITION.RWY
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(ICAOAirport a, int type, String name) throws DAOException {

		// Check the cache
		TerminalRoute tr = _rCache.get(name);
		if (tr != null) {
			if ((tr.getType() == type) && (tr.getICAO().equals(a.getICAO())))
				return tr;
		} else if (name == null)
			return null;
			
		// Split the name
		List<String> parts = StringUtils.split(name, ".");
		if (parts.size() == 2)
			parts.add("ALL");
		else if (parts.size() != 3)
			return null;

		try {
			prepareStatementWithoutLimits("SELECT * FROM common.SID_STAR WHERE (ICAO=?) AND (NAME=?) "
					+ "AND (TRANSITION=?) AND (RUNWAY=?) ORDER BY SEQ");
			_ps.setString(1, a.getICAO());
			_ps.setString(2, parts.get(0).toUpperCase());
			_ps.setString(3, parts.get(1).toUpperCase());
			_ps.setString(4, parts.get(2).toUpperCase());

			// Execute the query
			List<TerminalRoute> results = executeSIDSTAR();
			tr = results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		_rCache.add(tr);
		return tr;
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
	 * Returns the most likely Terminal Route used based on the Airport, Name, last waypoint and runway.
	 * @param a the Airport
	 * @param type the TerminalRoute type
	 * @param name the name
	 * @param wp the waypoint
	 * @param rwy the runway name, or null
	 * @return the TerminalRoute, or null if none found
	 * @see GetNavAirway#getBestRoute(ICAOAirport, int, String, String, Runway)
	 */
	public TerminalRoute getBestRoute(ICAOAirport a, int type, String name, String wp, Runway rwy) throws DAOException {
		return getBestRoute(a, type, name, wp, (rwy == null) ? "ALL" : "RW" + rwy.getName());
	}
	
	/**
	 * Returns the most likely Terminal Route used based on the Airport, Name, last waypoint and runway.
	 * @param a the Airport
	 * @param type the TerminalRoute type
	 * @param name the name
	 * @param wp the waypoint
	 * @param rwy the Runway bean, or null
	 * @return the TerminalRoute, or null if none found
	 */
	public TerminalRoute getBestRoute(ICAOAirport a, int type, String name, String wp, String rwy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT CONCAT_WS('.', NAME, TRANSITION, RUNWAY), IF(RUNWAY=?, 0, 1) "
					+ "AS PRF FROM common.SID_STAR WHERE (ICAO=?) AND (TYPE=?) AND (NAME=?) AND (WAYPOINT=?) ");
		if (rwy != null)
			buf.append("AND ((RUNWAY=?) OR (RUNWAY=?))");
		buf.append(" ORDER BY PRF, SEQ");
		
		try {
			prepareStatementWithoutLimits(buf.toString());
			_ps.setString(1, "ALL");
			_ps.setString(2, a.getICAO());
			_ps.setInt(3, type);
			_ps.setString(4, name);
			_ps.setString(5, wp);
			if (rwy != null) {
				_ps.setString(6, "ALL");
				_ps.setString(7, rwy);
			}
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			String code = rs.next() ? rs.getString(1) : null;
			rs.close();
			_ps.close();
			
			// Fetch the route itself
			log.info("Found " + code);
			return getRoute(a, type, code);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the available SID runways for a particular Airport.
	 * @param code the Airport ICAO code
	 * @return a Collection of Runway codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getSIDRunways(String code) throws DAOException {
		
		// Check the cache
		code = code.toUpperCase();
		CacheableSet<String> results = _rwCache.get(code);
		if (results != null)
			return results;
		
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT RUNWAY FROM common.SID_STAR WHERE (ICAO=?) AND "
					+ "(TYPE=?) ORDER BY RUNWAY");
			_ps.setString(1, code.toUpperCase());
			_ps.setInt(2, TerminalRoute.SID);
			
			// Execute the query
			results = new CacheableSet<String>(code);
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				String rwy = rs.getString(1);
				if (!"ALL".equals(rwy))
					results.add(rwy);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			_rwCache.add(results);
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