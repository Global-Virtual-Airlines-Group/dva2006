// Copyright 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.DispatchStatistics;

/**
 * A Data Access Object to load Dispatcher Activity statistics. 
 * @author Luke
 * @version 4.2
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
					+ "acars.CONS WHERE (PILOT_ID=?) AND (ENDDATE IS NOT NULL)");
			_ps.setInt(1, p.getID());
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					p.setDispatchHours(rs.getDouble(1));
			}
			
			_ps.close();
			
			// Load legs
			prepareStatement("SELECT COUNT(ID) FROM acars.FLIGHT_DISPATCHER WHERE (DISPATCHER_ID=?)");
			_ps.setInt(1, p.getID());
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					p.setDispatchFlights(rs.getInt(1));
			}
			
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Dispatchers who provided service between two Dates.
	 * @param dr the DateRange
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DispatchStatistics> getTopDispatchers(DateRange dr) throws DAOException {
		try {
			prepareStatement("SELECT PILOT_ID, SUM(UNIX_TIMESTAMP(IFNULL(ENDDATE, NOW()))-UNIX_TIMESTAMP(DATE)) / 3600 AS HRS "
				+ "FROM acars.CONS WHERE (DATE >= ?) AND (ENDDATE<?) GROUP BY PILOT_ID ORDER BY HRS DESC");
			_ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			_ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
			
			// Load the Hours
			Map<Integer, DispatchStatistics> results = new LinkedHashMap<Integer, DispatchStatistics>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					DispatchStatistics ds = new DispatchStatistics(rs.getInt(1));
					ds.setHours(rs.getDouble(2));
					results.put(Integer.valueOf(ds.getID()), ds);
				}
			}
			
			_ps.close();
			
			// Load the Legs
			prepareStatement("SELECT FD.DISPATCHER_ID, COUNT(FD.ID) FROM acars.FLIGHT_DISPATCHER FD, acars.FLIGHTS F WHERE "
				+ " (F.ID=FD.ID) AND (F.CREATED >= ?) AND (F.CREATED<?) GROUP BY FD.DISPATCHER_ID");
			_ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			_ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					DispatchStatistics ds = results.get(Integer.valueOf(rs.getInt(1)));
					if (ds != null)
						ds.setLegs(rs.getInt(2));
				}
			}

			_ps.close();
			return results.values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all date ranges with dispatched flights.
	 * @return a Collection of DateRange beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DateRange> getDispatchRanges() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT MONTH(F.CREATED), YEAR(F.CREATED) FROM acars.FLIGHTS F, "
				+ "acars.FLIGHT_DISPATCHER FD WHERE (F.ID=FD.ID) ORDER BY F.CREATED");
			
			// Execute the query
			Calendar cld = Calendar.getInstance();
			cld.set(Calendar.DAY_OF_MONTH, 1);
			
			Collection<DateRange> years = new LinkedHashSet<DateRange>();
			List<DateRange> results = new ArrayList<DateRange>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					cld.set(Calendar.MONTH, rs.getInt(1) - 1);
					cld.set(Calendar.YEAR, rs.getInt(2));
					java.util.Date dt = cld.getTime();
					results.add(DateRange.createMonth(dt));
					years.add(DateRange.createYear(dt));
				}
			}
			
			// Clean up and merge
			_ps.close();
			results.addAll(0, years);
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