// Copyright 2005, 2007, 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.hr.TransferRequest;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write equipment program Transfer Requests.
 * @author Luke
 * @version 7.5
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
		String db = formatDBName(dbName);
		try {
			startTransaction();
            prepareStatement("INSERT INTO " + db + ".TXREQUESTS (STATUS, EQTYPE, ACTYPE, CREATED, RATING_ONLY, ID) VALUES (?, ?, ?, NOW(), ?, ?)");
            _ps.setInt(1, txreq.getStatus());
            _ps.setString(2, txreq.getEquipmentType());
            _ps.setString(3, txreq.getAircraftType());
            _ps.setBoolean(4, txreq.getRatingOnly());
            _ps.setInt(5, txreq.getID());
            executeUpdate(1);
            
			// Write check ride IDs
			writeRides(txreq, db);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
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
			startTransaction();
			prepareStatement("UPDATE TXREQUESTS SET STATUS=?, EQTYPE=?, ACTYPE=?, CREATED=?, RATING_ONLY=? WHERE (ID=?)");
			_ps.setInt(1, txreq.getStatus());
			_ps.setString(2, txreq.getEquipmentType());
			_ps.setString(3, txreq.getAircraftType());
			_ps.setTimestamp(4, createTimestamp(txreq.getDate()));
			_ps.setBoolean(5, txreq.getRatingOnly());
			_ps.setInt(6, txreq.getID());
			executeUpdate(1);
			
			// Write check ride IDs
			writeRides(txreq, formatDBName(SystemData.get("airline.db")));
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write check ride IDs to the database.
	 */
	private void writeRides(TransferRequest txreq, String db) throws SQLException {
		
		// Clear the check rides
		prepareStatementWithoutLimits("DELETE FROM " + db + ".TXRIDES WHERE (ID=?)");
		_ps.setInt(1, txreq.getID());
		executeUpdate(0);
		
		// Write the check rides
		prepareStatementWithoutLimits("INSERT INTO " + db + ".TXRIDES (ID, CHECKRIDE_ID) VALUES (?, ?)");
		_ps.setInt(1, txreq.getID());
		for (Integer ID : txreq.getCheckRideIDs()) {
			_ps.setInt(2, ID.intValue());
			_ps.addBatch();
		}
		
		_ps.executeBatch();
		_ps.close();
	}

	/**
	 * Deletes a Transfer Request from the current database.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM TXREQUESTS WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}