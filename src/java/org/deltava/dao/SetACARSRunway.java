// Copyright 2012, 2017, 2018, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.*;

/**
 * A Data Access Object to write ACARS Runway and Gate data.
 * @author Luke
 * @version 10.5
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
		try (PreparedStatement ps = prepare("REPLACE INTO acars.RWYDATA (ID, ICAO, RUNWAY, LATITUDE, LONGITUDE, LENGTH, DISTANCE, ISTAKEOFF) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, flightID);
			if (rwyD != null) {
				int dist = (rwyD instanceof RunwayDistance) ? ((RunwayDistance) rwyD).getDistance() : 0;
				ps.setString(2, rwyD.getCode());
				ps.setString(3, rwyD.getName());
				ps.setDouble(4, rwyD.getLatitude());
				ps.setDouble(5, rwyD.getLongitude());
				ps.setInt(6, rwyD.getLength());
				ps.setInt(7, dist);
				ps.setBoolean(8, true);
				if (Math.abs(dist) < 32500)
					ps.addBatch();
			}
			
			if (rwyA != null) {
				int dist = (rwyA instanceof RunwayDistance) ? ((RunwayDistance) rwyA).getDistance() : 0;
				ps.setString(2, rwyA.getCode());
				ps.setString(3, rwyA.getName());
				ps.setDouble(4, rwyA.getLatitude());
				ps.setDouble(5, rwyA.getLongitude());
				ps.setInt(6, rwyA.getLength());
				ps.setInt(7, dist);
				ps.setBoolean(8, false);
				if (Math.abs(dist) < 32500)
					ps.addBatch();
			}
			
			executeUpdate(ps, 1, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes the gates used on a Flight to the database. 
	 * @param inf the FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeGates(FlightInfo inf) throws DAOException {
		if ((inf.getGateD() == null) && (inf.getGateA() == null)) return;
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.GATEDATA (ID, ICAO, GATE, LATITUDE, LONGITUDE, LL, ISDEPARTURE) VALUES (?, ?, ?, ?, ?, ST_PointFromText(?, ?), ?)")) {
			ps.setInt(1, inf.getID());
			if (inf.getGateD() != null) {
				ps.setString(2, inf.getGateD().getCode());
				ps.setString(3, inf.getGateD().getName());
				ps.setDouble(4, inf.getGateD().getLatitude());
				ps.setDouble(5, inf.getGateD().getLongitude());
				ps.setString(6, formatLocation(inf.getGateD()));
				ps.setInt(7, WGS84_SRID);
				ps.setBoolean(8, true);
				ps.addBatch();
			}
			
			if (inf.getGateA() != null) {
				ps.setString(2, inf.getGateA().getCode());
				ps.setString(3, inf.getGateA().getName());
				ps.setDouble(4, inf.getGateA().getLatitude());
				ps.setDouble(5, inf.getGateA().getLongitude());
				ps.setString(6, formatLocation(inf.getGateA()));
				ps.setInt(7, WGS84_SRID);
				ps.setBoolean(8, false);
				ps.addBatch();
			}
			
			executeUpdate(ps, 1, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}