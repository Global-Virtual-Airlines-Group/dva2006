// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.RoutePlan;

import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load stored ACARS dispatch routes.
 * @author Luke
 * @version 2.1
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
	public RoutePlan getRoute(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.ROUTES WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			
			// Execute the query
			List<RoutePlan> results = execute();
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
			
			// Execute the query
			Collection<Integer> results = new HashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all saved routes from the database.
	 * @return a Collection of RoutePlan beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<RoutePlan> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.ROUTES ORDER BY CREATEDON");

			// Execute the Query and load routes
			Collection<RoutePlan> results = execute();
			loadRoutes(results);
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
	public Collection<RoutePlan> getByAuthor(int authorID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.ROUTES WHERE (AUTHOR=?) ORDER BY CREATEDON");
			_ps.setInt(1, authorID);
			
			// Execute the Query and load routes
			Collection<RoutePlan> results = execute();
			loadRoutes(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all saved routes between two Airports.
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @return a Collection of RoutePlan beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<RoutePlan> getRoutes(Airport aD, Airport aA) throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.ROUTES WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)");
			_ps.setString(1, aD.getIATA());
			_ps.setString(2, aA.getIATA());
			
			// Execute the Query and load routes
			Collection<RoutePlan> results = execute();
			loadRoutes(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<RoutePlan> execute() throws SQLException {
		List<RoutePlan> results = new ArrayList<RoutePlan>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			RoutePlan rp = new RoutePlan(rs.getInt(1));
			rp.setAuthorID(rs.getInt(2));
			rp.setAirline(SystemData.getAirline(rs.getString(3)));
			rp.setAirportD(SystemData.getAirport(rs.getString(4)));
			rp.setAirportA(SystemData.getAirport(rs.getString(5)));
			rp.setAirportL(SystemData.getAirport(rs.getString(6)));
			rp.setCreatedOn(rs.getTimestamp(7));
			rp.setUseCount(rs.getInt(8));
			rp.setSID(rs.getString(9));
			rp.setSTAR(rs.getString(10));
			rp.setCruiseAltitude(rs.getString(11));
			rp.setComments(rs.getString(12));
			results.add(rp);
		}
		
		// Clean up
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to load route waypoints.
	 */
	private void loadRoutes(Collection<RoutePlan> routes) throws SQLException {
		if (routes.isEmpty())
			return;
		
		StringBuilder buf = new StringBuilder("SELECT * FROM acars.ROUTE_WP WHERE ID IN (");
		for (Iterator<RoutePlan> i = routes.iterator(); i.hasNext(); ) {
			RoutePlan rp = i.next();
			buf.append(String.valueOf(rp.getID()));
			if (i.hasNext())
				buf.append(',');
		}
		
		buf.append(") ORDER BY ID, SEQ");
		prepareStatementWithoutLimits(buf.toString());
		
		// Execute the query
		Map<Integer, RoutePlan> data = CollectionUtils.createMap(routes, "ID");
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			RoutePlan rp = data.get(new Integer(rs.getInt(1)));
			if (rp != null) {
				double lat = rs.getDouble(5);
				double lng = rs.getDouble(6);
				String type = NavigationDataBean.NAVTYPE_NAMES[rs.getInt(4)];
				NavigationDataBean nd = NavigationDataBean.create(type, lat, lng);
				nd.setCode(rs.getString(3));
				rp.addWaypoint(nd, rs.getString(7));
			}
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}
}