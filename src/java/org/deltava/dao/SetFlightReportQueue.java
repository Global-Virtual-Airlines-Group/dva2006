// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.flight.ApprovalOperation;

/**
 * A Data Access Object to write to asynchronous Flight Report operation queues. This are done for asynchronous updates of Flight Statistics,
 * Flight Report approvals and Elite Status updates. 
 * @author Luke
 * @version 12.2
 * @since 12.2
 */

public class SetFlightReportQueue extends DAO {

	/**
	 * Iniaitlaizes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetFlightReportQueue(Connection c) {
		super(c);
	}

	/**
	 * Adds an entry to the operation queue.
	 * @param id the Flight Report database ID
	 * @param hasElite TRUE if the Airline has an Elite program, otherwise FALSE
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void add(int id, boolean hasElite, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PIREP_AGGREGATE_QUEUE VALUES(?, NOW(), ?, ?, ?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, id);
			ps.setBoolean(2, false);
			ps.setBoolean(3, false);
			ps.setBoolean(4, !hasElite);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an entry from the operation queue.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clear(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREP_AGGREGATE_QUEUE WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a post-approval operation as complete in the queue.
	 * @param id the Flight Report database ID
	 * @param a the AppropvalOperation
	 * @throws DAOException if a JDBC error occurs
	 */
	public void complete(int id, ApprovalOperation a) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE PIREP_AGGREGATE_QUEUE SET ");
		sqlBuf.append(a.toString());
		sqlBuf.append("=? WHERE (ID=?)");
		
		try {
			startTransaction();
			
			// Update the column
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setBoolean(1, true);
				ps.setInt(2, id);
				executeUpdate(ps, 0);
			}
			
			// Clear if complete
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREP_AGGREGATE_QUEUE WHERE (COMPLETION=?) AND (STATS=?) AND (ELITE=?) AND (ID=?)")) {
				ps.setBoolean(1, true);
				ps.setBoolean(2, true);
				ps.setBoolean(3, true);
				ps.setInt(4, id);
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}