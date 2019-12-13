// Copyright 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;

import org.deltava.beans.schedule.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load raw schedule entries and tail codes.
 * @author Luke
 * @version 9.0
 * @since 8.0
 */

public class GetRawSchedule extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetRawSchedule(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all raw schedule sources.
	 * @return a Collection of ScheduleSourceInfo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleSourceInfo> getSources() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SRC, COUNT(*) AS TOTAL FROM RAW_SCHEDULE GROUP BY SRC")) {
			Collection<ScheduleSourceInfo> results = new LinkedHashSet<ScheduleSourceInfo>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(new ScheduleSourceInfo(ScheduleSource.valueOf(rs.getString(1)), rs.getInt(2)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all raw schedule entries for a particular day of the week.
	 * @param src the ScheduleSource
	 * @param ld the schedule effective date
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> load(ScheduleSource src, LocalDate ld) throws DAOException {
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RAW_SCHEDULE WHERE (SRC=?)");
		if (ld != null)
			sqlBuf.append(" AND (STARTDATE<=?) AND (ENDDATE>=?) AND ((DAYS & ?) != 0))");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, src.ordinal());
			if (ld != null) {
				ps.setTimestamp(2, Timestamp.valueOf(ld.atStartOfDay()));
				ps.setTimestamp(3, Timestamp.valueOf(ld.atTime(23, 59, 59)));
				ps.setInt(4, 1 << ld.getDayOfWeek().ordinal());
			}
			
			List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(rs.getString(6)), rs.getInt(7), rs.getInt(8));
					se.setSource(ScheduleSource.valueOf(rs.getString(1)));
					se.setLineNumber(rs.getInt(2));
					se.setStartDate(rs.getDate(3).toLocalDate());
					se.setEndDate(rs.getDate(4).toLocalDate());
					se.setAirportD(SystemData.getAirport(rs.getString(9)));
					se.setAirportA(SystemData.getAirport(rs.getString(10)));
					se.setEquipmentType(rs.getString(11));
					se.setTimeD(rs.getTimestamp(12).toLocalDateTime());
					se.setTimeA(rs.getTimestamp(13).toLocalDateTime());
					se.setCodeShare(rs.getString(14));
					se.setDayMap(rs.getInt(5));
					results.add(se);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}