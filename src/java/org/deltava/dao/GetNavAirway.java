// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 6.4
 * @since 1.0
 */

public class GetNavAirway extends GetNavData {
	
	private static final Logger log = Logger.getLogger(GetNavAirway.class);
	private static final Cache<TerminalRoute> _rCache = CacheManager.get(TerminalRoute.class, "NavSIDSTAR");
	private static final Cache<CacheableList<Airway>> _aCache = CacheManager.getCollection(Airway.class, "NavAirway"); 
	private static final Cache<CacheableSet<String>> _rwCache = CacheManager.getCollection(String.class, "NavRunway");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavAirway(Connection c) {
		super(c);
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
		if ((tkCount < 2) || (tkCount > 4))
			return null;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.SIDSTAR_META WHERE ");
		if (name.contains("%"))
			sqlBuf.append("(NAME LIKE ?)");
		else
			sqlBuf.append("(NAME=?)");
		
		sqlBuf.append(" AND (TRANSITION=?)");
		if (tkCount > 2)
			sqlBuf.append(" AND (RUNWAY=?)");
		if (tkCount > 3)
			sqlBuf.append(" AND (ICAO=?)");

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
			loadWaypoints(result);
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
	 * @param t the route type
	 * @param name the name of the Terminal Route, as NAME.TRANSITION.RWY
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(ICAOAirport a, TerminalRoute.Type t, String name) throws DAOException {
		return getRoute(a, t, name, false);
	}
	
	/**
	 * Retrieves a specifc SID/STAR.
	 * @param a the Airport
	 * @param t the route type
	 * @param name the name of the Terminal Route, as NAME.TRANSITION.RWY
	 * @param ignoreVersion TRUE if the sequence number should be ignored in favor of a newer or older version, otherwise FALSE 
	 * @return a TerminalRoute bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TerminalRoute getRoute(ICAOAirport a, TerminalRoute.Type t, String name, boolean ignoreVersion) throws DAOException {

		// Check the cache
		TerminalRoute tr = _rCache.get(name);
		if (tr != null) {
			if ((tr.getType() == t) && (tr.getICAO().equals(a.getICAO())))
				return tr;
		} else if (name == null)
			return null;
		
		// Split the name
		List<String> parts = StringUtils.split(name, ".");
		if (parts.size() == 2)
			parts.add("ALL");
		else if (parts.size() != 3)
			return null;
		
		// If we're ignoring the version, then strip off the digit
		if (ignoreVersion) {
			String newName = parts.get(0);
			if (TerminalRoute.isNameValid(newName))
				parts.set(0, TerminalRoute.makeGeneric(newName));
		}
			
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.SIDSTAR_META WHERE (ICAO=?) AND (NAME");
		sqlBuf.append(parts.get(0).contains("%") ? " LIKE " : "=");
		sqlBuf.append("?) AND (TRANSITION=?) AND (RUNWAY=?) ORDER BY SEQ");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, a.getICAO());
			_ps.setString(2, parts.get(0).toUpperCase());
			_ps.setString(3, parts.get(1).toUpperCase());
			_ps.setString(4, parts.get(2).toUpperCase());

			// Execute the query
			List<TerminalRoute> results = executeSIDSTAR();
			tr = results.isEmpty() ? null : results.get(0);
			loadWaypoints(tr);
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
			prepareStatementWithoutLimits("SELECT ICAO, TYPE, NAME, TRANSITION, RUNWAY FROM common.SIDSTAR_META ORDER BY ICAO, NAME, TRANSITION");
			List<TerminalRoute> results = executeSIDSTAR();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the most likely Terminal Route used based on the Airport, Name, last waypoint and runway.
	 * @param a the Airport
	 * @param t the TerminalRoute type
	 * @param name the name
	 * @param wp the waypoint
	 * @param rwy the runway name, or null
	 * @return the TerminalRoute, or null if none found
	 * @see GetNavAirway#getBestRoute(ICAOAirport, TerminalRoute.Type, String, String, Runway)
	 */
	public TerminalRoute getBestRoute(ICAOAirport a, TerminalRoute.Type t, String name, String wp, Runway rwy) throws DAOException {
		return getBestRoute(a, t, name, wp, (rwy == null) ? "ALL" : "RW" + rwy.getName());
	}
	
	/**
	 * Returns the most likely Terminal Route used based on the Airport, Name, last waypoint and runway.
	 * @param a the Airport
	 * @param t the TerminalRoute type
	 * @param name the name
	 * @param wp the waypoint
	 * @param rwy the Runway bean, or null
	 * @return the TerminalRoute, or null if none found
	 */
	public TerminalRoute getBestRoute(ICAOAirport a, TerminalRoute.Type t, String name, String wp, String rwy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT DISTINCT CONCAT_WS('.', SS.NAME, SS.TRANSITION, SS.RUNWAY), IF(SS.RUNWAY=?, 0, 1) AS PRF "
			+ "FROM common.SIDSTAR_META SS LEFT JOIN common.SIDSTAR_WAYPOINTS SW ON (SS.ICAO=SW.ICAO) AND (SS.ID=SW.ID) WHERE (SS.ICAO=?) "
			+ "AND (SS.TYPE=?) AND ");
		buf.append(name.contains("%") ? "(SS.NAME LIKE ?)" : "(SS.NAME=?)");
		if (wp != null)
			buf.append(" AND (SW.WAYPOINT=?) ");
		if (rwy != null)
			buf.append("AND ((SS.RUNWAY=?) OR (SS.RUNWAY=?))");
		buf.append(" ORDER BY PRF, SW.SEQ");
		
		try {
			int pos = 0;
			prepareStatementWithoutLimits(buf.toString());
			_ps.setString(++pos, "ALL");
			_ps.setString(++pos, a.getICAO());
			_ps.setInt(++pos, t.ordinal());
			_ps.setString(++pos, name);
			if (wp != null)
				_ps.setString(++pos, wp);
			if (rwy != null) {
				_ps.setString(++pos, "ALL");
				_ps.setString(++pos, rwy);
			}
			
			// Execute the query
			String code = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					code = rs.getString(1);
			}
			
			_ps.close();
			
			// Fetch the route itself
			log.info("Found " + code);
			return getRoute(a, t, code);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the available SID runways for a particular Airport.
	 * @param a an ICAOAirport
	 * @return a Collection of Runway codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getSIDRunways(ICAOAirport a) throws DAOException {
		
		// Check the cache
		CacheableSet<String> results = _rwCache.get(a.getICAO());
		if (results != null)
			return results;
		
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT RUNWAY FROM common.SIDSTAR_META WHERE (ICAO=?) AND (TYPE=?) ORDER BY RUNWAY");
			_ps.setString(1, a.getICAO());
			_ps.setInt(2, TerminalRoute.Type.SID.ordinal());
			
			// Execute the query
			results = new CacheableSet<String>(a.getICAO());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					String rwy = rs.getString(1);
					if (!"ALL".equals(rwy))
						results.add(rwy);
				}
			}
			
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
			prepareStatementWithoutLimits("SELECT * FROM common.SIDSTAR_META ORDER BY ICAO, NAME, TRANSITION, RUNWAY");
			_ps.setFetchSize(2500);
			Collection<TerminalRoute> results = executeSIDSTAR();
			for (TerminalRoute tr : results)
				loadWaypoints(tr);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all SIDs/STARs for a particular Airport.
	 * @param a the ICAOAirport
	 * @param t the SID/STAR type
	 * @return a List of TerminalRoutes
	 * @throws DAOException
	 */
	public Collection<TerminalRoute> getRoutes(ICAOAirport a, TerminalRoute.Type t) throws DAOException {
		List<TerminalRoute> results = null;
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.SIDSTAR_META WHERE (ICAO=?) AND (TYPE=?) ORDER BY NAME, TRANSITION, RUNWAY");
			_ps.setString(1, a.getICAO());
			_ps.setInt(2, t.ordinal());
			results = executeSIDSTAR();
			for (TerminalRoute tr : results)
				loadWaypoints(tr);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache and return
		_rCache.addAll(results);
		return results;
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
			prepareStatement("SELECT ID, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, REGION, FREQ, HIGH, LOW "
				+ "FROM common.AIRWAYS WHERE (NAME=?) ORDER BY ID, SEQ");
			_ps.setString(1, name.toUpperCase());

			// Execute the query
			Airway a = null; int lastID = -1;
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt(1);
					if (id != lastID) {
						lastID = id;
						a = new Airway(name, id);
						a.setHighLevel(rs.getBoolean(8));
						a.setLowLevel(rs.getBoolean(9));
						results.add(a);
					}
				
					Navaid nt = Navaid.values()[rs.getInt(3)]; String freq = rs.getString(7);
					NavigationDataBean nd = NavigationDataBean.create(nt, rs.getDouble(4), rs.getDouble(5));
					nd.setCode(rs.getString(2));
					nd.setRegion(rs.getString(6));
					if ((nd instanceof NavigationFrequencyBean) && (freq != null))
						((NavigationFrequencyBean) nd).setFrequency(freq);
						
					a.addWaypoint(nd);
				}
			}

			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Add to the cache
		_aCache.add(results);
		return results;
	}
	
	/*
	 * Helper method to iterate through a SIDSTAR_WAYPOINTS result set.
	 */
	private void loadWaypoints(TerminalRoute tr) throws SQLException {
		if (tr == null) return;
		prepareStatementWithoutLimits("SELECT * FROM common.SIDSTAR_WAYPOINTS WHERE (ICAO=?) AND (TYPE=?) AND (ID=?) ORDER BY SEQ");
		_ps.setString(1,  tr.getICAO());
		_ps.setInt(2, tr.getType().ordinal());
		_ps.setInt(3, tr.getSequence());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Navaid nt = Navaid.values()[rs.getInt(6)];
				NavigationDataBean nd = NavigationDataBean.create(nt, rs.getDouble(7), rs.getDouble(8));
				nd.setCode(rs.getString(5));
				nd.setRegion(rs.getString(9));
				tr.addWaypoint(nd);
			}
		}
		
		_ps.close();
	}
	
	/*
	 * Helper method to iterate through a SIDSTAR_META result set.
	 */
	private List<TerminalRoute> executeSIDSTAR() throws SQLException {
		List<TerminalRoute> results = new ArrayList<TerminalRoute>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				TerminalRoute.Type rt = TerminalRoute.Type.values()[rs.getInt(5)];
				TerminalRoute tr = new TerminalRoute(rs.getString(1), rs.getString(2), rt);
				tr.setTransition(rs.getString(3));
				tr.setRunway(rs.getString(4));
				tr.setSequence(rs.getInt(6));
				tr.setCanPurge(rs.getBoolean(7));
				results.add(tr);
			}
		}
		
		_ps.close();
		return results;
	}
}