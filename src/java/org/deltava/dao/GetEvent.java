// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.RoutePair;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Online Event data.
 * @author Luke
 * @version 7.0
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
			prepareStatement("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE "
					+ "(E.STARTTIME > NOW()) AND (E.STATUS != ?) AND (E.ID=EA.ID) AND (EA.AIRLINE=?) "
					+ "ORDER BY E.STARTTIME");
			_ps.setInt(1, Status.CANCELED.ordinal());
			_ps.setString(2, SystemData.get("airline.code"));
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			loadSignups(eMap);
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
			prepareStatement("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND "
					+ "(E.SU_DEADLINE < ?) AND (E.ENDTIME > ?) AND (E.STATUS != ?) AND (EA.AIRLINE=?) "
					+ "ORDER BY E.STARTTIME DESC");
			_ps.setTimestamp(1, now);
			_ps.setTimestamp(2, now);
			_ps.setInt(3, Status.CANCELED.ordinal());
			_ps.setString(4, SystemData.get("airline.code"));
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			loadSignups(eMap);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	public int getPossibleEvent(FlightReport fr) throws DAOException {
		return getPossibleEvent(fr, fr.getNetwork(), fr.getSubmittedOn());
	}
	
	/**
	 * Returns a possible Online Event for a particular flight report. This will select an Event that started prior to the submission date
	 * and ended less than 2 days before the submission date. 
	 * @param fr the RoutePair
	 * @param net the OnlineNetwork
	 * @param dt the Flight date/time
	 * @return the Online Event database ID, or zero if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPossibleEvent(RoutePair fr, OnlineNetwork net, java.time.Instant dt) throws DAOException {
		if (net == null) return 0;
		try {
			prepareStatementWithoutLimits("SELECT E.ID FROM events.EVENTS E, events.EVENT_AIRPORTS EA, events.AIRLINES EAL "
					+ "WHERE (E.ID=EA.ID) AND (E.ID=EAL.ID) AND (EA.AIRPORT_D=?) AND (EA.AIRPORT_A=?) AND (E.NETWORK=?) AND "
					+ "(EAL.AIRLINE=?) AND (E.STARTTIME < ?) AND (DATE_ADD(E.ENDTIME, INTERVAL 2 DAY) > ?) ORDER BY E.ID LIMIT 1");
			_ps.setString(1, fr.getAirportD().getIATA());
			_ps.setString(2, fr.getAirportA().getIATA());
			_ps.setInt(3, net.ordinal());
			_ps.setString(4, SystemData.get("airline.code"));
			_ps.setTimestamp(5, createTimestamp(dt));
			_ps.setTimestamp(6, createTimestamp(dt));
			
			// Execute the Query
			int eventID = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					eventID = rs.getInt(1);
			}
			
			_ps.close();
			return eventID;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Online Events within a certain date range.
	 * @param dr the DateRange
	 * @return a List of Event beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Event> getEventCalendar(DateRange dr) throws DAOException {
		try {
			prepareStatement("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND "
				+ "(E.STARTTIME >= ?) AND (E.STARTTIME <= ?) AND (E.STATUS !=?) AND (EA.AIRLINE=?) "
				+ "ORDER BY E.STARTTIME");
			_ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			_ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
			_ps.setInt(3, Status.CANCELED.ordinal());
			_ps.setString(4, SystemData.get("airline.code"));
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
			loadSignups(eMap);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns whether there are any active Online Events schedule.
	 * @return TRUE if at least one Event is scheduled, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean hasFutureEvents() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT E.ID FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) "
					+ "AND (E.STARTTIME > NOW()) AND (E.STATUS != ?) AND (EA.AIRLINE=?) LIMIT 1");
			_ps.setInt(1, Status.CANCELED.ordinal());
			_ps.setString(2, SystemData.get("airline.code"));
			
			// Execute the query
			boolean hasEvents = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					hasEvents = (rs.getInt(1) > 0);
			}
			
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
			prepareStatement("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND "
					+ "(EA.AIRLINE=?) ORDER BY E.STARTTIME DESC");
			_ps.setString(1, SystemData.get("airline.code"));
			List<Event> results = execute();
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, "ID");
			loadRoutes(eMap);
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
			prepareStatementWithoutLimits("SELECT E.*, EB.EXT FROM events.EVENTS E LEFT JOIN events.BANNERS EB "
					+ "ON (E.ID=EB.ID) WHERE (E.ID=?) LIMIT 1");
			_ps.setInt(1, id);

			// Execute the query and return null if nothing found
			List<Event> results = execute();
			if (results.isEmpty())
				return null;

			// Get the first event and populate it
			Event e = results.get(0);
			loadAirlines(e);
			loadEQTypes(e);
			loadContactAddrs(e);
			
			// Create a map and load the airports
			Map<Integer, Event> eMap = new HashMap<Integer, Event>();
			eMap.put(Integer.valueOf(e.getID()), e);
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

		// Execute the query
		prepareStatementWithoutLimits(sqlBuf.toString());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Event e = events.get(Integer.valueOf(rs.getInt(1)));
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
		}

		_ps.close();
	}

	/*
	 * Helper method to load events.
	 */
	private List<Event> execute() throws SQLException {
		List<Event> results = new ArrayList<Event>();
		try (ResultSet rs = _ps.executeQuery()) {
			boolean hasBanner = (rs.getMetaData().getColumnCount() > 11);
			while (rs.next()) {
				Event e = new Event(rs.getString(2));
				e.setID(rs.getInt(1));
				e.setStatus(Status.values()[rs.getInt(3)]);
				e.setNetwork(OnlineNetwork.values()[rs.getInt(4)]);
				e.setStartTime(rs.getTimestamp(5).toInstant());
				e.setEndTime(toInstant(rs.getTimestamp(6)));
				e.setSignupDeadline(toInstant(rs.getTimestamp(7)));
				e.setBriefing(rs.getString(8));
				e.setCanSignup(rs.getBoolean(9));
				e.setSignupURL(rs.getString(10));
				e.setOwner(SystemData.getApp(rs.getString(11)));
				if (hasBanner)
					e.setBannerExtension(rs.getString(12));

				results.add(e);
			}
		}

		_ps.close();
		return results;
	}

	/*
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

		// Execute the query and load the signups
		prepareStatementWithoutLimits(sqlBuf.toString());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Signup s = new Signup(rs.getInt(1), rs.getInt(3));
				s.setRouteID(rs.getInt(2));
				s.setEquipmentType(rs.getString(4));
				s.setRemarks(rs.getString(5));
				s.setAirportD(SystemData.getAirport(rs.getString(6)));
				s.setAirportA(SystemData.getAirport(rs.getString(7)));

				// Add to results
				Event e = events.get(Integer.valueOf(s.getID()));
				if (e != null)
					e.addSignup(s);
			}
		}

		_ps.close();
	}

	private void loadEQTypes(Event e) throws SQLException {
		prepareStatementWithoutLimits("SELECT RATING FROM events.EVENT_EQTYPES WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				e.addEquipmentType(rs.getString(1));
		}

		_ps.close();
	}
	
	private void loadContactAddrs(Event e) throws SQLException {
		prepareStatementWithoutLimits("SELECT ADDRESS FROM events.EVENT_CONTACTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				e.addContactAddr(rs.getString(1));
		}

		_ps.close();
	}
	
	private void loadAirlines(Event e) throws SQLException {
		prepareStatementWithoutLimits("SELECT AIRLINE FROM events.AIRLINES WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
			e.addAirline(SystemData.getApp(rs.getString(1)));	
		}
		
		_ps.close();
	}
}