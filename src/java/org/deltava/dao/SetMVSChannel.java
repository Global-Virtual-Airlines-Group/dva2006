// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.mvs.Channel;
import org.deltava.beans.system.AirlineInformation;

/**
 * A Data Access Object to write permanent voice channel data.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class SetMVSChannel extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetMVSChannel(Connection c) {
		super(c);
	}

	/**
	 * Writes an MVS channel to the database.
	 * @param ch the Channel
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Channel ch) throws DAOException {
		try {
			startTransaction();
			
			// Write channel entry
			prepareStatement("REPLACE INTO acars.CHANNELS (ID, NAME, DESCRIPTION, RATE, RNG, "
				+ "ISDEFAULT) VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, ch.getID());
			_ps.setString(2, ch.getName());
			_ps.setString(3, ch.getDescription());
			_ps.setInt(4, ch.getSampleRate().getRate());
			_ps.setInt(5, ch.getRange());
			_ps.setBoolean(6, ch.getIsDefault());
			executeUpdate(1);
			
			// Get ID
			if (ch.getID() == 0)
				ch.setID(getNewID());
			
			// Write airlines
			prepareStatementWithoutLimits("INSERT INTO acars.CHANNEL_AIRLINES (ID, CODE) VALUES (?, ?)");
			_ps.setInt(1, ch.getID());
			for (AirlineInformation ai : ch.getAirlines()) {
				_ps.setString(2, ai.getCode());
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
			
			// Write roles
			prepareStatementWithoutLimits("INSERT INTO acars.CHANNEL_ROLES (ID, ROLE, TYPE) VALUES (?, ?, ?)");
			_ps.setInt(1, ch.getID());
			for (Channel.Access a : Channel.Access.values()) {
				_ps.setInt(3, a.ordinal());
				for (String role : ch.getRoles(a)) {
					_ps.setString(2, role);
					_ps.addBatch();
				}
			}
			
			_ps.executeBatch();
			_ps.close();
			
			// Ensure we have a default channel and commit
			ensureDefaultChannel();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Sets a random Channel as the default for a Server if no default is specified.
	 */
	private void ensureDefaultChannel() throws SQLException {
		prepareStatement("SELECT COUNT(*) FROM acars.CHANNELS WHERE (ISDEFAULT=?)");
		_ps.setBoolean(1, true);
			
		// Check for a default channel
		ResultSet rs = _ps.executeQuery();
		boolean hasDefault = rs.next() ? (rs.getInt(1) > 0) : false;
		rs.close();
		_ps.close();
		if (hasDefault)
			return;
			
		// Update the default channel
		prepareStatementWithoutLimits("UPDATE acars.CHANNELS SET ISDEFAULT=? ORDER BY RAND() LIMIT 1");
		_ps.setBoolean(1, true);
		executeUpdate(1);
	}
	
	/**
	 * Deletes an MVS channel from the daatabase.
	 * @param id the channel database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			startTransaction();
			
			// Delete the channel
			prepareStatement("DELETE FROM acars.CHANNELS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
			
			// Ensure we have a default channel
			ensureDefaultChannel();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	} 
}