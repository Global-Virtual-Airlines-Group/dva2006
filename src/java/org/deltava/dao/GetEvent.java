// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.event.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Online Event data.
 * @author Luke
 * @version 2.1
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
	public List<Event> getFutureEvents() throws DAOException {
		try {
			prepareStatement("SELECT * FROM events.EVENTS WHERE (STARTTIME > NOW()) AND (STATUS != ?) "
					+ "ORDER BY STARTTIME");
			_ps.setInt(1, Event.CANCELED);
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			
			// Return the results
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all future Online Events with signups that are available for assignment.
	 * @return a List of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Event> getAssignableEvents() throws DAOException {
		try {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			prepareStatement("select E.*, COUNT(S.ID) from events.EVENTS E LEFT JOIN events.EVENT_SIGNUPS S "
					+ "ON (E.ID=S.ID) WHERE (E.SU_DEADLINE < ?) AND (E.ENDTIME > ?) AND (E.STATUS != ?) "
					+ "GROUP BY E.ID ORDER BY E.STARTTIME DESC");
			_ps.setTimestamp(1, now);
			_ps.setTimestamp(2, now);
			_ps.setInt(3, Event.CANCELED);
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			loadSignups(eMap);
			
			// Return the results
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the Online Event for a particular flight route. This will display the Events that took place within the past 2 days. 
	 * @param airportD the departure Airport
	 * @param airportA the arrival Airport
	 * @param network the online network code
	 * @return the Online Event database ID, or zero if none found
	 * @throws DAOException if a JDBC error occurs
	 * @see Event#NET_VATSIM
	 * @see Event#NET_IVAO
	 */
	public int getEvent(Airport airportD, Airport airportA, int network) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT E.ID FROM events.EVENTS E, events.EVENT_AIRPORTS EA WHERE (E.ID=EA.ID) "
					+ "AND (EA.AIRPORT_D=?) AND (EA.AIRPORT_A=?) AND (E.NETWORK=?) AND (E.STARTTIME < NOW()) AND "
					+ "(NOW() < DATE_ADD(E.ENDTIME, INTERVAL 2 DAY)) ORDER BY E.ID");
			_ps.setString(1, airportD.getIATA());
			_ps.setString(2, airportA.getIATA());
			_ps.setInt(3, network);
			
			// Execute the Query
			setQueryMax(0);
			ResultSet rs = _ps.executeQuery();
			int eventID = rs.next() ? rs.getInt(1) : 0;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return eventID;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Online Events within a certain date range.
	 * @param startDate the start Date
	 * @param days the number of days forward to query
	 * @return a List of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Event> getEventCalendar(java.util.Date startDate, int days) throws DAOException {
		try {
			prepareStatement("SELECT * FROM events.EVENTS WHERE (STARTTIME >= ?) AND "
					+ "(STARTTIME < DATE_ADD(?, INTERVAL ? DAY)) AND (STATUS !=?) ORDER BY STARTTIME");
			_ps.setTimestamp(1, createTimestamp(startDate));
			_ps.setTimestamp(2, createTimestamp(startDate));
			_ps.setInt(3, days);
			_ps.setInt(4, Event.CANCELED);
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			loadSignups(eMap);
			
			// Return the results
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns wether there are any active Online Events schedule.
	 * @return TRUE if at least one Event is scheduled, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean hasFutureEvents() throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM events.EVENTS WHERE (STARTTIME > NOW()) AND (STATUS != ?)");
			_ps.setInt(1, Event.CANCELED);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			boolean hasEvents = rs.next() ? (rs.getInt(1) > 0) : false;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return hasEvents;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Online Events.
	 * @return a List of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Event> getEvents() throws DAOException {
		try {
			prepareStatement("SELECT * FROM events.EVENTS ORDER BY STARTTIME DESC");
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			
			// Return the results
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Online Events with ACARS-logged flights.
	 * @return a Collection of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Event> getWithACARS() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT E.* FROM events.EVENTS E, PIREPS PR, ACARS_PIREPS APR "
					+ "WHERE (PR.EVENT_ID=E.ID) AND (APR.ID=PR.ID) AND (APR.ACARS_ID <> 0) ORDER BY "
					+ "E.STARTTIME DESC");
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
			setQueryMax(1);
			prepareStatement("SELECT * FROM events.EVENTS WHERE (ID=?)");
			_ps.setInt(1, id);

			// Execute the query and return null if nothing found
			List<Event> results = execute();
			setQueryMax(0);
			if (results.isEmpty())
				return null;

			// Get the first event and populate it
			Event e = results.get(0);
			loadFlightPlans(e);
			loadEQTypes(e);
			loadContactAddrs(e);
			
			// Create a map and load the airports
			Map<Integer, Event> eMap = new HashMap<Integer, Event>();
			eMap.put(new Integer(e.getID()), e);
			loadRoutes(eMap);
			loadSignups(eMap);
			return e;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load event airports/routes.
	 */
	private void loadRoutes(Map<Integer, Event> events) throws SQLException {
		if (events.isEmpty())
			return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EA.*, COUNT(ES.PILOT_ID) FROM events.EVENT_AIRPORTS EA "
				+ "LEFT JOIN events.EVENT_SIGNUPS ES ON (EA.ID=ES.ID) AND (EA.ROUTE_ID=ES.ROUTE_ID) WHERE (EA.ID IN (");
		for (Iterator<Integer> i = events.keySet().iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append(")) GROUP BY EA.ID, EA.ROUTE_ID ORDER BY EA.ID");

		// Init the prepared statement
		prepareStatementWithoutLimits(sqlBuf.toString());

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Event e = events.get(new Integer(rs.getInt(1)));
			if (e != null) {
				Route r = new Route(e.getID(), rs.getString(5));
				r.setRouteID(rs.getInt(2));
				r.setAirportD(SystemData.getAirport(rs.getString(3)));
				r.setAirportA(SystemData.getAirport(rs.getString(4)));
				r.setActive(rs.getBoolean(6));
				r.setIsRNAV(rs.getBoolean(7));
				r.setMaxSignups(rs.getInt(8));
				r.setName(rs.getString(9));
				r.setSignups(rs.getInt(10));
				e.addRoute(r);
			}
		}

		// Clean up after ourselves
		rs.close();
		_ps.close();
	}

	/**
	 * Helper method to load events.
	 */
	private List<Event> execute() throws SQLException {
		List<Event> results = new ArrayList<Event>();

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Event e = new Event(rs.getString(2));
			e.setID(rs.getInt(1));
			e.setStatus(rs.getInt(3));
			e.setNetwork(rs.getInt(4));
			e.setStartTime(rs.getTimestamp(5));
			e.setEndTime(rs.getTimestamp(6));
			e.setSignupDeadline(rs.getTimestamp(7));
			e.setBriefing(rs.getString(8));
			e.setCanSignup(rs.getBoolean(9));

			// Add to results
			results.add(e);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to load signups for an event.
	 */
	private void loadSignups(Map<Integer, Event> events) throws SQLException {
		if (events.isEmpty())
			return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ES.*, EA.AIRPORT_D, EA.AIRPORT_A FROM events.EVENT_SIGNUPS ES, "
				+ "events.EVENT_AIRPORTS EA WHERE (EA.ID=ES.ID) AND (EA.ROUTE_ID=ES.ROUTE_ID) AND (ES.ID IN (");
		for (Iterator<Integer> i = events.keySet().iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append("))");

		// Init the prepared statement
		prepareStatementWithoutLimits(sqlBuf.toString());

		// Execute the query and load the signups
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Signup s = new Signup(rs.getInt(1), rs.getInt(3));
			s.setRouteID(rs.getInt(2));
			s.setEquipmentType(rs.getString(4));
			s.setRemarks(rs.getString(5));
			s.setAirportD(SystemData.getAirport(rs.getString(6)));
			s.setAirportA(SystemData.getAirport(rs.getString(7)));

			// Add to results
			Event e = events.get(new Integer(s.getID()));
			if (e != null)
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
		prepareStatementWithoutLimits("SELECT EP.*, EA.AIRPORT_D, EA.AIRPORT_A FROM events.EVENT_PLANS EP, "
			+ "events.EVENT_AIRPORTS EA WHERE (EP.ID=?) AND (EP.ID=EA.ID) AND (EP.ROUTE_ID=EA.ROUTE_ID)");
		_ps.setInt(1, e.getID());

		// Execute the query and load the flight plans
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			FlightPlan fp = new FlightPlan(rs.getInt(3));
			fp.setID(e.getID());
			fp.setRouteID(rs.getInt(2));
			fp.load(rs.getBytes(4));
			fp.setAirportD(SystemData.getAirport(rs.getString(5)));
			fp.setAirportA(SystemData.getAirport(rs.getString(6)));
			e.addPlan(fp);
		}

		// Clean up
		rs.close();
		_ps.close();
	}

	private void loadEQTypes(Event e) throws SQLException {
		prepareStatementWithoutLimits("SELECT RATING FROM events.EVENT_EQTYPES WHERE (ID=?)");
		_ps.setInt(1, e.getID());

		// Execute the query and load the equipment types
		ResultSet rs = _ps.executeQuery();
		while (rs.next())
			e.addEquipmentType(rs.getString(1));

		// Clean up
		rs.close();
		_ps.close();
	}
	
	private void loadContactAddrs(Event e) throws SQLException {
		prepareStatementWithoutLimits("SELECT ADDRESS FROM events.EVENT_CONTACTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		
		// Execute the query and load the addresses
		ResultSet rs = _ps.executeQuery();
		while (rs.next())
			e.addContactAddr(rs.getString(1));

		// Clean up
		rs.close();
		_ps.close();
	}
}