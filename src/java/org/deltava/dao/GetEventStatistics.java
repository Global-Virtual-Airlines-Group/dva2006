// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.FlightStatus;
import org.deltava.beans.stats.EventStats;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to fetch Online Event statistics from the database.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class GetEventStatistics extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetEventStatistics(Connection c) {
		super(c);
	}

	/**
	 * Retrieves per-Event statistics.
	 * @return a List of EventStats beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EventStats> getStatistics() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT E.TITLE AS LBL, (SELECT COUNT(ES.PILOT_ID) FROM events.EVENT_SIGNUPS ES WHERE (E.ID=ES.ID)) AS SCNT, COUNT(DISTINCT P.ID) AS PCNT FROM events.EVENTS E LEFT JOIN PIREPS P ON ((P.EVENT_ID=E.ID) AND"
			+ "(P.STATUS<>?) AND (P.STATUS<>?)) WHERE (E.OWNER=?) GROUP BY E.ID ORDER BY E.STARTTIME DESC")) {
			ps.setInt(1, FlightStatus.DRAFT.ordinal());
			ps.setInt(2, FlightStatus.REJECTED.ordinal());
			ps.setString(3, SystemData.get("airline.code"));
			List<EventStats> results = new ArrayList<EventStats>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					EventStats es = new EventStats(rs.getString(1));
					es.setCount(1);
					es.setSignups(rs.getInt(2));
					es.setPilotSignups(es.getSignups());
					es.setFlights(rs.getInt(3));
					es.setPilotFlights(es.getFlights());
					results.add(es);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves monthly aggregated Event statistics.
	 * @return a List of EventStats beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EventStats> getMonthlyStatistics() throws DAOException {
		try {
		Map<String, EventStats> results = new LinkedHashMap<String, EventStats>();
			try (PreparedStatement ps = prepare("SELECT DATE_FORMAT(E.STARTTIME, '%M %Y') AS LBL, COUNT(DISTINCT E.ID) AS CNT, COUNT(ES.PILOT_ID) AS SCNT, COUNT(DISTINCT ES.PILOT_ID) AS SPCNT FROM events.EVENTS E LEFT JOIN events.EVENT_SIGNUPS ES ON "
				+ "(E.ID=ES.ID) WHERE (E.OWNER=?) GROUP BY LBL ORDER BY E.STARTTIME DESC")) {
				ps.setString(1, SystemData.get("airline.code"));			
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						EventStats es = new EventStats(rs.getString(1));
						es.setCount(rs.getInt(2));
						es.setSignups(rs.getInt(3));
						es.setPilotSignups(rs.getInt(4));
						results.put(es.getLabel(), es);
					}
				}
			}
			
			// Load PIREPs
			try (PreparedStatement ps = prepare("SELECT DATE_FORMAT(E.STARTTIME, '%M %Y') AS LBL, COUNT(P.ID) AS PCNT, COUNT(DISTINCT P.PILOT_ID) AS DPCNT FROM events.EVENTS E LEFT JOIN PIREPS P ON (E.ID=P.EVENT_ID) AND (P.STATUS<>?) AND (P.STATUS<>?) "
				+ "WHERE (E.OWNER=?) GROUP BY LBL ORDER BY E.STARTTIME DESC")) {
				ps.setInt(1, FlightStatus.DRAFT.ordinal());
				ps.setInt(2, FlightStatus.REJECTED.ordinal());
				ps.setString(3, SystemData.get("airline.code"));
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						EventStats es = results.get(rs.getString(1));
						if (es != null) {
							es.setFlights(rs.getInt(2));
							es.setPilotFlights(rs.getInt(3));
						}
					}
				}
			}

			return new ArrayList<EventStats>(results.values());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}