// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.fleet.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to write and update Fleet/Document Library metadata.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetLibrary extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetLibrary(Connection c) {
		super(c);
	}

	/**
	 * Logs a file downlaod.
	 * @param fName the download file name
	 * @param pilotID the Database ID of the user downloading the file
	 * @throws DAOException if a JDBC error occurs
	 */
	public void download(String fName, int pilotID) throws DAOException {
		try {
			prepareStatement("REPLACE INTO DOWNLOADS (FILENAME, DATE, USER_ID) VALUES (?, NOW(), ?)");
			_ps.setString(1, fName);
			_ps.setInt(2, pilotID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a Manual to the Document Library. This handles INSERT and UPDATE operations.
	 * @param m the Manual metadata
	 * @param isNew if we are performing an INSERT instead of an UPDATE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Manual m, boolean isNew) throws DAOException {
		try {
			startTransaction();
			if (isNew)
				prepareStatement("INSERT INTO DOCS (NAME, FILESIZE, VERSION, SECURITY, BODY, ONREG, FILENAME) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)");
			else
				prepareStatement("UPDATE DOCS SET NAME=?, FILESIZE=?, VERSION=?, SECURITY=?, BODY=?, ONREG=? "
						+ "WHERE (FILENAME=?)");

			// Update the prepared statement
			_ps.setString(1, m.getName());
			_ps.setLong(2, m.getSize());
			_ps.setInt(3, m.getMajorVersion());
			_ps.setInt(4, m.getSecurity());
			_ps.setString(5, m.getDescription());
			_ps.setBoolean(6, m.getShowOnRegister());
			_ps.setString(7, m.getFileName());
			executeUpdate(1);
			
			// Clean out the certification names
			if (!isNew) {
				prepareStatementWithoutLimits("DELETE FROM CERTDOCS WHERE (FILENAME=?)");
				_ps.setString(1, m.getFileName());
				executeUpdate(0);
			}
			
			// Write the certification names
			prepareStatement("INSERT INTO CERTDOCS (CERTNAME, FILENAME) VALUES (?, ?)");
			_ps.setString(2, m.getFileName());
			for (Iterator<String> i = m.getCertifications().iterator(); i.hasNext(); ) {
				_ps.setString(1, i.next());
				_ps.addBatch();
			}
			
			// Execute the batch statement
			_ps.executeBatch();
			_ps.close();
			
			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Newsletter to the Document Library. This handles INSERT and UPDATE operations.
	 * @param nws the Newsletter metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Newsletter nws) throws DAOException {
		try {
			if (nws.getDownloadCount() == 0)
				prepareStatement("REPLACE INTO NEWSLETTERS (NAME, CATEGORY, FILESIZE, SECURITY, PUBLISHED, "
						+ "BODY, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?)");
			else
				prepareStatement("UPDATE NEWSLETTERS SET NAME=?, CATEGORY=?, FILESIZE=?, SECURITY=?, "
						+ "PUBLISHED=?, BODY=? WHERE (FILENAME=?)");
			
			// Update the prepared statement
			_ps.setString(1, nws.getName());
			_ps.setString(2, nws.getCategory());
			_ps.setLong(3, nws.getSize());
			_ps.setInt(4, nws.getSecurity());
			_ps.setTimestamp(5, createTimestamp(nws.getDate()));
			_ps.setString(6, nws.getDescription());
			_ps.setString(7, nws.getFileName());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an Installer to the Fleet Library. This handles INSERT and UPDATE operations.
	 * @param i the Installer metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Installer i) throws DAOException {
		try {
			startTransaction();
			
			// Clean out airline entries
			prepareStatementWithoutLimits("DELETE FROM FLEET_AIRLINE WHERE (FILENAME=?)");
			_ps.setString(1, i.getFileName());
			executeUpdate(0);
			
			// Prepare the statement
			if (i.getDownloadCount() == 0)
				prepareStatement("REPLACE INTO FLEET (NAME, IMG, FILESIZE, MAJOR, MINOR, SUBMINOR, SECURITY, "
						+ "CODE, BODY, FSVERSION, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			else
				prepareStatement("UPDATE FLEET SET NAME=?, IMG=?, FILESIZE=?, MAJOR=?, MINOR=?, SUBMINOR=?, "
						+ "SECURITY=?, CODE=?, BODY=?, FSVERSION=? WHERE (FILENAME=?)");

			// Update the prepared statement
			_ps.setString(1, i.getName());
			_ps.setString(2, i.getImage());
			_ps.setLong(3, i.getSize());
			_ps.setInt(4, i.getMajorVersion());
			_ps.setInt(5, i.getMinorVersion());
			_ps.setInt(6, i.getSubVersion());
			_ps.setInt(7, i.getSecurity());
			_ps.setString(8, i.getCode());
			_ps.setString(9, i.getDescription());
			_ps.setString(10, StringUtils.listConcat(i.getFSVersions(), ","));
			_ps.setString(11, i.getFileName());
			executeUpdate(1);
			
			// Write the app entries
			prepareStatement("INSERT INTO FLEET_AIRLINE (FILENAME, CODE) VALUES (?, ?)");
			_ps.setString(1, i.getFileName());
			for (Iterator<AirlineInformation> ai = i.getApps().iterator(); ai.hasNext();) {
				AirlineInformation info = ai.next();
				_ps.setString(2, info.getCode());
				_ps.addBatch();
			}
			
			// Execute and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a File Library entry to the database. This handles INSERT and UPDATE operations.
	 * @param e the Library entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(FileEntry e) throws DAOException {
		try {
			if (e.getDownloadCount() == 0)
				prepareStatement("REPLACE INTO FILES (NAME, FILESIZE, SECURITY, AUTHOR, BODY, FILENAME) VALUES "
						+ "(?, ?, ?, ?, ?, ?)");
			else
				prepareStatement("UPDATE FILES SET NAME=?, FILESIZE=?, SECURITY=?, AUTHOR=?, BODY=? WHERE "
						+ "(FILENAME=?)");

			// Update the prepared statement
			_ps.setString(1, e.getName());
			_ps.setLong(2, e.getSize());
			_ps.setInt(3, e.getSecurity());
			_ps.setInt(4, e.getAuthorID());
			_ps.setString(5, e.getDescription());
			_ps.setString(6, e.getFileName());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Video Library entry to the database. This handles INSERT and UPDATE operations.
	 * @param v the Library entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Video v) throws DAOException {
		try {
			if (v.getDownloadCount() == 0)
				prepareStatement("REPLACE INTO VIDEOS (NAME, FILESIZE, SECURITY, AUTHOR, CATEGORY, BODY, "
						+ "FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?)");
			else
				prepareStatement("UPDATE VIDEOS SET NAME=?, FILESIZE=?, SECURITY=?, AUTHOR=?, CATEGORY=?, "
						+ "BODY=? WHERE (FILENAME=?)");
			
			// Update the prepared statement
			_ps.setString(1, v.getName());
			_ps.setLong(2, v.getSize());
			_ps.setInt(3, v.getSecurity());
			_ps.setInt(4, v.getAuthorID());
			_ps.setString(5, v.getCategory());
			_ps.setString(6, v.getDescription());
			_ps.setString(7, v.getFileName());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Library Entry from the database.
	 * @param entry the Library entry
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if an unknown LibraryEntry subclass is passed
	 */
	public void delete(LibraryEntry entry) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		if (entry instanceof Installer)
			sqlBuf.append("FLEET");
		else if (entry instanceof Manual)
			sqlBuf.append("DOCS");
		else if (entry instanceof FileEntry)
			sqlBuf.append("FILES");
		else if (entry instanceof Video)
			sqlBuf.append("VIDEOS");
		else
			throw new IllegalArgumentException("Unknown library entry type - " + entry.getClass().getName());

		sqlBuf.append(" WHERE (FILENAME=?)");

		try {
			startTransaction();

			// Delete the Library entry
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, entry.getFileName());
			executeUpdate(1);

			// Delete the downloads
			prepareStatementWithoutLimits("DELETE FROM DOWNLOADS WHERE (FILENAME=?)");
			_ps.setString(1, entry.getFileName());
			executeUpdate(0);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes Fleet Installer local System Information to the database.
	 * @param si the SystemInformation bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(SystemInformation si) throws DAOException {
		try {
			prepareStatement("REPLACE INTO common.SYSINFODATA (ID, OS, GPU, CPU, DIRECTX, MEMORY, "
					+ "FSVERSION, INSTALLER, CREATED) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, si.getID());
			_ps.setString(2, si.getOS());
			_ps.setString(3, si.getGPU());
			_ps.setString(4, si.getCPU());
			_ps.setString(5, si.getDirectX());
			_ps.setInt(6, si.getRAM());
			_ps.setInt(7, si.getFSVersion());
			_ps.setString(8, si.getCode());
			_ps.setTimestamp(9, createTimestamp(si.getDate()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}