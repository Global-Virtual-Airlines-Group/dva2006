// Copyright 2006, 2007, 2009, 2011, 2012, 2013, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to load ACARS client error logs.
 * @author Luke
 * @version 7.3
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
			Collection<Integer> results = new ArrayList<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
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
			Collection<Integer> results = new ArrayList<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
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
			prepareStatementWithoutLimits("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
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
			prepareStatement("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (USERID=?) ORDER BY CREATED_ON");
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
			prepareStatement("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (CLIENT_BUILD=?) ORDER BY CREATED_ON");
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
			prepareStatement("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS ORDER BY CREATED_ON");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse ACARS client error result sets.
	 */
	private List<ACARSError> execute() throws SQLException {
		List<ACARSError> results = new ArrayList<ACARSError>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				ACARSError err = new ACARSError(rs.getInt(2), rs.getString(15));
				err.setID(rs.getInt(1));
				err.setCreatedOn(rs.getTimestamp(3).toInstant());
				// skip #4, raw remote_addr
				err.setRemoteHost(rs.getString(5));
				err.setClientBuild(rs.getInt(6));
				err.setBeta(rs.getInt(7));
				err.setSimulator(Simulator.fromVersion(rs.getInt(8)));
				err.setFSUIPCVersion(rs.getString(9));
				err.setOSVersion(rs.getString(10));
				err.setCLRVersion(rs.getString(11));
				err.setIs64Bit(rs.getBoolean(12));
				err.setLocale(rs.getString(13));
				err.setTimeZone(rs.getString(14));
				err.setStackDump(rs.getString(16));
				err.setStateData(rs.getString(17));
				err.load(rs.getBytes(18));
				err.setRemoteAddr(rs.getString(19));
				results.add(err);
			}
		}
		
		_ps.close();
		return results;
	}
}