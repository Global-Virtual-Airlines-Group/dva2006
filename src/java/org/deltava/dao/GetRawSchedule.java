// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 8.0
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
	
	private static DayOfWeek getDayOfWeek(Instant dt) {
		return ZonedDateTime.ofInstant(dt.truncatedTo(ChronoUnit.DAYS), ZoneOffset.UTC).getDayOfWeek();
	}

	/**
	 * Loads all raw schedule entries for a particular day of the week.
	 * @param dt the schedule effective date
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<RawScheduleEntry> load(Instant dt) throws DAOException {
		Instant tdt = dt.truncatedTo(ChronoUnit.DAYS);
		try {
			prepareStatement("SELECT * FROM RAW_SCHEDULE WHERE ((DAYS & ?) > 0)");
			_ps.setInt(1, (1 << getDayOfWeek(dt).ordinal()));
			
			List<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					RawScheduleEntry se = new RawScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), 1);
					se.setAirportD(SystemData.getAirport(rs.getString(3)));
					se.setAirportA(SystemData.getAirport(rs.getString(4)));
					se.setEquipmentType(rs.getString(5));
					se.setTimeD(rs.getTimestamp(6).toLocalDateTime().plusSeconds(tdt.getEpochSecond()));
					se.setTimeA(rs.getTimestamp(7).toLocalDateTime().plusSeconds(tdt.getEpochSecond()));
					se.setCodeShare(rs.getString(9));
					int rawDays = rs.getInt(8);
					for (DayOfWeek dow : DayOfWeek.values()) {
						if ((rawDays & (1 << dow.ordinal())) > 0)
							se.addDay(dow);
					}

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
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM RAW_SCHEDULE WHERE ((DAYS & ?) > 0)");
			_ps.setInt(1, (1 << getDayOfWeek(dt).ordinal()));
			
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

	/**
	 * Loads all schedule provider tail code data.
	 * @return a map of TailCode beans, keyed by registration code
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, TailCode> getTailCodes() throws DAOException {
		try {
			prepareStatement("SELECT TAILCODE, ICAO FROM RAW_SCHEDULE_AC");
			Map<String, TailCode> results = new TreeMap<String, TailCode>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					TailCode tc = new TailCode(rs.getString(1), rs.getString(2));
					results.put(tc.getTailCode(), tc);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}