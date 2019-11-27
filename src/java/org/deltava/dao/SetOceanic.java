// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to write Oceanic Routes.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetOceanic extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetOceanic(Connection c) {
		super(c);
	}

	/**
	 * Purges the Oceanic Routes table.
	 * @param sd the start date for the purge operation; purge all records before this date
	 * @return the number of routes deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeOceanic(java.time.Instant sd) throws DAOException {
		try {
			if (sd != null) {
				try (PreparedStatement ps = prepare("DELETE FROM common.OCEANIC WHERE (VAILID_DATE<?)")) {
					ps.setTimestamp(1, createTimestamp(sd));
					return executeUpdate(ps, 0);
				}
			}

			try (PreparedStatement ps = prepare("DELETE FROM common.OCEANIC")) {
				return executeUpdate(ps, 0);
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an entry from the Oceanic Routes table.
	 * @param routeType the route type
	 * @param vd the validity date of the route
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteOceanic(OceanicTrackInfo.Type routeType, java.time.Instant vd) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.OCEANIC WHERE (ROUTETYPE=?) AND (VALID_DATE=?)")) {
			ps.setInt(1, routeType.ordinal());
			ps.setTimestamp(2, createTimestamp(vd));
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.OCEANIC (ROUTETYPE, VALID_DATE, SOURCE, ROUTE) VALUES (?, ?, ?, ?)")) {
			ps.setInt(1, or.getType().ordinal());
			ps.setTimestamp(2, createTimestamp(or.getDate()));
			ps.setString(3, or.getSource());
			ps.setString(4, or.getRoute());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Wirtes an Oceanic route into the database.
	 * @param ow the OceanicTrack bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(OceanicTrack ow) throws DAOException {
		try {
			startTransaction();
			
			// Clean out the route
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) AND (VALID_DATE=DATE(?)) AND (TRACK=?)")) {
				ps.setInt(1, ow.getType().ordinal());
				ps.setTimestamp(2, createTimestamp(ow.getDate()));
				ps.setString(3, ow.getTrack());
				executeUpdate(ps, 0);
			}
			
			// Write the route
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.OCEANIC_ROUTES (ROUTETYPE, VALID_DATE, TRACK, SEQ, WAYPOINT, LATITUDE, LONGITUDE) VALUES (?, ?, ?, ?, ?, ? ,?)")) {
				ps.setInt(1, ow.getType().ordinal());
				ps.setTimestamp(2, createTimestamp(ow.getDate()));
				ps.setString(3, ow.getTrack());
			
				// Write the waypoints
				int seq = 0;
				for (NavigationDataBean wp : ow.getWaypoints()) {
					if (wp.getCode().length() > 15)
						wp.setCode(wp.getCode().substring(0, 16));
				
					ps.setInt(4, ++seq);
					ps.setString(5, wp.getCode());
					ps.setDouble(6, wp.getLatitude());
					ps.setDouble(7, wp.getLongitude());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, ow.getWaypoints().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}