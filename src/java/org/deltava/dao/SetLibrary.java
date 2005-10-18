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
	 * Writes a Manual to the Document Library. This handles INSERT and UPDATE operations.
	 * @param m the Manual metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Manual m) throws DAOException {
		try {
		   if (m.getDownloadCount() == 0) {
		      prepareStatement("REPLACE INTO DOCS (NAME, FILESIZE, VERSION, SECURITY, BODY, FILENAME) VALUES " + 
		            "(?, ?, ?, ?, ?, ?)");
		   } else {
		      prepareStatement("UPDATE DOCS SET NAME=?, FILESIZE=?, VERSION=?, SECURITY=?, BODY=? WHERE (FILENAME=?)");		      
		   }
			
		   // Update the prepared statement
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
	 * Writes an Installer to the Fleet Library. This handles INSERT and UPDATE operations.
	 * @param i the Installer metadata
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Installer i) throws DAOException {
		try {
		   if (i.getDownloadCount() == 0) {
		      prepareStatement("REPLACE INTO FLEET (NAME, IMG, FILESIZE, MAJOR, MINOR, SUBMINOR, SECURITY, "
						+ "SECURITY, CODE, BODY, FILENAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		   } else {
		      prepareStatement("UPDATE FLEET SET NAME=?, IMG=?, FILESIZE=?, MAJOR=?, MINOR=?, SUBMINOR=?, "
					+ "SECURITY=?, CODE=?, BODY=? WHERE (FILENAME=?)");
		   }
		   
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
			_ps.setString(10, i.getFileName());

			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
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
	      if (e.getDownloadCount() == 0) {
	         prepareStatement("REPLACE INTO FILES (NAME, FILESIZE, SECURITY, AUTHOR, BODY, FILENAME) VALUES "
	               + "(?, ?, ?, ?, ?, ?)");
	      } else {
	         prepareStatement("UPDATE FILES SET NAME=?, FILESIZE=?, SECURITY=?, AUTHOR=?, BODY=? WHERE "
	               + "(FILENAME=?)");
	      }
	      
	      // Update the prepared statement
	      _ps.setString(1, e.getName());
	      _ps.setLong(2, e.getSize());
	      _ps.setInt(3, e.getSecurity());
	      _ps.setInt(4, e.getAuthorID());
	      _ps.setString(5, e.getDescription());
	      _ps.setString(6, e.getFileName());
	      
	      // Update the database
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
	   StringBuffer sqlBuf = new StringBuffer("DELETE FROM ");
	   if (entry instanceof Installer) {
	      sqlBuf.append("FLEET");
	   } else if (entry instanceof Manual) {
	      sqlBuf.append("DOCS");
	   } else if (entry instanceof FileEntry) {
	      sqlBuf.append("FILES");
	   } else {
	      throw new IllegalArgumentException("Unknown library entry type - " + entry.getClass().getName());
	   }
	   
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
			
			// Execute the update
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}