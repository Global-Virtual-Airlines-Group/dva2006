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
	 * Writes a new Transfer Request into a database.
	 * @param txreq the TransferRequest bean
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(TransferRequest txreq, String dbName) throws DAOException {
		try {
            prepareStatement("INSERT INTO " + formatDBName(dbName) + ".TXREQUESTS (STATUS, CHECKRIDE_ID, EQTYPE, "
            		+ "CREATED, RATING_ONLY, ID) VALUES (?, ?, ?, NOW(), ?, ?)");
            _ps.setInt(1, txreq.getStatus());
            _ps.setInt(2, txreq.getCheckRideID());
            _ps.setString(3, txreq.getEquipmentType());
            _ps.setBoolean(4, txreq.getRatingOnly());
            _ps.setInt(5, txreq.getID());
            executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Transfer Request in the current database.
	 * @param txreq the TransferRequest bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(TransferRequest txreq) throws DAOException {
		try {
			prepareStatement("UPDATE TXREQUESTS SET STATUS=?, CHECKRIDE_ID=?, EQTYPE=?, CREATED=?, RATING_ONLY=? WHERE (ID=?)");
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
	 * Deletes a Transfer Request from the current database.
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