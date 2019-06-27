// Copyright 2009, 2010, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.PositionData;

/**
 * A Data Access Object to write VATSIM/IVAO/PilotEdge Online tracks.
 * @author Luke
 * @version 8.6
 * @since 2.4
 */

public class SetOnlineTrack extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetOnlineTrack(Connection c) {
		super(c);
	}
	
	/**
	 * Writes a new Online Track record to the shared database.
	 * @param userID the Pilot's database ID
	 * @param net the OnlineNetwork used
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @param route the filed route
	 * @return the newly written track ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public int writeTrack(int userID, OnlineNetwork net, Airport aD, Airport aA, String route) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO online.TRACKS (USER_ID, NETWORK, CREATED_ON, AIRPORT_D, AIRPORT_A, ROUTE) VALUES (?, ?, NOW(), ?, ?, ?)");
			_ps.setInt(1, userID);
			_ps.setString(2, net.toString());
			_ps.setString(3, aD.getICAO());
			_ps.setString(4, aA.getICAO());
			_ps.setString(5, route);
			executeUpdate(1);
			return getNewID();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Position record to the shared database.
	 * @param pd a PositionData bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writePosition(PositionData pd) throws DAOException {
		if ((pd.getLatitude() == 0) && (pd.getLongitude() == 0) && (pd.getAltitude() == 0))
			return;
		
		try {
			prepareStatementWithoutLimits("REPLACE INTO online.TRACKDATA (ID, REPORT_TIME, LAT, LNG, ALT, HDG, SPEED) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, pd.getFlightID());
			_ps.setTimestamp(2, createTimestamp(pd.getDate()));
			_ps.setDouble(3, pd.getLatitude());
			_ps.setDouble(4, pd.getLongitude());
			_ps.setInt(5, pd.getAltitude());
			_ps.setInt(6, pd.getHeading());
			_ps.setInt(7, pd.getAirSpeed());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Records a ServInfo info pull.
	 * @param net the OnlineNetwork
	 * @param fetchTime the fetch date/time in UTC
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writePull(OnlineNetwork net, Instant fetchTime) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO online.TRACK_PULLS (NETWORK, PULLTIME) VALUES (?, ?)");
			_ps.setString(1, net.toString());
			_ps.setTimestamp(2, createTimestamp(fetchTime));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges raw data from the database.
	 * @param rawFlightID the flight ID of the raw flight data
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeRaw(int rawFlightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM online.TRACKS WHERE (ID=?)");
			_ps.setInt(1, rawFlightID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges all raw flight records started prior to a particular time. 
	 * @param hours the number of hours ago
	 * @return the number of flights purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeAll(int hours) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM online.TRACKS WHERE (CREATED_ON < DATE_SUB(NOW(), INTERVAL ? HOUR))");
			_ps.setInt(1, hours);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges linked data from the database.
	 * @param pirepID the flight report ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(int pirepID) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM ONLINE_TRACK WHERE (PIREP_ID=?)");
			_ps.setInt(1, pirepID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes position records to the database and links them to a Flight Report.
	 * @param pirepID the Flight Report database ID
	 * @param pds a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(int pirepID, Collection<PositionData> pds) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO ONLINE_TRACK (PILOT_ID, PIREP_ID, DATE, LAT, LNG, ALT, HEADING, ASPEED) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(2, pirepID);
			for (PositionData pd : pds) {
				_ps.setInt(1, pd.getPilotID());
				_ps.setTimestamp(3, createTimestamp(pd.getDate()));
				_ps.setDouble(4, pd.getLatitude());
				_ps.setDouble(5, pd.getLongitude());
				_ps.setInt(6, pd.getAltitude());
				_ps.setInt(7, pd.getHeading());
				_ps.setInt(8, pd.getAirSpeed());
				_ps.addBatch();
			}

			executeBatchUpdate(1, pds.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}