// Copyright 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Duration;

import org.deltava.beans.acars.TaxiTime;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to calculate average taxi times. 
 * @author Luke
 * @version 10.0
 * @since 8.6
 */

public class GetACARSTaxiTimes extends DAO {

	private static final Cache<TaxiTime> _cache = CacheManager.get(TaxiTime.class, "TaxiTime");
	
	private static class TaxiTotal implements Comparable<TaxiTotal> {
		private final String _icao;
		private final int _year;
		private long _outTotal;
		private long _inTotal;
		private int _outCount;
		private int _inCount;
		
		TaxiTotal(String icao, int year) {
			_icao = icao;
			_year = year;
		}

		@Override
		public int compareTo(TaxiTotal tt) {
			int tmpResult = _icao.compareTo(tt._icao);
			return (tmpResult == 0) ? Integer.compare(_year, tt._year) : tmpResult;
		}
	}
	
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
		TaxiTime tt = new TaxiTime(a.getICAO(), year);
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SUM(TAXI_IN*TOTAL_IN), SUM(TAXI_OUT*TOTAL_OUT), SUM(TOTAL_IN), SUM(TOTAL_OUT) FROM acars.TAXI_TIMES WHERE (ICAO=?) AND (YEAR=?) GROUP BY ICAO")) {
			ps.setString(1, a.getICAO());
			ps.setInt(2, year);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int inCount = rs.getInt(3); int outCount = rs.getInt(4);
					tt.setInboundTime((inCount == 0) ? Duration.ZERO : Duration.ofSeconds(rs.getLong(1) / inCount));
					tt.setOutboundTime((outCount == 0) ? Duration.ZERO : Duration.ofSeconds(rs.getLong(2) / outCount));
				}
			}
			
			return tt;
		} catch (SQLException se) {
			throw new DAOException(se);
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
		TaxiTime tt = _cache.get(a.getICAO());
		if (tt != null)
			return tt;
		
		Collection<TaxiTotal> rawResults = new ArrayList<TaxiTotal>(); 
		try (PreparedStatement ps = prepareWithoutLimits("SELECT YEAR, SUM(TAXI_IN*TOTAL_IN), SUM(TAXI_OUT*TOTAL_OUT), SUM(TOTAL_IN), SUM(TOTAL_OUT) FROM acars.TAXI_TIMES WHERE (ICAO=?) GROUP BY ICAO, YEAR ORDER BY YEAR")) {
			ps.setString(1, a.getICAO());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					TaxiTotal ttl = new TaxiTotal(a.getICAO(), rs.getInt(1));
					ttl._inTotal = rs.getLong(2);
					ttl._outTotal = rs.getLong(3);
					ttl._inCount = rs.getInt(4);
					ttl._outCount = rs.getInt(5);
					rawResults.add(ttl);
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Aggregate the totals
		TaxiTotal t = new TaxiTotal(a.getICAO(), 0);
		for (TaxiTotal ttl : rawResults) {
			t._inCount += ttl._inCount;
			t._inTotal += ttl._inTotal;
			t._outCount += ttl._outCount;
			t._outTotal += ttl._outTotal;
		}
		
		tt = new TaxiTime(a.getICAO(), 0);
		tt.setInboundTime(Duration.ofSeconds(t._inTotal / t._inCount));
		tt.setOutboundTime(Duration.ofSeconds(t._outTotal / t._outCount));
		_cache.add(tt);
		return tt;
	}
}