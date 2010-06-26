// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.PositionData;

/**
 * A Data Access Object to write VATSIM/IVAO Online tracks. This DAO, like the
 * {@link GetOnlineTrack} DAO, can write both "raw" track data obtained from the ServInfo
 * feed to a common table shared between all Airlines, as well as to a local table that links the
 * raw data to a specific Flight Report. 
 * @author Luke
 * @version 3.1
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
			prepareStatementWithoutLimits("INSERT INTO online.TRACKS (USER_ID, NETWORK, CREATED_ON, AIRPORT_D, "
				+ "AIRPORT_A, ROUTE) VALUES (?, ?, NOW(), ?, ?, ?)");
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
		try {
			prepareStatementWithoutLimits("REPLACE INTO online.TRACKDATA (ID, REPORT_TIME, LAT, LNG, ALT, HDG, SPEED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)");
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
	 * Purges raw data from the database.
	 * @param rawFlightID the flight ID of the raw flight data
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(int rawFlightID) throws DAOException {
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
	 * Writes position records to the database and links them to a Flight Report.
	 * @param pirepID the Flight Report database ID
	 * @param pds a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(int pirepID, Collection<PositionData> pds) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO ONLINE_TRACK (PILOT_ID, PIREP_ID, DATE, "
				+ "LAT, LNG, ALT, HEADING, ASPEED) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(2, pirepID);
			for (Iterator<PositionData> i = pds.iterator(); i.hasNext(); ) {
				PositionData pd = i.next();
				_ps.setInt(1, pd.getPilotID());
				_ps.setTimestamp(3, createTimestamp(pd.getDate()));
				_ps.setDouble(4, pd.getLatitude());
				_ps.setDouble(5, pd.getLongitude());
				_ps.setInt(6, pd.getAltitude());
				_ps.setInt(7, pd.getHeading());
				_ps.setInt(8, pd.getAirSpeed());
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}