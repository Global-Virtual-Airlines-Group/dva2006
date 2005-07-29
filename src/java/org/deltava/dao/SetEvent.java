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
			
			// Delete the record and clean up
			_ps.executeUpdate();
			_ps.close();
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
			
			// Delete the entry and clean up
			_ps.executeUpdate();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	private void writeCharts(Event e) throws SQLException {
		
		// Clear airports
		prepareStatement("DELETE FROM common.EVENT_CHARTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		_ps.executeUpdate();
		_ps.close();
		
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
		_ps.executeUpdate();
		_ps.close();
		
		// Create the prepared statement
		prepareStatement("INSERT INTO common.EVENT_AIRPORTS (ID, AIRPORT_D, AIRPORT_A) VALUES (?, ?, ?)");
		_ps.setInt(1, e.getID());
		_ps.setString(3, e.getAirportA().getIATA());
		
		// Add the airports
		for (Iterator i = e.getAirportD().iterator(); i.hasNext(); ) {
			Airport a = (Airport) i.next();
			_ps.setString(2, a.getIATA());
			_ps.addBatch();
		}
		
		// Update the database and clearn up
		_ps.executeBatch();
		_ps.close();
	}
	
	private void insert(Event e) throws SQLException {
		
		// Prepare the statement
		prepareStatement("INSERT INTO common.EVENTS (TITLE, NETWORK, STARTTIME, ENDTIME, SU_DEADLINE, "
				+ "ROUTE, BRIEFING) VALUES (?, ?, ?, ?, ?, ?, ?)");
		_ps.setString(1, e.getName());
		_ps.setInt(2, e.getNetwork());
		_ps.setTimestamp(3, createTimestamp(e.getStartTime()));
		_ps.setTimestamp(4, createTimestamp(e.getEndTime()));
		_ps.setTimestamp(5, createTimestamp(e.getSignupDeadline()));
		_ps.setString(6, e.getRoute());
		_ps.setString(7, e.getBriefing());
		
		// Execute the update and get the Event ID
		executeUpdate(1);
		e.setID(getNewID());
	}
	
	private void update(Event e) throws SQLException {
		
		// Prepare the statement
		prepareStatement("UPDATE common.EVENTS SET TITLE=?, NETWORK=?, STARTTIME=?, ENDTIME=?, SU_DEADLINE=? "
				+ "ROUTE=?, BRIEFING=? WHERE (ID=?)");
		_ps.setString(1, e.getName());
		_ps.setInt(2, e.getNetwork());
		_ps.setTimestamp(3, createTimestamp(e.getStartTime()));
		_ps.setTimestamp(4, createTimestamp(e.getEndTime()));
		_ps.setTimestamp(5, createTimestamp(e.getSignupDeadline()));
		_ps.setString(6, e.getRoute());
		_ps.setString(7, e.getBriefing());
		_ps.setInt(8, e.getID());
		
		// Execute the Update
		executeUpdate(1);
	}
}