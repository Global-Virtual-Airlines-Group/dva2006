// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object used to get information for purging the ACARS data tables. 
 * @author Luke
 * @version 9.0
 * @since 3.2
 */

public class GetACARSPurge extends GetACARSData {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSPurge(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all Flight Information entries without an associated Flight Report. A cutoff interval is provided to
	 * prevent the accidental inclusion of flights still in progress.
	 * @param cutoff the cutoff interval for flight entries, in hours
	 * @return a List of InfoEntry beans sorted by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightInfo> getUnreportedFlights(int cutoff) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT F.*, FD.ROUTE_ID, FDR.DISPATCHER_ID, C.PILOT_ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) LEFT JOIN acars.FLIGHT_DISPATCHER FDR "
			+ "ON (F.ID=FDR.ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE (F.PIREP=?) AND (F.ARCHIVED=?) AND (F.CREATED < DATE_SUB(NOW(), INTERVAL ? HOUR)) ORDER BY F.CREATED")) {
			ps.setBoolean(1, false);
			ps.setBoolean(2, false);
			ps.setInt(3, cutoff);
			return executeFlightInfo(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves Flight IDs for position entries older than a particular number of hours.
	 * @param hours the number of hours
	 * @return a Collection of ACARS Flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPositionFlightIDs(int hours) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT FLIGHT_ID FROM acars.POSITIONS WHERE (REPORT_TIME < DATE_SUB(NOW(), INTERVAL ? HOUR))")) {
			ps.setInt(1, hours);
			Collection<Integer> results = new TreeSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the IDs of all ACARS flights marked as archived with unarchived position entries.
	 * @return a Collection of flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getUnsynchedACARSFlights() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT P.FLIGHT_ID FROM acars.POSITIONS P, acars.FLIGHTS F WHERE (F.ARCHIVED=?) AND (P.FLIGHT_ID=F.ID)")) {
			ps.setBoolean(1, true);
			Collection<Integer> IDs = new LinkedHashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}

			return IDs;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the IDs of all ACARS flights marked as archived with unarchived position entries.
	 * @return a Collection of flight IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getUnsynchedXACARSFlights() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT XP.FLIGHT_ID FROM acars.POSITION_XARCHIVE XP, acars.FLIGHTS F WHERE (F.ARCHIVED=?) AND (XP.FLIGHT_ID=F.ID)")) {
			ps.setBoolean(1, true);
			Collection<Integer> IDs = new LinkedHashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}

			return IDs;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}