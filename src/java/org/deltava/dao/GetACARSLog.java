// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object to load ACARS log data.
 * @author Luke
 * @version 9.0
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
		StringBuilder buf = new StringBuilder("SELECT C.ID, C.PILOT_ID, C.DATE, C.ENDDATE, INET6_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, C.CLIENT_BUILD, C.BETA_BUILD FROM acars.CONS C ");
		if (!terms.isEmpty()) {
			buf.append(" WHERE ");
			for (Iterator<String> i = terms.iterator(); i.hasNext();) {
				buf.append(i.next());
				if (i.hasNext())
					buf.append(" AND ");
			}
		}

		buf.append(" GROUP BY C.ID ORDER BY C.DATE DESC");

		try (PreparedStatement ps = prepare(buf.toString())) {
			int psOfs = 0;
			if (criteria.getPilotID() != 0)
				ps.setInt(++psOfs, criteria.getPilotID());
			if (criteria.getStartDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getStartDate()));
			if (criteria.getEndDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getEndDate()));
			
			return executeConnectionInfo(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all ACARS text messages matching particular criteria.
	 * @param criteria the ACARS log search criteria
	 * @param searchStr text to search for in the message body, or null
	 * @return a List of TextMessage beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<TextMessage> getMessages(LogSearchCriteria criteria, String searchStr) throws DAOException {

		// Build the search criteria
		List<String> terms = new ArrayList<String>();
		if (criteria.getPilotID() != 0)
			terms.add("((AUTHOR=?) OR (RECIPIENT=?))");
		if (criteria.getStartDate() != null)
			terms.add("(DATE > ?)");
		if (criteria.getEndDate() != null)
			terms.add("(DATE < ?)");
		if (searchStr != null)
			terms.add("(LOCATE(?, BODY) > 0)");

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

		try (PreparedStatement ps = prepare(buf.toString())) {
			int psOfs = 0;
			if (criteria.getPilotID() != 0) {
				ps.setInt(++psOfs, criteria.getPilotID());
				ps.setInt(++psOfs, criteria.getPilotID());
			}

			if (criteria.getStartDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getStartDate()));
			if (criteria.getEndDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getEndDate()));
			if (searchStr != null)
				ps.setString(++psOfs, searchStr);

			return executeMsg(ps);
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
			terms.add("(F.PILOT_ID=?)");
		if (criteria.getStartDate() != null)
			terms.add("(F.CREATED > ?)");
		if (criteria.getEndDate() != null)
			terms.add("(F.CREATED < ?)");

		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT F.*, INET6_NTOA(F.REMOTE_ADDR), FD.ROUTE_ID, FDR.DISPATCHER_ID FROM acars.FLIGHTS F LEFT JOIN acars.FLIGHT_DISPATCH FD ON (F.ID=FD.ID) "
			+ "LEFT JOIN acars.FLIGHT_DISPATCHER FDR ON (F.ID=FDR.ID)");
		if (!terms.isEmpty()) {
			buf.append(" WHERE ");
			for (Iterator<String> i = terms.iterator(); i.hasNext();) {
				buf.append(i.next());
				if (i.hasNext())
					buf.append(" AND ");
			}
		}

		buf.append(" ORDER BY F.CREATED");

		try (PreparedStatement ps = prepare(buf.toString())) {
			int psOfs = 0;
			if (criteria.getPilotID() != 0)
				ps.setInt(++psOfs, criteria.getPilotID());
			if (criteria.getStartDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getStartDate()));
			if (criteria.getEndDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getEndDate()));

			return executeFlightInfo(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse Message result sets.
	 */
	private static List<TextMessage> executeMsg(PreparedStatement ps) throws SQLException {
		List<TextMessage> results = new ArrayList<TextMessage>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				TextMessage msg = new TextMessage(rs.getTimestamp(1).toInstant());
				msg.setAuthorID(rs.getInt(2));
				msg.setRecipientID(rs.getInt(3));
				msg.setMessage(rs.getString(4));
				results.add(msg);
			}
		}

		return results;
	}
}