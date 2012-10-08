// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object to write ACARS client version data.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class SetACARSBuilds extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSBuilds(Connection c) {
		super(c);
	}

	/**
	 * Sets the latest client version.
	 * @param ver a ClientVersion
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setLatest(ClientVersion ver) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)");
			_ps.setInt(2, ver.getVersion());
			switch (ver.getClientType()) {
				case DISPATCH:
					_ps.setString(1, "latestDispatch");
					_ps.setString(3, String.valueOf(ver.getClientBuild()));
					break;
					
				case ATC:
					_ps.setString(1, "latestATC");
					_ps.setString(3, String.valueOf(ver.getClientBuild()));
					break;
					
				default:
					_ps.setString(1, (ver.getBeta() != 0) ? "beta" : "latest");
					_ps.setString(3, getVersionBeta(ver));
			}
			
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Sets the minimum client version for a given role.
	 * @param ver a ClientVersion
	 * @param role an AccessRole, for ACARS clients only
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setMinimum(ClientVersion ver, AccessRole role) throws DAOException {
		boolean isBeta = (ver.getBeta() != 0);
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)");
			_ps.setInt(2, ver.getVersion());
			if (role == AccessRole.CONNECT) {
				switch (ver.getClientType()) {
					case DISPATCH:
						_ps.setString(1, "minDispatch");
						_ps.setString(3, String.valueOf(ver.getClientBuild()));
						break;
					
					case ATC:
						_ps.setString(1, "minATC");
						_ps.setString(3, String.valueOf(ver.getClientBuild()));
						break;
					
					default:
						_ps.setString(1, isBeta ? "minBeta" : "minBuild");
						_ps.setString(3, getVersionBeta(ver));
				}
			} else {
				_ps.setString(1, isBeta ? "minUploadBeta" : "minUpload");
				_ps.setString(3, getVersionBeta(ver));
			}
				
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Sets the builds that cannot request Dispatch service.
	 * @param ver a ClientVersion
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setNoDispatch(ClientVersion ver) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)");
			_ps.setString(1, "noDispatch");
			_ps.setInt(2, ver.getVersion());
			_ps.setString(3, String.valueOf(ver.getClientBuild()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
	
	/*
	 * Helper method to combine build/beta numbers.
	 */
	private static String getVersionBeta(ClientVersion ver) {
		StringBuilder buf = new StringBuilder(String.valueOf(ver.getClientBuild()));
		if (ver.getBeta() != 0) {
			buf.append('.');
			buf.append(ver.getBeta());
		}
		
		return buf.toString();
	}
}