// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to write Preferred Domestic/Oceanic Routes.
 * @author Luke
 * @version 2.2
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
	 * Purges the Oceanic Routes table.
	 * @param sd the start date for the purge operation; purge all records before this date
	 * @return the number of routes deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeOceanic(java.util.Date sd) throws DAOException {
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
	public void deleteOceanic(int routeType, java.util.Date vd) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.OCEANIC WHERE (ROUTETYPE=?) AND (VALID_DATE=?)");
			_ps.setInt(1, routeType);
			_ps.setTimestamp(2, createTimestamp(vd));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an Oceanic Route NOTAM into the database.
	 * @param or the OceanicNOTAM bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(OceanicNOTAM or) throws DAOException {
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
	
	/**
	 * Wirtes an Oceanic route into the database.
	 * @param ow the OceanicWaypoints bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(OceanicWaypoints ow) throws DAOException {
		try {
			startTransaction();
			
			// Clean out the route
			prepareStatement("DELETE FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) AND (VALID_DATE=?) AND "
					+ "(TRACK=?)");
			_ps.setInt(1, ow.getType());
			_ps.setTimestamp(2, createTimestamp(ow.getDate()));
			_ps.setString(3, ow.getTrack());
			executeUpdate(0);
			
			// Write the route
			prepareStatement("INSERT INTO common.OCEANIC_ROUTES (ROUTETYPE, VALID_DATE, TRACK, SEQ, WAYPOINT, "
					+ "LATITUDE, LONGITUDE) VALUES (?, ?, ?, ?, ?, ? ,?)");
			_ps.setInt(1, ow.getType());
			_ps.setTimestamp(2, createTimestamp(ow.getDate()));
			_ps.setString(3, ow.getTrack());
			
			// Write the waypoints
			int seq = 0;
			for (Iterator<NavigationDataBean> i = ow.getWaypoints().iterator(); i.hasNext(); ) {
				NavigationDataBean wp = i.next();
				_ps.setInt(4, ++seq);
				_ps.setString(5, wp.getCode());
				_ps.setDouble(6, wp.getLatitude());
				_ps.setDouble(7, wp.getLongitude());
				_ps.addBatch();
			}
			
			// Execute and clean up
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}