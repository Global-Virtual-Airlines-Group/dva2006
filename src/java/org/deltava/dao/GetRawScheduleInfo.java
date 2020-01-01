// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load raw flight schedule information.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */
public class GetRawScheduleInfo extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetRawScheduleInfo(Connection c) {
		super(c);
	}

	/**
	 * Returns the first available &quot;line number&quot; for manually entered raw schedule entries.
	 * @return the next line number
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getNextManualEntryLine() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT MAX(SRC_LINE) WHERE (SRC=?)")) {
			ps.setInt(1, ScheduleSource.MANUAL.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) + 1 : 1;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the next available Leg number for a Flight.
	 * @param a the Airline bean
	 * @param flightNumber the flight number
	 * @return the next available leg number
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getNextLeg(Airline a, int flightNumber) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT MAX(LEG) FROM RAW_SCHEDULE WHERE (SRC=?) AND (AIRLINE=?) AND (FLIGHT=?)")) {
			ps.setInt(1, ScheduleSource.MANUAL.ordinal());
			ps.setString(2, a.getCode());
			ps.setInt(3, flightNumber);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) + 1 : 1;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns departure Airports for a particular raw schedule source.
	 * @param src the ScheduleSource
	 * @param aA the arrival Airport, or null for all flights
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getOriginAirports(ScheduleSource src, Airport aA) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AIRPORT_D FROM RAW_SCHEDULE WHERE (SRC=?)");
		if (aA != null)
			sqlBuf.append(" AND (AIRPORT_A=?)");
		sqlBuf.append(" ORDER BY AIRPORT_D");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, src.ordinal());
			if (aA != null)
				ps.setString(2, aA.getIATA());
			
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirport(rs.getString(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns arrival Airports for a particular raw schedule source.
	 * @param src the ScheduleSource
	 * @param aD the departure Airport, or null for all flights
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getArrivalAirports(ScheduleSource src, Airport aD) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AIRPORT_A FROM RAW_SCHEDULE WHERE (SRC=?)");
		if (aD != null)
			sqlBuf.append(" AND (AIRPORT_D=?)");
		sqlBuf.append(" ORDER BY AIRPORT_A");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, src.ordinal());
			if (aD != null)
				ps.setString(2, aD.getIATA());
			
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirport(rs.getString(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all flight numbers in a particular range.
	 * @param a the Airline bean
	 * @param start the start of the range, or zero if none specified
	 * @param end the end of the range, or zero if none specified
	 * @return a Collection of Integers with flight numbers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getFlightNumbers(Airline a, int start, int end) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT FLIGHT FROM RAW_SCHEDULE WHERE (SRC=?) AND (AIRLINE=?)");
		Collection<String> params = new ArrayList<String>();
		if (start > 0)
			params.add("(FLIGHT > ?)");
		if (end > 0)
			params.add("(FLIGHT < ?)");

		// Add parameters
		if (!params.isEmpty()) {
			for (Iterator<String> i = params.iterator(); i.hasNext();) {
				String p = i.next();
				sqlBuf.append(" AND ");
				sqlBuf.append(p);
			}
		}

		sqlBuf.append(" ORDER BY FLIGHT");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, ScheduleSource.MANUAL.ordinal());
			ps.setString(2, a.getCode());
			if (start > 0) ps.setInt(3, start);
			if (end > 0) ps.setInt(4, end);
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}