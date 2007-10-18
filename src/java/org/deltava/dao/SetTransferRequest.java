// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.TransferRequest;

/**
 * A Data Access Object to write equipment program Transfer Requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetTransferRequest extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetTransferRequest(Connection c) {
		super(c);
	}

	/**
	 * Writes a Transfer Request to the database. This can handle INSERTs and UPDATEs.
	 * @param txreq the TransferRequest bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(TransferRequest txreq) throws DAOException {
		try {
			// Get the database
			prepareStatement("SELECT AI.DBNAME FROM common.AIRLINEINFO AI, common.EQPROGRAMS EP WHERE "
					+ "(AI.CODE=EP.AIRLINE) AND (EP.EQTYPE=?)");
			_ps.setString(1, txreq.getEquipmentType());
			ResultSet rs = _ps.executeQuery();
			String dbName = rs.next() ? formatDBName(rs.getString(1)) : null;
			rs.close();
			_ps.close();
			if (dbName == null)
				throw new DAOException("Unknown Equipment Type - " + txreq.getEquipmentType());

			// Write the request
			if (txreq.getDate() == null) {
				prepareStatement("INSERT INTO " + dbName + ".TXREQUESTS (STATUS, CHECKRIDE_ID, EQTYPE, CREATED, "
						+ "RATING_ONLY, ID) VALUES (?, ?, ?, ?, ?, ?)");
				txreq.setDate(new java.util.Date());
			} else
				prepareStatement("UPDATE " + dbName + ".TXREQUESTS SET STATUS=?, CHECKRIDE_ID=?, EQTYPE=?, "
						+ "CREATED=?, RATING_ONLY=? WHERE (ID=?)");

			// Update the prepared statement
			_ps.setInt(1, txreq.getStatus());
			_ps.setInt(2, txreq.getCheckRideID());
			_ps.setString(3, txreq.getEquipmentType());
			_ps.setTimestamp(4, createTimestamp(txreq.getDate()));
			_ps.setBoolean(5, txreq.getRatingOnly());
			_ps.setInt(6, txreq.getID());

			// Execute the update
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Transfer Request from the database.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID) throws DAOException {
		try {
			prepareStatement("DELETE FROM TXREQUESTS WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}