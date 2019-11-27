// Copyright 2005, 2007, 2011, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.hr.TransferRequest;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write equipment program Transfer Requests.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + db + ".TXREQUESTS (STATUS, EQTYPE, ACTYPE, CREATED, RATING_ONLY, SIM, ID) VALUES (?, ?, ?, NOW(), ?, ?, ?)")) {
				ps.setInt(1, txreq.getStatus().ordinal());
				ps.setString(2, txreq.getEquipmentType());
				ps.setString(3, txreq.getAircraftType());
				ps.setBoolean(4, txreq.getRatingOnly());
				ps.setInt(5,  txreq.getSimulator().getCode());
				ps.setInt(6, txreq.getID());
            	executeUpdate(ps, 1);
			}
            
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
			try (PreparedStatement ps = prepare("UPDATE TXREQUESTS SET STATUS=?, EQTYPE=?, ACTYPE=?, CREATED=?, RATING_ONLY=?, SIM=? WHERE (ID=?)")) {
				ps.setInt(1, txreq.getStatus().ordinal());
				ps.setString(2, txreq.getEquipmentType());
				ps.setString(3, txreq.getAircraftType());
				ps.setTimestamp(4, createTimestamp(txreq.getDate()));
				ps.setBoolean(5, txreq.getRatingOnly());
				ps.setInt(6, txreq.getSimulator().getCode());
				ps.setInt(7, txreq.getID());
				executeUpdate(ps, 1);
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + db + ".TXRIDES WHERE (ID=?)")) {
			ps.setInt(1, txreq.getID());
			executeUpdate(ps, 0);
		}
		
		// Write the check rides
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + db + ".TXRIDES (ID, CHECKRIDE_ID) VALUES (?, ?)")) {
			ps.setInt(1, txreq.getID());
			for (Integer ID : txreq.getCheckRideIDs()) {
				ps.setInt(2, ID.intValue());
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, txreq.getCheckRideIDs().size());
		}
	}

	/**
	 * Deletes a Transfer Request from the current database.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM TXREQUESTS WHERE (ID=?)")) {
			ps.setInt(1, pilotID);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}