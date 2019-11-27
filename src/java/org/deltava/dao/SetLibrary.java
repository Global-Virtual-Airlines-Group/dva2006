// Copyright 2005, 2006, 2007, 2008, 2010, 2012, 2014, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.fleet.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to write and update Fleet/Document Library metadata.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO DOWNLOADS (FILENAME, DATE, USER_ID) VALUES (?, CURDATE(), ?)")) {
			ps.setString(1, fName);
			ps.setInt(2, pilotID);
			executeUpdate(ps, 1);
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
			try (PreparedStatement ps = prepare("INSERT INTO DOCS (NAME, FILESIZE, VERSION, SECURITY, BODY, ONREG, IGNORE_CERTS, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
				+ "NAME=VALUES(NAME), FILESIZE=VALUES(FILESIZE), VERSION=VALUES(VERSION), SECURITY=VALUES(SECURITY), BODY=VALUES(BODY), ONREG=VALUES(ONREG), IGNORE_CERTS=VALUES(IGNORE_CERTS)")) {
				ps.setString(1, m.getName());
				ps.setLong(2, m.getSize());
				ps.setInt(3, m.getMajorVersion());
				ps.setInt(4, m.getSecurity().ordinal());
				ps.setString(5, m.getDescription());
				ps.setBoolean(6, m.getShowOnRegister());
				ps.setBoolean(7, m.getIgnoreCertifications());
				ps.setString(8, m.getFileName());
				executeUpdate(ps, 1);
			}
			
			// Clean out the certification names
			if (!isNew) {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTDOCS WHERE (FILENAME=?)")) {
					ps.setString(1, m.getFileName());
					executeUpdate(ps, 0);
				}
			}
			
			// Write the certification names
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTDOCS (FILENAME, CERT) VALUES (?, ?)")) {
				ps.setString(1, m.getFileName());
				for (String cert : m.getCertifications()) {
					ps.setString(2, cert);
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, m.getCertifications().size());
			}
			
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
		try (PreparedStatement ps = prepare("INSERT INTO NEWSLETTERS (NAME, CATEGORY, FILESIZE, SECURITY, PUBLISHED, BODY, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLCATE KEY UPDATE "
			+ "NAME=VALUES(NAME), CATEGORY=VALUES(CATEGORY), FILESIZE=VALUES(FILESIZE), SECURITY=VALUES(SECURITY), PUBLISHED=VALUES(PUBLISHED), BODY=VALUES(BODY)")) {
			ps.setString(1, nws.getName());
			ps.setString(2, nws.getCategory());
			ps.setLong(3, nws.getSize());
			ps.setInt(4, nws.getSecurity().ordinal());
			ps.setTimestamp(5, createTimestamp(nws.getDate()));
			ps.setString(6, nws.getDescription());
			ps.setString(7, nws.getFileName());
			executeUpdate(ps, 1);
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
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM FLEET_AIRLINE WHERE (FILENAME=?)")) {
				ps.setString(1, i.getFileName());
				executeUpdate(ps, 0);
			}
			
			// Write the entry
			try (PreparedStatement ps = prepare("INSERT INTO FLEET (NAME, IMG, FILESIZE, MAJOR, MINOR, SUBMINOR, SECURITY, CODE, BODY, FSVERSION, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
				+ "ON DUPLICATE KEY UPDATE NAME=VALUES(NAME), IMG=VALUES(IMG), FILESIZE=VALUES(FILESIZE), MAJOR=VALUES(MAJOR), MINOR=VALUES(MINOR), SUBMINOR=VALUES(SUBMINOR), "
				+ "CODE=VALUES(CODE), BODY=VALUES(BODY), FSVERSION=VALUES(FSVERSION)")) {
				ps.setString(1, i.getName());
				ps.setString(2, i.getImage());
				ps.setLong(3, i.getSize());
				ps.setInt(4, i.getMajorVersion());
				ps.setInt(5, i.getMinorVersion());
				ps.setInt(6, i.getSubVersion());
				ps.setInt(7, i.getSecurity().ordinal());
				ps.setString(8, i.getCode());
				ps.setString(9, i.getDescription());
				ps.setString(10, StringUtils.listConcat(i.getFSVersions(), ","));
				ps.setString(11, i.getFileName());
				executeUpdate(ps, 1);
			}
			
			// Write the app entries
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO FLEET_AIRLINE (FILENAME, CODE) VALUES (?, ?)")) {
				ps.setString(1, i.getFileName());
				for (AirlineInformation info : i.getApps()) {
					ps.setString(2, info.getCode());
					ps.addBatch();
				}

				executeUpdate(ps, 1, i.getApps().size());
			}
			
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
		try (PreparedStatement ps = prepare("INSERT INTO FILES (NAME, FILESIZE, SECURITY, AUTHOR, BODY, FILENAME) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
			+ "NAME=VALUES(NAME), FILESIZE=VALUES(FILESIZE), SECURITY=VALUES(SECURITY), AUTHOR=VALUES(AUTHOR) BODY=VALUES(BODY)")) {
			ps.setString(1, e.getName());
			ps.setLong(2, e.getSize());
			ps.setInt(3, e.getSecurity().ordinal());
			ps.setInt(4, e.getAuthorID());
			ps.setString(5, e.getDescription());
			ps.setString(6, e.getFileName());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare((v.getDownloadCount() == 0) ? "REPLACE INTO exams.VIDEOS (NAME, FILESIZE, SECURITY, AUTHOR, CATEGORY, BODY, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?)"
			: "UPDATE exams.VIDEOS SET NAME=?, FILESIZE=?, SECURITY=?, AUTHOR=?, CATEGORY=?, BODY=? WHERE (FILENAME=?)")) {
			ps.setString(1, v.getName());
			ps.setLong(2, v.getSize());
			ps.setInt(3, v.getSecurity().ordinal());
			ps.setInt(4, v.getAuthorID());
			ps.setString(5, v.getCategory());
			ps.setString(6, v.getDescription());
			ps.setString(7, v.getFileName());
			executeUpdate(ps, 1);
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
		else if (entry instanceof Video)
			sqlBuf.append("exams.VIDEOS");
		else if (entry instanceof FileEntry)
			sqlBuf.append("FILES");
		else
			throw new IllegalArgumentException("Unknown library entry type - " + entry.getClass().getName());

		sqlBuf.append(" WHERE (FILENAME=?)");

		try {
			startTransaction();

			// Delete the Library entry
			try (PreparedStatement ps = prepare(sqlBuf.toString())) {
				ps.setString(1, entry.getFileName());
				executeUpdate(ps, 1);
			}

			// Delete the downloads
			try (PreparedStatement ps = prepare("DELETE FROM DOWNLOADS WHERE (FILENAME=?)")) {
				ps.setString(1, entry.getFileName());
				executeUpdate(ps, 0);
			}

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}