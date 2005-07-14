// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Flight;
import org.deltava.beans.schedule.*;

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
	 * @param isRandom sort the results at random
	 * @return a List of Flights
	 * @throws DAOException if a JDBC error occurs
	 */
	public List search(Flight criteria, boolean isRandom) throws DAOException {
		
		// Build the where clause
		List conditions = new ArrayList();
		if (criteria.getAirline() != null) conditions.add("AIRLINE=\'" + criteria.getAirline().getCode() + "\'");
		if (criteria.getEquipmentType() != null) conditions.add("EQTYPE=\'" + criteria.getEquipmentType() + "\'");
		if (criteria.getFlightNumber() != 0) conditions.add("FLIGHT=" + criteria.getFlightNumber());
		if (criteria.getLeg() != 0) conditions.add("LEG=" + criteria.getLeg());
		if (criteria.getAirportD() != null) conditions.add("AIRPORT_D=\'" + criteria.getAirportD().getIATA() + "\'"); 
		if (criteria.getAirportA() != null) conditions.add("AIRPORT_A=\'" + criteria.getAirportA().getIATA() + "\'");
		
		// Set distance criteria +/- 150 miles
		if (criteria.getDistance() != 0) {
			conditions.add("DISTANCE >= " + String.valueOf(criteria.getDistance() - 150));
			conditions.add("DISTANCE <= " + String.valueOf(criteria.getDistance() + 150));
		}
		
		// Set flight time criteria +/- 1 hour
		if (criteria.getLength() != 0) {
			conditions.add("FLIGHT_TIME >= " + String.valueOf((criteria.getLength() / 10.0) - 1));
			conditions.add("FLIGHT_TIME <= " + String.valueOf((criteria.getLength() / 10.0) + 1));
		}
			
		// Build the query string
		StringBuffer buf = new StringBuffer("SELECT * FROM SCHEDULE WHERE ");
		for (Iterator i = conditions.iterator(); i.hasNext(); ) {
			buf.append('(');
			buf.append((String) i.next());
			buf.append(')');
			if (i.hasNext())
				buf.append(" AND ");
		}
		
		// Add randomness quotient
		if (isRandom)
		    buf.append(" ORDER BY RAND()");
		
		// Prepare the satement and execute the query
		try {
			prepareStatement(buf.toString());
			return execute();
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

		// Init the prepared statement
		try {
			prepareStatement("SELECT * FROM SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?) AND (LEG=?)");
			_ps.setString(1, f.getAirline().getCode());
			_ps.setInt(2, f.getFlightNumber());
			_ps.setInt(3, f.getLeg());
			
			// Execute the query, return null if not found
			List results = execute();
			return (results.size() == 0) ? null : (ScheduleEntry) results.get(0);
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
		return get(new ScheduleEntry(new Airline(aCode), flightNumber, leg));
	}
	
	/**
	 * Returns the average flight time for all flights in the Schedule database between two airports.
	 * @param airportD the origin airport IATA code
	 * @param airportA the destination airport IATA code
	 * @return the average time between the two airports in hours <i>multiplied by 10</i>, or 0 if no flights found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public int getFlightTime(String airportD, String airportA) throws DAOException {
	   
	   // Init the prepared statement
	   try {
	      prepareStatement("SELECT FLIGHT_TIME FROM SCHEDULE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)");
	      _ps.setString(1, airportD.toUpperCase());
	      _ps.setString(2, airportA.toUpperCase());
	      
	      // Set counters
	      int flights = 0;
	      int totalTime = 0;
	      
	      // Execute the Query
	      ResultSet rs = _ps.executeQuery();
	      while (rs.next()) {
	         flights++;
	         totalTime += rs.getInt(1);
	      }
	      
	      // Clean up and return
	      rs.close();
	      _ps.close();
	      return (flights == 0) ? 0 : (totalTime / flights);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Helper method to query the database.
	 */
	private List execute() throws SQLException {
		
		// Execute the query
		List results = new ArrayList();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			ScheduleEntry entry = new ScheduleEntry(SystemData.getAirline(rs.getString(2)), rs.getInt(3), rs.getInt(4));
			entry.setID(rs.getInt(1));
			entry.setAirportD(SystemData.getAirport(rs.getString(5)));
			entry.setAirportA(SystemData.getAirport(rs.getString(6)));
			entry.setEquipmentType(rs.getString(8));
			entry.setTimeD(rs.getTimestamp(10));
			entry.setTimeA(rs.getTimestamp(11));
			entry.setHistoric(rs.getBoolean(12));

			// Add to results
			results.add(entry);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}