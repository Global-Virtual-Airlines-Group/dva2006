// Copyright 2005, 2007, 2009, 2012, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to track user logins and logouts.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class SetPilotLogin extends PilotWriteDAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetPilotLogin(Connection c) {
		super(c);
	}
	
	/**
	 * Write the pilot's last login date to the database. This also resets the Pilot's status to ACTIVE, if on leave. 
	 * @param id the Pilot's database ID
	 * @param hostName the host from which the Pilot is logging in from
	 * @param dbName the database name 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void login(int id, String hostName, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS SET LAST_LOGIN=NOW(), LOGINHOSTNAME=?, LOGINS=LOGINS+1, STATUS=? WHERE (ID=?) LIMIT 1");
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, hostName);
			ps.setInt(2, PilotStatus.ACTIVE.ordinal());
			ps.setInt(3, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(id));
		}
	}
	
	/**
	 * Write the pilot's last logout date to the database. 
	 * @param id the Pilot's database ID
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logout(int id, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS SET LAST_LOGOFF=NOW() WHERE (ID=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(id));
		}
	}
}