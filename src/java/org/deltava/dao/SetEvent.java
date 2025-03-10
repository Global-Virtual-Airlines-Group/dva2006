// Copyright 2005, 2007, 2008, 2011, 2012, 2014, 2017, 2019, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Feedback;
import org.deltava.beans.event.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write Online Event data.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class SetEvent extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetEvent(Connection c) {
		super(c);
	}

	/**
	 * Writes an Online Event to the database. This handles INSERTs and UPDATEs.
	 * @param e the Online Event
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Event e) throws DAOException {
		try {
			startTransaction();

			// Determine if we are doing an insert or an update
			if (e.getID() == 0) {
				insert(e);
				if (e.isLoaded())
					writeBanner(e);
			} else
				update(e);

			// Write the child rows
			writeBriefing(e);
			writeAirlines(e);
			writeAirports(e);
			writeCharts(e);
			writeEQTypes(e);
			writeContactAddrs(e);
			writeFeaturedAirports(e);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a Signup to the database.
	 * @param s the Signup bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void signup(Signup s) throws DAOException {
		try (PreparedStatement ps =  prepare("REPLACE INTO events.EVENT_SIGNUPS (ID, ROUTE_ID, PILOT_ID, EQTYPE, REMARKS) VALUES (?,?,?,?,?)")) {
			ps.setInt(1, s.getID());
			ps.setInt(2, s.getRouteID());
			ps.setInt(3, s.getPilotID());
			ps.setString(4, s.getEquipmentType());
			ps.setString(5, s.getRemarks());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(s.getPilotID()));
		}
	}

	/**
	 * Saves a Flight Route to the database. This can handle create and update operations.
	 * @param r the Route bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void save(Route r) throws DAOException {
		try (PreparedStatement ps = prepare("INSERT INTO events.EVENT_AIRPORTS (ID, ROUTE_ID, AIRPORT_D, AIRPORT_A, ROUTE, RNAV, ACTIVE, MAX_SIGNUPS, NAME) VALUES "
			+ "(?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ROUTE=?, RNAV=?, NAME=?, MAX_SIGNUPS=?")) {
			ps.setInt(1, r.getID());
			ps.setInt(2, r.getRouteID());
			ps.setString(3, r.getAirportD().getIATA());
			ps.setString(4, r.getAirportA().getIATA());
			ps.setString(5, r.getRoute());
			ps.setBoolean(6, r.getIsRNAV());
			ps.setBoolean(7, r.getActive());
			ps.setInt(8, r.getMaxSignups());
			ps.setString(9, r.getName());
			ps.setString(10, r.getRoute());
			ps.setBoolean(11, r.getIsRNAV());
			ps.setString(12, r.getName());
			ps.setInt(13, r.getMaxSignups());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Online Event.
	 * @param e the Event bean 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Event e) throws DAOException {
		try (PreparedStatement ps =  prepare("DELETE FROM events.EVENTS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Online Event pilot signup.
	 * @param s the Signup bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Signup s) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM events.EVENT_SIGNUPS WHERE (ID=?) AND (PILOT_ID=?)")) {
			ps.setInt(1, s.getID());
			ps.setInt(2, s.getPilotID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(s.getPilotID()));
		}
	}

	/**
	 * Deletes an Online Event flight route.
	 * @param r the Route bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Route r) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM events.EVENT_AIRPORTS WHERE (ID=?) AND (ROUTE_ID=?)")) {
			ps.setInt(1, r.getID());
			ps.setInt(2, r.getRouteID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Online Event banner image from the database.
	 * @param id the Event database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteBanner(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM events.BANNERS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an Online Event banner image.
	 * @param e the Event bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeBanner(Event e) throws DAOException {
		try (PreparedStatement ps = prepare("REPLACE INTO events.BANNERS (ID, IMG, X, Y, EXT) VALUES (?,?,?,?,LCASE(?))")) {
			ps.setInt(1, e.getID());
			ps.setBinaryStream(2, e.getInputStream(), e.getSize());
			ps.setInt(3, e.getWidth());
			ps.setInt(4, e.getHeight());
			ps.setString(5, e.getFormat().name().toLowerCase());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Toggles the availability of an Event flight route. 
	 * @param r the Route bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void toggle(Route r) throws DAOException {
		try (PreparedStatement ps = prepare("UPDATE events.EVENT_AIRPORTS SET ACTIVE=(NOT ACTIVE) WHERE (ID=?) AND (ROUTE_ID=?)")) {
			ps.setInt(1, r.getID());
			ps.setInt(2, r.getRouteID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes Online Event feedback to the database.
	 * @param f a Feedback bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Feedback f) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO events.FEEDBACK (ID, PILOT_ID, CREATED, SCORE, COMMENTS) VALUES (?,?,?,?,?)")) {
			ps.setInt(1, f.getID());
			ps.setInt(2, f.getAuthorID());
			ps.setTimestamp(3, createTimestamp(f.getCreatedOn()));
			ps.setInt(4, f.getScore());
			ps.setString(5, f.getComments());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write Chart data to the database.
	 */
	private void writeCharts(Event e) throws SQLException {

		// Clear airports
		try (PreparedStatement ps = prepare("DELETE FROM events.EVENT_CHARTS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 0);
		}

		// Do nothing if we have an empty list
		if (e.getCharts().isEmpty())
			return;

		// Write the charts
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO events.EVENT_CHARTS (ID, CHART) VALUES (?, ?)")) {
			ps.setInt(1, e.getID());
			for (Chart c : e.getCharts()) {
				ps.setInt(2, c.getID());
				ps.addBatch();
			}

			executeUpdate(ps, 1, e.getCharts().size());
		}
	}

	private void writeAirports(Event e) throws SQLException {

		// Add the airports
		int maxRouteID = 0;
		for (Route r : e.getRoutes()) {
			maxRouteID = Math.max(maxRouteID, r.getRouteID() + 1);
			if (r.getRouteID() == 0)
				r.setRouteID(maxRouteID);
				
			try (PreparedStatement ps = prepare("INSERT INTO events.EVENT_AIRPORTS (AIRPORT_D, AIRPORT_A, ROUTE, ACTIVE, RNAV, MAX_SIGNUPS, NAME, ROUTE_ID, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
				+ "AIRPORT_D=VALUES(AIRPORT_D), AIRPORT_A=VALUES(AIRPORT_A), ROUTE=VALUES(ROUTE), ACTIVE=VALUES(ACTIVE), RNAV=VALUES(RNAV), MAX_SIGNUPS=VALUES(MAX_SIGNUPS), NAME=VALUES(NAME)")) {
				ps.setString(1, r.getAirportD().getIATA());
				ps.setString(2, r.getAirportA().getIATA());
				ps.setString(3, r.getRoute());
				ps.setBoolean(4, r.getActive());
				ps.setBoolean(5, r.getIsRNAV());
				ps.setInt(6, r.getMaxSignups());
				ps.setString(7, r.getName());
				ps.setInt(8, r.getRouteID());
				ps.setInt(9, e.getID());
				executeUpdate(ps, 1);
			}
		}
	}

	/*
	 * Writes participating airlines to the database.
	 */
	private void writeAirlines(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM events.AIRLINES WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 0);
		}
		
		try (PreparedStatement ps = prepare("INSERT INTO events.AIRLINES (ID, AIRLINE) VALUES (?, ?)")) {
			ps.setInt(1, e.getID());
			for (AirlineInformation ai : e.getAirlines()) {
				ps.setString(2, ai.getCode());
				ps.addBatch();
			}

			executeUpdate(ps, 1, e.getAirlines().size());
		}
	}

	/*
	 * Writes equipment types to the database.
	 */
	private void writeEQTypes(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM events.EVENT_EQTYPES WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 0);
		}

		try (PreparedStatement ps = prepare("INSERT INTO events.EVENT_EQTYPES (ID, RATING) VALUES (?, ?)")) {
			ps.setInt(1, e.getID());
			for (String eq : e.getEquipmentTypes()) {
				ps.setString(2, eq);
				ps.addBatch();
			}

			executeUpdate(ps, 1, e.getEquipmentTypes().size());
		}
	}
	
	/*
	 * Writes featured Airports to the database.
	 */
	private void writeFeaturedAirports(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM events.AIRPORTS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 0);
		}
		
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO events.AIRPORTS (ID, AIRPORT) VALUES (?,?)")) {
			ps.setInt(1, e.getID());
			for (Airport a : e.getFeaturedAirports()) {
				ps.setString(2, a.getIATA());
				ps.addBatch();
			}
			
			executeUpdate(ps, 1, e.getFeaturedAirports().size());
		}
	}
	
	/*
	 * Writes briefing data to the database.
	 */
	private void writeBriefing(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM events.BRIEFINGS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 0);
		}
		
		Briefing b = e.getBriefing();
		if (b == null) return;
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO events.BRIEFINGS (ID, ISPDF, SIZE, DATA) VALUES (?, ?, ?, ?)")) {
			ps.setInt(1, e.getID());
			ps.setBoolean(2, b.getIsPDF());
			ps.setInt(3, b.getSize());
			ps.setBinaryStream(4, b.getInputStream());
			executeUpdate(ps, 1);
		}
	}

	/*
	 * Writes ATC contact addresses to the database.
	 */
	private void writeContactAddrs(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM events.EVENT_CONTACTS WHERE (ID=?)")) {
			ps.setInt(1, e.getID());
			executeUpdate(ps, 0);
		}

		try (PreparedStatement ps = prepare("INSERT INTO events.EVENT_CONTACTS (ID, ADDRESS) VALUES (?, ?)")) {
			ps.setInt(1, e.getID());
			for (String addr : e.getContactAddrs()) {
				ps.setString(2, addr);
				ps.addBatch();
			}

			executeUpdate(ps, 1, e.getContactAddrs().size());
		}
	}

	/*
	 * Adds a new Online Event to the database.
	 */
	private void insert(Event e) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO events.EVENTS (TITLE, NETWORK, STATUS, STARTTIME, ENDTIME, SU_DEADLINE, CAN_SIGNUP, SIGNUP_URL, OWNER) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setString(1, e.getName());
			ps.setInt(2, e.getNetwork().ordinal());
			ps.setInt(3, e.getStatus().ordinal());
			ps.setTimestamp(4, createTimestamp(e.getStartTime()));
			ps.setTimestamp(5, createTimestamp(e.getEndTime()));
			ps.setTimestamp(6, createTimestamp(e.getCanSignup() ? e.getSignupDeadline() : e.getStartTime()));
			ps.setBoolean(7, e.getCanSignup());
			ps.setString(8, e.getSignupURL());
			ps.setString(9, e.getOwner().getCode());

			// Execute the update and get the Event ID
			executeUpdate(ps, 1);
			e.setID(getNewID());
		}
	}

	/*
	 * Updates an existing Online Event in the database.
	 */
	private void update(Event e) throws SQLException {
		try (PreparedStatement ps = prepare("UPDATE events.EVENTS SET TITLE=?, NETWORK=?, STARTTIME=?, ENDTIME=?, SU_DEADLINE=?, CAN_SIGNUP=?, SIGNUP_URL=?, STATUS=?, OWNER=? WHERE (ID=?)")) {
			ps.setString(1, e.getName());
			ps.setInt(2, e.getNetwork().ordinal());
			ps.setTimestamp(3, createTimestamp(e.getStartTime()));
			ps.setTimestamp(4, createTimestamp(e.getEndTime()));
			ps.setTimestamp(5, createTimestamp(e.getCanSignup() ? e.getSignupDeadline() : e.getStartTime()));
			ps.setBoolean(6, e.getCanSignup());
			ps.setString(7, e.getSignupURL());
			ps.setInt(8, e.getStatus().ordinal());
			ps.setString(9, e.getOwner().getCode());
			ps.setInt(10, e.getID());
			executeUpdate(ps, 1);
		}
	}
}