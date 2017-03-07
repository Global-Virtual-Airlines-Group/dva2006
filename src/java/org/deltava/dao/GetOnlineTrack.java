// Copyright 2009, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.PositionData;

/**
 * A Data Access Object to load VATSIM/IVAO data tracks. This DAO can load from the ONLINE_TRACKS table
 * in each Airline's database, which stores track data already associated with a Flight Report. It can also load "raw" metadta
 * from the online track database which contains information for all Airlines populated from the ServInfo feed by the 
 * {@link org.deltava.tasks.OnlineTrackTask} scheduled task. 
 * @author Luke
 * @version 7.0
 * @since 2.4
 */

public class GetOnlineTrack extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetOnlineTrack(Connection c) {
		super(c);
	}

	/**
	 * Determines whethere there is an existing raw track ID for a particular flight for a particular Pilot. 
	 * @param pilotID the Pilot's database ID
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @param net the OnlineNetwork to use
	 * @param dt the date/time to search before
	 * @return the track ID, or zero if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getTrackID(int pilotID, OnlineNetwork net, java.time.Instant dt, Airport aD, Airport aA) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT OT.ID FROM online.TRACKS OT WHERE (OT.USER_ID=?) AND (OT.AIRPORT_D=?) AND (OT.AIRPORT_A=?) "
					+ "AND (OT.NETWORK=?) AND (OT.CREATED_ON > DATE_SUB(?, INTERVAL ? HOUR)) ORDER BY OT.ID DESC LIMIT 1");
			_ps.setInt(1, pilotID);
			_ps.setString(2, aD.getICAO());
			_ps.setString(3, aA.getICAO());
			_ps.setString(4, net.toString());
			_ps.setTimestamp(5, createTimestamp(dt));
			_ps.setInt(6, 18);
			
			// Execute the query
			int id = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					id = rs.getInt(1);
			}
			
			_ps.close();
			return id;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves the filed route for a particular raw track.
	 * @param trackID the raw track database ID
	 * @return the route, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getRoute(int trackID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ROUTE FROM online.TRACKS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, trackID);
			String route = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					route = rs.getString(1);
			}
			
			_ps.close();
			return route;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Fetches raw track data for a Pilot, for a flight between two airports in the time preceeding a specific date/time. 
	 * @param trackID the track ID
	 * @return a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<PositionData> getRaw(int trackID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT OD.*, OT.USER_ID FROM online.TRACKS OT, online.TRACKDATA OD WHERE "
				+ "(OT.ID=?) AND (OT.ID=OD.ID) ORDER BY OD.REPORT_TIME");
			_ps.setInt(1, trackID);
			
			// Execute the query
			List<PositionData> results = new ArrayList<PositionData>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					PositionData pd = new PositionData(rs.getTimestamp(2).toInstant());
					pd.setFlightID(rs.getInt(1));
					pd.setPosition(rs.getDouble(3), rs.getDouble(4), rs.getInt(5));
					pd.setHeading(rs.getInt(6));
					pd.setAirSpeed(rs.getInt(7));
					pd.setPilotID(rs.getInt(8));
					results.add(pd);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * An optimized method to determine if a Flight Report has online track data.
	 * @param pirepID the Flight Report database ID
	 * @return TRUE if track data exists, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean hasTrack(int pirepID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT PILOT_ID FROM ONLINE_TRACK WHERE (PIREP_ID=?) LIMIT 1");
			_ps.setInt(1, pirepID);
			boolean hasTrack = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					hasTrack = (rs.getInt(1) != 0);
			}
			
			_ps.close();
			return hasTrack;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Fetches the track data for a particular Flight Report.
	 * @param pirepID the Flight Report database ID
	 * @return a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<PositionData> get(int pirepID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM ONLINE_TRACK WHERE (PIREP_ID=?) ORDER BY DATE");
			_ps.setInt(1, pirepID);
			
			// Execute the query
			List<PositionData> results = new ArrayList<PositionData>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					PositionData pd = new PositionData(rs.getTimestamp(3).toInstant());
					pd.setPilotID(rs.getInt(1));
					pd.setFlightID(rs.getInt(2));
					pd.setPosition(rs.getDouble(4), rs.getDouble(5), rs.getInt(6));
					pd.setHeading(rs.getInt(7));
					pd.setAirSpeed(rs.getInt(8));
					results.add(pd);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}