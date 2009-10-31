// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.File;
import java.util.*;
import java.sql.*;

import org.deltava.beans.fleet.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load metadata from the Fleet/Document Libraries.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class GetLibrary extends DAO implements CachingDAO {
	
	private static final Cache<CacheableLong> _dlCache = new ExpiringCache<CacheableLong>(128, 3600);

	/**
	 * Initialze the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetLibrary(Connection c) {
		super(c);
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_dlCache);
	}
	
	/**
	 * Returns the contents of a Fleet Library. This takes a database name so we can display the contents of other
	 * airlines' libraries.
	 * @param dbName the database name
	 * @param isAdmin TRUE if in admin mode and all files should be returned, otherwise FALSE
	 * @return a List of Installer beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Installer> getFleet(String dbName, boolean isAdmin) throws DAOException {

		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT F.* FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".FLEET_AIRLINE FA, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".FLEET F WHERE (F.FILENAME=FA.FILENAME)");
		if (!isAdmin)
			sqlBuf.append(" AND (FA.CODE=?)");
		sqlBuf.append(" GROUP BY F.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			if (!isAdmin)
				_ps.setString(1, SystemData.get("airline.code"));
			
			List<Installer> results = loadInstallers();
			loadDownloadCounts(results);
			return results;
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
	public Installer getInstaller(String fName, String dbName) throws DAOException {

		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".FLEET WHERE (FILENAME=?) LIMIT 1");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, fName);

			// Get results - if empty return null
			List<Installer> results = loadInstallers();
			if (results.isEmpty())
				return null;
			
			// Get airline data
			loadDownloadCounts(results);
			Installer i = results.get(0);
			prepareStatementWithoutLimits("SELECT CODE FROM FLEET_AIRLINE WHERE (FILENAME=?)");
			_ps.setString(1, fName);
			
			// Do the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				i.addApp(SystemData.getApp(rs.getString(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return i;
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
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".FLEET WHERE (CODE=?) LIMIT 1");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, code.toUpperCase());

			// Get results - if empty return null
			List<Installer> results = loadInstallers();
			if (results.isEmpty())
				return null;
			
			// Get airline data
			loadDownloadCounts(results);
			Installer i = results.get(0);
			prepareStatementWithoutLimits("SELECT CODE FROM FLEET_AIRLINE WHERE (FILENAME=?)");
			_ps.setString(1, i.getFileName());
			
			// Do the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				i.addApp(SystemData.getApp(rs.getString(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return i;
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
			prepareStatementWithoutLimits("SELECT F.*, COUNT(L.FILENAME) FROM FILES F LEFT JOIN DOWNLOADS L "
					+ "ON (F.FILENAME=L.FILENAME) WHERE (F.FILENAME=?) GROUP BY F.NAME ORDER BY F.NAME LIMIT 1");
			_ps.setString(1, fName);

			// Get results - if empty return null
			List<FileEntry> results = loadFiles(false);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns metadata about a specifc video <i>in the current database</i>.
	 * @param fName the filename
	 * @return a FileEntry, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Video getVideo(String fName) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT V.*, COUNT(L.FILENAME) FROM exams.VIDEOS V LEFT JOIN DOWNLOADS L "
					+ "ON (V.FILENAME=L.FILENAME) WHERE (V.FILENAME=?) GROUP BY V.NAME ORDER BY V.NAME LIMIT 1");
			_ps.setString(1, fName);
			
			// Get results - if empty return null
			List<FileEntry> results = loadFiles(true);
			return (results.isEmpty()) ? null : (Video) results.get(0); 
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
	public Collection<FileEntry> getFiles(String dbName) throws DAOException {

		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT F.*, COUNT(L.FILENAME) FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".FILES F LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".DOWNLOADS L ON (F.FILENAME=L.FILENAME) GROUP BY F.NAME ORDER BY F.NAME");

		try {
			prepareStatement(sqlBuf.toString());
			return loadFiles(false);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to load from the File/Video Library tables.
	 */
	private List<FileEntry> loadFiles(boolean isVideo) throws SQLException {
		
		// Determine the path
		String path = SystemData.get(isVideo ? "path.video" : "path.userfiles");

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasTotals = (rs.getMetaData().getColumnCount() > 7);

		// Iterate through the result set
		List<FileEntry> results = new ArrayList<FileEntry>();
		while (rs.next()) {
			File f = new File(path, rs.getString(1));
			FileEntry entry = isVideo ? new Video(f.getPath()) : new FileEntry(f.getPath());
			entry.setName(rs.getString(2));
			entry.setCategory(rs.getString(3));
			entry.setSecurity(rs.getInt(5));
			entry.setAuthorID(rs.getInt(6));
			entry.setDescription(rs.getString(7));
			if (hasTotals)
				entry.setDownloadCount(rs.getInt(8));

			// Add to results
			results.add(entry);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Helper method to load from the Fleet Library table.
	 */
	private List<Installer> loadInstallers() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasTotals = (rs.getMetaData().getColumnCount() > 11);

		// Iterate through the result set
		List<Installer> results = new ArrayList<Installer>();
		while (rs.next()) {
			File f = new File(SystemData.get("path.library"), rs.getString(1));
			Installer entry = new Installer(f.getPath());
			entry.setName(rs.getString(2));
			entry.setImage(rs.getString(3));
			entry.setVersion(rs.getInt(5), rs.getInt(6), rs.getInt(7));
			entry.setSecurity(rs.getInt(8));
			entry.setCode(rs.getString(9));
			entry.setFSVersions(rs.getString(10));
			entry.setDescription(rs.getString(11));
			if (f.exists())
				entry.setLastModified(new java.util.Date(f.lastModified()));
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
	 * Helper method to fetch download counts from the cache.
	 */
	protected void loadDownloadCounts(Collection<? extends FleetEntry> files) throws SQLException {
		
		// Check the cache and build the SQL statement
		int entrySize = 0;
		StringBuilder sqlBuf = new StringBuilder("SELECT FILENAME, COUNT(*) FROM DOWNLOADS WHERE FILENAME IN (");
		for (Iterator<? extends FleetEntry> i = files.iterator(); i.hasNext(); ) {
			FleetEntry fe = i.next();
			CacheableLong cnt = _dlCache.get(fe.getFileName());
			if (cnt == null) {
				entrySize++;
				sqlBuf.append('\'');
				sqlBuf.append(fe.getFileName());
				sqlBuf.append("\',");
			} else
				fe.setDownloadCount((int) cnt.getValue());
		}
		
		if (entrySize == 0)
			return;
		
		// Load from the database
		sqlBuf.setLength(sqlBuf.length() - 1);
		sqlBuf.append(") GROUP BY FILENAME");
		Map<String, ? extends FleetEntry> is = CollectionUtils.createMap(files, "fileName");
		prepareStatement(sqlBuf.toString());
		
		// Parse the results
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			FleetEntry fe = is.get(rs.getString(1));
			if (fe != null) {
				fe.setDownloadCount(rs.getInt(2));
				CacheableLong cnt = new CacheableLong(fe.getFileName(), fe.getDownloadCount());
				_dlCache.add(cnt);
			}
		}
		
		rs.close();
		_ps.close();
	}
}