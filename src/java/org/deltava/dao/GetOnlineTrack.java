// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load VATSIM data tracks. 
 * @author Luke
 * @version 2.4
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
	 * Fetches track data for a Pilot, for a flight between two airports in the time preceeding a specific date/time. 
	 * @param pilotID the Pilot's database ID
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @param net the OnlineNetwork to use
	 * @param dt the date/time to search before
	 * @param hours the number of hours to search before
	 * @return a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<PositionData> getTrack(int pilotID, OnlineNetwork net, java.util.Date dt, int hours, Airport aD, Airport aA) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM ONLINE_TRACK WHERE (PILOT_ID=?) AND (AIRPORT_D=?) AND "
				+ "(AIRPORT_A=?) AND (NETWORK=?) AND (PIREP_ID=0) AND (DATE > DATE_SUB(?, INTERVAL ? HOUR) "
				+ "ORDER BY DATE");
			_ps.setInt(1, pilotID);
			_ps.setString(2, aD.getIATA());
			_ps.setString(3, aA.getIATA());
			_ps.setString(4, net.toString());
			_ps.setTimestamp(5, createTimestamp(dt));
			_ps.setInt(6, hours);
			return execute();
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
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse position data result sets.
	 */
	public List<PositionData> execute() throws SQLException {
		
		ResultSet rs = _ps.executeQuery();
		List<PositionData> results = new ArrayList<PositionData>();
		while (rs.next()) {
			PositionData pd = new PositionData(OnlineNetwork.valueOf(rs.getString(2)), rs.getTimestamp(4));
			pd.setPilotID(rs.getInt(1));
			pd.setFlightID(rs.getInt(3));
			pd.setAirportD(SystemData.getAirport(rs.getString(5)));
			pd.setAirportA(SystemData.getAirport(rs.getString(6)));
			pd.setPosition(rs.getDouble(7), rs.getDouble(8), rs.getInt(9));
			pd.setHeading(rs.getInt(10));
			pd.setAirSpeed(rs.getInt(11));
			results.add(pd);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}