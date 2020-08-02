// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.flight.FlightStatus;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to calculate average taxi times. 
 * @author Luke
 * @version 9.1
 * @since 8.6
 */

public class GetACARSTaxiTimes extends DAO {

	private static final Cache<CacheableLong> _cache = CacheManager.get(CacheableLong.class, "TaxiTime");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetACARSTaxiTimes(Connection c) {
		super(c);
	}
	
	private static String buildCacheKey(Airport a, String db, boolean isDeparture) {
		StringBuilder buf = new StringBuilder(db);
		buf.append('!').append(a.getICAO());
		buf.append('!').append(isDeparture ? 'D' : 'A');
		return buf.toString();
	}

	/**
	 * Returns the average outbound taxi time for a particular Airport.
	 * @param a the Airport
	 * @param db the database name
	 * @return the average taxi time, in seconds
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getTaxiOutTime(Airport a, String db) throws DAOException {
		
		// Check the cache
		String key = buildCacheKey(a, db, true);
		CacheableLong depTime = _cache.get(key);
		if (depTime != null)
			return depTime.intValue();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AVG(TIMESTAMPDIFF(SECOND, TAXI_TIME, TAKEOFF_TIME)) AS TX_TKO FROM ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".ACARS_PIREPS AP, ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PIREPS P WHERE (AP.ID=P.ID) AND (AP.TAXI_TIME < AP.TAKEOFF_TIME) AND (AP.TAKEOFF_TIME < DATE_ADD(AP.TAXI_TIME, INTERVAL 2 HOUR)) AND (P.AIRPORT_D=?) AND (P.STATUS=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, a.getIATA());
			ps.setInt(2, FlightStatus.OK.ordinal());
			
			int result = -1;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}
			
			_cache.add(new CacheableLong(key, result));
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the average inbound taxi time for a particular Airport.
	 * @param a the Airport
	 * @param db the database name
	 * @return the average taxi time, in seconds
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getTaxiInTime(Airport a, String db) throws DAOException {
		
		// Check the cache
		String key = buildCacheKey(a, db, false);
		CacheableLong arrTime = _cache.get(key);
		if (arrTime != null)
			return arrTime.intValue();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AVG(TIMESTAMPDIFF(SECOND, LANDING_TIME, END_TIME)) AS TX_LND FROM ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".ACARS_PIREPS AP, ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PIREPS P WHERE (AP.ID=P.ID) AND (AP.LANDING_TIME < AP.END_TIME) AND (AP.END_TIME < DATE_ADD(AP.LANDING_TIME, INTERVAL 2 HOUR)) AND (P.AIRPORT_A=?) AND (P.STATUS=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, a.getIATA());
			ps.setInt(2, FlightStatus.OK.ordinal());
			
			int result = -1;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}
			
			_cache.add(new CacheableLong(key, result));
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}