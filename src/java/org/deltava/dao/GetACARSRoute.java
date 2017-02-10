// Copyright 2007, 2008, 2009, 2011, 2012, 2014, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load stored ACARS dispatch routes.
 * @author Luke
 * @version 7.2
 * @since 2.0
 */

public class GetACARSRoute extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSRoute(Connection c) {
		super(c);
	}
	
	/**
	 * Loads a saved route from the database.
	 * @param id the route ID
	 * @return a RoutePlan bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public DispatchRoute getRoute(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.ROUTES WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			
			// Execute the query
			List<DispatchRoute> results = execute();
			if (results.isEmpty())
				return null;
			
			// Load routes and return
			loadRoutes(results);
			return results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the database IDs of all dispatchers who have saved a Route.
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getAuthorIDs() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT AUTHOR FROM acars.ROUTES");
			Collection<Integer> results = new HashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all saved routes from the database.
	 * @param activeOnly TRUE if only active routes hould be loaded, otherwise FALSE
	 * @param loadWP TRUE if waypoints should be loaded, otherwise FALSE
	 * @return a Collection of RoutePlan beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DispatchRoute> getAll(boolean activeOnly, boolean loadWP) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM acars.ROUTES ");
		if (activeOnly)
			sqlBuf.append("WHERE (ACTIVE=?) ");
		sqlBuf.append("ORDER BY ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			if (activeOnly)
				_ps.setBoolean(1, true);

			// Execute the Query and load routes
			Collection<DispatchRoute> results = execute();
			if (loadWP)
				loadRoutes(results);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads Airports from all saved routes in the database.
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getAirports() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT AIRPORT_D, AIRPORT_A FROM acars.ROUTES");
			Collection<Airport> results = new HashSet<Airport>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					results.add(SystemData.getAirport(rs.getString(1)));
					results.add(SystemData.getAirport(rs.getString(2)));
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all saved routes from a particular Dispatcher.
	 * @param authorID the Dispatcher database ID
	 * @return a Collection of RoutePlan beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DispatchRoute> getByAuthor(int authorID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.ROUTES WHERE (AUTHOR=?) ORDER BY CREATEDON");
			_ps.setInt(1, authorID);
			
			// Execute the Query and load routes
			Collection<DispatchRoute> results = execute();
			loadRoutes(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all saved routes between two Airports.
	 * @param rp the RoutePair - either airport can be null
	 * @param activeOnly TRUE if only active routes should be returned, otherwise FALSE
	 * @return a Collection of RoutePlan beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DispatchRoute> getRoutes(RoutePair rp, boolean activeOnly) throws DAOException {
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM acars.ROUTES WHERE ");
		Collection<String> params = new ArrayList<String>();
		if (rp.getAirportD() != null)
			params.add("(AIRPORT_D=?)");
		if (rp.getAirportA() != null) 
			params.add("(AIRPORT_A=?)");
		if (activeOnly)
			params.add("(ACTIVE=?)");
		for (Iterator<String> i = params.iterator(); i.hasNext(); ) {
			sqlBuf.append(i.next());
			if (i.hasNext())
				sqlBuf.append(" AND ");
		}
		sqlBuf.append(" ORDER BY USED DESC");
		
		try {
			int param = 0;
			prepareStatement(sqlBuf.toString());
			if (rp.getAirportD() != null)
				_ps.setString(++param, rp.getAirportD().getIATA());
			if (rp.getAirportA() != null)
				_ps.setString(++param, rp.getAirportA().getIATA());
			if (activeOnly)
				_ps.setBoolean(++param, true);
			
			// Execute the Query and load routes
			Collection<DispatchRoute> results = execute();
			loadRoutes(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Searches for duplicate routes between two Airports.
	 * @param rp the RoutePair
	 * @param route the route waypoints, separated by spaces
	 * @return the route database ID, or zero if no duplicate found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int hasDuplicate(RoutePair rp, String route) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT R.ID, GROUP_CONCAT(RW.CODE ORDER BY RW.SEQ SEPARATOR ?) "
					+ "AS WPS FROM acars.ROUTES R LEFT JOIN acars.ROUTE_WP RW ON (R.ID=RW.ID) WHERE "
					+ "(R.AIRPORT_D=?) AND (R.AIRPORT_A=?) AND (R.ACTIVE=?) GROUP BY R.ID HAVING (WPS=?) LIMIT 1");
			_ps.setString(1, " ");
			_ps.setString(2, rp.getAirportD().getIATA());
			_ps.setString(3, rp.getAirportA().getIATA());
			_ps.setBoolean(4, true);
			_ps.setString(5, route);
			
			// Do the query
			int id = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) id = rs.getInt(1);
			}
			
			_ps.close();
			return id;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private List<DispatchRoute> execute() throws SQLException {
		List<DispatchRoute> results = new ArrayList<DispatchRoute>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				DispatchRoute rp = new DispatchRoute();
				rp.setID(rs.getInt(1));
				rp.setAuthorID(rs.getInt(2));
				rp.setAirline(SystemData.getAirline(rs.getString(3)));
				rp.setAirportD(SystemData.getAirport(rs.getString(4)));
				rp.setAirportA(SystemData.getAirport(rs.getString(5)));
				rp.setAirportL(SystemData.getAirport(rs.getString(6)));
				rp.setCreatedOn(rs.getTimestamp(7).toInstant());
				rp.setLastUsed(toInstant(rs.getTimestamp(8)));
				rp.setUseCount(rs.getInt(9));
				rp.setActive(rs.getBoolean(10));
				rp.setSID(rs.getString(11));
				rp.setSTAR(rs.getString(12));
				rp.setCruiseAltitude(rs.getString(13));
				rp.setDispatchBuild(rs.getInt(14));
				rp.setComments(rs.getString(15));
				rp.setRoute(rs.getString(16));
				results.add(rp);
			}
		}
		
		_ps.close();
		return results;
	}
	
	/*
	 * Helper method to load route waypoints.
	 */
	private void loadRoutes(Collection<DispatchRoute> routes) throws SQLException {
		if (routes.isEmpty())
			return;
		
		StringBuilder buf = new StringBuilder("SELECT * FROM acars.ROUTE_WP WHERE ID IN (");
		for (Iterator<DispatchRoute> i = routes.iterator(); i.hasNext(); ) {
			DispatchRoute rp = i.next();
			buf.append(String.valueOf(rp.getID()));
			if (i.hasNext())
				buf.append(',');
		}
		
		buf.append(") ORDER BY ID, SEQ");
		prepareStatementWithoutLimits(buf.toString());
		
		// Execute the query
		Map<Integer, DispatchRoute> data = CollectionUtils.createMap(routes, DispatchRoute::getID);
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				DispatchRoute rp = data.get(Integer.valueOf(rs.getInt(1)));
				if (rp != null) {
					double lat = rs.getDouble(5);
					double lng = rs.getDouble(6);
					Navaid nt = Navaid.values()[rs.getInt(4)];
					NavigationDataBean nd = NavigationDataBean.create(nt, lat, lng);
					nd.setCode(rs.getString(3));
					nd.setRegion(rs.getString(8));
					rp.addWaypoint(nd, rs.getString(7));
				}	
			}
		}
		
		_ps.close();
	}
}