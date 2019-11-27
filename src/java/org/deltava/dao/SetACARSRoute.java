// Copyright 2008, 2009, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.DispatchRoute;
import org.deltava.beans.navdata.NavigationDataBean;

/**
 * A Data Access Object to write ACARS Dispatcher routes.
 * @author Luke
 * @version 9.0
 * @since 2.2
 */

public class SetACARSRoute extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSRoute(Connection c) {
		super(c);
	}
	
	/**
	 * Creates a new ACARS dispatcher route.
	 * @param rp the RoutePlan bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(DispatchRoute rp) throws DAOException {
		try {
			startTransaction();
			
			// Prepare the statement
			try (PreparedStatement ps = prepare("INSERT INTO acars.ROUTES (AUTHOR, AIRLINE, AIRPORT_D, AIRPORT_A, AIRPORT_L, ALTITUDE, SID, STAR, BUILD, REMARKS, ROUTE, USED, CREATEDON, ID) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0, NOW(), ?) ON DUPLICATE KEY UPDATE AUTHOR=VALUES(AUTHOR), AIRLINE=VALUES(AIRLINE), AIRPORT_D=VALUES(AIRPORT_D), AIRPORT_A=VALUES(AIRPORT_A), "
				+ "ALTITUDE=VALUES(ALTITUDE), SID=VALUES(SID), STAR=VALUES(STAR), REMARKS=VALUES(REMARKS), ROUTE=VALUES(ROUTE)")) {
			
				// Write the route
				ps.setInt(1, rp.getAuthorID());
				ps.setString(2, rp.getAirline().getCode());
				ps.setString(3, rp.getAirportD().getIATA());
				ps.setString(4, rp.getAirportA().getIATA());
				ps.setString(5, (rp.getAirportL() == null) ? null : rp.getAirportL().getIATA());
				ps.setString(6, rp.getCruiseAltitude());
				ps.setString(7, rp.getSID());
				ps.setString(8, rp.getSTAR());
				ps.setString(9, rp.getComments());
				ps.setString(10, rp.getRoute());
				ps.setInt(11, rp.getID());
				executeUpdate(ps, 1);
			}
			
			// Clear the waypoints if necessary
			if (rp.getID() != 0) {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM acars.ROUTE_WP WHERE (ID=?)")) {
					ps.setInt(1, rp.getID());
					executeUpdate(ps, 0);
				}
			} else
				rp.setID(getNewID());
			
			// Save the waypoints
			int seq = -1;
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO acars.ROUTE_WP (ID, SEQ, CODE, ITEMTYPE, LATITUDE, LONGITUDE, AIRWAY, REGION) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, rp.getID());
				for (Iterator<NavigationDataBean> i = rp.getWaypoints().iterator(); i.hasNext(); ) {
					NavigationDataBean nd = i.next();
					ps.setInt(2, ++seq);
					ps.setString(3, nd.getCode());
					ps.setInt(4, nd.getType().ordinal());
					ps.setDouble(5, nd.getLatitude());
					ps.setDouble(6, nd.getLongitude());
					ps.setString(7, nd.isInTerminalRoute() ? null : nd.getAirway());
					ps.setString(8, nd.getRegion());
					ps.addBatch();
				}
				
				executeUpdate(ps, 1, rp.getWaypoints().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a saved route as active or inactive.
	 * @param id the route ID
	 * @param isActive TRUE if active, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void activate(int id, boolean isActive) throws DAOException {
		try (PreparedStatement ps = prepare("UPDATE acars.ROUTES SET ACTIVE=? WHERE (ID=?)")) {
			ps.setBoolean(1, isActive);
			ps.setInt(2, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a saved route from the database.
	 * @param id the route ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM acars.ROUTES WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}