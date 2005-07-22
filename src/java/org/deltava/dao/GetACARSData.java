// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load ACARS information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetACARSData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSData(Connection c) {
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
			prepareStatement("SELECT DISTINCT LAT, LNG FROM acars.POSITIONS WHERE (FLIGHT_ID=?) "
			      + "ORDER BY REPORT_TIME");
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
	 * @param includeOnGround TRUE if entries on the ground are RouteEntry beans, otherwise FALSE
	 * @return a List of RouteEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getRouteEntries(int flightID, boolean includeOnGround) throws DAOException {
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

				// Add to results - or just log a GeoPosition if we're on the ground
				if (entry.isFlagSet(ACARSFlags.FLAG_ONGROUND) && (!includeOnGround)) {
					results.add(new GeoPosition(entry));
				} else {
					results.add(entry);
				}
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the filed route for a particular ACARS flight.
	 * @param flightID the ACARS flight ID
	 * @return the filed route
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getRoute(int flightID) throws DAOException {
		try {
			prepareStatement("SELECT ROUTE from acars.FLIGHTS WHERE (ID=?)");
			_ps.setInt(1, flightID);
			setQueryMax(1);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			String result = (rs.next()) ? rs.getString(1) : null;

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns information about a particular ACARS flight.
	 * @param flightID the ACARS flight ID
	 * @return the Flight Information, nor null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightInfo getInfo(int flightID) throws DAOException {
	   try {
	      prepareStatement("SELECT * FROM acars.FLIGHTS WHERE (ID=?)");
	      _ps.setInt(1, flightID);
	      setQueryMax(1);
	      
	      // Get the first entry, or null
	      List results = executeFlightInfo();
	      return results.isEmpty() ? null : (FlightInfo) results.get(0);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Returns information about a particular ACARS connection. 
	 * @param conID the ACARS connection ID
	 * @return the Connection information, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ConnectionEntry getConnection(long conID) throws DAOException {
	   try {
	      prepareStatement("SELECT ID, PILOT_ID, DATE, INET_NTOA(REMOTE_ADDR), REMOTE_HOST FROM "
	      		+ "acars.CONS WHERE (ID=?)");
	      _ps.setLong(1, conID);
	      setQueryMax(1);
	      
	      // Get the first entry, or null
	      List results = executeConnectionInfo();
	      return results.isEmpty() ? null : (ConnectionEntry) results.get(0);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Helper method to parse Flight Info result sets.
	 */
	protected List executeFlightInfo() throws SQLException {
	   
	   // Execute the query
	   ResultSet rs = _ps.executeQuery();
	   
	   // Iterate through the results
	   List results = new ArrayList();
	   while (rs.next()) {
	      FlightInfo info = new FlightInfo(rs.getInt(1), rs.getLong(2));
	      info.setStartTime(rs.getTimestamp(3));
	      info.setEndTime(rs.getTimestamp(4));
	      info.setFlightCode(rs.getString(5));
	      info.setEquipmentType(rs.getString(6));
	      info.setAltitude(rs.getString(7));
	      info.setAirportD(SystemData.getAirport(rs.getString(8)));
	      info.setAirportA(SystemData.getAirport(rs.getString(9)));
	      info.setRoute(rs.getString(11));
	      info.setRemarks(rs.getString(12));
	      info.setFSVersion(rs.getInt(13));
	      
	      // Add to results
	      results.add(info);
	   }
	   
	   // Clean up and return
	   rs.close();
	   _ps.close();
	   return results;
	}
	
	/**
	 * Helper method to parse Connection result sets.
	 */
	protected List executeConnectionInfo() throws SQLException {

	   // Execute the query
	   ResultSet rs = _ps.executeQuery();
	   
	   // Iterate through the results
	   List results = new ArrayList();
	   while (rs.next()) {
	      ConnectionEntry entry = new ConnectionEntry(rs.getLong(1));
	      entry.setPilotID(rs.getInt(2));
	      entry.setDate(rs.getTimestamp(3));
	      entry.setRemoteAddr(rs.getString(4));
	      entry.setRemoteHost(rs.getString(5));
	      
	      // Add to results
	      results.add(entry);
	   }
	   
	   // Clean up and return
	   rs.close();
	   _ps.close();
	   return results;
	}
}