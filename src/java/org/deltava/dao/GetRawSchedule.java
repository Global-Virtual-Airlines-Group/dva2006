// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.time.temporal.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load raw schedule entries and tail codes.
 * @author Luke
 * @version 8.3
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
	 * @param dt the schedule effective date
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> load(Instant dt) throws DAOException {
		DayOfWeek dow = ZonedDateTime.ofInstant(dt.truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC).getDayOfWeek();
		try {
			prepareStatement("SELECT * FROM RAW_SCHEDULE WHERE (DAY=?)");
			_ps.setInt(1, dow.ordinal());
			
			List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(rs.getString(2)), rs.getInt(3), rs.getInt(4));
					se.setAirportD(SystemData.getAirport(rs.getString(5)));
					se.setAirportA(SystemData.getAirport(rs.getString(6)));
					se.setEquipmentType(rs.getString(7));
					se.setTimeD(rs.getTimestamp(8).toLocalDateTime());
					se.setTimeA(rs.getTimestamp(9).toLocalDateTime());
					se.setCodeShare(rs.getString(10));
					se.setDay(dow);
					results.add(se);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns whether there are any raw schedule entries for a particular day of the week.
	 * @param dt the schedule effective date
	 * @return TRUE if at least one raw schedule entry is present, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean hasEntries(Instant dt) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM RAW_SCHEDULE WHERE (DAY=?)");
			_ps.setInt(1, ZonedDateTime.ofInstant(dt.truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC).getDayOfWeek().ordinal());
			
			boolean hasEntries = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					hasEntries = (rs.getInt(1) > 0);
			}

			_ps.close();
			return hasEntries;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}