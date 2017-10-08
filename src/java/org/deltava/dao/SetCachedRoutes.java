// Copyright 2009, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to saved cached flight routes to the database.
 * @author Luke
 * @version 8.0
 * @since 2.6
 */

public class SetCachedRoutes extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetCachedRoutes(Connection c) {
		super(c);
	}

	/**
	 * Writes a number of Flight routes to the database. 
	 * @param routes
	 * @throws DAOException
	 */
	public void write(Collection<? extends FlightRoute> routes) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO common.ROUTE_CACHE (AIRPORT_D, AIRPORT_A, CREATED, ALTITUDE, SOURCE, COMMENTS, ROUTE) VALUES (?, ?, ?, ?, ?, ?, ?)");
			for (FlightRoute rt : routes) {
				_ps.setString(1, rt.getAirportD().getICAO());
				_ps.setString(2, rt.getAirportA().getICAO());
				_ps.setTimestamp(3, createTimestamp(rt.getCreatedOn()));
				_ps.setString(4, rt.getCruiseAltitude());
				_ps.setString(5, ((ExternalFlightRoute) rt).getSource());
				_ps.setString(6, rt.getComments());
				_ps.setString(7, rt.getFullRoute());
				_ps.addBatch();
			}

			executeBatchUpdate(1, routes.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges all saved routes between two airports from the database.
	 * @param rp the RoutePair
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(RoutePair rp) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.ROUTE_CACHE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)");
			_ps.setString(1, rp.getAirportD().getICAO());
			_ps.setString(2, rp.getAirportA().getICAO());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges routes older than a certain date.
	 * @param days the number of days old to purge
	 * @return the number of routes purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge(int days) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.ROUTE_CACHE WHERE (CREATED < DATE_SUB(NOW(), INTERVAL ? DAY))");
			_ps.setInt(1, days);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}