// Copyright 2005, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.stats.*;

/**
 * A Data Access Object to retrieve ACARS System Information data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetSystemInfo extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSystemInfo(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the total number of statistics entries for totals calculation.
	 * @return the total number of entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getTotals() throws DAOException {
		try {
			int result = 0;
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM acars.SYSINFO");
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns system configuration data for a particular Pilot.
	 * @param id the user's database ID
	 * @return a SystemInformation bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public SystemInformation get(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * from acars.SYSINFO WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			List<SystemInformation> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Fleet Installer statistics for a particular database field.
	 * @param groupBy the database field to group by
	 * @param sortLabel TRUE if sorted by label, FALSE if sorted by total
	 * @return a List of InstallerStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<SystemStatistics<Integer>> getStatistics(String groupBy, boolean sortLabel) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, COUNT(ID) AS TTL FROM acars.SYSINFO GROUP BY ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" ORDER BY ");
		sqlBuf.append(sortLabel ? "LABEL" : "TTL DESC");

		try {
			prepareStatement(sqlBuf.toString());
			List<SystemStatistics<Integer>> results = new ArrayList<SystemStatistics<Integer>>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(new SystemStatistics<Integer>(rs.getString(1), Integer.valueOf(rs.getInt(2))));
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to iterate through the result set.
	 */
	private List<SystemInformation> execute() throws SQLException {
		List<SystemInformation> results = new ArrayList<SystemInformation>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				SystemInformation sysinfo = new SystemInformation(rs.getInt(1));
				sysinfo.setDate(rs.getTimestamp(2).toInstant());
				sysinfo.setOSVersion(rs.getString(3));
				sysinfo.setCLRVersion(rs.getString(4));
				sysinfo.setDotNETVersion(rs.getString(5));
				sysinfo.setIs64Bit(rs.getBoolean(6));
				sysinfo.setIsSLI(rs.getBoolean(7));
				sysinfo.setLocale(rs.getString(8));
				sysinfo.setTimeZone(rs.getString(9));
				sysinfo.setMemorySize(rs.getInt(10));
				sysinfo.setCPU(rs.getString(11));
				sysinfo.setCPUSpeed(rs.getInt(12));
				sysinfo.setSockets(rs.getInt(13));
				sysinfo.setCores(rs.getInt(14));
				sysinfo.setThreads(rs.getInt(15));
				sysinfo.setGPU(rs.getString(16));
				sysinfo.setGPUDriverVersion(rs.getString(17));
				sysinfo.setVideoMemorySize(rs.getInt(18));
				sysinfo.setScreenSize(rs.getInt(19), rs.getInt(20));
				sysinfo.setColorDepth(rs.getInt(21));
				sysinfo.setScreenCount(rs.getInt(22));
				results.add(sysinfo);
			}
		}

		_ps.close();
		return results;
	}
}