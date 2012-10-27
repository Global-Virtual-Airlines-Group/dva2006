// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.stats.DisposalQueueStats;

import static org.deltava.beans.EquipmentType.*;
import static org.deltava.beans.flight.FlightReport.*;

/**
 * A Data Access Object to return Flight Report disposal queue information.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class GetFlightReportQueue extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReportQueue(Connection c) {
		super(c);
	}

	/**
	 * Returns the number of Flight Reports awaiting disposition.
	 * @return the number Flight Reports in SUBMITTED status
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getDisposalQueueSize() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM PIREPS WHERE (STATUS=?)");
			_ps.setInt(1, SUBMITTED);

			int results = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					results = rs.getInt(1);
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the size of the pending Flight Report queue for an Equipment program. 
	 * @param eqType the equipment program name 
	 * @return the number of pending or held PIREPs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getDisposalQueueSize(String eqType) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(PR.ID) FROM PIREPS PR, EQRATINGS ER WHERE ((PR.STATUS=?) OR (PR.STATUS=?)) "
				+ "AND (PR.EQTYPE=ER.RATED_EQ) AND (ER.RATING_TYPE=?) AND (ER.EQTYPE=?)");
			_ps.setInt(1, HOLD);
			_ps.setInt(2, SUBMITTED);
			_ps.setInt(3, PRIMARY_RATING);
			_ps.setString(4, eqType);
			
			int results = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					results = rs.getInt(1);
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns statistics about the pending Flight Report queue.
	 * @return a DisposalQueueStats bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public DisposalQueueStats getDisposalQueueStats() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT COUNT(ID), AVG(TIMESTAMPDIFF(HOUR,,SUBMITTED,NOW())) FROM PIREPS WHERE (STATUS=?)");
			_ps.setInt(1, SUBMITTED);
			
			DisposalQueueStats dq = new DisposalQueueStats(new java.util.Date(), 0, 0);
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					dq = new DisposalQueueStats(new java.util.Date(), rs.getInt(1), rs.getDouble(2));
			}
			
			_ps.close();
			prepareStatementWithoutLimits("SELECT ER.EQTYPE, COUNT(PR.ID) AS CNT FROM PIREPS PR, EQRATINGS ER WHERE (PR.STATUS=?) "
				+ "AND (PR.EQTYPE=ER.RATED_EQ) AND (ER.RATING_TYPE=?) GROUP BY ER.EQTYPE ORDER BY CNT DESC, ER.EQTYPE");
			_ps.setInt(1, SUBMITTED);
			_ps.setInt(2, PRIMARY_RATING);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					dq.addCount(rs.getString(1), rs.getInt(2));
			}
			
			_ps.close();
			return dq;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}