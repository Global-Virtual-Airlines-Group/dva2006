// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.io.IOException;

import org.deltava.beans.schedule.*;

import org.deltava.crypt.MessageDigester;

/**
 * A Data Access Object to write Approach Charts.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepare("INSERT INTO common.CHARTS (ICAO, TYPE, IMGFORMAT, NAME, SIZE, LASTMODIFIED, HASH, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) AS N ON DUPLICATE KEY UPDATE "
				+ "ICAO=N.ICAO, TYPE=N.TYPE, IMGFORMAT=N.IMGFORMAT, NAME=N.NAME, SIZE=N.SIZE, LASTMODIFIED=N.LASTMODIFIED, HASH=N.HASH")) {
				ps.setString(1, c.getAirport().getICAO());
				ps.setInt(2, c.getType().ordinal());
				ps.setInt(3, c.getImgType().ordinal());
				ps.setString(4, c.getName());
				ps.setInt(5, c.getSize());
				ps.setTimestamp(6, createTimestamp(c.getLastModified()));
				ps.setString(7, md5);
				ps.setInt(8, c.getID());
				executeUpdate(ps, 1);
			}

			// Get the database ID
			if (c.getID() == 0) c.setID(getNewID());
			
			// Write the image
			if (c.isLoaded()) {
				try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.CHARTIMGS (ID, IMG) VALUES (?, ?)")) {
					ps.setInt(1, c.getID());
					ps.setBinaryStream(2, c.getInputStream(), c.getSize());
					executeUpdate(ps, 1);
				}
			}
			
			// Write the URL
			if (c.getIsExternal()) {
				ExternalChart ec = (ExternalChart) c;
				try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.CHARTURLS (ID, SOURCE, URL, EXTERNAL_ID) VALUES (?, ?, ?, ?)")) {
					ps.setInt(1, ec.getID());
					ps.setString(2, ec.getSource());
					ps.setString(3, ec.getURL());
					ps.setString(4, ec.getExternalID());
					executeUpdate(ps, 1);
				}
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.CHARTS SET ICAO=?, NAME=?, TYPE=?, LASTMODIFIED=? WHERE (ID=?)")) {
			ps.setString(1, c.getAirport().getICAO());
			ps.setString(2, c.getName());
			ps.setInt(3, c.getType().ordinal());
			ps.setTimestamp(4, createTimestamp(c.getLastModified()));
			ps.setInt(5, c.getID());
			executeUpdate(ps, 1);
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
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.CHARTS SET LASTMODIFIED=NOW(), SIZE=?, HASH=? WHERE (ID=?)")) {
				ps.setInt(1, c.getSize());
				ps.setString(2, md5);
				ps.setInt(3, c.getID());
				executeUpdate(ps, 1);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.CHARTIMGS (ID, IMG) VALUES (?, ?)")) {
				ps.setInt(1, c.getID());
				ps.setBinaryStream(2, c.getInputStream(), c.getSize());
				executeUpdate(ps, 1);
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.CHARTS SET USECOUNT=USECOUNT+1 WHERE (ID=?)")) {
			ps.setInt(1, c.getID());
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.CHARTS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}