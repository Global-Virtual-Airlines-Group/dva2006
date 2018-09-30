// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load ACARS on-time data from the database.  
 * @author Luke
 * @version 8.4
 * @since 8.4
 */

public class GetACARSOnTime extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSOnTime(Connection c) {
		super(c);
	}

	/**
	 * Loads on-time data. The flight report will have its departure and arrival times set.
	 * @param afr the ACARSFlightReport
	 * @return a ScheduleEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ScheduleEntry getOnTime(ACARSFlightReport afr) throws DAOException {
		if (afr.getOnTime() == OnTime.UNKNOWN) return null;
		
		try {
			prepareStatementWithoutLimits("SELECT AIRLINE, FLIGHT, LEG, TIME_D, TIME_A, ATIME_D, ATIME_A FROM ACARS_ONTIME WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, afr.getID());
			
			ScheduleEntry se = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					se = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
					se.setAirportD(afr.getAirportD()); se.setAirportA(afr.getAirportA());
					se.setTimeD(rs.getTimestamp(4).toLocalDateTime());
					se.setTimeA(rs.getTimestamp(5).toLocalDateTime());
					afr.setDepartureTime(toInstant(rs.getTimestamp(6)));
					afr.setArrivalTime(toInstant(rs.getTimestamp(7)));
				}
			}
			
			_ps.close();
			return se;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}