// Copyright 2019, 2020, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.Duration;

import org.deltava.beans.acars.TaxiTime;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to calculate average taxi times. 
 * @author Luke
 * @version 11.2
 * @since 8.6
 */

public class GetACARSTaxiTimes extends DAO {

	private static final Cache<TaxiTime> _cache = CacheManager.get(TaxiTime.class, "TaxiTime");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetACARSTaxiTimes(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves the average taxi time for an Airport in a given year.
	 * @param a the Airport
	 * @param year the year
	 * @return a TaxiTime
	 * @throws DAOException if a JDBC error occurs
	 */
	public TaxiTime getTaxiTime(Airport a, int year) throws DAOException {
		
		// Check the cache
		TaxiTime t = new TaxiTime(a.getICAO(), year);
		TaxiTime tt = _cache.get(t.cacheKey());
		if (tt != null)
			return tt;
		
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(ID), IFNULL(AVG(TAXITIME),0), IFNULL(STDDEV(TAXITIME),0) FROM acars.TAXI_TIMES WHERE (IATA=?) AND (IS_DEPARTURE=?) AND (YEAR=?)")) {
				ps.setString(1, a.getICAO());
				ps.setBoolean(2, true);
				ps.setInt(3, year);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						t.setInboundCount(rs.getInt(1));
						t.setInboundTime(Duration.ofSeconds(rs.getInt(2)));
						t.setInboundStdDev(Duration.ofSeconds(rs.getInt(3)));
					}
				}
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(ID), IFNULL(AVG(TAXITIME),0), IFNULL(STDDEV(TAXITIME),0) FROM acars.TAXI_TIMES WHERE (IATA=?) AND (IS_DEPARTURE=?) AND (YEAR=?)")) {
				ps.setString(1, a.getICAO());
				ps.setBoolean(2, false);
				ps.setInt(3, year);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						t.setOutboundCount(rs.getInt(1));
						t.setOutboundTime(Duration.ofSeconds(rs.getInt(2)));
						t.setOutboundStdDev(Duration.ofSeconds(rs.getInt(3)));
					}
				}
			}
			
			return t;
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			rollbackTransaction();
		}
	}
	
	/**
	 * Retrieves the average taxi time for an Airport.
	 * @param a the Airport
	 * @return a TaxiTime
	 * @throws DAOException if a JDBC error occurs
	 */
	public TaxiTime getTaxiTime(Airport a) throws DAOException {
		
		// Check the cache
		TaxiTime t = new TaxiTime(a.getICAO(), 0);
		TaxiTime tt = _cache.get(t.cacheKey());
		if (tt != null)
			return tt;
		
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(ID), AVG(TAXITIME), STDDEV(TAXITIME) FROM acars.TAXI_TIMES WHERE (IATA=?) AND (IS_DEPARTURE=?)")) {
				ps.setString(1, a.getIATA());
				ps.setBoolean(2, false);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						t.setInboundCount(rs.getInt(1));
						t.setInboundTime(Duration.ofSeconds(rs.getInt(2)));
						t.setInboundStdDev(Duration.ofSeconds(rs.getInt(3)));
					}
				}
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(ID), AVG(TAXITIME), STDDEV(TAXITIME) FROM acars.TAXI_TIMES WHERE (IATA=?) AND (IS_DEPARTURE=?)")) {
				ps.setString(1, a.getIATA());
				ps.setBoolean(2, true);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						t.setOutboundCount(rs.getInt(1));
						t.setOutboundTime(Duration.ofSeconds(rs.getInt(2)));
						t.setOutboundStdDev(Duration.ofSeconds(rs.getInt(3)));
					}
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			rollbackTransaction();
		}
		
		_cache.add(t);
		return t;
	}

	/**
	 * Returns whether a given ACARS flight has logged taxi times.
	 * @param id the ACARS Flight ID
	 * @return TRUE if taxi times have been logged, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean hasTimes(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(*) FROM acars.TAXI_TIMES WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && (rs.getInt(1) > 0);
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}