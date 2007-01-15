// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.FlightReport;

/**
 * A Data Access Object to get Flight Reports for Pilot recognition.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetFlightReportRecognition extends GetFlightReports {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetFlightReportRecognition(Connection c) {
		super(c);
	}

	/**
	 * Returns Flight Reports with the smoothest touchdown speed.
	 * @param days the number of days in the past to search
	 * @param clientBuild the minimum ACARS client build number
	 * @return a List of ACARSFlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getGreasedLandings(int days, int clientBuild) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM "
				+ "PILOTS P LEFT JOIN PIREPS PR ON (PR.PILOT_ID=P.ID) LEFT JOIN PIREP_COMMENT PC ON (PC.ID=PR.ID) "
				+ "LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) "
				+ "LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE (C.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND "
				+ "(APR.LANDING_VSPEED < 0)");

		// Append number of days
		if (days > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");

		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, clientBuild);
			_ps.setInt(2, FlightReport.OK);
			if (days > 0)
				_ps.setInt(3, days);

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves ACARS Flight Reports logged by staff members.
	 * @param days the number of days in the past to search
	 * @param clientBuild the minimum ACARS client build number
	 * @return a List of ACARSFlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getStaffReports(int days, int clientBuild) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM "
				+ "(PILOTS P, STAFF S) LEFT JOIN PIREPS PR ON (PR.PILOT_ID=P.ID) LEFT JOIN PIREP_COMMENT PC ON "
				+ "(PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN acars.FLIGHTS F ON "
				+ "(F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE (P.ID=S.ID) AND "
				+ "(C.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND (APR.LANDING_VSPEED < 0)");

		// Append number of days
		if (days > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");

		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, clientBuild);
			_ps.setInt(2, FlightReport.OK);
			if (days > 0)
				_ps.setInt(3, days);

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves ACARS Flight Reports for a particular equipment type.
	 * @param eqType the equipment type
	 * @param days the number of days in the past to search
	 * @param clientBuild the minimum ACARS client build number
	 * @return a List of ACARSFlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getGreasedLandings(String eqType, int days, int clientBuild) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM "
				+ "PILOTS P LEFT JOIN PIREPS PR ON (PR.PILOT_ID=P.ID) LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) "
				+ "LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) "
				+ "LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE (PR.EQTYPE=?) AND (C.CLIENT_BUILD >= ?) AND "
				+ "(PR.STATUS=?) AND (APR.LANDING_VSPEED < 0)");

		// Append number of days
		if (days > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");

		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, eqType);
			_ps.setInt(2, clientBuild);
			_ps.setInt(3, FlightReport.OK);
			if (days > 0)
				_ps.setInt(4, days);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all equipment types with ACARS Flight Reports.
	 * @param minLegs the minimum number of Flight Reports required for inclusion
	 * @param clientBuild the minimum ACARS client build equired for inclusion
	 * @return a List of equipment types
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getACARSEquipmentTypes(int minLegs, int clientBuild) throws DAOException {
		try {
			prepareStatement("SELECT P.EQTYPE, COUNT(P.ID) AS CNT FROM PIREPS P, ACARS_PIREPS APR LEFT JOIN "
					+ "acars.FLIGHTS F ON (APR.ACARS_ID=F.ID) LEFT JOIN acars.CONS C ON (F.CON_ID=C.ID) WHERE (P.ID=APR.ID) "
					+ "AND (P.STATUS=?) AND (C.CLIENT_BUILD >= ?) AND (APR.LANDING_VSPEED < 0) GROUP BY P.EQTYPE "
					+ "HAVING (CNT >= ?) ORDER BY CNT DESC");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, clientBuild);
			_ps.setInt(3, minLegs);

			// Execute the query
			Collection<String> results = new LinkedHashSet<String>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(rs.getString(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return new ArrayList<String>(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves the number of legs a Pilot has completed that count towards promotion to Captain.
	 * @param pilotID the Pilot's database ID
	 * @param eqType the equipment program name
	 * @return the number of completed legs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPromotionCount(int pilotID, String eqType) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(PR.ID) FROM PIREPS PR, PROMO_EQ PE WHERE (PR.ID=PE.ID) AND "
					+ "(PR.PILOT_ID=?) AND (PE.EQTYPE=?) AND (PR.STATUS=?)");
			_ps.setInt(1, pilotID);
			_ps.setString(2, eqType);
			_ps.setInt(3, FlightReport.OK);
			_ps.setMaxRows(1);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			int results = rs.next() ? rs.getInt(1) : 0;

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Determines the ratio of in-schedule to out-of-schedule flight legs. This counts all non-draft and non-rejected
	 * Flight Reports when calculating the ratio.
	 * @param pilotID the Pilot's database ID
	 * @return the ratio of scheduled to non-scheduled flights, or zero if no flights flown
	 * @throws DAOException if a JDBC error occurs
	 */
	public double getScheduledRatio(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(ID), SUM(IF((ATTR & ?) > 0, 1, 0)) FROM PIREPS WHERE (PILOT_ID=?) AND "
					+ "(STATUS <> ?) AND (STATUS <> ?)");
			_ps.setInt(1, FlightReport.ATTR_ROUTEWARN);
			_ps.setInt(2, pilotID);
			_ps.setInt(3, FlightReport.REJECTED);
			_ps.setInt(4, FlightReport.DRAFT);

			// Execute the query - return zero if no flights flown
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return 0;
			}

			// Calculate the numbers
			int totalFlights = rs.getInt(1);
			int invalidFlights = rs.getInt(2);

			// Clean up and return ratio
			rs.close();
			_ps.close();
			return (totalFlights - invalidFlights) * 1.0 / invalidFlights;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Determines the ratio of total valid to Charter flight legs.
	 * @param pilotID the Pilot's database ID
	 * @return the ratio of valid to charter flights, or zero if no flights flown
	 * @throws DAOException if a JDBC error occurs
	 */
	public double getCharterRatio(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT SUM(IF((ATTR & ?) > 0, 0, 1)), SUM(IF((ATTR & ?) > 0, 1, 0)) FROM PIREPS WHERE "
					+ "(PILOT_ID=?) AND (STATUS <> ?) AND (STATUS <> ?)");
			_ps.setInt(1, FlightReport.ATTR_ROUTEWARN | FlightReport.ATTR_CHARTER);
			_ps.setInt(2, FlightReport.ATTR_CHARTER);
			_ps.setInt(3, pilotID);
			_ps.setInt(4, FlightReport.REJECTED);
			_ps.setInt(5, FlightReport.DRAFT);

			// Execute the query - return zero if no flights flown
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return 0;
			}

			// Calculate the numbers
			int validFlights = rs.getInt(1);
			int charterFlights = rs.getInt(2);

			// Clean up and return ratio
			rs.close();
			_ps.close();
			return validFlights * 1.0 / charterFlights;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}