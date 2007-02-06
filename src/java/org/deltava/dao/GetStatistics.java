// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.*;

import org.deltava.util.CalendarUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to retrieve airline statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetStatistics extends DAO {

	private static final Cache<CacheableInteger> _coolerStatsCache = new ExpiringCache<CacheableInteger>(100, 1800);
	private static final Cache<CacheableInteger> _cache = new ExpiringCache<CacheableInteger>(2, 1800);

	/**
	 * Initializes the Data Access Object.
	 * @param c a JDBC connection
	 */
	public GetStatistics(Connection c) {
		super(c);
	}

	/**
	 * Returns Airline Totals.
	 * @return the AirlineTotals for this airline
	 * @throws DAOException if a JDBC error occurs
	 */
	public AirlineTotals getAirlineTotals() throws DAOException {

		AirlineTotals result = new AirlineTotals(System.currentTimeMillis());
		try {
			// Create prepared statement
			prepareStatement("SELECT COUNT(ID), ROUND(SUM(FLIGHT_TIME), 1), SUM(DISTANCE) "
					+ "FROM PIREPS WHERE (DATE > ?) AND (STATUS=?)");
			_ps.setQueryTimeout(5);
			_ps.setInt(2, FlightReport.OK);

			// Count all airline totals
			_ps.setTimestamp(1, new Timestamp(AirlineTotals.BIRTHDATE.getTimeInMillis()));
			ResultSet rs = _ps.executeQuery();
			rs.next();
			result.setTotalLegs(rs.getInt(1));
			result.setTotalHours(rs.getDouble(2));
			result.setTotalMiles(rs.getLong(3));
			rs.close();
			rs = null;

			// Count MTD totals
			Calendar c = CalendarUtils.getInstance(null, true);
			c.set(Calendar.DAY_OF_MONTH, 1);
			_ps.setTimestamp(1, new Timestamp(c.getTimeInMillis()));
			rs = _ps.executeQuery();
			rs.next();
			result.setMTDLegs(rs.getInt(1));
			result.setMTDHours(rs.getDouble(2));
			result.setMTDMiles(rs.getInt(3));
			rs.close();
			rs = null;

			// Count YTD totals
			c.set(Calendar.DAY_OF_YEAR, 1);
			_ps.setTimestamp(1, new Timestamp(c.getTimeInMillis()));
			rs = _ps.executeQuery();
			rs.next();
			result.setYTDLegs(rs.getInt(1));
			result.setYTDHours(rs.getDouble(2));
			result.setYTDMiles(rs.getInt(3));
			rs.close();
			rs = null;
			_ps.close();
			_ps = null;

			// Get Online totals
			prepareStatement("SELECT COUNT(DISTANCE), ROUND(SUM(FLIGHT_TIME), 1), SUM(DISTANCE) "
					+ "FROM PIREPS WHERE ((ATTR & ?) > 0)");
			_ps.setInt(1, FlightReport.ATTR_ONLINE_MASK);
			rs = _ps.executeQuery();
			rs.next();
			result.setOnlineLegs(rs.getInt(1));
			result.setOnlineHours(rs.getDouble(2));
			result.setOnlineMiles(rs.getLong(3));
			rs.close();
			_ps.close();

			// Get ACARS totals
			prepareStatement("SELECT COUNT(P.DISTANCE), ROUND(SUM(P.FLIGHT_TIME), 1), SUM(P.DISTANCE) FROM PIREPS P, "
					+ "ACARS_PIREPS A WHERE (P.ID=A.ID)");
			rs = _ps.executeQuery();
			rs.next();
			result.setACARSLegs(rs.getInt(1));
			result.setACARSHours(rs.getDouble(2));
			result.setACARSMiles(rs.getInt(3));
			rs.close();
			_ps.close();

			// Get Pilot Totals
			prepareStatement("SELECT COUNT(ID) FROM PILOTS");
			rs = _ps.executeQuery();
			rs.next();
			result.setTotalPilots(rs.getInt(1));
			rs.close();
			_ps.close();

			// Get Pilot Totals
			prepareStatement("SELECT COUNT(ID) FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?))");
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);
			rs = _ps.executeQuery();
			rs.next();
			result.setActivePilots(rs.getInt(1));
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Return totals
		return result;
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
			ResultSet rs = _ps.executeQuery();
			int totalSize = (rs.next()) ? rs.getInt(1) : 0;
			rs.close();
			_ps.close();
			if (totalSize == 0)
				return Collections.emptyMap();
			
			// Build the quantiles
			Map<Integer, java.util.Date> results = new LinkedHashMap<Integer, java.util.Date>();
			for (Iterator<Float> i = keys.iterator(); i.hasNext(); ) {
				float key = i.next().floatValue();
				if (key > 99.0f)
					key = 99.0f;
					
				// Prepare the statement
				setQueryStart(Math.round(totalSize * key / 100));
				setQueryMax(1);
				prepareStatement("SELECT CREATED FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?)) ORDER BY CREATED");
				_ps.setInt(1, Pilot.ACTIVE);
				_ps.setInt(2, Pilot.ON_LEAVE);

				// Execute the Query
				rs = _ps.executeQuery();
				if (rs.next())
					results.put(new Integer(Math.round(key)), rs.getDate(1));
				
				// Clean up
				setQueryMax(0);
				rs.close();
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
			ResultSet rs = _ps.executeQuery();
			Collection<MembershipTotals> results = new ArrayList<MembershipTotals>();
			while (rs.next()) {
				MembershipTotals mt = new MembershipTotals(rs.getDate(2));
				mt.setID(rs.getInt(1));
				mt.setCount(rs.getInt(3));
				results.add(mt);
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
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param groupBy
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @param descSort TRUE if a descending sort, otherwise FALSE
	 * @return a List of StatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightStatsEntry> getPIREPStatistics(String groupBy, String orderBy, boolean descSort)
			throws DAOException {

		// Generate SQL statement
		StringBuilder sqlBuf = (groupBy.indexOf("P.") != -1) ? getPilotJoinSQL(groupBy) : getSQL(groupBy);
		sqlBuf.append(orderBy);
		if (descSort)
			sqlBuf.append(" DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.ATTR_ACARS);
			_ps.setInt(2, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(3, FlightReport.ATTR_HISTORIC);
			_ps.setInt(4, FlightReport.OK);

			// Execute the query
			List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(4), rs.getInt(3));
				entry.setACARSLegs(rs.getInt(7));
				entry.setOnlineLegs(rs.getInt(8));
				entry.setHistoricLegs(rs.getInt(9));
				results.add(entry);
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
	 * Returns Water Cooler posting statistics for a number of users.
	 * @param ids a Collection of database IDs
	 * @return a Map of post counts, indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Integer> getCoolerStatistics(Collection<Integer> ids) throws DAOException {
		if (ids.isEmpty())
			return Collections.emptyMap();

		// Load from the cache
		Map<Integer, Integer> results = new HashMap<Integer, Integer>();
		for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
			Integer id = i.next();
			if (_coolerStatsCache.contains(id)) {
				CacheableInteger result = _coolerStatsCache.get(id);
				results.put(id, new Integer(result.getValue()));
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

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.put(new Integer(rs.getInt(1)), new Integer(rs.getInt(2)));
			
			// Clean up and return
			rs.close();
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
	public List<CoolerStatsEntry> getCoolerStatistics(String orderBy, String groupBy, String distinctBy)
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

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List<CoolerStatsEntry> results = new ArrayList<CoolerStatsEntry>();
			while (rs.next()) {
				CoolerStatsEntry entry = new CoolerStatsEntry(rs.getString(1), rs.getInt(2), rs.getInt(3));
				results.add(entry);
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
	 * Retrieves Water Cooler post counts.
	 * @param days the number of days in the past to count
	 * @return the number of posts in the specified interval
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCoolerStatistics(int days) throws DAOException {

		// Check the cache
		CacheableInteger result = _cache.get(new Integer(days));
		if (result != null)
			return result.getValue();

		try {
			setQueryMax(1);
			prepareStatement("SELECT COUNT(*) FROM common.COOLER_POSTS WHERE "
					+ "(CREATED > DATE_SUB(NOW(), INTERVAL ? DAY))");
			_ps.setInt(1, days);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			result = new CacheableInteger(new Integer(days), rs.next() ? rs.getInt(1) : 0);
			setQueryMax(0);

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Return the result
		_cache.add(result);
		return result.getValue();
	}

	/**
	 * Private helper method to return SQL statement that doesn't involve joins on the <i>PILOTS </i> table.
	 */
	private StringBuilder getSQL(String groupBy) {
		StringBuilder buf = new StringBuilder("SELECT ");
		buf.append(groupBy);
		buf.append(" AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, ROUND(SUM(F.FLIGHT_TIME), 1) "
				+ "AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(DISTANCE) AS AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) "
				+ "AS ACARSLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS FROM "
				+ "PIREPS F WHERE (F.STATUS=?) GROUP BY LABEL ORDER BY ");
		return buf;
	}

	/**
	 * Private helper method to return SQL statement that involves a join on the <i>PILOTS </i> table.
	 */
	private StringBuilder getPilotJoinSQL(String groupBy) {
		StringBuilder buf = new StringBuilder("SELECT ");
		buf.append(groupBy);
		buf.append(" AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, "
			+ "ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) AS "
			+ "AVGMILES, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS ACARSLEGS, SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS OLEGS, "
			+ "SUM(IF((F.ATTR & ?) > 0, 1, 0)) AS HISTLEGS FROM PILOTS P, PIREPS F WHERE (P.ID=F.PILOT_ID) AND "
			+ "(F.STATUS=?) GROUP BY LABEL ORDER BY ");
		return buf;
	}
}