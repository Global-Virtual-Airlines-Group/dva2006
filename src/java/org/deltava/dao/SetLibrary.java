// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.fleet.*;

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
			prepareStatement("INSERT INTO DOWNLOADS (FILENAME, DATE, USER_ID) VALUES (?, ?, ?)");
			_ps.setString(1, fName);
			_ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			_ps.setInt(3, pilotID);

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Manaul to the Document Library.
	 * @param m the Manual metadata
	 * @throws DAOException
	 */
	public void createManual(Manual m) throws DAOException {
		try {
			prepareStatement("INSERT INTO DOCS (FILENAME, NAME, FILESIZE, VERSION, SECURITY, BODY) VALUES " + "(?, ?, ?, ?, ?, ?)");
			_ps.setString(1, m.getFileName());
			_ps.setString(2, m.getName());
			_ps.setLong(3, m.getSize());
			_ps.setInt(4, m.getMajorVersion());
			_ps.setInt(5, m.getSecurity());
			_ps.setString(6, m.getDescription());

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Manual in the Document Library.
	 * @param m the Manual metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateManual(Manual m) throws DAOException {
		try {
			prepareStatement("UPDATE DOCS SET NAME=?, FILESIZE=?, VERSION=?, SECURITY=?, BODY=? WHERE (FILENAME=?)");
			_ps.setString(1, m.getName());
			_ps.setLong(2, m.getSize());
			_ps.setInt(3, m.getMajorVersion());
			_ps.setInt(4, m.getSecurity());
			_ps.setString(5, m.getDescription());
			_ps.setString(6, m.getFileName());

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Installer to the Fleet Library.
	 * @param i the Installer metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void createInstaller(Installer i) throws DAOException {
		try {
			prepareStatement("INSERT INTO FLEET (FILENAME, NAME, IMG, FILESIZE, MAJOR, MINOR, SUBMINOR, "
					+ "SECURITY, CODE, BODY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, i.getFileName());
			_ps.setString(2, i.getName());
			_ps.setString(3, i.getImage());
			_ps.setLong(4, i.getSize());
			_ps.setInt(5, i.getMajorVersion());
			_ps.setInt(6, i.getMinorVersion());
			_ps.setInt(7, i.getSubVersion());
			_ps.setInt(8, i.getSecurity());
			_ps.setString(9, i.getCode());
			_ps.setString(10, i.getDescription());

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Installer in the Fleet Library.
	 * @param i the Installer metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateInstaller(Installer i) throws DAOException {
		try {
			prepareStatement("UPDATE FLEET SET NAME=?, IMG=?, FILESIZE=?, MAJOR=?, MINOR=?, SUBMINOR=?, "
					+ "SECURITY=?, CODE=?, BODY=? WHERE (FILENAME=?)");
			_ps.setString(1, i.getName());
			_ps.setString(2, i.getImage());
			_ps.setLong(3, i.getSize());
			_ps.setInt(4, i.getMajorVersion());
			_ps.setInt(5, i.getMinorVersion());
			_ps.setInt(6, i.getSubVersion());
			_ps.setInt(7, i.getSecurity());
			_ps.setString(8, i.getCode());
			_ps.setString(9, i.getDescription());
			_ps.setString(10, i.getFileName());

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
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
			
			// Execute the update
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}