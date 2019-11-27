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
	 * Loads all raw schedule entries for a particular day of the week.
	 * @param ld the schedule effective date
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> load(LocalDate ld) throws DAOException {
		DayOfWeek dow = ld.getDayOfWeek();
		try (PreparedStatement ps = prepare("SELECT * FROM RAW_SCHEDULE WHERE (STARTDATE<=?) AND (ENDDATE>=?) AND ((DAYS & ?) != 0)")) {
			ps.setTimestamp(1, Timestamp.valueOf(ld.atStartOfDay()));
			ps.setTimestamp(2, Timestamp.valueOf(ld.atTime(23, 59, 59)));
			ps.setInt(3, 1 << dow.ordinal());
			
			List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(rs.getString(6)), rs.getInt(7), rs.getInt(8));
					se.setSource(rs.getString(1));
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