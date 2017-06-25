// Copyright 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to write ACARS client version data.
 * @author Luke
 * @version 7.5
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
	 * @param isForced TRUE if minimum to force an upgrade, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setLatest(ClientVersion ver, boolean isForced) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)");
			_ps.setInt(2, ver.getVersion());
			switch (ver.getClientType()) {
				case DISPATCH:
					_ps.setString(1, isForced ? "forcedDispatch" : "latestDispatch");
					_ps.setString(3, String.valueOf(ver.getClientBuild()));
					break;
					
				case ATC:
					_ps.setString(1, isForced ? "forcedATC" : "latestATC");
					_ps.setString(3, String.valueOf(ver.getClientBuild()));
					break;
					
				default:
					_ps.setString(1, ver.isBeta() ? "beta" : (isForced ? "forced" : "latest"));
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
						_ps.setString(1, ver.isBeta() ? "minBeta" : "minBuild");
						_ps.setString(3, getVersionBeta(ver));
				}
			} else {
				_ps.setString(1, ver.isBeta() ? "minUploadBeta" : "minUpload");
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
	 * @param builds a Collection of build numbers
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setNoDispatch(ClientVersion ver, Collection<Integer> builds) throws DAOException {
		Collection<String> blds = new TreeSet<String>();
		blds.add(String.valueOf(ver.getClientBuild()));
		for (Integer b : builds)
			blds.add(b.toString());
		
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)");
			_ps.setString(1, "noDispatch");
			_ps.setInt(2, ver.getVersion());
			_ps.setString(3, StringUtils.listConcat(blds, ","));
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
		if (ver.isBeta()) {
			buf.append('.');
			buf.append(ver.getBeta());
		}
		
		return buf.toString();
	}
}