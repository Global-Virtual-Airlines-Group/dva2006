// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.ClientInfo;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load ACARS build data. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class GetACARSBuilds extends DAO {
	
	// Enumeration to store access roles
	public enum AccessRole {
		CONNECT, UPLOAD;
	}

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSBuilds(Connection c) {
		super(c);
	}

	/**
	 * Returns the latest beta version for a particular ACARS build.
	 * @param version the ACARS version
	 * @param build the build number
	 * @return a ClientInfo bean, or null if none
	 * @throws DAOException if a JDBC error occurs
	 */
	public ClientInfo getLatestBeta(int version, int build) throws DAOException {
		try {
			prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?) AND (DATA LIKE ?)");
			_ps.setString(1, "beta");
			_ps.setInt(2, version);
			_ps.setString(3, String.valueOf(build) + ".%");
			
			ClientInfo inf = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					String info = rs.getString(1);
					int beta = StringUtils.parse(info.substring(info.indexOf('.') + 1), 0);
					inf = new ClientInfo(version, build, beta);
				}
			}
			
			_ps.close();
			return inf;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the latest build for a particular ACARS version.
	 * @param version the ACARS version
	 * @return a ClientInfo bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ClientInfo getLatestBuild(int version) throws DAOException {
		try {
			prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?)");
			_ps.setString(1, "latest");
			_ps.setInt(2, version);
			
			ClientInfo inf = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					inf = new ClientInfo(version, Integer.valueOf(rs.getString(1)).intValue());
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
			prepareStatement("SELECT COUNT(*) FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?) AND (DATA>=?)");
			_ps.setInt(2, inf.getVersion());
			_ps.setInt(3, inf.getClientBuild());
			if (role == AccessRole.CONNECT)
				_ps.setString(1, inf.isDispatch() ? "minDispatch" : "minBuild");
			else
				_ps.setString(1, "minUpload");

			// Check if the build is OK
			boolean isOK = false;
			try (ResultSet rs = _ps.executeQuery()) {
				isOK = rs.next() ? (rs.getInt(1) > 0) : false;
			}
			
			// Check the beta version
			_ps.close();
			if (isOK && inf.isBeta() && !inf.isDispatch()) {
				prepareStatement("SELECT DATA FROM acars.VERSION_INFO WHERE (NAME=?) AND (VER=?) AND (DATA LIKE ?)");
				_ps.setString(1, (role == AccessRole.CONNECT) ? "minBeta" : "minUploadBeta");
				_ps.setInt(2, inf.getVersion());
				_ps.setString(3, String.valueOf(inf.getClientBuild()) + ".%");
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next()) {
						String info = rs.getString(1);
						int beta = StringUtils.parse(info.substring(info.indexOf('.') + 1), 0);
						isOK = (inf.getBeta() >= beta);
					}
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
	public boolean isDispatchAvailable(ClientInfo inf) throws DAOException {
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
							isOK =false;
					}
				} else
					isOK = false;
			}
			
			_ps.close();
			return isOK;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}