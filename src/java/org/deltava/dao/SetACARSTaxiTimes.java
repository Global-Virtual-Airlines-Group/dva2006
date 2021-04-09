// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.flight.FlightStatus;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write ACARS taxi times to the database.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class SetACARSTaxiTimes extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSTaxiTimes(Connection c) {
		super(c);
	}

	/**
	 * Calculates average inbound and outbound taxi times for a given Airport in the current year.
	 * @param a the Airport
	 * @throws DAOException if a JDBC error occurs
	 */
	public void calculate(Airport a) throws DAOException {
		try {
			startTransaction();
			int avgTO = 0; int cntTO = 0;
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(AP.ACARS_ID) AS CNT, AVG(TIMESTAMPDIFF(SECOND, TAXI_TIME, TAKEOFF_TIME)) AS TX_TKO FROM ACARS_PIREPS AP, PIREPS P WHERE (AP.ID=P.ID) AND "
				+"(P.DATE > DATE_SUB(CURDATE(), INTERVAL DAYOFYEAR(CURDATE()) DAY)) AND (AP.TAXI_TIME < AP.TAKEOFF_TIME) AND (AP.TAKEOFF_TIME < DATE_ADD(AP.TAXI_TIME, INTERVAL 2 HOUR)) AND (P.AIRPORT_D=?) AND (P.STATUS=?)")) {
				ps.setString(1, a.getIATA());
				ps.setInt(2, FlightStatus.OK.ordinal());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						cntTO = rs.getInt(1);
						avgTO = rs.getInt(2);
					}
				}
			}
			
			int avgTI = 0; int cntTI = 0;
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(AP.ACARS_ID) AS CNT, AVG(TIMESTAMPDIFF(SECOND, LANDING_TIME, END_TIME)) AS TX_LND FROM ACARS_PIREPS AP, PIREPS P WHERE (AP.ID=P.ID) AND "
				+"(P.DATE > DATE_SUB(CURDATE(), INTERVAL DAYOFYEAR(CURDATE()) DAY)) AND (AP.LANDING_TIME < AP.END_TIME) AND (AP.END_TIME < DATE_ADD(AP.LANDING_TIME, INTERVAL 2 HOUR)) AND (P.AIRPORT_A=?) AND (P.STATUS=?)")) {
				ps.setString(1, a.getIATA());
				ps.setInt(2, FlightStatus.OK.ordinal());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						cntTI = rs.getInt(1);
						avgTI = rs.getInt(2);
					}
				}
			}
			
			// Write average and totals
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.TAXI_TIMES (ICAO, APPCODE, YEAR, TAXI_IN, TAXI_OUT, TOTAL_IN, TOTAL_OUT) VALUES (?, ?, YEAR(CURDATE()), ?, ?, ?, ?)")) {
				ps.setString(1, a.getICAO());
				ps.setString(2, SystemData.get("airline.code"));
				ps.setInt(3, avgTI);
				ps.setInt(4, avgTO);
				ps.setInt(5, cntTI);
				ps.setInt(6, cntTO);
				executeUpdate(ps, 1);
			}

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} 
	}
}