// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.academy.InstructionFlight;

/**
 * A Data Access Object to update Flight Academy Flight reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetAcademyFlight extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAcademyFlight(Connection c) {
		super(c);
	}

	/**
	 * Writes an Instruction flight entry to the database.
	 * @param flight the InstructionFlight bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(InstructionFlight flight) throws DAOException {
		try {
			// Prepare the statement
			prepareStatement("REPLACE INTO INSLOG (ID, INSTRUCTOR_ID, COURSE, EQTYPE, STARTDATE, "
					+ "FLIGHT_TIME, REMARKS) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, flight.getID());
			_ps.setInt(2, flight.getInstructorID());
			_ps.setInt(3, flight.getCourseID());
			_ps.setString(4, flight.getEquipmentType());
			_ps.setTimestamp(5, createTimestamp(flight.getDate()));
			_ps.setFloat(6, flight.getLength() / 10.0f);
			_ps.setString(7, flight.getComments());
			executeUpdate(1);
			
			// Update the ID
			if (flight.getID() == 0)
				flight.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Instruction Flight entry from the database.
	 * @param id the database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM INSLOG WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}