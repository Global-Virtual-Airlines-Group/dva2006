// Copyright 2011, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.mvs.Channel;
import org.deltava.beans.system.AirlineInformation;

/**
 * A Data Access Object to write permanent voice channel data.
 * @author Luke
 * @version 8.0
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
			prepareStatement("REPLACE INTO acars.CHANNELS (ID, NAME, DESCRIPTION, RATE, RNG, MAXUSERS) "
				+ " VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, ch.getID());
			_ps.setString(2, ch.getName());
			_ps.setString(3, ch.getDescription());
			_ps.setInt(4, ch.getSampleRate().getRate());
			_ps.setInt(5, ch.getRange());
			_ps.setInt(6, ch.getMaxUsers());
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
			
			executeBatchUpdate(1, ch.getAirlines().size());
			
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

			executeBatchUpdate(1, 0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an MVS channel from the daatabase.
	 * @param id the channel database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM acars.CHANNELS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	} 
}