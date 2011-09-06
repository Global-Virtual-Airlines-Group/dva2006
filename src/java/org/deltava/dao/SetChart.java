// Copyright 2005, 2006, 2007, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.io.IOException;

import org.deltava.beans.schedule.*;

import org.deltava.crypt.MessageDigester;

/**
 * A Data Access Object to write Approach Charts.
 * @author Luke
 * @version 4.0
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

			startTransaction();

			// Write the metadata
			prepareStatementWithoutLimits("REPLACE INTO common.CHARTS (ICAO, TYPE, IMGFORMAT, NAME, SIZE, LASTMODIFIED, HASH, ID) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, c.getAirport().getICAO());
			_ps.setInt(2, c.getType());
			_ps.setInt(3, c.getImgType());
			_ps.setString(4, c.getName());
			_ps.setInt(5, c.getSize());
			_ps.setTimestamp(6, createTimestamp(c.getLastModified()));
			_ps.setString(7, MessageDigester.convert(md5data));
			_ps.setInt(8, c.getID());
			executeUpdate(1);

			// Get the database ID
			if (c.getID() == 0)
				c.setID(getNewID());
			
			// Write the image
			prepareStatementWithoutLimits("INSERT INTO common.CHARTIMGS (ID, IMG) VALUES (?, ?)");
			_ps.setInt(1, c.getID());
			_ps.setBinaryStream(2, c.getInputStream(), c.getSize());
			executeUpdate(1);
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	/**
	 * Writes an external chart to the database.
	 * @param ec an ExternalChart
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeExternal(ExternalChart ec) throws DAOException {
		try {
			startTransaction();
			
			// Write the metadata
			prepareStatementWithoutLimits("REPLACE INTO common.CHARTS (ICAO, TYPE, IMGFORMAT, NAME, SIZE, LASTMODIFIED, "
					+ "HASH, ID) VALUES (?, ?, ?, ?, ?, ?, 'EXT', ?)");
			_ps.setString(1, ec.getAirport().getICAO());
			_ps.setInt(2, ec.getType());
			_ps.setInt(3, ec.getImgType());
			_ps.setString(4, ec.getName());
			_ps.setInt(5, ec.getSize());
			_ps.setTimestamp(6, createTimestamp(ec.getLastModified()));
			_ps.setInt(7, ec.getID());
			executeUpdate(1);

			// Get the database ID
			if (ec.getID() == 0)
				ec.setID(getNewID());
			
			// Write the URL
			prepareStatementWithoutLimits("INSERT INTO common.CHARTURLS (ID, SOURCE, URL) VALUES (?, ?, ?)");
			_ps.setInt(1, ec.getID());
			_ps.setString(2, ec.getSource());
			_ps.setString(3, ec.getURL());
			executeUpdate(1);

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
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
			prepareStatementWithoutLimits("UPDATE common.CHARTS SET ICAO=?, NAME=?, TYPE=? WHERE (ID=?)");
			_ps.setString(1, c.getAirport().getICAO());
			_ps.setString(2, c.getName());
			_ps.setInt(3, c.getType());
			_ps.setInt(4, c.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an Approach Chart's usage counter.
	 * @param c the Approach Chart
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logUse(Chart c) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.CHARTS SET USECOUNT=USECOUNT+1 WHERE (ID=?)");
			_ps.setInt(1, c.getID());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges all Approach Charts for a given Airport.
	 * @param a the Airport
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(Airport a) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.CHARTS WHERE (ICAO=?)");
			_ps.setString(1, a.getICAO());
			executeUpdate(0);
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