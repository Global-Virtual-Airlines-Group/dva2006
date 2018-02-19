// Copyright 2011, 2012, 2013, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.ClientBuildStats;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load ACARS build data. 
 * @author Luke
 * @version 8.2
 * @since 4.1
 */

public class GetACARSBuilds extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSBuilds(Connection c) {
		super(c);
	}

	/**
	 * Returns the latest beta version for a particular ACARS build.
	 * @param ver the ClientVersion
	 * @return a ClientInfo bean, or null if none
	 * @throws DAOException if a JDBC error occurs
	 */
	public ClientInfo getLatestBeta(ClientVersion ver) throws DAOException {
		try {
			prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?) AND (DATA LIKE ?)");
			_ps.setString(1, "beta");
			_ps.setInt(2, ver.getVersion());
			_ps.setString(3, "%.%");
			
			ClientInfo inf = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					String info = rs.getString(1);
					int pos = info.indexOf('.');
					int build = StringUtils.parse(info.substring(0, pos), ver.getClientBuild());
					int beta = StringUtils.parse(info.substring(pos + 1), 0);
					inf = new ClientInfo(ver.getVersion(), build, beta);
				}
			}
			
			_ps.close();
			return inf;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the latest build for a particular ACARS version and client type.
	 * @param info the ClientInfo
	 * @return a ClientInfo bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ClientInfo getLatestBuild(ClientInfo info) throws DAOException {
		return getLatestBuild(info, false);
	}
	
	/**
	 * Returns the latest build for a particular ACARS version and client type.
	 * @param info the ClientInfo
	 * @param isForced TRUE if selecting minimum build to force an upgrade
	 * @return a ClientInfo bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ClientInfo getLatestBuild(ClientInfo info, boolean isForced) throws DAOException {
		try {
			prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?)");
			_ps.setInt(2, info.getVersion());
			switch (info.getClientType()) {
				case DISPATCH:
					_ps.setString(1, isForced ? "latestDispatch" : "forcedDispatch");
					break;
			
				case ATC:
					_ps.setString(1, isForced ? "forcedATC" : "latestATC");
					break;
					
				case PILOT:
				default:
					_ps.setString(1, isForced ? "forced" : "latest");
					break;
			}
			
			ClientInfo inf = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					inf = new ClientInfo(info.getVersion(), Integer.valueOf(rs.getString(1)).intValue());
					inf.setClientType(info.getClientType());
				}
			}
			
			_ps.close();
			return inf;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns whether a particular client/build/beta combination can connect to ACARS.
	 * @param inf a ClientInfo bean
	 * @return TRUE if can connect, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean isValid(ClientInfo inf) throws DAOException {
		return isValid(inf, AccessRole.CONNECT);
	}
	
	/**
	 * Returns whether a particular client/build/beta combination can access ACARS.
	 * @param inf a ClientInfo bean
	 * @param role the Access role
	 * @return TRUE if can connect, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean isValid(ClientInfo inf, AccessRole role) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?) AND (DATA<=?)");
			_ps.setInt(2, inf.getVersion());
			_ps.setInt(3, inf.getClientBuild());
			if (role == AccessRole.CONNECT) {
				switch (inf.getClientType()) {
					case DISPATCH: 
						_ps.setString(1, "minDispatch");
						break;
					
					case ATC:
						_ps.setString(1, "minATC");
						break;
				
					default:
						_ps.setString(1, "minBuild");
				}
			} else
				_ps.setString(1, "minUpload");

			// Check if the build is OK
			boolean isOK = false;
			try (ResultSet rs = _ps.executeQuery()) {
				isOK = rs.next() ? (rs.getInt(1) > 0) : false;
			}
			
			// Check the beta version
			_ps.close();
			if (isOK && inf.isBeta() && !inf.isDispatch()) {
				prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?)");
				_ps.setString(1, (role == AccessRole.CONNECT) ? "minBeta" : "minUploadBeta");
				_ps.setInt(2, inf.getVersion());
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next()) {
						String info = rs.getString(1);
						int bld = StringUtils.parse(info.substring(0, info.indexOf('.')), Integer.MAX_VALUE);
						int beta = StringUtils.parse(info.substring(info.indexOf('.') + 1), 0);
						isOK = (inf.getClientBuild() >= bld) || ((inf.getClientBuild() == bld) && (inf.getBeta() >= beta));
					} else
						isOK = false;
				}
				
				_ps.close();
			}
			
			return isOK;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns whether a particular ACARS build can request Dispatch service.
	 * @param inf a ClientInfo bean
	 * @return TRUE if Dispatch service can be requested, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean isDispatchAvailable(ClientVersion inf) throws DAOException {
		try {
			prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?)");
			_ps.setString(1, "noDispatch");
			_ps.setInt(2, inf.getVersion());
			
			boolean isOK = true;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					List<String> builds = StringUtils.split(rs.getString(1), ",");
					for (Iterator<String> i = builds.iterator(); isOK && i.hasNext(); ) {
						int build = StringUtils.parse(i.next(), 0);
						if (build == inf.getClientBuild())
							isOK = false;
					}
				}
			}
			
			_ps.close();
			return isOK;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads ACARS client build statistics by week.
	 * @param weeks the number of weeks to load
	 * @return a Collection of ClientBuildStats beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ClientBuildStats> getBuildStatistics(int weeks) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DATE(DATE_SUB(F.CREATED, INTERVAL WEEKDAY(F.CREATED) DAY)) AS DT, F.CLIENT_BUILD, COUNT(F.ID), SUM(P.FLIGHT_TIME) FROM PIREPS P, "
				+ "ACARS_PIREPS AP, acars.FLIGHTS F WHERE (F.CREATED > DATE_SUB(CURDATE(), INTERVAL ? MONTH)) AND (P.ID=AP.ID) AND (F.ID=AP.ACARS_ID) AND (P.STATUS=?) AND "
				+ "(F.FDR=?) GROUP BY DT DESC, F.CLIENT_BUILD");
			_ps.setInt(1, weeks + 2);
			_ps.setInt(2, FlightStatus.OK.ordinal());
			_ps.setInt(3, Recorder.ACARS.ordinal());
			
			Collection<ClientBuildStats> results = new ArrayList<ClientBuildStats>();
			try (ResultSet rs = _ps.executeQuery()) {
				ClientBuildStats stats = null;
				while (rs.next() && (results.size() <= weeks)) {
					Instant dt = toInstant(rs.getTimestamp(1));
					if ((stats == null) || !dt.equals(stats.getDate())) {
						stats = new ClientBuildStats(dt);
						results.add(stats);
					}
					
					stats.addCount(rs.getInt(2), rs.getInt(3), rs.getDouble(4));
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}