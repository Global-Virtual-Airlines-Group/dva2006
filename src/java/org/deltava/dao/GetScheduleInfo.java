// Copyright 2006, 2010, 2011, 2012, 2019, 2020, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to extract Flight Schedule data.
 * @author Luke
 * @version 10.5
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
	 * Returns the size of the Flight Schedule.
	 * @return the number of legs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getFlightCount() throws DAOException {
		
		// Check the cache
		CacheableLong result = _schedSizeCache.get(GetSchedule.class);
		if (result != null)
			return result.intValue();
		
		try (PreparedStatement ps = prepare("SELECT COUNT(*) FROM SCHEDULE")) {
			try (ResultSet rs = ps.executeQuery()) {
				result = new CacheableLong(GetSchedule.class, rs.next() ? rs.getInt(1) : 0);
			}

			_schedSizeCache.add(result);
			return result.intValue();
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT A.COUNTRY FROM common.AIRPORTS A, SCHEDULE S WHERE (A.IATA=S.AIRPORT_D) OR (A.IATA=S.AIRPORT_A)")) {
			Collection<Country> results = new LinkedHashSet<Country>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Country.get(rs.getString(1)));
			}
			
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
		
		try (PreparedStatement ps = prepareWithoutLimits(buf.toString())) {
			Map<Airline, Integer> results = new LinkedHashMap<Airline, Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.put(SystemData.getAirline(rs.getString(1)), Integer.valueOf(rs.getInt(2)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns schedule filter history.
	 * @return a Collection of ScheduleSourceHistory beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleSourceHistory> getHistory() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM RAW_SCHEDULE_HISTORY ORDER BY IMPORTDATE DESC, SRC DESC")) {
			Collection<ScheduleSourceHistory> results = new ArrayList<ScheduleSourceHistory>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleSourceHistory ssh = new ScheduleSourceHistory(ScheduleSource.values()[rs.getInt(1)]);
					ssh.setEffectiveDate(toInstant(rs.getTimestamp(2)));
					ssh.setImportDate(toInstant(rs.getTimestamp(3)));
					ssh.setTime(rs.getInt(4));
					ssh.setLegs(rs.getInt(5));
					ssh.setSkipped(rs.getInt(6));
					ssh.setAdjusted(rs.getInt(7));
					ssh.setPurged(rs.getBoolean(8));
					ssh.setAuthorID(rs.getInt(9));
					StringUtils.split(rs.getString(10), ",").stream().map(ac -> SystemData.getAirline(ac)).filter(Objects::nonNull).forEach(a -> ssh.addLegs(a, 1));
					results.add(ssh);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}