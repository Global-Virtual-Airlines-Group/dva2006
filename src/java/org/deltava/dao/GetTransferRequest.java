// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
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
					+ "(TX.CHECKRIDE_ID=CR.ID) WHERE (ID=?)");
			_ps.setInt(1, pilotID);

			// Execute the query, if empty return null
			List<TransferRequest> results = execute();
			return results.isEmpty() ? null : results.get(0);
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
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Transfer Requests.
	 * @return a List of TransferRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<TransferRequest> getAll() throws DAOException {
		try {
			prepareStatement("SELECT TX.*, CR.STATUS FROM TXREQUESTS TX LEFT JOIN CHECKRIDES CR ON "
					+ "(TX.CHECKRIDE_ID=CR.ID) ORDER BY TX.CREATED");
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