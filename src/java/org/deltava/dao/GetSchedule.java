// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Flight;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to search the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSchedule extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSchedule(Connection c) {
		super(c);
	}

	/**
	 * Searches the Schedule database for flights matching particular criteria.
	 * @param criteria the search criteria. Null properties are ignored
	 * @param sortBy orderBy column
	 * @return a List of Flights
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ScheduleEntry> search(ScheduleSearchCriteria criteria, String sortBy) throws DAOException {

		// Build the conditions
		final Collection<String> conditions = new LinkedHashSet<String>();
		final List<String> params = new ArrayList<String>();
		
		// Add airline
		if (criteria.getAirline() != null) {
			conditions.add("AIRLINE=?");
			params.add(criteria.getAirline().getCode());
		}
			
		// Add flight number
		if (criteria.getFlightNumber() != 0) {
			conditions.add("FLIGHT=?");
			params.add(String.valueOf(criteria.getFlightNumber()));
		}
			
		// Add leg
		if (criteria.getLeg() != 0) {
			conditions.add("LEG=?");
			params.add(String.valueOf(criteria.getLeg()));
		}
		
		// Add departure airport
		if (criteria.getAirportD() != null) {
			conditions.add("AIRPORT_D=?");
			params.add(criteria.getAirportD().getIATA());
		}
		
		// Add arrival airport
		if (criteria.getAirportA() != null) {
			conditions.add("AIRPORT_A=?");
			params.add(criteria.getAirportA().getIATA());
		}
			
		// Set distance criteria +/- 150 miles
		if (criteria.getDistance() != 0) {
			conditions.add("DISTANCE >= ?");
			conditions.add("DISTANCE <= ?");
			params.add(String.valueOf(criteria.getDistance() - 150));
			params.add(String.valueOf(criteria.getDistance() + 150));
		}

		// Set flight time criteria +/- 1 hour
		if (criteria.getLength() != 0) {
			conditions.add("FLIGHT_TIME >= ?");
			conditions.add("FLIGHT_TIME <= ?");
			params.add(String.valueOf((criteria.getLength() / 10.0) - 1));
			params.add(String.valueOf((criteria.getLength() / 10.0) + 1));
		}

		// Set departure/arrival time criteria +/- 2 hours
		if (criteria.getHourD() != -1) {
			conditions.add("TIME_D >= ?");
			conditions.add("TIME_D <= ?");
			params.add(StringUtils.format(criteria.getHourD() - 1, "00") + ":00\'");
			params.add(StringUtils.format(criteria.getHourD() + 1, "00") + ":00\'");
		}

		if (criteria.getHourA() != -1) {
			conditions.add("TIME_A >= ?");
			conditions.add("TIME_A <= ?");
			params.add(StringUtils.format(criteria.getHourA() - 1, "00") + ":00\'");
			params.add(StringUtils.format(criteria.getHourA() + 1, "00") + ":00\'");
		}
		
		if (!criteria.getIncludeAcademy()) {
			conditions.add("ACADEMY=?");
			params.add("0");
		}

		// Build the query string
		StringBuilder buf = new StringBuilder("SELECT * FROM ");
		buf.append(formatDBName(criteria.getDBName()));
		buf.append(".SCHEDULE WHERE ");
		for (Iterator<String> i = conditions.iterator(); i.hasNext();) {
			buf.append('(');
			buf.append(i.next());
			buf.append(')');
			if (i.hasNext())
				buf.append(" AND ");
		}
		
		// Build the equipment type query
		if (!CollectionUtils.isEmpty(criteria.getEquipmentTypes())) {
			if (!conditions.isEmpty())
				buf.append(" AND (");
			
			for (Iterator<String> i = criteria.getEquipmentTypes().iterator(); i.hasNext(); ) {
				String eqType = i.next();
				buf.append("(EQTYPE=?)");
				params.add(eqType);
				if (i.hasNext())
					buf.append(" OR ");
			}

			if (!conditions.isEmpty())
				buf.append(')');
		}
		
		// Add sort column
		buf.append(" ORDER BY ");
		buf.append(sortBy);

		// Prepare the satement and execute the query
		try {
			prepareStatement(buf.toString());
			for (int x = 1; x <= params.size(); x++)
				_ps.setString(x, params.get(x - 1));
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Return a particular flight from the Schedule database.
	 * @param f the Flight to return, using the airline code, flight number and leg
	 * @param dbName the database name
	 * @return a ScheduleEntry matching the criteria, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if f is null
	 */
	public ScheduleEntry get(Flight f, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?) AND (LEG=?)");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, f.getAirline().getCode());
			_ps.setInt(2, f.getFlightNumber());
			_ps.setInt(3, f.getLeg());

			// Execute the query, return null if not found
			List<ScheduleEntry> results = execute();
			return (results.size() == 0) ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Return a particular flight from the Schedule database.
	 * @param f the Flight to return, using the airline code, flight number and leg
	 * @return a ScheduleEntry matching the criteria, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if f is null
	 */
	public ScheduleEntry get(Flight f) throws DAOException {
		return get(f, SystemData.get("airline.db"));
	}

	/**
	 * Returns all flights from a particular airport, sorted by Airline and Flight Number.
	 * @param a the origin Airport bean
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleEntry> getFlights(Airport a) throws DAOException {
		try {
			prepareStatement("SELECT * FROM SCHEDULE WHERE (AIRPORT_D=?) ORDER BY AIRLINE, FLIGHT, LEG");
			_ps.setString(1, a.getIATA());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Return a particular flight from the Schedule database.
	 * @param aCode the Airline Code
	 * @param flightNumber the Flight Number
	 * @param leg the Leg
	 * @return a ScheduleEntry matching the criteria, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ScheduleEntry get(String aCode, int flightNumber, int leg) throws DAOException {
		return get(new ScheduleEntry(new Airline(aCode), flightNumber, leg), SystemData.get("airline.db"));
	}

	/**
	 * Returns the average flight time for all flights in the Schedule database between two airports.
	 * @param airportD the origin Airport
	 * @param airportA the destination Airport
	 * @return the average time between the two airports in hours <i>multiplied by 10</i>, or 0 if no flights found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public int getFlightTime(Airport airportD, Airport airportA, String dbName) throws DAOException {

		// Build the prepared statement
		StringBuilder sqlBuf = new StringBuilder("SELECT IFNULL(ROUND(AVG(FLIGHT_TIME)), 0) FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".SCHEDULE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?) AND (ACADEMY=?)");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, airportD.getIATA());
			_ps.setString(2, airportA.getIATA());
			_ps.setBoolean(3, false);

			// Execute the Query
			ResultSet rs = _ps.executeQuery();
			int result = rs.next() ? rs.getInt(1) : 0;

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the average flight time for all flights in the Schedule database between two airports.
	 * @param airportD the origin airport IATA code
	 * @param airportA the destination airport IATA code
	 * @return the average time between the two airports in hours <i>multiplied by 10</i>, or 0 if no flights found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public int getFlightTime(Airport airportD, Airport airportA) throws DAOException {
		return getFlightTime(airportD, airportA, SystemData.get("airline.db"));
	}

	/**
	 * Exports the entire Flight Schedule.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleEntry> export() throws DAOException {
		try {
			prepareStatement("SELECT * FROM SCHEDULE ORDER BY AIRLINE, FLIGHT, LEG");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the size of the Flight Schedule.
	 * @return the number of legs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getFlightCount() throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM SCHEDULE");

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			int result = rs.next() ? rs.getInt(1) : 0;

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to query the database.
	 */
	private List<ScheduleEntry> execute() throws SQLException {

		// Execute the query
		List<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			ScheduleEntry entry = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
			entry.setAirportD(SystemData.getAirport(rs.getString(4)));
			entry.setAirportA(SystemData.getAirport(rs.getString(5)));
			entry.setEquipmentType(rs.getString(7));
			entry.setLength(rs.getInt(8));
			entry.setTimeD(rs.getTimestamp(9));
			entry.setTimeA(rs.getTimestamp(10));
			entry.setHistoric(rs.getBoolean(11));
			entry.setCanPurge(rs.getBoolean(12));
			entry.setAcademy(rs.getBoolean(13));

			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}