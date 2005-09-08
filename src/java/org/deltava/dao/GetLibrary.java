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
			Collection results = loadInstallers();
			appendDB(results, dbName.toUpperCase());
			return results;
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

			// Get results - if empty return null
			List results = loadInstallers();
			return results.isEmpty() ? null : (Installer) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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

	/**
	 * Append the database name to the end of the entry names.
	 */
	private void appendDB(Collection entries, String dbName) {
		for (Iterator i = entries.iterator(); i.hasNext();) {
			FleetEntry entry = (FleetEntry) i.next();
			StringBuffer buf = new StringBuffer(entry.getName());
			buf.append(" - ");
			buf.append(dbName);
			entry.setName(buf.toString());
		}
	}
}