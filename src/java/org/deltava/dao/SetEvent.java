// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.event.*;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to write Online Event data.
 * @author Luke
 * @version 2.1
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
				writeBanner(e);
			} else
				update(e);

			// Write the child rows
			writeAirports(e);
			writeCharts(e);
			writeEQTypes(e);
			writeContactAddrs(e);

			// Commit the transaction
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
		try {
			prepareStatement("REPLACE INTO events.EVENT_SIGNUPS (ID, ROUTE_ID, PILOT_ID, EQTYPE, REMARKS) "
					+ "VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, s.getID());
			_ps.setInt(2, s.getRouteID());
			_ps.setInt(3, s.getPilotID());
			_ps.setString(4, s.getEquipmentType());
			_ps.setString(5, s.getRemarks());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Saves a new Flight Route to the database.
	 * @param r the Route bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void save(Route r) throws DAOException {
		try {
			prepareStatement("INSERT INTO events.EVENT_AIRPORTS (ID, ROUTE_ID, AIRPORT_D, AIRPORT_A, ROUTE, "
					+ "RNAV, ACTIVE, MAX_SIGNUPS, NAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, r.getID());
			_ps.setInt(2, r.getRouteID());
			_ps.setString(3, r.getAirportD().getIATA());
			_ps.setString(4, r.getAirportA().getIATA());
			_ps.setString(5, r.getRoute());
			_ps.setBoolean(6, r.getIsRNAV());
			_ps.setBoolean(7, r.getActive());
			_ps.setInt(8, r.getMaxSignups());
			_ps.setString(9, r.getName());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Saves an Online Event flight plan.
	 * @param fp the FlightPlan bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void save(FlightPlan fp) throws DAOException {
		try {
			prepareStatement("REPLACE INTO events.EVENT_PLANS (ID, ROUTE_ID, PLANTYPE, PLANDATA) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, fp.getID());
			_ps.setInt(2, fp.getRouteID());
			_ps.setInt(3, fp.getType());
			_ps.setBinaryStream(4, fp.getInputStream(), fp.getSize());
			executeUpdate(1);
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
		try {
			prepareStatement("DELETE FROM events.EVENT_SIGNUPS WHERE (ID=?) AND (PILOT_ID=?)");
			_ps.setInt(1, s.getID());
			_ps.setInt(2, s.getPilotID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Online Event flight plan.
	 * @param fp the FlightPlan bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(FlightPlan fp) throws DAOException {
		try {
			prepareStatement("DELETE FROM events.EVENT_PLANS WHERE (ID=?) AND (PLANTYPE=?) AND (ROUTE_ID=?)");
			_ps.setInt(1, fp.getID());
			_ps.setInt(2, fp.getType());
			_ps.setInt(3, fp.getRouteID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Online Event flight route.
	 * @param r the Route bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Route r) throws DAOException {
		try {
			prepareStatement("DELETE FROM events.EVENT_AIRPORTS WHERE (ID=?) AND (ROUTE_ID=?)");
			_ps.setInt(1, r.getID());
			_ps.setInt(2, r.getRouteID());
			executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("DELETE FROM events.BANNERS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
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
		try {
			prepareStatement("REPLACE INTO events.BANNERS (ID, IMG, X, Y, EXT) VALUES (?, ?, ?, ?, LCASE(?))");
			_ps.setInt(1, e.getID());
			_ps.setBinaryStream(2, e.getInputStream(), e.getSize());
			_ps.setInt(3, e.getWidth());
			_ps.setInt(4, e.getHeight());
			_ps.setString(5, e.getTypeName());
			executeUpdate(1);
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
		try {
			prepareStatement("UPDATE events.EVENT_AIRPORTS SET ACTIVE=(NOT ACTIVE) WHERE (ID=?) AND (ROUTE_ID=?)");
			_ps.setInt(1, r.getID());
			_ps.setInt(2, r.getRouteID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private void writeCharts(Event e) throws SQLException {

		// Clear airports
		prepareStatement("DELETE FROM events.EVENT_CHARTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		executeUpdate(0);

		// Do nothing if we have an empty list
		if (e.getCharts().isEmpty())
			return;

		// Create the prepared statement
		prepareStatement("INSERT INTO events.EVENT_CHARTS (ID, CHART) VALUES (?, ?)");
		_ps.setInt(1, e.getID());

		// Write the charts
		for (Iterator<Chart> i = e.getCharts().iterator(); i.hasNext();) {
			Chart c = i.next();
			_ps.setInt(2, c.getID());
			_ps.addBatch();
		}

		// Write to the database and clean up
		_ps.executeBatch();
		_ps.close();
	}

	private void writeAirports(Event e) throws SQLException {

		// Add the airports
		int maxRouteID = 0;
		for (Iterator<Route> i = e.getRoutes().iterator(); i.hasNext();) {
			Route r = i.next();
			maxRouteID = Math.max(maxRouteID, r.getRouteID() + 1);
			if (r.getRouteID() == 0) {
				r.setRouteID(maxRouteID);
				prepareStatement("INSERT INTO events.EVENT_AIRPORTS (AIRPORT_D, AIRPORT_A, ROUTE, ACTIVE, "
						+ "RNAV, MAX_SIGNUPS, NAME, ROUTE_ID, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			} else
				prepareStatement("UPDATE events.EVENT_AIRPORTS SET AIRPORT_D=?, AIRPORT_A=?, ROUTE=?, "
						+ "ACTIVE=?, RNAV=?, MAX_SIGNUPS=?, NAME=? WHERE (ROUTE_ID=?) AND (ID=?)");

			_ps.setString(1, r.getAirportD().getIATA());
			_ps.setString(2, r.getAirportA().getIATA());
			_ps.setString(3, r.getRoute());
			_ps.setBoolean(4, r.getActive());
			_ps.setBoolean(5, r.getIsRNAV());
			_ps.setInt(6, r.getMaxSignups());
			_ps.setString(7, r.getName());
			_ps.setInt(8, r.getRouteID());
			_ps.setInt(9, e.getID());
			executeUpdate(1);
		}
	}

	private void writeEQTypes(Event e) throws SQLException {

		// Clear equipment types
		prepareStatement("DELETE FROM events.EVENT_EQTYPES WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		executeUpdate(0);

		// Add the equipment types
		prepareStatement("INSERT INTO events.EVENT_EQTYPES (ID, RATING) VALUES (?, ?)");
		_ps.setInt(1, e.getID());
		for (Iterator<String> i = e.getEquipmentTypes().iterator(); i.hasNext();) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}

		// Update the database and clearn up
		_ps.executeBatch();
		_ps.close();
	}

	private void writeContactAddrs(Event e) throws SQLException {

		// Clear contacts
		prepareStatementWithoutLimits("DELETE FROM events.EVENT_CONTACTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		executeUpdate(0);

		// Create the prepared statement
		prepareStatement("INSERT INTO events.EVENT_CONTACTS (ID, ADDRESS) VALUES (?, ?)");
		_ps.setInt(1, e.getID());
		for (Iterator<String> i = e.getContactAddrs().iterator(); i.hasNext();) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}

		// Update the database and clean up
		_ps.executeBatch();
		_ps.close();
	}

	/**
	 * Adds a new Online Event to the database.
	 */
	private void insert(Event e) throws SQLException {
		prepareStatement("INSERT INTO events.EVENTS (TITLE, NETWORK, STATUS, STARTTIME, ENDTIME, SU_DEADLINE, "
				+ "BRIEFING, CAN_SIGNUP) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		_ps.setString(1, e.getName());
		_ps.setInt(2, e.getNetwork());
		_ps.setInt(3, e.getStatus());
		_ps.setTimestamp(4, createTimestamp(e.getStartTime()));
		_ps.setTimestamp(5, createTimestamp(e.getEndTime()));
		_ps.setTimestamp(6, createTimestamp(e.getCanSignup() ? e.getSignupDeadline() : e.getStartTime()));
		_ps.setString(7, e.getBriefing());
		_ps.setBoolean(8, e.getCanSignup());

		// Execute the update and get the Event ID
		executeUpdate(1);
		e.setID(getNewID());
	}

	/**
	 * Updates an existing Online Event in the database.
	 */
	private void update(Event e) throws SQLException {
		prepareStatement("UPDATE events.EVENTS SET TITLE=?, NETWORK=?, STARTTIME=?, ENDTIME=?, SU_DEADLINE=?, "
				+ "BRIEFING=?, CAN_SIGNUP=?, STATUS=? WHERE (ID=?)");
		_ps.setString(1, e.getName());
		_ps.setInt(2, e.getNetwork());
		_ps.setTimestamp(3, createTimestamp(e.getStartTime()));
		_ps.setTimestamp(4, createTimestamp(e.getEndTime()));
		_ps.setTimestamp(5, createTimestamp(e.getCanSignup() ? e.getSignupDeadline() : e.getStartTime()));
		_ps.setString(6, e.getBriefing());
		_ps.setBoolean(7, e.getCanSignup());
		_ps.setInt(8, e.getStatus());
		_ps.setInt(9, e.getID());
		executeUpdate(1);
	}
}