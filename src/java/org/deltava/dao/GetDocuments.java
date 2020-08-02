// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.*;

import org.deltava.beans.fleet.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Documents from the Libraries.
 * @author Luke
 * @version 9.1
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
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT D.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".DOCS D LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".DOWNLOADS L ON (D.FILENAME=L.FILENAME) WHERE (D.FILENAME=?) GROUP BY D.NAME");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, fName);
			List<Manual> results = loadManuals(ps);
			if (results.isEmpty())
				return null;
			
			// Load the certifications
			Manual m = results.get(0);
			m.addCertifications(getCertifications(fName));
			return m;
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
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT N.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".NEWSLETTERS N LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".DOWNLOADS L ON (N.FILENAME=L.FILENAME) WHERE (N.FILENAME=?) GROUP BY N.NAME");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, fName);
			List<Newsletter> results = loadNewsletters(ps);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all newsletters. This takes a database name so we can display the contents of other airlines' libraries.
	 * @param dbName the database name
	 * @return a Collection of Newsletter beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Newsletter> getNewsletters(String dbName) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT N.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".NEWSLETTERS N LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".DOWNLOADS L ON (N.FILENAME=L.FILENAME) GROUP BY N.NAME ORDER BY "
				+ "N.CATEGORY, N.PUBLISHED DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return loadNewsletters(ps);
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
		try (PreparedStatement ps = prepare("SELECT N.*, COUNT(L.FILENAME) FROM NEWSLETTERS N LEFT JOIN DOWNLOADS L ON (N.FILENAME=L.FILENAME) WHERE (N.CATEGORY=?) GROUP BY N.NAME "
			+ "ORDER BY N.PUBLISHED DESC")) {
			ps.setString(1, catName);
			return loadNewsletters(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the contents of the Document Library that are available on the Registration page.
	 * @return a Collection of Manual beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Manual> getRegistrationManuals() throws DAOException {
		Collection<Manual> results = getManuals(SystemData.get("airline.db"));
		for (Iterator<Manual> i = results.iterator(); i.hasNext(); ) {
			Manual m = i.next();
			if (!m.getShowOnRegister())
				i.remove();
		}
		
		return results;
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
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".DOCS ORDER BY NAME");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			Collection<Manual> results = loadManuals(ps);
			for (Manual m : results)
				loadDownloadCount(m);
			
			Map<String, Manual> docMap = CollectionUtils.createMap(results, Manual::getFileName);
			loadCertifications(docMap);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Manuals associated with a particular Flight Academy certification.
	 * @param dbName the database name
	 * @param certName the Certification name
	 * @return a Collection of Manual beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Manual> getByCertification(String dbName, String certName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT D.* FROM exams.CERTDOCS CD, ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".DOCS D WHERE (CD.FILENAME=D.FILENAME) AND (CD.CERT=?) ORDER BY D.NAME");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, certName);
			Collection<Manual> results = loadManuals(ps);
			for (Manual m : results)
				loadDownloadCount(m);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load Flight Academy Certifications into Manual beans.
	 */
	private void loadCertifications(Map<String, Manual> manuals) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM exams.CERTDOCS")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Manual m = manuals.get(rs.getString(1));
					if (m != null)
						m.addCertification(rs.getString(2));
				}
			}
		}
	}
	
	/*
	 * Helper method to return all Flight Academy Certifications associated with a particular Manual.
	 */
	private Collection<String> getCertifications(String fileName) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT CERT FROM exams.CERTDOCS WHERE (FILENAME=?) ORDER BY CERT")) {
			ps.setString(1, fileName);
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}

			return results;
		}
	}

	/*
	 * Helper method to load from the Document Library table.
	 */
	private static List<Manual> loadManuals(PreparedStatement ps) throws SQLException {
		List<Manual> results = new ArrayList<Manual>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasTotals = (rs.getMetaData().getColumnCount() > 8);
			while (rs.next()) {
				File f = new File(SystemData.get("path.library"), rs.getString(1));
				Manual doc = new Manual(f);
				doc.setName(rs.getString(2));
				doc.setVersion(rs.getInt(4));
				doc.setSecurity(Security.values()[rs.getInt(5)]);
				doc.setShowOnRegister(rs.getBoolean(6));
				doc.setIgnoreCertifcations(rs.getBoolean(7));
				doc.setDescription(rs.getString(8));
				if (f.exists())
					doc.setLastModified(Instant.ofEpochMilli(f.lastModified()));
				if (hasTotals)
					doc.setDownloadCount(rs.getInt(9));

				results.add(doc);
			}
		}

		return results;
	}

	/*
	 * Helper method to load from the Newsletter Library table.
	 */
	private static List<Newsletter> loadNewsletters(PreparedStatement ps) throws SQLException {
		List<Newsletter> results = new ArrayList<Newsletter>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasTotals = (rs.getMetaData().getColumnCount() > 7);
			while (rs.next()) {
				Newsletter nws = new Newsletter(new File(SystemData.get("path.newsletter"), rs.getString(1)));
				nws.setName(rs.getString(2));
				nws.setCategory(rs.getString(3));
				nws.setSecurity(Security.values()[rs.getInt(5)]);
				nws.setDate(expandDate(rs.getDate(6)));
				nws.setDescription(rs.getString(7));
				if (hasTotals)
					nws.setDownloadCount(rs.getInt(8));

				results.add(nws);
			}
		}

		return results;
	}
}