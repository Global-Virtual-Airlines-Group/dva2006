// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.io.File;
import java.util.*;
import java.sql.*;

import org.deltava.beans.fleet.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load metadata from the Fleet/Document Libraries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetLibrary extends DAO {

	/**
	 * Initialze the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetLibrary(Connection c) {
		super(c);
	}

	/**
	 * Returns the contents of a Fleet Library. This takes a database name so we can display the contents of other
	 * airlines' libraries.
	 * @param dbName the database name
	 * @return a List of Installer beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection getFleet(String dbName) throws DAOException {

		// Build the SQL statement
		StringBuffer sqlBuf = new StringBuffer("SELECT F.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".FLEET F LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOWNLOADS L ON (F.FILENAME=L.FILENAME) GROUP BY F.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			return loadInstallers();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the contents of the Document Library. This takes a database name so we can display the contents of other
	 * airlines' libraries.
	 * @param dbName the database name
	 * @return a List of Manual beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection getManuals(String dbName) throws DAOException {

		// Build the SQL statement
		StringBuffer sqlBuf = new StringBuffer("SELECT D.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOCS D LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOWNLOADS L ON (D.FILENAME=L.FILENAME) GROUP BY D.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			return loadManuals();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns metadata about a specific Manual <i>in the current database</i>.
	 * @param fName the filename
	 * @return a Manual, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Manual getManual(String fName) throws DAOException {
		try {
			prepareStatement("SELECT D.*, COUNT(L.FILENAME) FROM DOCS D LEFT JOIN DOWNLOADS L ON "
					+ "(D.FILENAME=L.FILENAME) WHERE (D.FILENAME=?) GROUP BY D.NAME ORDER BY D.NAME");
			_ps.setString(1, fName);
			setQueryMax(1);

			// Get results - if empty return null
			List results = loadManuals();
			return results.isEmpty() ? null : (Manual) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns metadata about a specifc Installer <i>in the current database</i>.
	 * @param fName the filename
	 * @return an Installer, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Installer getInstaller(String fName) throws DAOException {
		try {
			prepareStatement("SELECT F.*, COUNT(L.FILENAME) FROM FLEET F LEFT JOIN DOWNLOADS L ON "
					+ "(F.FILENAME=L.FILENAME) WHERE (F.FILENAME=?) GROUP BY F.NAME ORDER BY F.NAME");
			_ps.setString(1, fName);
			setQueryMax(1);

			// Get results - if empty return null
			List results = loadInstallers();
			return results.isEmpty() ? null : (Installer) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns metadata about a specifc Installer <i>in the current database</i>.
	 * @param code the Installer code
	 * @param dbName the database Name
	 * @return an Installer, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Installer getInstallerByCode(String code, String dbName) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuffer sqlBuf = new StringBuffer("SELECT F.*, COUNT(L.FILENAME) FROM ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".FLEET F LEFT JOIN ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".DOWNLOADS L ON (F.FILENAME=L.FILENAME) WHERE (UCASE(F.CODE)=?) GROUP BY "
	         + "F.NAME ORDER BY F.NAME"); 
	   
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, code.toUpperCase());
			setQueryMax(1);
			
			// Get results - if empty return null
			List results = loadInstallers();
			return results.isEmpty() ? null : (Installer) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns metadata about a specifc file <i>in the current database</i>.
	 * @param fName the filename
	 * @return a FileEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FileEntry getFile(String fName) throws DAOException {
	   try {
	      prepareStatement("SELECT F.*, COUNT(L.FILENAME) FROM FILES F LEFT JOIN DOWNLOADS L ON "
					+ "(F.FILENAME=L.FILENAME) WHERE (F.FILENAME=?) GROUP BY F.NAME ORDER BY F.NAME");
			_ps.setString(1, fName);
			setQueryMax(1);
			
			// Get results - if empty return null
			List results = loadFiles();
			return results.isEmpty() ? null : (FileEntry) results.get(0);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Returns the contents of the File Library. This takes a database name so we can display the contents of other
	 * airlines' libraries.
	 * @param dbName the database name
	 * @return a List of FileEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection getFiles(String dbName) throws DAOException {
	   
		// Build the SQL statement
		StringBuffer sqlBuf = new StringBuffer("SELECT F.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".FILES F LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOWNLOADS L ON (F.FILENAME=L.FILENAME) GROUP BY F.NAME");
		
		try {
			prepareStatement(sqlBuf.toString());
			return loadFiles();
		} catch (SQLException se) {
		   throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load from the File Library table.
	 */
	private List loadFiles() throws SQLException {
	   
	   // Execute the query
	   ResultSet rs = _ps.executeQuery();
	   boolean hasTotals = (rs.getMetaData().getColumnCount() > 6);
	   
	   // Iterate through the result set
	   List results = new ArrayList();
	   while (rs.next()) {
	      File f = new File(SystemData.get("path.userfiles"), rs.getString(1));
	      FileEntry entry = new FileEntry(f.getPath());
	      entry.setName(rs.getString(2));
	      entry.setSecurity(rs.getInt(4));
	      entry.setAuthorID(rs.getInt(5));
	      entry.setDescription(rs.getString(6));
	      if (hasTotals)
	         entry.setDownloadCount(rs.getInt(7));
	      
	      // Add to results
	      results.add(entry);
	   }
	   
	   // Clean up and return
	   rs.close();
	   _ps.close();
	   return results;
	}

	/**
	 * Helper method to load from the Document Library table.
	 */
	private List loadManuals() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasTotals = (rs.getMetaData().getColumnCount() > 6);

		// Iterate through the result set
		List results = new ArrayList();
		while (rs.next()) {
			File f = new File(SystemData.get("path.library"), rs.getString(1));
			Manual doc = new Manual(f.getPath());
			doc.setName(rs.getString(2));
			doc.setVersion(rs.getInt(4));
			doc.setSecurity(rs.getInt(5));
			doc.setDescription(rs.getString(6));
			if (hasTotals)
				doc.setDownloadCount(rs.getInt(7));

			// Add to results
			results.add(doc);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to load from the Fleet Library table.
	 */
	private List loadInstallers() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasTotals = (rs.getMetaData().getColumnCount() > 11);

		// Iterate through the result set
		List results = new ArrayList();
		while (rs.next()) {
			File f = new File(SystemData.get("path.library"), rs.getString(1));

			Installer entry = new Installer(f.getPath());
			entry.setName(rs.getString(2));
			entry.setImage(rs.getString(3));
			entry.setVersion(rs.getInt(5), rs.getInt(6), rs.getInt(7));
			entry.setSecurity(rs.getInt(8));
			entry.setCode(rs.getString(9));
			entry.setDescription(rs.getString(11));
			if (hasTotals)
				entry.setDownloadCount(rs.getInt(12));

			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}