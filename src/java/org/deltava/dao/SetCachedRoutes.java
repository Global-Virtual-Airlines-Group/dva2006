// Copyright 2009, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to saved cached flight routes to the database.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.ROUTE_CACHE (AIRPORT_D, AIRPORT_A, CREATED, ALTITUDE, SOURCE, COMMENTS, ROUTE) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			for (FlightRoute rt : routes) {
				ps.setString(1, rt.getAirportD().getICAO());
				ps.setString(2, rt.getAirportA().getICAO());
				ps.setTimestamp(3, createTimestamp(rt.getCreatedOn()));
				ps.setString(4, rt.getCruiseAltitude());
				ps.setString(5, ((ExternalFlightRoute) rt).getSource());
				ps.setString(6, rt.getComments());
				ps.setString(7, rt.getFullRoute());
				ps.addBatch();
			}

			executeUpdate(ps, 1, routes.size());
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.ROUTE_CACHE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)")) {
			ps.setString(1, rp.getAirportD().getICAO());
			ps.setString(2, rp.getAirportA().getICAO());
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.ROUTE_CACHE WHERE (CREATED < DATE_SUB(NOW(), INTERVAL ? DAY))")) {
			ps.setInt(1, days);
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}