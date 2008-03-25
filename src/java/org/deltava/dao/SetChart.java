// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.io.IOException;

import org.deltava.beans.schedule.Chart;

import org.deltava.crypt.MessageDigester;

/**
 * A Data Access Object to write Approach Charts.
 * @author Luke
 * @version 2.1
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
	 * Adds or updates an Approach Chart to the database.
	 * @param c the Chart bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Chart c) throws DAOException {
		try {
			// Calculate the MD5 hash
			MessageDigester md = new MessageDigester("MD5");
			byte[] md5data = md.digest(c.getInputStream());

			// Start transaction
			startTransaction();

			// Write the metadata
			prepareStatement("REPLACE INTO common.CHARTS (ICAO, TYPE, IMGFORMAT, NAME, SIZE, HASH, ID) VALUES " +
					"(?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, c.getAirport().getICAO());
			_ps.setInt(2, c.getType());
			_ps.setInt(3, c.getImgType());
			_ps.setString(4, c.getName());
			_ps.setInt(5, c.getSize());
			_ps.setString(6, MessageDigester.convert(md5data));
			_ps.setInt(7, c.getID());
			executeUpdate(1);

			// Get the database ID
			if (c.getID() == 0)
				c.setID(getNewID());
			
			// Write the image
			prepareStatement("INSERT INTO common.CHARTIMGS (ID, IMG) VALUES (?, ?)");
			_ps.setInt(1, c.getID());
			_ps.setBinaryStream(2, c.getInputStream(), c.getSize());
			executeUpdate(1);
			
			// Commit
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Updates an Approach Chart's metadata.
	 * @param c the Approach Chart
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Chart c) throws DAOException {
		try {
			prepareStatement("UPDATE common.CHARTS SET ICAO=?, NAME=?, TYPE=? WHERE (ID=?)");
			_ps.setString(1, c.getAirport().getICAO());
			_ps.setString(2, c.getName());
			_ps.setInt(3, c.getType());
			_ps.setInt(4, c.getID());

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
			prepareStatementWithoutLimits("DELETE FROM common.CHARTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}