// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.LoadType;
import org.deltava.beans.flight.FlightStatus;
import org.deltava.beans.stats.*;

/**
 * A Data Access Object to retrieve Flight Report load statistics from the database.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class GetFlightReportLoad extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReportLoad(Connection c) {
		super(c);
	}

	/**
	 * Retrieves flight load statistics from the database. 
	 * @param daysBack the number of days back to reutrn.
	 * @param grp a FlightStatsGroup bean for grouping
	 * @return a Collection of LoadStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LoadStatistics> getLoad(int daysBack, FlightStatsGroup grp) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(grp.getSQL());
		sqlBuf.append(" AS LABEL, AVG(F.LOADFACTOR), SUM(F.PAX) FROM PIREPS F WHERE (F.LOADFACTOR>0) AND (F.STATUS=?) AND (F.DATE>DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY LABEL ORDER BY F.DATE DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, daysBack);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves flight load statistics from the database based on load type.
	 * @param daysBack the number of days back to reutrn.
	 * @param isAssigned TRUE to retrieve assigend flight loads, FALSE for actual
	 * @param grp a FlightStatsGroup bean for grouping
	 * @return a Collection of LoadStatistics beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LoadStatistics> getLoad(int daysBack, boolean isAssigned, FlightStatsGroup grp) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(grp.getSQL());
		sqlBuf.append(" AS LABEL, AVG(F.LOADFACTOR), SUM(F.PAX) FROM PIREPS F LEFT JOIN ACARS_PIREPS AP ON (F.ID=AP.ID) LEFT JOIN acars.FLIGHT_LOAD FL ON (AP.ACARS_ID=FL.ID) WHERE (F.STATUS=?) AND "
			+ "(FL.LOADTYPE=?) AND (F.DATE>DATE_SUB(CURDATE(), INTERVAL ? DAY)) GROUP BY LABEL ORDER BY F.DATE DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, isAssigned ? LoadType.ASSIGNED.ordinal() : LoadType.ACTUAL.ordinal());
			ps.setInt(3, daysBack);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse load statistics result sets.
	 */
	private static List<LoadStatistics> execute(PreparedStatement ps) throws SQLException {
		List<LoadStatistics> results = new ArrayList<LoadStatistics>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				LoadStatistics ls = new LoadStatistics(rs.getString(1));
				ls.setLoad(rs.getDouble(2));
				ls.setPax(rs.getInt(3));
				results.add(ls);
			}
		}
		
		return results;
	}
}