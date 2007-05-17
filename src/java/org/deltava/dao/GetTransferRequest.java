// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.Test;
import org.deltava.beans.system.TransferRequest;

/**
 * A Data Access Object to read Pilot Transfer requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetTransferRequest extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetTransferRequest(Connection c) {
		super(c);
	}

	/**
	 * Returns a Transfer Request for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a TransferRequest bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TransferRequest get(int pilotID) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT TX.*, CR.STATUS FROM TXREQUESTS TX LEFT JOIN CHECKRIDES CR ON "
					+ "(TX.CHECKRIDE_ID=CR.ID) WHERE (TX.ID=?)");
			_ps.setInt(1, pilotID);

			// Execute the query, if empty return null
			List<TransferRequest> results = execute();
			setQueryMax(0);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of pending Transfer Requests into a particular equipment program.
	 * @param eqType the Equipment program name, or null for all
	 * @return the number of Transfer Requests
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCount(String eqType) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT COUNT(*) FROM TXREQUESTS");
		if (eqType != null)
			buf.append(" WHERE (EQTYPE=?)");
		
		try {
			prepareStatementWithoutLimits(buf.toString());
			if (eqType != null)
				_ps.setString(1, eqType);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			int result = rs.next() ? rs.getInt(1) : 0;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Transfer Request associated with a paritcular Check Ride.
	 * @param checkRideID the Check Ride database ID
	 * @return a TransferRequest bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TransferRequest getByCheckRide(int checkRideID) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT TX.*, CR.STATUS FROM TXREQUESTS TX LEFT JOIN CHECKRIDES CR ON "
					+ "(TX.CHECKRIDE_ID=CR.ID) WHERE (TX.CHECKRIDE_ID=?)");
			_ps.setInt(1, checkRideID);

			// Execute the query, if empty return null
			List<TransferRequest> results = execute();
			setQueryMax(0);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Transfer Requests older than a certain age.
	 * @param minAge the number of days
	 * @return a Collection of TransferRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TransferRequest> getAged(int minAge) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT TX.*, CR.STATUS FROM TXREQUESTS TX LEFT JOIN CHECKRIDES CR ON "
					+ "(TX.CHECKRIDE_ID=CR.ID) WHERE (TX.CREATED < DATE_SUB(NOW(), INTERVAL ? DAY)) AND "
					+ "(TX.STATUS<>?) AND (CR.STATUS<>?) AND (CR.STATUS<>?) ORDER BY TX.CREATED");
			_ps.setInt(1, minAge);
			_ps.setInt(2, TransferRequest.OK);
			_ps.setInt(3, Test.SUBMITTED);
			_ps.setInt(4, Test.SCORED);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Transfer Requests.
	 * @param orderBy the sort order
	 * @return a List of TransferRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<TransferRequest> getAll(String orderBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT TX.*, CR.STATUS, P.LASTNAME FROM (TXREQUESTS TX, "
				+ "PILOTS P) LEFT JOIN CHECKRIDES CR ON (TX.CHECKRIDE_ID=CR.ID) WHERE (TX.ID=P.ID) ORDER BY ");
		sqlBuf.append((orderBy != null) ? orderBy : "TX.STATUS DESC, CR.STATUS DESC, TX.CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List<TransferRequest> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the result set
		List<TransferRequest> results = new ArrayList<TransferRequest>();
		while (rs.next()) {
			TransferRequest txreq = new TransferRequest(rs.getInt(1), rs.getString(4));
			txreq.setStatus(rs.getInt(2));
			txreq.setCheckRideID(rs.getInt(3));
			txreq.setDate(rs.getTimestamp(5));
			txreq.setRatingOnly(rs.getBoolean(6));
			txreq.setCheckRideSubmitted((rs.getInt(7) == Test.SUBMITTED));

			// Add to results
			results.add(txreq);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}