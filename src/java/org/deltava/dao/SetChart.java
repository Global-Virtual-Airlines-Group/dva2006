// Copyright 2005, 2006, 2007, 2008, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.io.IOException;

import org.deltava.beans.schedule.*;

import org.deltava.crypt.MessageDigester;

/**
 * A Data Access Object to write Approach Charts.
 * @author Luke
 * @version 5.0
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
			String md5 = null;
			if (c.isLoaded()) {
				byte[] md5data = md.digest(c.getInputStream());
				md5 = MessageDigester.convert(md5data);
			} else if (c.getIsExternal())
				md5 = "EXT";

			startTransaction();

			// Write the metadata
			if (c.getID() == 0)
				prepareStatementWithoutLimits("INSERT INTO common.CHARTS (ICAO, TYPE, IMGFORMAT, NAME, SIZE, LASTMODIFIED, HASH) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)");
			else
				prepareStatementWithoutLimits("UPDATE common.CHARTS SET ICAO=?, TYPE=?, IMGFORMAT=?, NAME=?, SIZE=?, LASTMODIFIED=?, "
					+ "HASH=? WHERE (ID=?)");
			
			_ps.setString(1, c.getAirport().getICAO());
			_ps.setInt(2, c.getType().ordinal());
			_ps.setInt(3, c.getImgType().ordinal());
			_ps.setString(4, c.getName());
			_ps.setInt(5, c.getSize());
			_ps.setTimestamp(6, createTimestamp(c.getLastModified()));
			_ps.setString(7, md5);
			if (c.getID() != 0)
				_ps.setInt(8, c.getID());
			executeUpdate(1);

			// Get the database ID
			if (c.getID() == 0)
				c.setID(getNewID());
			
			// Write the image
			if (c.isLoaded()) {
				prepareStatementWithoutLimits("REPLACE INTO common.CHARTIMGS (ID, IMG) VALUES (?, ?)");
				_ps.setInt(1, c.getID());
				_ps.setBinaryStream(2, c.getInputStream(), c.getSize());
				executeUpdate(1);
			}
			
			// Write the URL
			if (c.getIsExternal()) {
				ExternalChart ec = (ExternalChart) c;
				prepareStatementWithoutLimits("REPLACE INTO common.CHARTURLS (ID, SOURCE, URL, EXTERNAL_ID) VALUES (?, ?, ?, ?)");
				_ps.setInt(1, ec.getID());
				_ps.setString(2, ec.getSource());
				_ps.setString(3, ec.getURL());
				_ps.setString(4, ec.getExternalID());
				executeUpdate(1);
			}
			
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
			prepareStatementWithoutLimits("UPDATE common.CHARTS SET ICAO=?, NAME=?, TYPE=?, LASTMODIFIED=? WHERE (ID=?)");
			_ps.setString(1, c.getAirport().getICAO());
			_ps.setString(2, c.getName());
			_ps.setInt(3, c.getType().ordinal());
			_ps.setTimestamp(4, createTimestamp(c.getLastModified()));
			_ps.setInt(5, c.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Saves an approach chart image.
	 * @param c the Chart
	 * @throws IllegalStateException if the Chart is not loaded
	 * @throws DAOException if a JDBC error occurs
	 */
	public void save(Chart c) throws DAOException {
		if (!c.isLoaded()) throw new IllegalStateException("Chart not populated");
		try {
			MessageDigester md = new MessageDigester("MD5");
			byte[] md5data = md.digest(c.getInputStream());
			String md5 = MessageDigester.convert(md5data);
			
			startTransaction();
			prepareStatementWithoutLimits("UPDATE common.CHARTS SET LASTMODIFIED=NOW(), SIZE=?, HASH=? WHERE (ID=?)");
			_ps.setInt(1, c.getSize());
			_ps.setString(2, md5);
			_ps.setInt(3, c.getID());
			executeUpdate(1);
			prepareStatementWithoutLimits("REPLACE INTO common.CHARTIMGS (ID, IMG) VALUES (?, ?)");
			_ps.setInt(1, c.getID());
			_ps.setBinaryStream(2, c.getInputStream(), c.getSize());
			executeUpdate(1);
			commitTransaction();
		} catch (IOException | SQLException se) {
			rollbackTransaction();
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