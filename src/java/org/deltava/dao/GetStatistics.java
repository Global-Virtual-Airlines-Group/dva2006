// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to retrieve Airline statistics.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetStatistics extends DAO  {

	private static final Cache<CacheableLong> _coolerStatsCache = new ExpiringCache<CacheableLong>(100, 1800);
	private static final Cache<CacheableLong> _cache = new ExpiringCache<CacheableLong>(2, 1800);
	private static final Cache<AirlineTotals> _aCache = new ExpiringCache<AirlineTotals>(1, 1800);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetStatistics(Connection c) {
		super(c);
	}

	/**
	 * Returns Airline Totals. This method is synchronized <i>across all instances</i> because of the expense of the
	 * database queries.
	 * @return the AirlineTotals for this airline
	 * @throws DAOException if a JDBC error occurs
	 */
	public AirlineTotals getAirlineTotals() throws DAOException {

		synchronized (GetStatistics.class) {
			// Check the cache
			AirlineTotals result = _aCache.get(AirlineTotals.class);
			if (result != null)
				return result;

			result = new AirlineTotals(Instant.now());
			try {
				// Create prepared statement
				prepareStatement("SELECT COUNT(P.ID), ROUND(SUM(P.FLIGHT_TIME), 1), SUM(P.DISTANCE), "
						+ "SUM(IF((P.ATTR & ?) > 0, 1, 0)), ROUND(SUM(IF((P.ATTR & ?) > 0, P.FLIGHT_TIME, 0)), 1), "
						+ "SUM(IF((P.ATTR & ?) > 0, P.DISTANCE, 0)), COUNT(AP.ACARS_ID), "
						+ "ROUND(SUM(IF(AP.ACARS_ID, P.FLIGHT_TIME, 0)), 1), SUM(IF(AP.ACARS_ID, P.DISTANCE, 0)) "
						+ "FROM PIREPS P LEFT JOIN ACARS_PIREPS AP ON (P.ID=AP.ID) WHERE (P.STATUS=?)");
				_ps.setQueryTimeout(25);
				_ps.setInt(1, FlightReport.ATTR_ONLINE_MASK);
				_ps.setInt(2, FlightReport.ATTR_ONLINE_MASK);
				_ps.setInt(3, FlightReport.ATTR_ONLINE_MASK);
				_ps.setInt(4, FlightReport.OK);

				// Count all airline totals
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next()) {
						result.setTotalLegs(rs.getInt(1));
						result.setTotalHours(rs.getDouble(2));
						result.setTotalMiles(rs.getLong(3));
						result.setOnlineLegs(rs.getInt(4));
						result.setOnlineHours(rs.getDouble(5));
						result.setOnlineMiles(rs.getLong(6));
						result.setACARSLegs(rs.getInt(7));
						result.setACARSHours(rs.getDouble(8));
						result.setACARSMiles(rs.getInt(9));
					}
				}

				_ps.close();

				// Get MTD/YTD start dates
				Timestamp mt = new Timestamp(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).toEpochSecond() * 1000); 
				Timestamp yt = new Timestamp(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withDayOfYear(1).toEpochSecond() * 1000);

				// Count MTD/YTD totals
				prepareStatement("SELECT SUM(IF((DATE >= ?), 1, 0)), ROUND(SUM(IF((DATE >= ?), FLIGHT_TIME, 0))), SUM(IF((DATE >= ?), DISTANCE, 0)), SUM(IF((DATE >= ?), 1, 0)), ROUND(SUM(IF((DATE >= ?), FLIGHT_TIME, 0))), "
						+ "SUM(IF((DATE >= ?), DISTANCE, 0)) FROM PIREPS WHERE (DATE >= ?) AND (STATUS=?)");
				_ps.setQueryTimeout(10);
				_ps.setTimestamp(1, mt);
				_ps.setTimestamp(2, mt);
				_ps.setTimestamp(3, mt);
				_ps.setTimestamp(4, yt);
				_ps.setTimestamp(5, yt);
				_ps.setTimestamp(6, yt);
				_ps.setTimestamp(7, yt);
				_ps.setInt(8, FlightReport.OK);

				// Do the query
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next()) {
						result.setMTDLegs(rs.getInt(1));
						result.setMTDHours(rs.getDouble(2));
						result.setMTDMiles(rs.getInt(3));
						result.setYTDLegs(rs.getInt(4));
						result.setYTDHours(rs.getDouble(5));
						result.setYTDMiles(rs.getInt(6));
					}
				}

				_ps.close();

				// Get Pilot Totals
				prepareStatement("SELECT COUNT(ID), SUM(CASE STATUS WHEN ? THEN 1 WHEN ? THEN 1 END) FROM PILOTS");
				_ps.setQueryTimeout(10);
				_ps.setInt(1, Pilot.ACTIVE);
				_ps.setInt(2, Pilot.ON_LEAVE);
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next()) {
						result.setTotalPilots(rs.getInt(1));
						result.setActivePilots(rs.getInt(2));
					}
				}
						
				_ps.close();
			} catch (SQLException se) {
				throw new DAOException(se);
			}

			// Return totals
			_aCache.add(result);
			return result;
		}
	}

	/**
	 * Returns the number of active Pilots in an Airline.
	 * @param dbName the database name
	 * @return the number of Active/On Leave pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getActivePilots(String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(ID) FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS WHERE ((STATUS=?) OR (STATUS=?))");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);

			// Execute the query
			int result = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}

			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns membership data by percentiles.
	 * @param splitInto the number of segments to divide into
	 * @return a Map of percentile and joining date.
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, java.util.Date> getMembershipQuantiles(int splitInto) throws DAOException {

		// Build the percentiles to divide into
		Collection<Float> keys = new ArrayList<Float>();
		float portion = (100.0f / splitInto);
		for (int x = 1; x <= splitInto; x++)
			keys.add(new Float(x * portion));

		try {
			// Get total Active Pilots
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?))");
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);

			// Execute the Query
			int totalSize = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					totalSize = rs.getInt(1);
			}
			
			_ps.close();
			if (totalSize == 0)
				return Collections.emptyMap();

			// Build the quantiles
			Map<Integer, java.util.Date> results = new LinkedHashMap<Integer, java.util.Date>();
			for (Iterator<Float> i = keys.iterator(); i.hasNext();) {
				float key = Math.max(99, i.next().floatValue());

				// Prepare the statement
				setQueryStart(Math.round(totalSize * key / 100));
				prepareStatementWithoutLimits("SELECT CREATED FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?)) "
						+ "ORDER BY CREATED LIMIT 1");
				_ps.setInt(1, Pilot.ACTIVE);
				_ps.setInt(2, Pilot.ON_LEAVE);

				// Execute the Query
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next())
						results.put(Integer.valueOf(Math.round(key)), rs.getDate(1));
				}

				_ps.close();
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns membership Join date statistics.
	 * @return a Collection of MembershipTotals beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<MembershipTotals> getJoinStats() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ROUND(DATEDIFF(CURDATE(), CREATED) / 30 + 1) * 30 AS MEMAGE, "
					+ "DATE_SUB(CURDATE(), INTERVAL ROUND(DATEDIFF(CURDATE(), CREATED) / 30 + 1) * 30 DAY) AS MEMDT, "
					+ "COUNT(ID) FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?)) GROUP BY MEMAGE ORDER BY MEMAGE DESC");
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);

			// Execute the Query
			Collection<MembershipTotals> results = new ArrayList<MembershipTotals>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					MembershipTotals mt = new MembershipTotals(rs.getDate(2));
					mt.setID(rs.getInt(1));
					mt.setCount(rs.getInt(3));
					results.add(mt);
				}
			}

			// Clean up and return
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Water Cooler posting statistics for a number of users.
	 * @param ids a Collection of database IDs
	 * @return a Map of post counts, indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Long> getCoolerStatistics(Collection<Integer> ids) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();

		// Load from the cache
		Map<Integer, Long> results = new HashMap<Integer, Long>();
		for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
			Integer id = i.next();
			CacheableLong result = _coolerStatsCache.get(id);
			if (result != null) {
				results.put(id, Long.valueOf(result.getValue()));
				i.remove();
			}
		}

		// If we've loaded everything from the cache, return
		if (ids.isEmpty())
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AUTHOR_ID, COUNT(*) FROM common.COOLER_POSTS WHERE (AUTHOR_ID");
		sqlBuf.append((ids.size() == 1) ? "=" : " IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append((ids.size() == 1) ? ")" : "))");
		sqlBuf.append(" GROUP BY AUTHOR_ID");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), Long.valueOf(rs.getInt(2)));
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Water Cooler posting statistics.
	 * @param orderBy the order by column within SQL
	 * @param groupBy the label SQL function
	 * @param distinctBy the column to bring in to count distinct entries
	 * @return a List of CoolerStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CoolerStatsEntry<String>> getCoolerStatistics(String orderBy, String groupBy, String distinctBy)
			throws DAOException {

		// Generate SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append("AS LBL, COUNT(DISTINCT CP.POST_ID) AS PC, COUNT(DISTINCT ");
		sqlBuf.append(distinctBy);
		sqlBuf.append(") AS DSTNCT FROM PILOTS P, common.COOLER_POSTS CP WHERE (P.ID=CP.AUTHOR_ID)");
		sqlBuf.append(" GROUP BY LBL ORDER BY ");
		sqlBuf.append(orderBy);

		try {
			prepareStatement(sqlBuf.toString());
			List<CoolerStatsEntry<String>> results = new ArrayList<CoolerStatsEntry<String>>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(new CoolerStatsEntry<String>(rs.getString(1), rs.getInt(2), rs.getInt(3)));
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves Water Cooler post counts.
	 * @param days the number of days in the past to count
	 * @return the number of posts in the specified interval
	 * @throws DAOException if a JDBC error occurs
	 */
	public synchronized long getCoolerStatistics(int days) throws DAOException {

		// Check the cache
		CacheableLong result = _cache.get(Integer.valueOf(days));
		if (result != null)
			return result.getValue();

		try {
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM common.COOLER_POSTS WHERE (CREATED > DATE_SUB(NOW(), INTERVAL ? DAY)) LIMIT 1");
			_ps.setInt(1, days);
			try (ResultSet rs = _ps.executeQuery()) {
				result = new CacheableLong(Integer.valueOf(days), rs.next() ? rs.getInt(1) : 0);
			}

			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		_cache.add(result);
		return result.getValue();
	}
}