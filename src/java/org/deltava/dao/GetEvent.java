// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.event.*;

import org.deltava.util.CollectionUtils;
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
			prepareStatement("SELECT * FROM common.EVENTS WHERE (STARTTIME > NOW()) AND (STATUS != ?) "
					+ "ORDER BY STARTTIME");
			_ps.setInt(1, Event.CANCELED);
			List results = execute();
			
			// Load the airports
			Map eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			
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
			prepareStatement("SELECT COUNT(*) FROM common.EVENTS WHERE (STARTTIME > NOW()) AND (STATUS != ?)");
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
	public List getEvents() throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.EVENTS ORDER BY STARTTIME DESC");
			List results = execute();
			
			// Load the airports
			Map eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			
			// Return the results
			return results;
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
			prepareStatement("SELECT * FROM common.EVENTS WHERE (ID=?)");
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
			
			// Create a map and load the airports
			Map eMap = new HashMap();
			eMap.put(new Integer(e.getID()), e);
			loadRoutes(eMap);
			return e;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load event airports/routes.
	 */
	private void loadRoutes(Map events) throws SQLException {
		if (events.isEmpty())
			return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.EVENT_AIRPORTS WHERE (ID IN (");
		for (Iterator i = events.keySet().iterator(); i.hasNext();) {
			Integer id = (Integer) i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append("))");

		// Init the prepared statement
		prepareStatementWithoutLimits(sqlBuf.toString());

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Event e = (Event) events.get(new Integer(rs.getInt(1)));
			if (e != null) {
				Route r = new Route(e.getID(), rs.getString(4));
				r.setAirportD(SystemData.getAirport(rs.getString(2)));
				r.setAirportA(SystemData.getAirport(rs.getString(3)));
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
	private List execute() throws SQLException {
		List results = new ArrayList();

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
	private void loadSignups(Event e) throws SQLException {

		// Init the prepared statement
		prepareStatementWithoutLimits("SELECT * FROM common.EVENT_SIGNUPS WHERE (ID=?)");
		_ps.setInt(1, e.getID());

		// Execute the query and load the signups
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Signup s = new Signup(e.getID(), rs.getInt(2));
			s.setEquipmentType(rs.getString(3));
			s.setAirportD(SystemData.getAirport(rs.getString(4)));
			s.setAirportA(SystemData.getAirport(rs.getString(5)));
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

		// Init the prepared statement
		prepareStatementWithoutLimits("SELECT * FROM common.EVENT_PLANS WHERE (ID=?)");
		_ps.setInt(1, e.getID());

		// Execute the query and load the flight plans
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			FlightPlan fp = new FlightPlan(rs.getInt(2));
			fp.setID(e.getID());
			fp.setAirportD(SystemData.getAirport(rs.getString(3)));
			fp.setAirportA(SystemData.getAirport(rs.getString(4)));
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