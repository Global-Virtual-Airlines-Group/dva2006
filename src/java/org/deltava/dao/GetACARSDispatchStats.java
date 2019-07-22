// Copyright 2010, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.DispatchStatistics;

/**
 * A Data Access Object to load Dispatcher Activity statistics. 
 * @author Luke
 * @version 8.6
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
			prepareStatement("SELECT SUM(UNIX_TIMESTAMP(ENDDATE)-UNIX_TIMESTAMP(DATE)) / 3600 AS HRS FROM acars.CONS WHERE (PILOT_ID=?) AND (ENDDATE IS NOT NULL)");
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
			prepareStatement("SELECT PILOT_ID, SUM(UNIX_TIMESTAMP(IFNULL(ENDDATE, NOW()))-UNIX_TIMESTAMP(DATE)) / 3600 AS HRS FROM acars.CONS WHERE "
				+ "(DATE >= ?) AND (ENDDATE<?) GROUP BY PILOT_ID ORDER BY HRS DESC");
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
			prepareStatement("SELECT FD.DISPATCHER_ID, COUNT(FD.ID) FROM acars.FLIGHT_DISPATCHER FD, acars.FLIGHTS F WHERE (F.ID=FD.ID) AND (F.CREATED >= ?) "
				+ "AND (F.CREATED<?) GROUP BY FD.DISPATCHER_ID");
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
	public List<DateRange> getDispatchRanges() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT MONTH(F.CREATED), YEAR(F.CREATED) FROM acars.FLIGHTS F, acars.FLIGHT_DISPATCHER FD WHERE (F.ID=FD.ID) ORDER BY F.CREATED");
			
			// Execute the query
			ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
			Collection<DateRange> years = new TreeSet<DateRange>(Collections.reverseOrder());
			List<DateRange> results = new ArrayList<DateRange>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					zdt = zdt.withMonth(rs.getInt(1)).withYear(rs.getInt(2));
					results.add(DateRange.createMonth(zdt));
					zdt = zdt.withMonth(1);
					years.add(DateRange.createYear(zdt));
				}
			}
			
			_ps.close();
			
			// Add today and merge
			DateRange cdr = DateRange.createMonth(ZonedDateTime.now());
			if (!results.contains(cdr))
				results.add(cdr);
			
			Collections.reverse(results);
			results.addAll(years);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}