// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.event.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Online Event data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetEvent extends DAO {
	
	/**
	 * Initailize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetEvent(Connection c) {
		super(c);
	}

	/**
	 * Returns all future Online Events that have not been canceled.
	 * @return a List of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getFutureEvents() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT E.*, EA.AIRPORT_A, EA.AIRPORT_D FROM common.EVENTS E LEFT JOIN "
					+ "common.EVENT_AIRPORTS EA ON (E.ID=EA.ID) WHERE (E.STARTTIME > NOW()) AND (E.STATUS != ?) "
					+ "ORDER BY E.STARTTIME");
			_ps.setInt(1, Event.CANCELED);

			// Execute the query
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Online Events.
	 * @return a List of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getEvents() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT E.*, EA.AIRPORT_A, EA.AIRPORT_D FROM common.EVENTS E LEFT JOIN "
					+ "common.EVENT_AIRPORTS EA ON (E.ID=EA.ID) ORDER BY E.STARTTIME DESC");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a particular Online Event.
	 * @param id the Online Event database ID
	 * @return the Online Event bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Event get(int id) throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT E.*, EA.AIRPORT_A, EA.AIRPORT_D FROM common.EVENTS E LEFT JOIN "
					+ "common.EVENT_AIRPORTS EA ON (E.ID=EA.ID) WHERE (E.ID=?)");
			_ps.setInt(1, id);

			// Execute the query and return null if nothing found
			List results = execute();
			if (results.isEmpty())
				return null;

			// Get the first event and populate it
			Event e = (Event) results.get(0);
			loadFlightPlans(e);
			loadSignups(e);
			loadEQTypes(e);
			return e;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to load events.
	 */
	private List execute() throws SQLException {
		List results = new ArrayList();
		int lastID = -1;
		Event e = null;

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			if (id != lastID) {
				lastID = id;
				e = new Event(rs.getString(2));
				e.setID(rs.getInt(1));
				e.setStatus(rs.getInt(3));
				e.setNetwork(rs.getInt(4));
				e.setStartTime(rs.getTimestamp(5));
				e.setEndTime(rs.getTimestamp(6));
				e.setSignupDeadline(rs.getTimestamp(7));
				e.setRoute(rs.getString(8));
				e.setBriefing(rs.getString(9));

				// Add to results
				results.add(e);
			}
			
			// Add airports
			e.addAirportD(SystemData.getAirport(rs.getString(11)));
			e.setAirportA(SystemData.getAirport(rs.getString(10)));
		}

		// Clean up
		rs.close();
		_ps.close();
		
		// Check the events - if any has a null for an origin airport, select all
		for (Iterator i = results.iterator(); i.hasNext(); ) {
			e = (Event) i.next();
			if (e.getAirportD().isEmpty())
				e.addAirportD(Airport.ALL);
		}
		
		return results;
	}

	/**
	 * Helper method to load signups for an event.
	 */
	private void loadSignups(Event e) throws SQLException {

		// Get airports
		Map airports = (Map) SystemData.getObject("airports");

		// Init the prepared statement
		prepareStatementWithoutLimits("SELECT * FROM common.EVENT_SIGNUPS WHERE (ID=?)");
		_ps.setInt(1, e.getID());

		// Execute the query and load the signups
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Signup s = new Signup(e.getID(), rs.getInt(2));
			s.setEquipmentType(rs.getString(3));
			s.setAirportD((Airport) airports.get(rs.getString(4)));
			s.setAirportA((Airport) airports.get(rs.getString(5)));
			s.setRemarks(rs.getString(6));

			// Add to results
			e.addSignup(s);
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}

	/**
	 * Helper method to load flight plans for an event.
	 */
	private void loadFlightPlans(Event e) throws SQLException {

		// Get airports
		Map airports = (Map) SystemData.getObject("airports");

		// Init the prepared statement
		prepareStatementWithoutLimits("SELECT * FROM common.EVENT_PLANS WHERE (ID=?)");
		_ps.setInt(1, e.getID());

		// Execute the query and load the flight plans
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			FlightPlan fp = new FlightPlan(rs.getInt(2));
			fp.setID(e.getID());
			fp.setAirportD((Airport) airports.get(rs.getString(3)));
			fp.setAirportA((Airport) airports.get(rs.getString(4)));
			fp.load(rs.getBytes(5));

			// Add to results
			e.addPlan(fp);
		}

		// Clean up
		rs.close();
		_ps.close();
	}
	
	private void loadEQTypes(Event e) throws SQLException {

	   // Init the prepared statement
	   prepareStatementWithoutLimits("SELECT RATING FROM common.EVENT_EQTYPES WHERE (ID=?)");
	   _ps.setInt(1, e.getID());
	   
	   // Execute the query and load the equipment types
	   ResultSet rs = _ps.executeQuery();
	   while (rs.next())
	      e.addEquipmentType(rs.getString(1));
	      
	   // Clean up
	   rs.close();
	   _ps.close();
	}
}