// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A Data Access Object to load ACARS route information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetACARSRoute extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSRoute(Connection c) {
		super(c);
	}

	/**
	 * Loads route position data for a particular ACARS flight ID.
	 * @param flightID the ACARS flight ID
	 * @return a List of GeoPosition beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getRoutePositions(int flightID) throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT LAT, LNG FROM acars.POSITIONS WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
			_ps.setInt(1, flightID);
			
			// Execute the query
			List results = new ArrayList();
			ResultSet rs = _ps.executeQuery();
			
			// Iterate through the result set
			while (rs.next())
				results.add(new GeoPosition(rs.getDouble(1), rs.getDouble(2)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads complete route data for a particular ACARS flight ID.
	 * @param flightID the ACARS flight ID
	 * @return a List of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getRouteEntries(int flightID) throws DAOException {
	   try {
	      prepareStatement("SELECT REPORT_TIME, LAT, LNG, B_ALT, HEADING, ASPEED, GSPEED, VSPEED, N1, "
	            + "N2, FLAPS, FLAGS FROM acars.POSITIONS WHERE (FLIGHT_ID=?) ORDER BY REPORT_TIME");
	      _ps.setInt(1, flightID);
	      
			// Execute the query
			List results = new ArrayList();
			ResultSet rs = _ps.executeQuery();
			
			// Iterate through the result set
			while (rs.next()) {
			   RouteEntry entry = new RouteEntry(rs.getTimestamp(1), rs.getDouble(2), rs.getDouble(3));
			   entry.setAltitude(rs.getInt(4));
			   entry.setHeading(rs.getInt(5));
			   entry.setAirSpeed(rs.getInt(6));
			   entry.setGroundSpeed(rs.getInt(7));
			   entry.setVerticalSpeed(rs.getInt(8));
			   entry.setN1(rs.getDouble(9));
			   entry.setN2(rs.getDouble(10));
			   entry.setFlaps(rs.getInt(11));
			   entry.setFlags(rs.getInt(12));
			   
			   // Add to results
			   results.add(entry);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
}