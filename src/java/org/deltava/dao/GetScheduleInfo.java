// Copyright 2006, 2010, 2011, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to extract Flight Schedule data.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class GetScheduleInfo extends DAO {
	
	private static final Cache<CacheableLong> _schedSizeCache = new ExpiringCache<CacheableLong>(2, 1800);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleInfo(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all flight numbers in a particular range.
	 * @param a the Airline bean
	 * @param start the start of the range, or zero if none specified
	 * @param end the end of the range, or zero if none specified
	 * @return a Collection of Integers with flight numbers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getFlightNumbers(Airline a, int start, int end) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT FLIGHT FROM SCHEDULE WHERE (AIRLINE=?)");
		Collection<String> params = new ArrayList<String>();
		if (start > 0)
			params.add("(FLIGHT > ?)");
		if (end > 0)
			params.add("(FLIGHT < ?)");

		// Add parameters
		if (!params.isEmpty()) {
			for (Iterator<String> i = params.iterator(); i.hasNext();) {
				String p = i.next();
				sqlBuf.append(" AND ");
				sqlBuf.append(p);
			}
		}

		sqlBuf.append(" ORDER BY FLIGHT");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, a.getCode());
			if (start > 0)
				_ps.setInt(2, start);
			if (end > 0)
				_ps.setInt(3, end);

			// Execute the query
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the next available Leg number for a Flight.
	 * @param a the Airline bean
	 * @param flightNumber the flight number
	 * @return the next available leg number
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if flightNumber is zero or negative
	 */
	public int getNextLeg(Airline a, int flightNumber) throws DAOException {
		if (flightNumber < 1)
			throw new IllegalArgumentException("Invalid Flight Number -  " + flightNumber);

		try {
			prepareStatement("SELECT MAX(LEG) FROM SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?)");
			_ps.setString(1, a.getCode());
			_ps.setInt(2, flightNumber);

			// Do the query
			int leg = 1;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					leg = rs.getInt(1) + 1;
			}

			_ps.close();
			return leg;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a map of serviced Airports by Airline.
	 * @return an AirportServiceMap bean
	 * @throws DAOException if a JDBC error occurs
	 * @see AirportServiceMap
	 */
	public AirportServiceMap getRoutePairs() throws DAOException {
		AirportServiceMap svcMap = new AirportServiceMap();
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT AIRLINE, AIRPORT_D, AIRPORT_A FROM SCHEDULE ORDER BY AIRPORT_D, AIRPORT_A");
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Airline a = SystemData.getAirline(rs.getString(1));
					svcMap.add(a, SystemData.getAirport(rs.getString(2)));
					svcMap.add(a, SystemData.getAirport(rs.getString(3)));
				}
			}
			
			_ps.close();
			return svcMap;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the size of the Flight Schedule.
	 * @return the number of legs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getFlightCount() throws DAOException {
		
		// Check the cache
		CacheableLong result = _schedSizeCache.get(GetSchedule.class);
		if (result != null)
			return (int) result.getValue();
		
		try {
			prepareStatement("SELECT COUNT(*) FROM SCHEDULE");
			try (ResultSet rs = _ps.executeQuery()) {
				result = new CacheableLong(GetSchedule.class, rs.next() ? rs.getInt(1) : 0);
			}

			_ps.close();
			_schedSizeCache.add(result);
			return (int) result.getValue();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the Countries served by Airports in the Flight Schedule.
	 * @return a Collection of Country beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Country> getCountries() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT A.COUNTRY FROM common.AIRPORTS A, SCHEDULE S WHERE (A.IATA=S.AIRPORT_D) OR (A.IATA=S.AIRPORT_A)");
			Collection<Country> results = new LinkedHashSet<Country>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Country.get(rs.getString(1)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of flights in the schedule per Airline.
	 * @param dbName the database name
	 * @return a Map of Integers, keyed by Airline
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Airline, Integer> getAirlineCounts(String dbName) throws DAOException {
		
		StringBuilder buf = new StringBuilder("SELECT AIRLINE, COUNT(*) AS CNT FROM ");
		buf.append(formatDBName(dbName));
		buf.append(".SCHEDULE GROUP BY AIRLINE ORDER BY AIRLINE");
		
		try {
			prepareStatementWithoutLimits(buf.toString());
			Map<Airline, Integer> results = new LinkedHashMap<Airline, Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.put(SystemData.getAirline(rs.getString(1)), Integer.valueOf(rs.getInt(2)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}