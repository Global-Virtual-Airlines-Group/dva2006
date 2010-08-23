// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to load Dispatcher Activity statistics. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class GetACARSDispatchStats extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSDispatchStats(Connection c) {
		super(c);
	}

	/**
	 * Loads dispatcher totals for a pilot.
	 * @param p the Pilot bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getDispatchTotals(Pilot p) throws DAOException {
		if (p.getDispatchFlights() >= 0)
			return;
		
		try {
			// Load hours
			prepareStatement("SELECT SUM(UNIX_TIMESTAMP(ENDDATE)-UNIX_TIMESTAMP(DATE)) / 3600 AS HRS FROM "
					+ "acars.CONS WHERE (PILOT_ID=?) AND (DISPATCH=?) AND (ENDDATE IS NOT NULL)");
			_ps.setInt(1, p.getID());
			_ps.setBoolean(2, true);
			ResultSet rs = _ps.executeQuery();
			if (rs.next())
				p.setDispatchHours(rs.getDouble(1));
			
			rs.close();
			_ps.close();
			
			// Load legs
			prepareStatement("SELECT COUNT(ID) FROM acars.FLIGHT_DISPATCHER WHERE (DISPATCHER_ID=?)");
			_ps.setInt(1, p.getID());
			rs = _ps.executeQuery();
			if (rs.next())
				p.setDispatchFlights(rs.getInt(1));
			
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Dispatchers who provided service between two Dates.
	 * @param startDate the start date/time
	 * @param endDate the end date/time, or null for the current date
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getDispatchers(java.util.Date startDate, java.util.Date endDate) throws DAOException {
		if (endDate == null)
			endDate = new java.util.Date();
		
		try {
			prepareStatement("SELECT DISTINCT PILOT_ID FROM acars.CONS WHERE (DATE>=?) AND (DATE<=?) "
				+ "AND (ENDDATE IS NOT NULL) AND (DISPATCH=?)");
			_ps.setTimestamp(1, createTimestamp(startDate));
			_ps.setTimestamp(2, createTimestamp(endDate));
			_ps.setBoolean(3, true);
			
			Collection<Integer> results = new HashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads Dispatch totals for a group of Pilots.
	 * @param pilots a Map of Pilots, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getDispatchTotals(Map<Integer, Pilot> pilots) throws DAOException {
		for (Iterator<Map.Entry<Integer, Pilot>> i = pilots.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Integer, Pilot> me = i.next();
			Pilot p = me.getValue();
			if (p.getDispatchFlights() < 0)
				getDispatchTotals(p);
		}
	}
}