// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
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
	 * @param p the Pilot
	 * @throws DAOException if a JDBC error occurs
	 */
	public void login(Pilot p) throws DAOException {
		
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET LAST_LOGIN=?, LOGINHOSTNAME=?, LOGINS=LOGINS+1, " +
					"STATUS=? WHERE (ID=?)");
			_ps.setTimestamp(1, createTimestamp(p.getLastLogin()));
			_ps.setString(2, p.getLoginHost());
			_ps.setInt(3, Pilot.ACTIVE);
			_ps.setInt(4, p.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Invalidate the Pilot in the cache
		PilotReadDAO._cache.remove(p);
	}
	
	/**
	 * Write the pilot's last logout date to the database. 
	 * @param p the Pilot
	 * @throws DAOException if a JDBC error occurs
	 */
	public void logout(Pilot p) throws DAOException {
		
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET LAST_LOGOFF=? WHERE (ID=?)");
			_ps.setTimestamp(1, createTimestamp(p.getLastLogoff()));
			_ps.setInt(2, p.getID());
			
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Invalidate the Pilot in the cache
		PilotReadDAO._cache.remove(p);
	}
}