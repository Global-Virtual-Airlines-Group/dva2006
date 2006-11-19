// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.Chart;

/**
 * A Data Access Object to write Approach Charts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetChart extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetChart(Connection c) {
		super(c);
	}

	/**
	 * Adds an Approach Chart to the database.
	 * @param c the Approach Chart
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Chart c) throws DAOException {

		// Make sure this is a new chart
		if (c.getID() != 0)
			throw new DAOException("Chart already exists");

		try {
			prepareStatement("INSERT INTO CHARTS (IATA, TYPE, NAME, SIZE, IMG) VALUES (?, ?, ?, ?, ?)");
			_ps.setString(1, c.getAirport().getIATA());
			_ps.setInt(2, c.getImgType());
			_ps.setString(3, c.getName());
			_ps.setInt(4, c.getSize());
			_ps.setBinaryStream(5, c.getInputStream(), c.getSize());

			// Update the database
			executeUpdate(1);

			// Get the database ID
			c.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an Approach Chart's metadata.
	 * @param c the Approach Chart
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Chart c) throws DAOException {
		try {
			prepareStatement("UPDATE CHARTS SET IATA=?, NAME=? WHERE (ID=?)");
			_ps.setString(1, c.getAirport().getIATA());
			_ps.setString(2, c.getName());
			_ps.setInt(3, c.getID());

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Approach Chart from the Database.
	 * @param id the Chart database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM CHARTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}