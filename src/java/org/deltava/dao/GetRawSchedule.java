// Copyright 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load raw schedule entries and tail codes.
 * @author Luke
 * @version 10.0
 * @since 8.0
 */

public class GetRawSchedule extends DAO {
	
	private static final Cache<CacheableCollection<ScheduleSourceInfo>> _srcCache = CacheManager.getCollection(ScheduleSourceInfo.class, "ScheduleSource"); 

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetRawSchedule(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all raw schedule sources.
	 * @param isLoaded TRUE to only include loaded sources, otherwise FALSE
	 * @param db the database name
	 * @return a Collection of ScheduleSourceInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleSourceInfo> getSources(boolean isLoaded, String db) throws DAOException {
		
		// Check the cache
		String dbName = formatDBName(db);
		String cacheKey = dbName + "!!" + (isLoaded ? "Loaded" : "ALL");
		CacheableCollection<ScheduleSourceInfo> results = _srcCache.get(cacheKey);
		if (results != null)
			return results.clone();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT RS.SRC, RS.AIRLINE, COUNT(RS.SRCLINE) AS TOTAL, MAX(RS.SRCLINE) AS ML, RSD.EFFDATE, RSD.IMPORTDATE, RSD.ISAUTO FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".RAW_SCHEDULE RS LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".RAW_SCHEDULE_DATES RSD ON (RS.SRC=RSD.SRC) GROUP BY SRC, AIRLINE ORDER BY SRC");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			Collection<ScheduleSourceInfo> srcs = new LinkedHashSet<ScheduleSourceInfo>();
			ScheduleSourceInfo inf = null;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleSource ss = ScheduleSource.values()[rs.getInt(1)];
					if ((inf == null) || (ss != inf.getSource())) {
						inf = new ScheduleSourceInfo(ss);
						srcs.add(inf);
					}
					
					inf.addLegs(SystemData.getAirline(rs.getString(2)), rs.getInt(3));
					inf.setMaxLineNumber(Math.max(inf.getMaxLineNumber(), rs.getInt(4)));
					inf.setEffectiveDate(toInstant(rs.getTimestamp(5)));
					inf.setImportDate(toInstant(rs.getTimestamp(6)));
					inf.setAutoImport(rs.getBoolean(7));
				}
			}
			
			// Filter and add to the cache
			results = new CacheableSet<ScheduleSourceInfo>(cacheKey);
			if (isLoaded)
				srcs.stream().filter(ssi -> (ssi.getImportDate() != null)).forEach(results::add);
			else
				results.addAll(srcs);
			
			_srcCache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the mapping of Airlines to Schedule sources.
	 * @return a Map of Collections of Airlines, keyed by ScheduleSource
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<ScheduleSource, Collection<Airline>> getSourceAirlines() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT SRC, AIRLINE FROM RAW_SCHEDULE_AIRLINES")) {
			Map<ScheduleSource, Collection<Airline>> results = new TreeMap<ScheduleSource, Collection<Airline>>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleSource src = ScheduleSource.values()[rs.getInt(1)];
					Collection<Airline> airlines = results.get(src);
					if (airlines == null) {
						airlines = new TreeSet<Airline>();
						results.put(src, airlines);
					}
					
					airlines.add(SystemData.getAirline(rs.getString(2)));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads an individual raw schedule entry.
	 * @param src the ScheduleSource
	 * @param line the source line number
	 * @return a RawScheduleEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public RawScheduleEntry get(ScheduleSource src, int line) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?) AND (SRCLINE=?)")) {
			ps.setInt(1, src.ordinal());
			ps.setInt(2, line);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the Airlines that service a particular Airport in a schedule source.
	 * @param src the ScheduleSource or null for all
	 * @param a the Airport
	 * @return a Collection of Airline beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airline> getAirlines(ScheduleSource src, Airport a) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AIRLINE, COUNT(SRCLINE) AS CNT FROM RAW_SCHEDULE WHERE ((AIRPORT_D=?) OR (AIRPORT_A=?))");
		if (src != null)
			sqlBuf.append(" AND (SRC=?)");
		
		sqlBuf.append(" GROUP BY AIRLINE ORDER BY CNT DESC");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, a.getIATA());
			ps.setString(2, a.getIATA());
			if (src != null)
				ps.setInt(3, src.ordinal());
			
			Collection<Airline> results = new LinkedHashSet<Airline>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirline(rs.getString(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the days of the week an Airport has flights from a particular schedule source.
	 * @param src the ScheduleSource, or null for all
	 * @param a the Airport
	 * @param isDestination TRUE if selecting arriving flights, otherwise FALSE
	 * @return a Collection of DayOfWeek enums
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<DayOfWeek> getDays(ScheduleSource src, Airport a, boolean isDestination) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT DAYS FROM RAW_SCHEDULE WHERE (");
		sqlBuf.append(isDestination ? "AIRPORT_D" : "AIRPORT_A");
		sqlBuf.append("=?)");
		if (src != null)
			sqlBuf.append(" AND (SRC=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, a.getIATA());
			if (src != null)
				ps.setInt(2, src.ordinal());
			
			Collection<DayOfWeek> results = new TreeSet<DayOfWeek>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int bitmap = rs.getInt(1);
					for (DayOfWeek d : DayOfWeek.values()) {
						if ((bitmap & (1 << d.ordinal())) != 0)
							results.add(d);		
					}
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Lists all raw schedule entries between two Airports from a particular schedule source.
	 * @param src a ScheduleSource
	 * @param aD the departure Airport, or null for all
	 * @param aA the arrival Airport, or null for all
	 * @return a List of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> list(ScheduleSource src, Airport aD, Airport aA) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?) ");
		if (aD != null)
			sqlBuf.append("AND (AIRPORT_D=?)");
		if (aA != null)
			sqlBuf.append(" AND (AIRPORT_A=?)");
		sqlBuf.append(" ORDER BY SRCLINE");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int pos = 0;
			ps.setInt(++pos, src.ordinal());
			if (aD != null)
				ps.setString(++pos, aD.getIATA());
			if (aA != null)
				ps.setString(++pos, aA.getIATA());

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all raw schedule entries for a particular day of the week.
	 * @param src the ScheduleSource
	 * @param ld the schedule effective date, or null for all
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> load(ScheduleSource src, LocalDate ld) throws DAOException {
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?)");
		if (ld != null)
			sqlBuf.append(" AND (STARTDATE<=?) AND (ENDDATE>=?) AND ((DAYS & ?) != 0)");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, src.ordinal());
			if (ld != null) {
				ps.setTimestamp(2, Timestamp.valueOf(ld.atStartOfDay()));
				ps.setTimestamp(3, Timestamp.valueOf(ld.atTime(23, 59, 59)));
				ps.setInt(4, 1 << ld.getDayOfWeek().ordinal());
			}

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Raw Schedule result sets.
	 */
	private static List<RawScheduleEntry> execute(PreparedStatement ps) throws SQLException {
		List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(rs.getString(6)), rs.getInt(7), rs.getInt(8));
				se.setSource(ScheduleSource.values()[rs.getInt(1)]);
				se.setLineNumber(rs.getInt(2));
				se.setStartDate(rs.getDate(3).toLocalDate());
				se.setEndDate(rs.getDate(4).toLocalDate());
				se.setDayMap(rs.getInt(5));
				se.setAirportD(SystemData.getAirport(rs.getString(9)));
				se.setAirportA(SystemData.getAirport(rs.getString(10)));
				se.setEquipmentType(rs.getString(11));
				se.setTimeD(rs.getTimestamp(12).toLocalDateTime());
				se.setTimeA(rs.getTimestamp(13).toLocalDateTime());
				se.setForceInclude(rs.getBoolean(14));
				se.setAcademy(rs.getBoolean(15));
				se.setCodeShare(rs.getString(16));
				results.add(se);
			}
		}
		
		return results;
	}
}