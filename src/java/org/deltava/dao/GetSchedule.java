// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 4.1
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
	 * @return a List of Flights
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ScheduleEntry> search(ScheduleSearchCriteria criteria) throws DAOException {

		// Build the conditions
		final Collection<String> conditions = new LinkedHashSet<String>();
		final List<String> params = new ArrayList<String>();
		if (criteria.getDispatchOnly())
			criteria.setCheckDispatchRoutes(true);
		
		// Add airline
		if (criteria.getAirline() != null) {
			conditions.add("S.AIRLINE=?");
			params.add(criteria.getAirline().getCode());
		}
			
		// Add flight number
		if (criteria.getFlightNumber() != 0) {
			conditions.add("S.FLIGHT=?");
			params.add(String.valueOf(criteria.getFlightNumber()));
		}
			
		// Add leg
		if (criteria.getLeg() != 0) {
			conditions.add("S.LEG=?");
			params.add(String.valueOf(criteria.getLeg()));
		}
		
		// Add departure airport
		if (criteria.getAirportD() != null) {
			conditions.add("S.AIRPORT_D=?");
			params.add(criteria.getAirportD().getIATA());
		}
		
		// Add arrival airport
		if (criteria.getAirportA() != null) {
			conditions.add("S.AIRPORT_A=?");
			params.add(criteria.getAirportA().getIATA());
		}
			
		// Set distance criteria +/- 150 miles
		if (criteria.getDistance() != 0) {
			conditions.add("S.DISTANCE >= ?");
			conditions.add("S.DISTANCE <= ?");
			params.add(String.valueOf(criteria.getDistance() - criteria.getDistanceRange()));
			params.add(String.valueOf(criteria.getDistance() + criteria.getDistanceRange()));
		}

		// Set flight time criteria +/- 1 hour
		if (criteria.getLength() != 0) {
			conditions.add("S.FLIGHT_TIME >= ?");
			conditions.add("S.FLIGHT_TIME <= ?");
			params.add(String.valueOf((criteria.getLength() / 10.0) - 1));
			params.add(String.valueOf((criteria.getLength() / 10.0) + 1));
		}

		// Set departure/arrival time criteria +/- 2 hours
		if (criteria.getHourD() != -1) {
			conditions.add("S.TIME_D >= ?");
			conditions.add("S.TIME_D <= ?");
			params.add(StringUtils.format(criteria.getHourD() - 1, "00") + ":00\'");
			params.add(StringUtils.format(criteria.getHourD() + 1, "00") + ":00\'");
		}

		if (criteria.getHourA() != -1) {
			conditions.add("S.TIME_A >= ?");
			conditions.add("S.TIME_A <= ?");
			params.add(StringUtils.format(criteria.getHourA() - 1, "00") + ":00\'");
			params.add(StringUtils.format(criteria.getHourA() + 1, "00") + ":00\'");
		}
		
		// Check whether to include Flight Academy flights
		if (!criteria.getIncludeAcademy()) {
			conditions.add("S.ACADEMY=?");
			params.add("0");
		}
		
		// Build the query string
		StringBuilder buf = new StringBuilder("SELECT S.*");
		if (criteria.getCheckDispatch())
			buf.append(", COUNT(R.ID) AS RCNT");
		buf.append(" FROM ");
		buf.append(formatDBName(criteria.getDBName()));
		buf.append(".SCHEDULE S");
		if (criteria.getCheckDispatch())
			buf.append(" LEFT JOIN acars.ROUTES R ON ((S.AIRPORT_D=R.AIRPORT_D) AND (S.AIRPORT_A=R.AIRPORT_A) AND (R.ACTIVE=1))");
		buf.append(" WHERE ");
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
				buf.append("(S.EQTYPE=?)");
				params.add(eqType);
				if (i.hasNext())
					buf.append(" OR ");
			}

			if (!conditions.isEmpty())
				buf.append(')');
		}
		
		// Add sort column
		if (criteria.getCheckDispatch())
			buf.append(" GROUP BY S.AIRLINE, S.FLIGHT, S.LEG");
		if (criteria.getDispatchOnly())
			buf.append(" HAVING (RCNT>0)");
		buf.append(" ORDER BY ");
		buf.append(criteria.getSortBy());

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
		sqlBuf.append(".SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?)");
		if (f.getLeg() != 0)
			sqlBuf.append("AND (LEG=?)");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, f.getAirline().getCode());
			_ps.setInt(2, f.getFlightNumber());
			if (f.getLeg() != 0)
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
		return getFlights(a, null);
	}
	
	/**
	 * Returns all flights from a particular airport with a particular airline, sorted by Airline and Flight Number.
	 * @param a the origin Airport bean
	 * @param al the Airline bean
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleEntry> getFlights(Airport a, Airline al) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM SCHEDULE WHERE (AIRPORT_D=?) ");
		if (al != null)
			sqlBuf.append("AND (AIRLINE=?)");
		sqlBuf.append("ORDER BY AIRLINE, FLIGHT, LEG");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, a.getIATA());
			if (al != null)
				_ps.setString(2, al.getCode());
			
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
	 * Returns the Airlines that provide service on a particular route.
	 * @param airportD the origin Airport
	 * @param airportA the destination Airport
	 * @return a Collection of Airline beans
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public Collection<Airline> getAirlines(Airport airportD, Airport airportA) throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT AIRLINE FROM SCHEDULE WHERE (AIRPORT_D=?) AND "
					+ "(AIRPORT_A=?) AND (ACADEMY=?)");
			_ps.setString(1, airportD.getIATA());
			_ps.setString(2, airportA.getIATA());
			_ps.setBoolean(3, false);
			
			// Execute the query
			Collection<Airline> results = new TreeSet<Airline>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(SystemData.getAirline(rs.getString(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Airports only serviced by Flight Academy flights.
	 * @return a Collection of Airports
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getAcademyAirports() throws DAOException {
		try {
			Collection<Airport> results = new TreeSet<Airport>();
			
			// Select departure airports
			prepareStatementWithoutLimits("SELECT AIRPORT_D, SUM(1) AS CNT, SUM(ACADEMY) AS FACNT FROM SCHEDULE GROUP BY AIRPORT_D HAVING (CNT=FACNT)");
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(SystemData.getAirport(rs.getString(1)));
			
			rs.close();
			_ps.close();
			
			// Arrivate departure airports
			prepareStatementWithoutLimits("SELECT AIRPORT_A, SUM(1) AS CNT, SUM(ACADEMY) AS FACNT FROM SCHEDULE GROUP BY AIRPORT_A HAVING (CNT=FACNT)");
			rs = _ps.executeQuery();
			while (rs.next())
				results.add(SystemData.getAirport(rs.getString(1)));
			
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
	 * @param airportD the origin Airport
	 * @param airportA the destination Airport
	 * @return the average time between the two airports in hours <i>multiplied by 10</i>, or 0 if no flights found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public int getFlightTime(Airport airportD, Airport airportA) throws DAOException {
		return getFlightTime(airportD, airportA, SystemData.get("airline.db"));
	}
	
	/**
	 * Returns the lowest flight/leg number between two airports. 
	 * @param airportD the origin Airport
	 * @param airportA the destination Airport
	 * @param dbName the database name
	 * @return a ScheduleEntry bean with the Airline, Flight and Leg number or null if none found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public ScheduleEntry getFlightNumber(Airport airportD, Airport airportA, String dbName) throws DAOException {
		
		// Build the SQL Statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT S.AIRLINE, S.FLIGHT, S.LEG, S.EQTYPE, AI.CODE FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".SCHEDULE S, common.AIRLINEINFO AI WHERE (AI.DBNAME=?) AND (S.AIRPORT_D=?) "
			+ "AND (S.AIRPORT_A=?) AND (S.ACADEMY=?) ORDER BY IF (S.AIRLINE=AI.CODE, 0, 1), FLIGHT LIMIT 1");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, db);
			_ps.setString(2, airportD.getIATA());
			_ps.setString(3, airportA.getIATA());
			_ps.setBoolean(4, false);
			
			// Execute the query
			ScheduleEntry f = null;
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				f = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
				f.setEquipmentType(rs.getString(4));
				f.setAirportD(airportD);
				f.setAirportA(airportA);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return f;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the lowest flight/leg number between two airports. 
	 * @param airportD the origin Airport
	 * @param airportA the destination Airport
	 * @return a Flight bean with the Airline, Flight and Leg number
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public Flight getFlightNumber(Airport airportD, Airport airportA) throws DAOException {
		return getFlightNumber(airportD, airportA, SystemData.get("airline.db"));
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
	 * Helper method to query the database.
	 */
	private List<ScheduleEntry> execute() throws SQLException {

		// Execute the query
		List<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		ResultSet rs = _ps.executeQuery();
		boolean hasDispatch = (rs.getMetaData().getColumnCount() > 13);
		while (rs.next()) {
			ScheduleEntry entry = null;
			if (hasDispatch) {
				ScheduleSearchEntry sse = new ScheduleSearchEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
				sse.setDispatchRoutes(rs.getInt(14));
				entry = sse;
			} else
				entry = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
			
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