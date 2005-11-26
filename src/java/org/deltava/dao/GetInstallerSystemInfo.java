// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.fleet.InstallerStatistics;
import org.deltava.beans.fleet.SystemInformation;

/**
 * A Data Access Object to retrieve Fleet Installer System Information data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetInstallerSystemInfo extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetInstallerSystemInfo(Connection c) {
		super(c);
	}

	/**
	 * Returns all Operating Systems a Fleet Installer has been executed on.
	 * @return a List of Operating System names
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getOperatingSystems() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT OS FROM common.SYSINFODATA ORDER BY OS DESC");

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List<String> results = new ArrayList<String>();
			while (rs.next())
				results.add(rs.getString(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Fleet Installer codes.
	 * @return a List of Installer codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getInstallerCodes() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT INSTALLER FROM common.SYSINFODATA ORDER BY INSTALLER DESC");

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			List<String> results = new ArrayList<String>();
			while (rs.next())
				results.add(rs.getString(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Fleet Installer entries for a particular user code.
	 * @param userCode the user code
	 * @return a List of SystemInformation beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<SystemInformation> getByUserCode(String userCode) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.SYSINFODATA WHERE (ID=?)");
			_ps.setString(1, userCode);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Fleet Installer entries for a particular Installer.
	 * @param installerCode the Installer code
	 * @return a List of SystemInformation beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetInstallerSystemInfo#getInstallerCodes()
	 */
	public List<SystemInformation> getByInstallerCode(String installerCode) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.SYSINFODATA WHERE (INSTALLER=?)");
			_ps.setString(1, installerCode);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Fleet Installer entries for a particular Operating System.
	 * @param osName the operating system name
	 * @return a List of SystemInformation beans
	 * @throws DAOException if a JDBC error occurs
	 * @see GetInstallerSystemInfo#getOperatingSystems()
	 */
	public List<SystemInformation> getByOperatingSystem(String osName) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.SYSINFODATA WHERE (OS=?)");
			_ps.setString(1, osName);
			return execute();
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
	public List<InstallerStatistics> getStatistics(String groupBy, boolean sortLabel) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, COUNT(ID) AS TTL FROM common.SYSINFODATA GROUP BY ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" ORDER BY ");
		sqlBuf.append(sortLabel ? "LABEL" : "TTL");
		sqlBuf.append(" DESC");

		try {
			prepareStatement(sqlBuf.toString());
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			
			// Iterate through the results
			List<InstallerStatistics> results = new ArrayList<InstallerStatistics>();
			while (rs.next())
				results.add(new InstallerStatistics(rs.getString(1), rs.getInt(2)));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List<SystemInformation> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<SystemInformation> results = new ArrayList<SystemInformation>();
		while (rs.next()) {
			SystemInformation sysinfo = new SystemInformation(rs.getString(1));
			sysinfo.setOS(rs.getString(2));
			sysinfo.setGPU(rs.getString(3));
			sysinfo.setCPU(rs.getString(4));
			sysinfo.setDirectX(rs.getString(5));
			sysinfo.setRAM(rs.getInt(6));
			sysinfo.setFSVersion(rs.getInt(7));
			sysinfo.setCode(rs.getString(8));
			sysinfo.setDate(rs.getTimestamp(9));

			// Add to results
			results.add(sysinfo);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}