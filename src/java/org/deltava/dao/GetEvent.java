// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2014, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.RoutePair;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Online Event data.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.STARTTIME > NOW()) AND (E.STATUS != ?) AND (E.ID=EA.ID) AND (EA.AIRLINE=?) ORDER BY E.STARTTIME")) {
			ps.setInt(1, Status.CANCELED.ordinal());
			ps.setString(2, SystemData.get("airline.code"));
			List<Event> results = execute(ps);
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, Event::getID);
			loadRoutes(eMap);
			loadSignups(eMap);
			loadBriefings(eMap);
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
		try (PreparedStatement ps = prepare("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND (E.SU_DEADLINE < ?) AND (E.ENDTIME > ?) AND (E.STATUS != ?) AND (EA.AIRLINE=?) ORDER BY E.STARTTIME DESC")) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ps.setTimestamp(1, now);
			ps.setTimestamp(2, now);
			ps.setInt(3, Status.CANCELED.ordinal());
			ps.setString(4, SystemData.get("airline.code"));
			List<Event> results = execute(ps);
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, Event::getID);
			loadRoutes(eMap);
			loadSignups(eMap);
			loadBriefings(eMap);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns possible Online Events for a particular flight report. This will select an Event that started prior to the submission date and ended less than 2 days before the submission date. 
	 * @param fr the FlightReport
	 * @return a Collection of Evens
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Event> getPossibleEvents(FlightReport fr) throws DAOException {
		return getPossibleEvents(fr, fr.getNetwork(), fr.getSubmittedOn(), SystemData.get("airline.code"));
	}
	
	/**
	 * Returns possible Online Events for a particular flight. This will select an Event that started prior to the submission date and ended less than 2 days before the submission date. 
	 * @param rp the RoutePair
	 * @param net the OnlineNetwork
	 * @param dt the Flight date/time
	 * @param airlineCode the Airline Code
	 * @return a Collection of Events
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Event> getPossibleEvents(RoutePair rp, OnlineNetwork net, java.time.Instant dt, String airlineCode) throws DAOException {
		if (net == null) return Collections.emptyList();
		try (PreparedStatement ps = prepareWithoutLimits("SELECT E.* FROM events.EVENTS E, events.EVENT_AIRPORTS EA, events.AIRLINES EAL WHERE (E.ID=EA.ID) AND (E.ID=EAL.ID) AND (EA.AIRPORT_D=?) "
			+ "AND (EA.AIRPORT_A=?) AND (E.NETWORK=?) AND (EAL.AIRLINE=?) AND (E.STARTTIME < ?) AND (DATE_ADD(E.ENDTIME, INTERVAL 2 DAY) > ?) ORDER BY E.ID LIMIT 1")) {
			ps.setString(1, rp.getAirportD().getIATA());
			ps.setString(2, rp.getAirportA().getIATA());
			ps.setInt(3, net.ordinal());
			ps.setString(4, airlineCode);
			ps.setTimestamp(5, createTimestamp(dt));
			ps.setTimestamp(6, createTimestamp(dt));
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND (E.STARTTIME >= ?) AND (E.STARTTIME <= ?) AND (E.STATUS !=?) AND (EA.AIRLINE=?) "
			+ "ORDER BY E.STARTTIME")) {
			ps.setTimestamp(1, createTimestamp(dr.getStartDate()));
			ps.setTimestamp(2, createTimestamp(dr.getEndDate()));
			ps.setInt(3, Status.CANCELED.ordinal());
			ps.setString(4, SystemData.get("airline.code"));
			List<Event> results = execute(ps);
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, Event::getID);
			loadRoutes(eMap);
			loadSignups(eMap);
			loadBriefings(eMap);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT E.ID FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND (E.STARTTIME > NOW()) AND (E.STATUS != ?) AND (EA.AIRLINE=?) LIMIT 1")) {
			ps.setInt(1, Status.CANCELED.ordinal());
			ps.setString(2, SystemData.get("airline.code"));
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && (rs.getInt(1) > 0);
			}
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
		try (PreparedStatement ps = prepare("SELECT E.* FROM events.EVENTS E, events.AIRLINES EA WHERE (E.ID=EA.ID) AND (EA.AIRLINE=?) ORDER BY E.STARTTIME DESC")) {
			ps.setString(1, SystemData.get("airline.code"));
			List<Event> results = execute(ps);
			
			// Load the airports
			Map<Integer, Event> eMap = CollectionUtils.createMap(results, Event::getID);
			loadRoutes(eMap);
			loadBriefings(eMap);
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
		try (PreparedStatement ps = prepare("SELECT DISTINCT E.* FROM events.EVENTS E, PIREPS PR, ACARS_PIREPS APR WHERE (PR.EVENT_ID=E.ID) AND (APR.ID=PR.ID) AND (APR.ACARS_ID <> 0) ORDER BY E.STARTTIME DESC")) {
			return execute(ps);
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
			Event e = null;
			try (PreparedStatement ps = prepareWithoutLimits("SELECT E.*, EB.EXT, ISNULL(EBR.DATA), EBR.DATA FROM events.EVENTS E LEFT JOIN events.BANNERS EB ON (E.ID=EB.ID) LEFT JOIN events.BRIEFINGS EBR ON (E.ID=EBR.ID) WHERE (E.ID=?) LIMIT 1")) {
				ps.setInt(1, id);
				e = execute(ps).stream().findFirst().orElse(null);
			}
			
			if (e == null) return null;

			// Get the first event and populate it
			loadAirlines(e);
			loadEQTypes(e);
			loadContactAddrs(e);
			loadFeaturedAirports(e);
			
			// Create a map and load the airports
			Map<Integer, Event> eMap = Collections.singletonMap(Integer.valueOf(e.getID()), e);
			loadRoutes(eMap);
			loadSignups(eMap);
			return e;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the events which a user has signed up for or participated in. 
	 * @param userID the user's database ID
	 * @return a Collection of Event IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getMyEventIDs(int userID) throws DAOException {
		try {
			Collection<Integer> IDs = new TreeSet<Integer>();
			
			// Load from signups
			try (PreparedStatement ps = prepareWithoutLimits("SELECT ID from events.EVENT_SIGNUPS WHERE (PILOT_ID=?)")) {
				ps.setInt(1, userID);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						IDs.add(Integer.valueOf(rs.getInt(1)));
				}
			}
			
			// Load from Flight Reports
			try (PreparedStatement ps = prepareWithoutLimits("SELECT EVENT_ID FROM PIREPS WHERE (EVENT_ID>0) AND (PILOT_ID=?) AND (STATUS=?)")) {
				ps.setInt(1, userID);
				ps.setInt(2, FlightStatus.OK.ordinal());
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						IDs.add(Integer.valueOf(rs.getInt(1)));
				}
			}

			return IDs;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load event airports/routes.
	 */
	private void loadRoutes(Map<Integer, Event> events) throws SQLException {
		if (events.isEmpty()) return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EA.*, COUNT(ES.PILOT_ID) FROM events.EVENT_AIRPORTS EA LEFT JOIN events.EVENT_SIGNUPS ES ON (EA.ID=ES.ID) AND (EA.ROUTE_ID=ES.ROUTE_ID) WHERE (EA.ID IN (");
		sqlBuf.append(StringUtils.listConcat(events.keySet(), ","));
		sqlBuf.append(")) GROUP BY EA.ID, EA.ROUTE_ID ORDER BY EA.ID");

		// Execute the query
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Event e = events.get(Integer.valueOf(rs.getInt(1)));
					if (e == null) continue;
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
	}

	/*
	 * Helper method to load events.
	 */
	private static List<Event> execute(PreparedStatement ps) throws SQLException {
		List<Event> results = new ArrayList<Event>();
		try (ResultSet rs = ps.executeQuery()) {
			ResultSetMetaData md = rs.getMetaData();
			boolean hasBanner = (md.getColumnCount() > 10);
			boolean hasBriefing = (md.getColumnCount() > 12);
			while (rs.next()) {
				Event e = new Event(rs.getString(2));
				e.setID(rs.getInt(1));
				e.setStatus(Status.values()[rs.getInt(3)]);
				e.setNetwork(OnlineNetwork.values()[rs.getInt(4)]);
				e.setStartTime(rs.getTimestamp(5).toInstant());
				e.setEndTime(toInstant(rs.getTimestamp(6)));
				e.setSignupDeadline(toInstant(rs.getTimestamp(7)));
				e.setCanSignup(rs.getBoolean(8));
				e.setSignupURL(rs.getString(9));
				e.setOwner(SystemData.getApp(rs.getString(10)));
				if (hasBanner)
					e.setBannerExtension(rs.getString(11));
				if (hasBriefing && !rs.getBoolean(12))
					e.setBriefing(new Briefing(rs.getBytes(13)));

				results.add(e);
			}
		}

		return results;
	}

	/*
	 * Helper method to load signups for an event.
	 */
	private void loadSignups(Map<Integer, Event> events) throws SQLException {
		if (events.isEmpty()) return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ES.*, EA.AIRPORT_D, EA.AIRPORT_A FROM events.EVENT_SIGNUPS ES, events.EVENT_AIRPORTS EA WHERE (EA.ID=ES.ID) AND (EA.ROUTE_ID=ES.ROUTE_ID) AND (ES.ID IN (");
		sqlBuf.append(StringUtils.listConcat(events.keySet(), ","));
		sqlBuf.append("))");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Signup s = new Signup(rs.getInt(1), rs.getInt(3));
					s.setRouteID(rs.getInt(2));
					s.setEquipmentType(rs.getString(4));
					s.setRemarks(rs.getString(5));
					s.setAirportD(SystemData.getAirport(rs.getString(6)));
					s.setAirportA(SystemData.getAirport(rs.getString(7)));

					Event e = events.get(Integer.valueOf(s.getID()));
					if (e != null)
						e.addSignup(s);
				}
			}
		}
	}
	
	private void loadBriefings(Map<Integer, Event> events) throws SQLException {
		if (events.isEmpty()) return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, ISPDF, SIZE FROM events.BRIEFINGS WHERE (ID IN (");
		sqlBuf.append(StringUtils.listConcat(events.keySet(), ","));
		sqlBuf.append("))");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Briefing b = new Briefing(null); 
					b.setID(rs.getInt(1));
					b.setForcePDF(rs.getBoolean(2));
					b.setForceSize(rs.getInt(3));

					Event e = events.get(Integer.valueOf(b.getID()));
					if (e != null)
						e.setBriefing(b);
				}
			}
		}
	}

	private void loadEQTypes(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT RATING FROM events.EVENT_EQTYPES WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					e.addEquipmentType(rs.getString(1));
			}
		}
	}
	
	private void loadContactAddrs(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ADDRESS FROM events.EVENT_CONTACTS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					e.addContactAddr(rs.getString(1));
			}
		}
	}
	
	private void loadFeaturedAirports(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRPORT FROM events.AIRPORTS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					e.addFeaturedAirport(SystemData.getAirport(rs.getString(1)));
			}
		}
	}
	
	private void loadAirlines(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRLINE FROM events.AIRLINES WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					e.addAirline(SystemData.getApp(rs.getString(1)));	
			}
		}
	}
}