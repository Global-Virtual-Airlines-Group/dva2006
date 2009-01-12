// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.PositionData;

/**
 * A Data Access Object to write VATSIM/IVAO Online tracks.
 * @author Luke
 * @version 2.4
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
	 * Purges all entries older than a specific number hours that have not been associated
	 * with a particular Flight Report.
	 * @param hours the number of hours
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(int hours) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM ONLINE_TRACK WHERE (PIREP_ID=0) AND "
				+ "(DATE < DATE_SUB(NOW(), INTERVAL ? HOUR))");
			_ps.setInt(1, hours);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes position records to the database.
	 * @param pds a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Collection<PositionData> pds) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO ONLINE_TRACK (PILOT_ID, PIREP_ID, DATE, "
				+ "NETWORK, AIRPORT_D, AIRPORT_A, LAT, LNG, ALT, HEADING, ASPEED) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			for (Iterator<PositionData> i = pds.iterator(); i.hasNext(); ) {
				PositionData pd = i.next();
				_ps.setInt(1, pd.getPilotID());
				_ps.setInt(2, pd.getFlightID());
				_ps.setTimestamp(3, createTimestamp(pd.getDate()));
				_ps.setString(4, pd.getNetwork().toString());
				_ps.setString(5, pd.getAirportD().getIATA());
				_ps.setString(6, pd.getAirportA().getIATA());
				_ps.setDouble(7, pd.getLatitude());
				_ps.setDouble(8, pd.getLongitude());
				_ps.setInt(9, pd.getAltitude());
				_ps.setInt(10, pd.getHeading());
				_ps.setInt(11, pd.getAirSpeed());
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Assigns position entries by a Pilot between a particular airport pair within the previous time period
	 * to a specific Flight Report. 
	 * @param pirepID the Flight Report database ID
	 * @param dt the end date/time
	 * @param hours the number of hours prior to search
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @throws DAOException if a JDBC error occurs
	 */
	public void assign(int pirepID, OnlineNetwork net, java.util.Date dt, int hours, Airport aD, Airport aA) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE ONLINE_TRACK SET PIREP_ID=? WHERE (PIREP_ID=0) AND "
				+ "(AIRPORT_D=?) AND (AIRPORT_A=?) AND (NETWORK=?) AND (DATE > DATE_SUB(?, INTERVAL ? HOUR)");
			_ps.setInt(1, pirepID);
			_ps.setString(2, aD.getIATA());
			_ps.setString(3, aA.getIATA());
			_ps.setString(4, net.toString());
			_ps.setTimestamp(5, createTimestamp(dt));
			_ps.setInt(6, hours);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}