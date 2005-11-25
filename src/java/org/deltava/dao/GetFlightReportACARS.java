// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.List;

import org.deltava.beans.FlightReport;

/**
 * A Data Access Object to retrieve ACARS Flight Reports from the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetFlightReportACARS extends GetFlightReports {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReportACARS(Connection c) {
		super(c);
	}

	/**
	 * Returns all Flight Reports associated with a particular Online Event.
	 * @param id the Online Event database ID
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public final List<FlightReport> getByEvent(int id) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM PILOTS P, "
					+ "PIREPS PR, ACARS_PIREPS APR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) WHERE "
					+ "(PR.ID=APR.ID) AND (PR.PILOT_ID=P.ID) AND (PR.EVENT_ID=?)");
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Reports flown on a certain date.
	 * @param dt the date
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public final List<FlightReport> getByDate(java.util.Date dt) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM PILOTS P, "
				+ "PIREPS PR, ACARS_PIREPS APR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) WHERE (PR.ID=APR.ID) "
				+ "AND (PR.PILOT_ID=P.ID) AND (PR.DATE=DATE(?))");
			_ps.setTimestamp(1, createTimestamp(dt));
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Reports for a particular Pilot, using a sort column.
	 * @param id the Pilot database ID
	 * @param orderBy the sort column (or null)
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public final List<FlightReport> getByPilot(int id, String orderBy) throws DAOException {

		// Build the statement
		StringBuilder buf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM "
				+ "PILOTS P, PIREPS PR, ACARS_PIREPS APR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) WHERE "
				+ "(PR.ID=APR.ID) AND (PR.PILOT_ID=P.ID) AND (P.ID=?)");
		if (orderBy != null) {
			buf.append(" ORDER BY PR.");
			buf.append(orderBy);
		}

		try {
			prepareStatement(buf.toString());
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}