// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object to load ACARS log data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetACARSLog extends GetACARSData {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSLog(Connection c) {
		super(c);
	}

	/**
	 * Returns all ACARS connection log entries matching particular criteria.
	 * @param criteria the search criteria
	 * @return a List of ConnectionEntry beans sorted by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ConnectionEntry> getConnections(LogSearchCriteria criteria) throws DAOException {

		// Build the search criteria
		List<String> terms = new ArrayList<String>();
		if (criteria.getPilotID() != 0)
			terms.add("(C.PILOT_ID=?)");
		if (criteria.getStartDate() != null)
			terms.add("(C.DATE > ?)");
		if (criteria.getEndDate() != null)
			terms.add("(C.DATE < ?)");

		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT C.ID, C.PILOT_ID, C.DATE, INET_NTOA(C.REMOTE_ADDR), "
				+ "C.REMOTE_HOST, C.CLIENT_BUILD, COUNT(DISTINCT F.ID), COUNT(P.CON_ID) FROM acars.CONS C "
				+ "LEFT JOIN acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.POSITIONS P ON (C.ID=P.CON_ID)");

		// Add the terms
		if (!terms.isEmpty()) {
			buf.append(" WHERE ");
			for (Iterator<String> i = terms.iterator(); i.hasNext();) {
				buf.append(i.next());
				if (i.hasNext())
					buf.append(" AND ");
			}
		}

		buf.append(" GROUP BY C.ID ORDER BY C.DATE DESC");

		try {
			prepareStatement(buf.toString());
			int psOfs = 0;
			if (criteria.getPilotID() != 0)
				_ps.setInt(++psOfs, criteria.getPilotID());

			if (criteria.getStartDate() != null)
				_ps.setTimestamp(++psOfs, createTimestamp(criteria.getStartDate()));

			if (criteria.getEndDate() != null)
				_ps.setTimestamp(++psOfs, createTimestamp(criteria.getEndDate()));
			
			return executeConnectionInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all ACARS connection log entries with no associated Flight Info logs or text messages. A cutoff interval
	 * is provided to prevent the accidental inclusion of flights still in progress.
	 * @param cutoff the cutoff interval for connection entries, in hours
	 * @return a List of ConnectionEntry beans sorted by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<ConnectionEntry> getUnusedConnections(int cutoff) throws DAOException {
		try {
			prepareStatement("SELECT C.ID, C.PILOT_ID, C.DATE, INET_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, "
					+ "C.CLIENT_BUILD, COUNT(DISTINCT F.ID) AS FC, COUNT(P.FLIGHT_ID) AS PC FROM acars.CONS C "
					+ "LEFT JOIN acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.POSITIONS P ON (F.ID=P.FLIGHT_ID) "
					+ "WHERE (C.DATE < DATE_SUB(NOW(), INTERVAL ? HOUR)) GROUP BY C.ID HAVING (FC=0) AND (PC=0) "
					+ "ORDER BY C.DATE");
			_ps.setInt(1, cutoff);
			return executeConnectionInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all ACARS text messages matching particular criteria.
	 * @param criteria the ACARS log search criteria
	 * @return a List of TextMessage beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<TextMessage> getMessages(LogSearchCriteria criteria) throws DAOException {

		// Build the search criteria
		List<String> terms = new ArrayList<String>();
		if (criteria.getPilotID() != 0)
			terms.add("((AUTHOR=?) OR (RECIPIENT=?))");
		if (criteria.getStartDate() != null)
			terms.add("(DATE > ?)");
		if (criteria.getEndDate() != null)
			terms.add("(DATE < ?)");

		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT * FROM acars.MESSAGES ");
		if (!terms.isEmpty()) {
			buf.append("WHERE ");
			for (Iterator<String> i = terms.iterator(); i.hasNext();) {
				buf.append(i.next());
				if (i.hasNext())
					buf.append(" AND ");
			}
		}

		buf.append(" ORDER BY DATE");

		try {
			prepareStatement(buf.toString());
			int psOfs = 0;
			if (criteria.getPilotID() != 0) {
				_ps.setInt(++psOfs, criteria.getPilotID());
				_ps.setInt(++psOfs, criteria.getPilotID());
			}

			if (criteria.getStartDate() != null)
				_ps.setTimestamp(++psOfs, createTimestamp(criteria.getStartDate()));

			if (criteria.getEndDate() != null)
				_ps.setTimestamp(++psOfs, createTimestamp(criteria.getEndDate()));

			return executeMsg();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Information entries matching particular criteria.
	 * @param criteria the search criteria
	 * @return a List of InfoEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightInfo> getFlights(LogSearchCriteria criteria) throws DAOException {

		// Build the search criteria
		List<String> terms = new ArrayList<String>();
		if (criteria.getPilotID() != 0)
			terms.add("(C.PILOT_ID=?)");
		if (criteria.getStartDate() != null)
			terms.add("(F.CREATED > ?)");
		if (criteria.getEndDate() != null)
			terms.add("(F.CREATED < ?)");

		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT F.*, C.PILOT_ID FROM acars.CONS C, acars.FLIGHTS F "
				+ "WHERE (C.ID=F.CON_ID) ");
		if (!terms.isEmpty()) {
			buf.append("AND");
			for (Iterator<String> i = terms.iterator(); i.hasNext();) {
				buf.append(i.next());
				if (i.hasNext())
					buf.append(" AND ");
			}
		}

		buf.append(" ORDER BY F.CREATED");

		try {
			prepareStatement(buf.toString());
			int psOfs = 0;
			if (criteria.getPilotID() != 0)
				_ps.setInt(++psOfs, criteria.getPilotID());

			if (criteria.getStartDate() != null)
				_ps.setTimestamp(++psOfs, createTimestamp(criteria.getStartDate()));

			if (criteria.getEndDate() != null)
				_ps.setTimestamp(++psOfs, createTimestamp(criteria.getEndDate()));

			return executeFlightInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Information entries without an associated Flight Report. A cutoff interval is provided to
	 * prevent the accidental inclusion of flights still in progress.
	 * @param cutoff the cutoff interval for flight entries, in hours
	 * @return a List of InfoEntry beans sorted by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightInfo> getUnreportedFlights(int cutoff) throws DAOException {
		try {
			prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.FLIGHTS F LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) "
					+ "WHERE (F.PIREP=?) AND (F.ARCHIVED=?) AND (F.CREATED < DATE_SUB(NOW(), INTERVAL ? HOUR)) "
					+ "ORDER BY F.CREATED");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, false);
			_ps.setInt(3, cutoff);
			return executeFlightInfo();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Message result sets.
	 */
	private List<TextMessage> executeMsg() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the requests
		List<TextMessage> results = new ArrayList<TextMessage>();
		while (rs.next()) {
			TextMessage msg = new TextMessage(rs.getTimestamp(1));
			msg.setAuthorID(rs.getInt(2));
			msg.setRecipientID(rs.getInt(3));
			msg.setMessage(rs.getString(4));

			// Add to results
			results.add(msg);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}