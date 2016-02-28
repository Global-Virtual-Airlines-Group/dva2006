// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.navdata.*;

/**
 * A Data Access Object to write ACARS Runway and Gate data.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class SetACARSRunway extends SetACARSData {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSRunway(Connection c) {
		super(c);
	}
	
	/**
	 * Writes the runways used on a Flight to the database.
	 * @param flightID the ACARS Flight ID
	 * @param rwyD the departure Runway
	 * @param rwyA the arrival Runway
	 * @throws DAOException if a JDBC error occured
	 */
	public void writeRunways(int flightID, Runway rwyD, Runway rwyA) throws DAOException {
		try {
			prepareStatement("REPLACE INTO acars.RWYDATA (ID, ICAO, RUNWAY, LATITUDE, LONGITUDE, LENGTH, DISTANCE, ISTAKEOFF) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, flightID);
			if (rwyD != null) {
				int dist = (rwyD instanceof RunwayDistance) ? ((RunwayDistance) rwyD).getDistance() : 0;
				_ps.setString(2, rwyD.getCode());
				_ps.setString(3, rwyD.getName());
				_ps.setDouble(4, rwyD.getLatitude());
				_ps.setDouble(5, rwyD.getLongitude());
				_ps.setInt(6, rwyD.getLength());
				_ps.setInt(7, dist);
				_ps.setBoolean(8, true);
				if (dist < 65200)
					_ps.addBatch();
			}
			
			if (rwyA != null) {
				int dist = (rwyA instanceof RunwayDistance) ? ((RunwayDistance) rwyA).getDistance() : 0;
				_ps.setString(2, rwyA.getCode());
				_ps.setString(3, rwyA.getName());
				_ps.setDouble(4, rwyA.getLatitude());
				_ps.setDouble(5, rwyA.getLongitude());
				_ps.setInt(6, rwyA.getLength());
				_ps.setInt(7, dist);
				_ps.setBoolean(8, false);
				if (dist < 65200)
					_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes the gates used on a Flight to the database. 
	 * @param flightID the ACARS Flight ID
	 * @param gD the departure Gate, or null
	 * @param gA the arrival Gate, or null
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeGates(int flightID, Gate gD, Gate gA) throws DAOException {
		if ((gD == null) && (gA == null)) return;
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.GATEDATA (ID, ICAO, GATE, ISDEPARTURE) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, flightID);
			if (gD != null) {
				_ps.setString(2, gD.getCode());
				_ps.setString(3, gD.getName());
				_ps.setBoolean(4, true);
				_ps.addBatch();
			}
			
			if (gA != null) {
				_ps.setString(2, gA.getCode());
				_ps.setString(3, gA.getName());
				_ps.setBoolean(4, false);
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}