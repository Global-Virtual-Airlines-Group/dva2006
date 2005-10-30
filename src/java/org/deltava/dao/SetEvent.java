// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.event.*;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to write Online Event data.
 * @author Luke
 * @version 1.0
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
			} else {
				update(e);
			}
			
			// Write the child rows
			writeAirports(e);
			writeCharts(e);
			writeEQTypes(e);
			writeRoutes(e);
			
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
			prepareStatement("INSERT INTO common.EVENT_SIGNUPS (ID, PILOT_ID, EQTYPE, AIRPORT_D, AIRPORT_A, REMARKS) "
					+ "VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, s.getEventID());
			_ps.setInt(2, s.getPilotID());
			_ps.setString(3, s.getEquipmentType());
			_ps.setString(4, s.getAirportD().getIATA());
			_ps.setString(5, s.getAirportA().getIATA());
			_ps.setString(6, s.getRemarks());
			
			// Update the database
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
			prepareStatement("REPLACE INTO common.EVENT_AIRPORTS (ID, AIRPORT_D, AIRPORT_A, ROUTE) VALUES "
					+ "(?, ?, ?, ?)");
			_ps.setInt(1, r.getID());
			_ps.setString(2, r.getAirportD().getIATA());
			_ps.setString(3, r.getAirportA().getIATA());
			_ps.setString(4, r.getRoute());
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
			prepareStatement("REPLACE INTO common.EVENT_PLANS (ID, PLANTYPE, AIRPORT_D, AIRPORT_A, PLANDATA) "
					+ "VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, fp.getID());
			_ps.setInt(2, fp.getType());
			_ps.setString(3, fp.getAirportD().getIATA());
			_ps.setString(4, fp.getAirportA().getIATA());
			_ps.setBinaryStream(5, fp.getInputStream(), fp.getSize());
			
			// Write the entry
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
			prepareStatement("DELETE FROM common.EVENT_SIGNUPS WHERE (ID=?) AND (PILOT_ID=?)");
			_ps.setInt(1, s.getEventID());
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
			prepareStatement("DELETE FROM common.EVENT_PLANS WHERE (ID=?) AND (PLANTYPE=?) AND (AIRPORT_D=?) "
					+ "AND (AIRPORT_A=?)");
			_ps.setInt(1, fp.getID());
			_ps.setInt(2, fp.getType());
			_ps.setString(3, fp.getAirportD().getIATA());
			_ps.setString(4, fp.getAirportA().getIATA());
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
			startTransaction();
			
			// Delete the route 
			prepareStatement("DELETE FROM common.EVENT_AIRPORTS WHERE (ID=?) AND (AIRPORT_D=?) AND (AIRPORT_A=?)");
			_ps.setInt(1, r.getID());
			_ps.setString(2, r.getAirportD().getIATA());
			_ps.setString(3, r.getAirportA().getIATA());
			executeUpdate(1);
			
			// Delete the signups
			prepareStatement("DELETE FROM common.EVENT_SIGNUPS WHERE (ID=?) AND (AIRPORT_D=?) AND (AIRPORT_A=?)");
			_ps.setInt(1, r.getID());
			_ps.setString(2, r.getAirportD().getIATA());
			_ps.setString(3, r.getAirportA().getIATA());
			executeUpdate(0);
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	private void writeCharts(Event e) throws SQLException {
		
		// Clear airports
		prepareStatement("DELETE FROM common.EVENT_CHARTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		executeUpdate(0);
		
		// Do nothing if we have an empty list
		if (e.getCharts().isEmpty())
			return;
		
		// Create the prepared statement
		prepareStatement("INSERT INTO common.EVENT_CHARTS (ID, CHART) VALUES (?, ?)");
		_ps.setInt(1, e.getID());
		
		// Write the charts
		for (Iterator i = e.getCharts().iterator(); i.hasNext(); ) {
			Chart c = (Chart) i.next();
			_ps.setInt(2, c.getID());
			_ps.addBatch();
		}
		
		// Write to the database and clean up
		_ps.executeBatch();
		_ps.close();
	}
	
	private void writeAirports(Event e) throws SQLException {
		
		// Clear airports
		prepareStatement("DELETE FROM common.EVENT_AIRPORTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		executeUpdate(0);
		
		// Create the prepared statement
		prepareStatement("INSERT INTO common.EVENT_AIRPORTS (ID, AIRPORT_D, AIRPORT_A, ROUTE) VALUES (?, ?, ?, ?)");
		_ps.setInt(1, e.getID());
		
		// Add the airports
		for (Iterator i = e.getRoutes().iterator(); i.hasNext(); ) {
			Route r = (Route) i.next();
			_ps.setString(2, r.getAirportD().getIATA());
			_ps.setString(3, r.getAirportA().getIATA());
			_ps.setString(4, r.getRoute());
			_ps.addBatch();
		}
		
		// Update the database and clearn up
		_ps.executeBatch();
		_ps.close();
	}
	
	private void writeEQTypes(Event e) throws SQLException {
	   
	   // Clear equipment types
	   prepareStatement("DELETE FROM common.EVENT_EQTYPES WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		_ps.executeUpdate();
		_ps.close();

		// Add the equipment types
		prepareStatement("INSERT INTO common.EVENT_EQTYPES (ID, RATING) VALUES (?, ?)");
		_ps.setInt(1, e.getID());
		for (Iterator i = e.getEquipmentTypes().iterator(); i.hasNext(); ) {
			_ps.setString(2, (String) i.next());
			_ps.addBatch();
		}

		// Update the database and clearn up
		_ps.executeBatch();
		_ps.close();
	}
	
	private void writeRoutes(Event e) throws SQLException {

		// Clear routes
		prepareStatement("DELETE FROM common.EVENT_AIRPORTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		executeUpdate(0);
		
		// Create the prepared statement
		prepareStatement("INSERT INTO common.EVENT_AIRPORTS (ID, AIRPORT_D, AIRPORT_A, ROUTE) VALUES (?, ?, ?, ?)");
		_ps.setInt(1, e.getID());
		for (Iterator i = e.getRoutes().iterator(); i.hasNext(); ) {
			Route r = (Route) i.next();
			_ps.setString(2, r.getAirportD().getIATA());
			_ps.setString(3, r.getAirportA().getIATA());
			_ps.setString(4, r.getRoute());
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
		prepareStatement("INSERT INTO common.EVENTS (TITLE, NETWORK, STATUS, STARTTIME, ENDTIME, SU_DEADLINE, "
				+ "BRIEFING) VALUES (?, ?, ?, ?, ?, ?, ?)");
		_ps.setString(1, e.getName());
		_ps.setInt(2, e.getNetwork());
		_ps.setInt(3, e.getStatus());
		_ps.setTimestamp(4, createTimestamp(e.getStartTime()));
		_ps.setTimestamp(5, createTimestamp(e.getEndTime()));
		_ps.setTimestamp(6, createTimestamp(e.getSignupDeadline()));
		_ps.setString(7, e.getBriefing());
		
		// Execute the update and get the Event ID
		executeUpdate(1);
		e.setID(getNewID());
	}

	/**
	 * Updates an existing Online Event in the database.
	 */
	private void update(Event e) throws SQLException {
		// Prepare the statement
		prepareStatement("UPDATE common.EVENTS SET TITLE=?, NETWORK=?, STARTTIME=?, ENDTIME=?, SU_DEADLINE=?, "
				+ "BRIEFING=?, STATUS=? WHERE (ID=?)");
		_ps.setString(1, e.getName());
		_ps.setInt(2, e.getNetwork());
		_ps.setTimestamp(3, createTimestamp(e.getStartTime()));
		_ps.setTimestamp(4, createTimestamp(e.getEndTime()));
		_ps.setTimestamp(5, createTimestamp(e.getSignupDeadline()));
		_ps.setString(6, e.getBriefing());
		_ps.setInt(7, e.getStatus());
		_ps.setInt(8, e.getID());
		
		// Execute the Update
		executeUpdate(1);
	}
}