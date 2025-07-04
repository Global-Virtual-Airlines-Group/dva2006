// Copyright 2006, 2007, 2009, 2011, 2012, 2013, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.ACARSError;

/**
 * A Data Access Object to load ACARS client error logs.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("SELECT DISTINCT CLIENT_BUILD FROM acars.ERRORS ORDER BY CLIENT_BUILD")) {
			Collection<Integer> results = new ArrayList<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
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
		try (PreparedStatement ps = prepare("SELECT DISTINCT USERID FROM acars.ERRORS")) {
			Collection<Integer> results = new ArrayList<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
				
				return results;
			}
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
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
		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (USERID=?) ORDER BY CREATED_ON")) {
			ps.setInt(1, id);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS WHERE (CLIENT_BUILD=?) ORDER BY CREATED_ON")) {
			ps.setInt(1, build);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REMOTE_ADDR) FROM acars.ERRORS ORDER BY CREATED_ON")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse ACARS client error result sets.
	 */
	private static List<ACARSError> execute(PreparedStatement ps) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			List<ACARSError> results = new ArrayList<ACARSError>();
			while (rs.next()) {
				ACARSError err = new ACARSError(rs.getInt(2), rs.getString(17));
				err.setID(rs.getInt(1));
				err.setCreatedOn(rs.getTimestamp(3).toInstant());
				// skip #4, raw remote_addr
				err.setRemoteHost(rs.getString(5));
				err.setClientBuild(rs.getInt(6));
				err.setBeta(rs.getInt(7));
				err.setSimulator(Simulator.fromVersion(rs.getInt(8), Simulator.UNKNOWN));
				err.setPluginVersion(rs.getString(9));
				err.setBridgeVersion(rs.getString(10));
				err.setOSVersion(rs.getString(11));
				err.setCLRVersion(rs.getString(12));
				err.setIs64Bit(rs.getBoolean(13));
				err.setLocale(rs.getString(14));
				err.setTimeZone(rs.getString(15));
				err.setIsInfo(rs.getBoolean(16));
				err.setStackDump(rs.getString(18));
				err.setStateData(rs.getString(19));
				err.load(rs.getBytes(20));
				err.setRemoteAddr(rs.getString(21));
				results.add(err);
			}
			
			return results;
		}
	}
}