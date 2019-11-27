// Copyright 2012, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to write ACARS client version data.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)")) {
			ps.setInt(2, ver.getVersion());
			switch (ver.getClientType()) {
				case DISPATCH:
					ps.setString(1, isForced ? "forcedDispatch" : "latestDispatch");
					ps.setString(3, String.valueOf(ver.getClientBuild()));
					break;
					
				case ATC:
					ps.setString(1, isForced ? "forcedATC" : "latestATC");
					ps.setString(3, String.valueOf(ver.getClientBuild()));
					break;
					
				default:
					ps.setString(1, ver.isBeta() ? "beta" : (isForced ? "forced" : "latest"));
					ps.setString(3, getVersionBeta(ver));
			}
			
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)")) {
			ps.setInt(2, ver.getVersion());
			if (role == AccessRole.CONNECT) {
				switch (ver.getClientType()) {
					case DISPATCH:
						ps.setString(1, "minDispatch");
						ps.setString(3, String.valueOf(ver.getClientBuild()));
						break;
					
					case ATC:
						ps.setString(1, "minATC");
						ps.setString(3, String.valueOf(ver.getClientBuild()));
						break;
					
					default:
						ps.setString(1, ver.isBeta() ? "minBeta" : "minBuild");
						ps.setString(3, getVersionBeta(ver));
				}
			} else {
				ps.setString(1, ver.isBeta() ? "minUploadBeta" : "minUpload");
				ps.setString(3, getVersionBeta(ver));
			}
				
			executeUpdate(ps, 1);
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
		
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.VERSION_INFO (NAME, VER, DATA) VALUES (?, ?, ?)")) {
			ps.setString(1, "noDispatch");
			ps.setInt(2, ver.getVersion());
			ps.setString(3, StringUtils.listConcat(blds, ","));
			executeUpdate(ps, 1);
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