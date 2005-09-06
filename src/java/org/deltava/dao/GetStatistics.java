package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.FlightReport;
import org.deltava.beans.stats.*;

/**
 * A Data Access Object to retrieve airline statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetStatistics extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c a JDBC connection
	 */
	public GetStatistics(Connection c) {
		super(c);
	}

	/**
	 * Returns Airline Totals.
	 * @return the AirlineTotals for this airline
	 * @throws DAOException if a JDBC error occurs
	 */
	public AirlineTotals getAirlineTotals() throws DAOException {

		AirlineTotals result = new AirlineTotals(System.currentTimeMillis());

		try {
			// Create prepared statement
			prepareStatement("SELECT COUNT(ID), ROUND(SUM(FLIGHT_TIME), 1), SUM(DISTANCE) "
					+ "FROM PIREPS WHERE (DATE > ?)");
			_ps.setQueryTimeout(5);

			// Count all airline totals
			_ps.setTimestamp(1, new Timestamp(AirlineTotals.BIRTHDATE.getTimeInMillis()));
			ResultSet rs = _ps.executeQuery();
			rs.next();
			result.setTotalLegs(rs.getInt(1));
			result.setTotalHours(rs.getDouble(2));
			result.setTotalMiles(rs.getLong(3));
			rs.close();

			// Count YTD totals
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			_ps.setTimestamp(1, new Timestamp(c.getTimeInMillis()));
			rs = _ps.executeQuery();
			rs.next();
			result.setMTDLegs(rs.getInt(1));
			result.setMTDHours(rs.getDouble(2));
			result.setMTDMiles(rs.getInt(3));
			rs.close();

			// Count YTD totals
			c.set(Calendar.DAY_OF_YEAR, 1);
			_ps.setTimestamp(1, new Timestamp(c.getTimeInMillis()));
			rs = _ps.executeQuery();
			rs.next();
			result.setYTDLegs(rs.getInt(1));
			result.setYTDHours(rs.getDouble(2));
			result.setYTDMiles(rs.getInt(3));
			rs.close();
			_ps.close();

			// Get Online totals
			prepareStatement("SELECT COUNT(DISTANCE), ROUND(SUM(FLIGHT_TIME), 1), SUM(DISTANCE) "
					+ "FROM PIREPS WHERE ((ATTR & ?) > 0)");
			_ps.setInt(1, FlightReport.ATTR_ONLINE_MASK);
			rs = _ps.executeQuery();
			rs.next();
			result.setOnlineLegs(rs.getInt(1));
			result.setOnlineHours(rs.getDouble(2));
			result.setOnlineMiles(rs.getLong(3));
			rs.close();
			_ps.close();

			// Get ACARS totals
			prepareStatement("SELECT COUNT(P.DISTANCE), ROUND(SUM(P.FLIGHT_TIME), 1), SUM(P.DISTANCE) FROM PIREPS P, "
					+ "ACARS_PIREPS A WHERE (P.ID=A.ID)");
			rs = _ps.executeQuery();
			rs.next();
			result.setACARSLegs(rs.getInt(1));
			result.setACARSHours(rs.getDouble(2));
			result.setACARSMiles(rs.getInt(3));
			rs.close();
			_ps.close();

			// Get Pilot Totals
			prepareStatement("SELECT COUNT(ID) FROM PILOTS");
			rs = _ps.executeQuery();
			rs.next();
			result.setTotalPilots(rs.getInt(1));
			rs.close();
			_ps.close();

			// Get Pilot Totals
			prepareStatement("SELECT COUNT(ID) FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?))");
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);
			rs = _ps.executeQuery();
			rs.next();
			result.setActivePilots(rs.getInt(1));
			rs.close();
			_ps.close();

			// Get database Totals
			long totalSize = 0;
			int totalRows = 0;
			prepareStatement("SHOW TABLE STATUS");
			rs = _ps.executeQuery();
			while (rs.next()) {
				totalSize += rs.getLong(7);
				totalSize += rs.getLong(9);
				totalRows += rs.getInt(5);
			}

			// Update result bean
			rs.close();
			result.setDBRows(totalRows);
			result.setDBSize(totalSize);

			// Clean up
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Return totals
		return result;
	}

	/**
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param groupBy
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @param descSort TRUE if a descending sort, otherwise FALSE
	 * @return a List of StatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getPIREPStatistics(String groupBy, String orderBy, boolean descSort) throws DAOException {

		// Generate sql statement
		StringBuffer sqlBuf = (groupBy.indexOf("P.") != -1) ? getPilotJoinSQL(groupBy) : getSQL(groupBy);
		sqlBuf.append(orderBy);
		if (descSort) sqlBuf.append(" DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.OK);

			// Execute the query
			List results = new ArrayList();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(4), rs.getInt(3)));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Private helper method to return SQL statement that doesn't involve joins on the <i>PILOTS </i> table.
	 */
	private StringBuffer getSQL(String groupBy) {
		StringBuffer buf = new StringBuffer("SELECT ");
		buf.append(groupBy);
		buf.append(" AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, ROUND(SUM(F.FLIGHT_TIME), 1) ");
		buf.append("AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(DISTANCE) AS AVGMILES FROM PIREPS F WHERE ");
		buf.append("(F.STATUS=?) GROUP BY LABEL ORDER BY ");
		return buf;
	}

	/**
	 * Private helper method to return SQL statement that involves a join on the <i>PILOTS </i> table.
	 */
	private StringBuffer getPilotJoinSQL(String groupBy) {
		StringBuffer buf = new StringBuffer("SELECT ");
		buf.append(groupBy);
		buf.append(" AS LABEL, COUNT(F.DISTANCE) AS LEGS, SUM(F.DISTANCE) AS MILES, ");
		buf.append("ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, AVG(F.FLIGHT_TIME) AS AVGHOURS, AVG(F.DISTANCE) ");
		buf.append("AS AVGMILES FROM PILOTS P, PIREPS F WHERE (P.ID=F.PILOT_ID) AND (F.STATUS=?) GROUP BY ");
		buf.append("LABEL ORDER BY ");
		return buf;
	}
}