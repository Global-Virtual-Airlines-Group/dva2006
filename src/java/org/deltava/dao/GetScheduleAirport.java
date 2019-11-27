// Copyright 2005, 2006, 2011, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Schedule route data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetScheduleAirport extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleAirport(Connection c) {
		super(c);
	}

	/**
	 * Returns all Airports for an Airline with at least one flight departing.
	 * @param al the Airline bean or null
	 * @return a Collection of Airport beans, sorted by airport name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getOriginAirports(Airline al) throws DAOException {
		return getAirports("AIRPORT_D", al);
	}
	
	/**
	 * Returns all Airports for an Airline with at least one flight arriving.
	 * @param al the Airline bean or null
	 * @return a Collection of Airport beans, sorted by airport name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getDestinationAirports(Airline al) throws DAOException {
		return getAirports("AIRPORT_A", al);
	}
	
	/**
	 * Returns departing flight statistics for a particular Airport.
	 * @param a the Airport
	 * @return a Collection of ScheduleStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleStatsEntry> getDepartureStatistics(Airport a) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT HOUR(S.TIME_D) AS HR, COUNT(S.FLIGHT) AS CNT, SUM(IF(DA.COUNTRY=AA.COUNTRY,1,0)) AS DOMESTIC FROM SCHEDULE S LEFT JOIN "
				+ "common.AIRPORTS DA ON (S.AIRPORT_D=DA.IATA) LEFT JOIN common.AIRPORTS AA ON (S.AIRPORT_A=AA.IATA) WHERE (S.AIRPORT_D=?) GROUP BY HR ORDER BY HR")) {
			ps.setString(1, a.getIATA());
			
			Collection<ScheduleStatsEntry> results = new ArrayList<ScheduleStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleStatsEntry entry = new ScheduleStatsEntry(rs.getInt(1));
					int total = rs.getInt(2); int domestic = rs.getInt(3);
					entry.setDepartureLegs(domestic, total - domestic);
					results.add(entry);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns arriving flight statistics for a particular Airport.
	 * @param a the Airport
	 * @return a Collection of ScheduleStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleStatsEntry> getArrivalStatistics(Airport a) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT HOUR(S.TIME_A) AS HR, COUNT(S.FLIGHT) AS CNT, SUM(IF(DA.COUNTRY=AA.COUNTRY,1,0)) AS DOMESTIC FROM SCHEDULE S LEFT JOIN "
				+ "common.AIRPORTS DA ON (S.AIRPORT_D=DA.IATA) LEFT JOIN common.AIRPORTS AA ON (S.AIRPORT_A=AA.IATA) WHERE (S.AIRPORT_A=?) GROUP BY HR ORDER BY HR")) {
			ps.setString(1, a.getIATA());
			
			Collection<ScheduleStatsEntry> results = new ArrayList<ScheduleStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleStatsEntry entry = new ScheduleStatsEntry(rs.getInt(1));
					int total = rs.getInt(2); int domestic = rs.getInt(3);
					entry.setArrivalLegs(domestic, total - domestic);
					results.add(entry);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Returns origin/departure airports for an airline.
	 */
	private Collection<Airport> getAirports(String fName, Airline al) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT S.");
		sqlBuf.append(fName);
		sqlBuf.append(" FROM SCHEDULE S, common.AIRPORTS A WHERE (S.");
		sqlBuf.append(fName);
		sqlBuf.append("=A.IATA)");
		if (al != null)
			sqlBuf.append(" AND (S.AIRLINE=?)");
		
		sqlBuf.append(" ORDER BY A.NAME");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (al != null)
				ps.setString(1, al.getCode());

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Airports with flights departing or arriving at a particular Airport for a particular Airline.
	 * @param a the Airport bean
	 * @param from TRUE if returning destination airports for flights originating at a, otherwise FALSE
	 * @param al the Airline bean
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getConnectingAirports(Airport a, boolean from, Airline al) throws DAOException {
		if (a == null)
			return from ? getDestinationAirports(al) : getOriginAirports(al);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT S.");
		sqlBuf.append(from ? "AIRPORT_A" : "AIRPORT_D");
		sqlBuf.append(" AS CODE FROM SCHEDULE S, common.AIRPORTS A WHERE (S.");
		sqlBuf.append(from ? "AIRPORT_A" : "AIRPORT_D");
		sqlBuf.append("=A.IATA) AND (S.");
		sqlBuf.append(from ? "AIRPORT_D" : "AIRPORT_A");
		sqlBuf.append("=?)");
		if (al != null)
			sqlBuf.append(" AND (S.AIRLINE=?)");
		
		sqlBuf.append(" ORDER BY A.NAME");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, a.getIATA());
			if (al != null)
				ps.setString(2, al.getCode());

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse Airport result sets.
	 */
	private static List<Airport> execute(PreparedStatement ps) throws SQLException {
		List<Airport> results = new ArrayList<Airport>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Airport ap = SystemData.getAirport(rs.getString(1));
				if (ap != null)
					results.add(ap);
			}
		}
		
		return results;
	}
}