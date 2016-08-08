// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

/**
 * A Data Access Object to write aggregated VATSIM usage data.
 * @author Luke
 * @version 7.1
 * @since 6.1
 */

public class SetOnlineTime extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connecton to use
	 */
	public SetOnlineTime(Connection c) {
		super(c);
	}

	/**
	 * Aggregates VATSIM connection data to the database.
	 * @param users a Colection of ConnectedUser beans
	 * @param interval the assumed connected time in minutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(OnlineNetwork net, Collection<ConnectedUser> users, int interval) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO stats.");
		sqlBuf.append(net.name());
		sqlBuf.append("_STATS (ID, DATE, CALLSIGN, USETIME, RATING) VALUES (?, CURDATE(), ?, ?, ?) ON DUPLICATE KEY UPDATE USETIME=USETIME+?");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			for (ConnectedUser usr : users) {
				_ps.setInt(1, usr.getID());
				_ps.setString(2, usr.getCallsign());
				_ps.setInt(3, interval);
				_ps.setInt(4, (usr.getType() == NetworkUser.Type.PILOT) ? 0 : usr.getRating().ordinal());
				_ps.setInt(5, interval);
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}