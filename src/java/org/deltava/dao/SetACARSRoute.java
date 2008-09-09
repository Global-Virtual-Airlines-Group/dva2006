// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.RoutePlan;
import org.deltava.beans.navdata.NavigationDataBean;

/**
 * A Data Access Object to write ACARS Dispatcher routes.
 * @author Luke
 * @version 2.2
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
	public void write(RoutePlan rp) throws DAOException {
		try {
			startTransaction();
			
			// Create the route
			prepareStatementWithoutLimits("INSERT INTO acars.ROUTES (AUTHOR, AIRLINE, AIRPORT_D, "
					+ "AIRPORT_A, AIRPORT_L, CREATEDON, USED, ALTITUDE, SID, STAR, BUILD, REMARKS, "
					+ "ROUTE) VALUES (?, ?, ?, ?, ?, NOW(), 1, ?, ?, ?, 0, ?, ?)");
			_ps.setInt(1, rp.getAuthorID());
			_ps.setString(2, rp.getAirline().getCode());
			_ps.setString(3, rp.getAirportD().getIATA());
			_ps.setString(4, rp.getAirportA().getIATA());
			_ps.setString(5, (rp.getAirportL() == null) ? null : rp.getAirportL().getIATA());
			_ps.setString(6, rp.getCruiseAltitude());
			_ps.setString(7, rp.getSID());
			_ps.setString(8, rp.getSTAR());
			_ps.setString(9, rp.getComments());
			_ps.setString(10, rp.getRoute());
			
			// Save the data
			_ps.executeUpdate();
			if (rp.getID() == 0)
				rp.setID(getNewID());
			
			// Save the waypoints
			int seq = -1;
			prepareStatementWithoutLimits("INSERT INTO acars.ROUTE_WP (ID, SEQ, CODE, ITEMTYPE, LATITUDE, "
					+ "LONGITUDE, AIRWAY, REGION) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, rp.getID());
			for (Iterator<NavigationDataBean> i = rp.getWaypoints().iterator(); i.hasNext(); ) {
				NavigationDataBean nd = i.next();
				_ps.setInt(2, ++seq);
				_ps.setString(3, nd.getCode());
				_ps.setInt(4, nd.getType());
				_ps.setDouble(5, nd.getLatitude());
				_ps.setDouble(6, nd.getLongitude());
				_ps.setString(7, nd.getAirway());
				_ps.setString(8, nd.getRegion());
				_ps.addBatch();
			}

			// Write and commit
			_ps.executeBatch();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a saved route from the database.
	 * @param id the route ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM acars.ROUTES WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}