// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.File;
import java.sql.*;
import java.util.*;

import org.deltava.beans.fleet.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Documents from the Libraries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetDocuments extends GetLibrary {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetDocuments(Connection c) {
		super(c);
	}

	/**
	 * Returns metadata about a specific Manual .
	 * @param fName the filename
	 * @param dbName the database name
	 * @return a Manual, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Manual getManual(String fName, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT D.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOCS D LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOWNLOADS L ON (D.FILENAME=L.FILENAME) WHERE (D.FILENAME=?) GROUP BY D.NAME");

		try {
			setQueryMax(1);
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, fName);

			// Get results - if empty return null
			List<Manual> results = loadManuals();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns metadata about a specific Newsletter.
	 * @param fName the filename
	 * @param dbName the database name
	 * @return a Newsletter, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Newsletter getNewsletter(String fName, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT N.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".NEWSLETTERS N LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOWNLOADS L ON (N.FILENAME=L.FILENAME) WHERE (N.FILENAME=?) GROUP BY N.NAME");

		try {
			setQueryMax(1);
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, fName);
			
			// Get results - if empty return null
			List<Newsletter> results = loadNewsletters();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all newsletters. This takes a database name so we can display the contents of other
	 * airlines' libraries.
	 * @param dbName the database name
	 * @return a Collection of Newsletter beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Newsletter> getNewsletters(String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT N.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".NEWSLETTERS N LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".DOWNLOADS L ON (N.FILENAME=L.FILENAME) GROUP BY N.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			return loadNewsletters();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Newsletters within a particular category.
	 * @param catName the category name
	 * @return a Collection of Newsletter beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Newsletter> getNewslettersByCategory(String catName) throws DAOException {
		try {
			prepareStatement("SELECT N.*, COUNT(L.FILENAME) FROM NEWSLETTERS N LEFT JOIN "
					+ "DOWNLOADS L ON (N.FILENAME=L.FILENAME) WHERE (N.CATEGORY=?) GROUP BY N.NAME");
			_ps.setString(1, catName);
			return loadNewsletters();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the contents of the Document Library. This takes a database name so we can display the contents of other
	 * airlines' libraries.
	 * @param dbName the database name
	 * @return a Collection of Manual beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Manual> getManuals(String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT D.*, COUNT(L.FILENAME) FROM ");
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
	 * Helper method to load from the Document Library table.
	 */
	private List<Manual> loadManuals() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasTotals = (rs.getMetaData().getColumnCount() > 6);

		// Iterate through the result set
		List<Manual> results = new ArrayList<Manual>();
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
	 * Helper method to load from the Newsletter Library table.
	 */
	private List<Newsletter> loadNewsletters() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasTotals = (rs.getMetaData().getColumnCount() > 7);
		
		// Iterate through the result set
		List<Newsletter> results = new ArrayList<Newsletter>();
		while (rs.next()) {
			File f = new File(SystemData.get("path.newsletter"), rs.getString(1));
			Newsletter nws = new Newsletter(f.getPath());
			nws.setName(rs.getString(2));
			nws.setCategory(rs.getString(3));
			nws.setSecurity(rs.getInt(5));
			nws.setDate(expandDate(rs.getDate(6)));
			nws.setDescription(rs.getString(7));
			if (hasTotals)
				nws.setDownloadCount(rs.getInt(8));
			
			// Add to results
			results.add(nws);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}
