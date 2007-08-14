// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to track user logins and logouts.
 * @author Luke
 * @version 1.0
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
	 * @throws DAOException if a JDBC error occurs
	 */
	public void login(int id, String hostName) throws DAOException {
		invalidate(id);
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET LAST_LOGIN=NOW(), LOGINHOSTNAME=?, LOGINS=LOGINS+1, " +
					"STATUS=? WHERE (ID=?)");
			_ps.setString(1, hostName);
			_ps.setInt(2, Pilot.ACTIVE);
			_ps.setInt(3, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Write the pilot's last logout date to the database. 
	 * @param id the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logout(int id) throws DAOException {
		invalidate(id);
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET LAST_LOGOFF=NOW() WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}