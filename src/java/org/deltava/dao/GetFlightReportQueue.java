// Copyright 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.DisposalQueueStats;

/**
 * A Data Access Object to return Flight Report disposal queue information.
 * @author Luke
 * @version 7.1
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
	 * Returns the size of the pending Flight Report queue for an Equipment program.
	 * @param eqType the equipment program name or null for all programs
	 * @return the number of pending or held PIREPs
	 * @throws DAOException if a JDBC error occurs
	 */
	public DisposalQueueStats getDisposalQueueStats(String eqType) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(PR.ID) AS CNT, AVG(TIMESTAMPDIFF(HOUR,PR.SUBMITTED,NOW())) AS AV, "
				+ "MAX(TIMESTAMPDIFF(HOUR,PR.SUBMITTED,NOW())) AS MX, MIN(TIMESTAMPDIFF(HOUR,PR.SUBMITTED,NOW())) AS MN FROM "
				+ "PIREPS PR");
		if (eqType != null)
			sqlBuf.append(", EQRATINGS ER WHERE (PR.EQTYPE=ER.RATED_EQ) AND (ER.RATING_TYPE=?) AND (ER.EQTYPE=?) AND");
		else
			sqlBuf.append(" WHERE");
		
		sqlBuf.append(" (PR.STATUS=?) AND ((PR.ATTR & ?)=0)");
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			if (eqType != null) {
				_ps.setInt(1, EquipmentType.Rating.PRIMARY.ordinal());
				_ps.setString(2, eqType);
				_ps.setInt(3, FlightReport.SUBMITTED);
				_ps.setInt(4, FlightReport.ATTR_CHECKRIDE);
			} else {
				_ps.setInt(1, FlightReport.SUBMITTED);
				_ps.setInt(2, FlightReport.ATTR_CHECKRIDE);
			}

			DisposalQueueStats dq = new DisposalQueueStats(new java.util.Date(), 0, 0);
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					dq = new DisposalQueueStats(new java.util.Date(), rs.getInt(1), rs.getDouble(2));
					dq.setMinMax(rs.getDouble(4), rs.getDouble(3));
				}
			}

			_ps.close();
			
			prepareStatementWithoutLimits("SELECT ER.EQTYPE, COUNT(PR.ID) AS CNT FROM PIREPS PR, EQRATINGS ER WHERE (PR.STATUS=?) "
					+ "AND (PR.EQTYPE=ER.RATED_EQ) AND (ER.RATING_TYPE=?) GROUP BY ER.EQTYPE ORDER BY CNT DESC, ER.EQTYPE");
			_ps.setInt(1, FlightReport.SUBMITTED);
			_ps.setInt(2, EquipmentType.Rating.PRIMARY.ordinal());
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