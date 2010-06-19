// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.servinfo.PositionData;

/**
 * A Data Access Object to write VATSIM/IVAO Online tracks.
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
	 * Writes position records to the database and links them to a Flight Report.
	 * @param pirepID the Flight Report database ID
	 * @param pds a Collection of PositionData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(int pirepID, Collection<PositionData> pds) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO ONLINE_TRACK (PILOT_ID, PIREP_ID, DATE, "
				+ "NETWORK, AIRPORT_D, AIRPORT_A, LAT, LNG, ALT, HEADING, ASPEED) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(2, pirepID);
			for (Iterator<PositionData> i = pds.iterator(); i.hasNext(); ) {
				PositionData pd = i.next();
				_ps.setInt(1, pd.getPilotID());
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
}