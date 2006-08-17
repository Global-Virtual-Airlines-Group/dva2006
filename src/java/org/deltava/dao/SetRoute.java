// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Date;

import org.deltava.beans.schedule.OceanicRoute;
import org.deltava.beans.schedule.PreferredRoute;

/**
 * A Data Access Object to write Preferred Domestic/Oceanic Routes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetRoute extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetRoute(Connection c) {
		super(c);
	}

	/**
	 * Purges the Domestic Preferred Routes table.
	 * @return the number of routes deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeDomestic() throws DAOException {
		try {
			prepareStatement("DELETE FROM ROUTES");
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges the Oceanic Routes table.
	 * @param sd the start date for the purge operation; purge all records before this date
	 * @return the number of routes deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeOceanic(Date sd) throws DAOException {
		try {
			// Init the prepared statement
			if (sd == null) {
				prepareStatement("DELETE FROM common.OCEANIC");
			} else {
				prepareStatement("DELETE FROM common.OCEANIC WHERE (VAILID_DATE < ?)");
				_ps.setTimestamp(1, createTimestamp(sd));
			}

			// Purge the table
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an entry from the Oceanic Routes table.
	 * @param routeType the route type code
	 * @param vd the validity date of the route
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteOceanic(int routeType, Date vd) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("DELETE FROM common.OCEANIC WHERE (ROUTETYPE=?) AND (VALID_DATE=?)");
			_ps.setInt(1, routeType);
			_ps.setTimestamp(2, createTimestamp(vd));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a domestic Preferred Route into the database.
	 * @param pr the PreferredRoute bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(PreferredRoute pr) throws DAOException {
		try {
			prepareStatement("INSERT INTO ROUTES (AIRPORT_D, AIRPORT_A, ARTCC, ROUTE) VALUES (?, ?, ?, ?)");
			_ps.setString(1, pr.getAirportD().getIATA());
			_ps.setString(2, pr.getAirportA().getIATA());
			_ps.setString(3, pr.getARTCC());
			_ps.setString(4, pr.getRoute());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an Oceanic Route into the database.
	 * @param or the OceanicRoute bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(OceanicRoute or) throws DAOException {
		try {
			prepareStatement("REPLACE INTO common.OCEANIC (ROUTETYPE, VALID_DATE, SOURCE, ROUTE) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, or.getType());
			_ps.setTimestamp(2, createTimestamp(or.getDate()));
			_ps.setString(3, or.getSource());
			_ps.setString(4, or.getRoute());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}