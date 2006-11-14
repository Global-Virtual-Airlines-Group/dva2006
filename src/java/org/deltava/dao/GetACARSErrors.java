// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to load ACARS client error logs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetACARSErrors extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSErrors(Connection c) {
		super(c);
	}

	/**
	 * Returns the ACARS Client builds with error reports.
	 * @return a Collection of Build numbers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getBuilds() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT CLIENT_BUILD FROM acars.ERRORS ORDER BY CLIENT_BUILD");
			
			// Execute the query
			Collection<Integer> results = new ArrayList<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the Pilots with error reports.
	 * @return a Collection of datbase ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPilots() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT USERID FROM acars.ERRORS");
			
			// Execute the query
			Collection<Integer> results = new ArrayList<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns a specific ACARS Client error log entry.
	 * @param id the entry database ID
	 * @return an ACARSError bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ACARSError get(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT *, INET_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query, get the first result
			List<ACARSError> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all ACARS client error log entries logged by a particular Pilot.
	 * @param id the Pilot's database ID
	 * @return a Collection of ACARSError beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ACARSError> getByPilot(int id) throws DAOException {
		try {
			prepareStatement("SELECT *, INET_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (USERID=?) ORDER BY CREATED_ON");
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all ACARS client error log entries logged by a particular ACARS client build.
	 * @param build the client build number
	 * @return a Collection of ACARSError beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ACARSError> getByBuild(int build) throws DAOException {
		try {
			prepareStatement("SELECT *, INET_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (CLIENT_BUILD=?) ORDER BY CREATED_ON");
			_ps.setInt(1, build);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all ACARS client error log entries.
	 * @return a Collection of ACARSError beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ACARSError> getAll() throws DAOException {
		try {
			prepareStatement("SELECT *, INET_NTOA(REMOTE_ADDR) FROM acars.ERRORS ORDER BY CREATED_ON");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse ACARS client error result sets.
	 */
	private List<ACARSError> execute() throws SQLException {
		List<ACARSError> results = new ArrayList<ACARSError>();
		
		// Execute the Query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			ACARSError err = new ACARSError(rs.getInt(2), rs.getString(9));
			err.setID(rs.getInt(1));
			err.setCreatedOn(rs.getTimestamp(3));
			// skip #4, raw remote_addr
			err.setRemoteHost(rs.getString(5));
			err.setClientBuild(rs.getInt(6));
			err.setFSVersion(rs.getInt(7));
			err.setFSUIPCVersion(rs.getString(8));
			err.setStackDump(rs.getString(10));
			err.setStateData(rs.getString(11));
			err.setRemoteAddr(rs.getString(12));
			
			// Add to results
			results.add(err);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}