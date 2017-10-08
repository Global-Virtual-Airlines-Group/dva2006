// Copyright 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

/**
 * A Data Access Object to write aggregated VATSIM and IVAO usage data.
 * @author Luke
 * @version 8.0
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
	 * Aggregates online connection data to the database.
	 * @param net the OnlineNetwork
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
			
			executeBatchUpdate(1, users.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes Pilot Ratings to the database.
	 * @param ratings a Collection of PilotRating beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeRatings(Collection<PilotRating> ratings) throws DAOException {
		try {
			startTransaction();
			prepareStatementWithoutLimits("INSERT INTO online.RATINGS (ID, NETWORK, RATING, CREATED, UPDATED, INS_ID, ATO) VALUES (?, ?, ?, ?, NOW(), ?, ?) ON DUPLICATE KEY UPDATE UPDATED=NOW()");
			int cnt = 0;
			for (PilotRating pr : ratings) {
				_ps.setInt(1, pr.getID());
				_ps.setString(2, pr.getNetwork().toString());
				_ps.setString(3, pr.getRatingCode());
				_ps.setTimestamp(4, createTimestamp(pr.getIssueDate()));
				_ps.setInt(5, pr.getInstructor());
				_ps.setString(6, pr.getATOName());
				_ps.addBatch();
				cnt++;
				if ((cnt % 100) == 0) {
					_ps.executeBatch();
					cnt = 0;
				}
			}
			
			if (cnt > 0)
				executeBatchUpdate(1, cnt);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}