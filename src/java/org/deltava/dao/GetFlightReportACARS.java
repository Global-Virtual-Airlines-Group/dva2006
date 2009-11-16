// Copyright 2005, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.List;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

/**
 * A Data Access Object to retrieve ACARS Flight Reports from the database.
 * @author Luke
 * @version 2.7
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
	public List<FlightReport> getByEvent(int id) throws DAOException {
		try {
			prepareStatement("SELECT PR.*, PC.COMMENTS, APR.* FROM PIREPS PR, ACARS_PIREPS APR LEFT JOIN "
					+ "PIREP_COMMENT PC ON (APR.ID=PC.ID) WHERE (PR.ID=APR.ID) AND (PR.EVENT_ID=?)");
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
	public List<FlightReport> getByDate(java.util.Date dt) throws DAOException {
		try {
			prepareStatement("SELECT PR.*, PC.COMMENTS, APR.* FROM PIREPS PR, ACARS_PIREPS APR LEFT JOIN "
					+ "PIREP_COMMENT PC ON (APR.ID=PC.ID) WHERE (PR.ID=APR.ID) AND (PR.DATE=DATE(?))");
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
	public List<FlightReport> getByPilot(int id, String orderBy) throws DAOException {

		// Build the statement
		StringBuilder buf = new StringBuilder("SELECT PR.*, PC.COMMENTS, APR.* FROM PIREPS PR, ACARS_PIREPS APR "
				+ "LEFT JOIN PIREP_COMMENT PC ON (APR.ID=PC.ID) WHERE (PR.ID=APR.ID) AND (PR.PILOT_ID=?)");
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
	
	/**
	 * Checks for duplicate ACARS flight submissions.
	 * @param dbName the database name
	 * @param f the Flight information
	 * @param pilotID the Pilot's database ID
	 * @return a List of FlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> checkDupes(String dbName, Flight f, int pilotID) throws DAOException {
		
		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, APR.* FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS PR, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".ACARS_PIREPS APR LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREP_COMMENT PC ON (APR.ID=PC.ID) WHERE (PR.ID=APR.ID) AND (PR.PILOT_ID=?) "
				+ "AND (PR.SUBMITTED > DATE_SUB(NOW(), INTERVAL ? MINUTE)) AND (PR.STATUS=?) AND "
				+ "(PR.AIRLINE=?) AND (PR.FLIGHT=?) AND (PR.LEG=?) AND (PR.AIRPORT_D=?) AND (PR.AIRPORT_A=?) "
				+ "AND (PR.EQTYPE=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, pilotID);
			_ps.setInt(2, 20);
			_ps.setInt(3, FlightReport.SUBMITTED);
			_ps.setString(4, f.getAirline().getCode());
			_ps.setInt(5, f.getFlightNumber());
			_ps.setInt(6, f.getLeg());
			_ps.setString(7, f.getAirportD().getIATA());
			_ps.setString(8, f.getAirportA().getIATA());
			_ps.setString(9, f.getEquipmentType());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Checks for duplicate ACARS flight submisions.
	 * @param dbName the database name
	 * @param acarsID the ACARS flight ID
	 * @return a List of FlightReport beans 
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> checkDupes(String dbName, int acarsID) throws DAOException {
		
		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, APR.* FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS PR, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".ACARS_PIREPS APR LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREP_COMMENT PC ON (APR.ID=PC.ID) WHERE (PR.ID=APR.ID) AND (APR.ACARS_ID=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, acarsID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}