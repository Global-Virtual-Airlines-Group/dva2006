// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.mvs.Channel;

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
			prepareStatement("REPLACE INTO acars.CHANNELS (ID, NAME, RATE, LAT, LNG, RNG) VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, ch.getID());
			_ps.setString(2, ch.getName());
			_ps.setInt(3, ch.getSampleRate().getRate());
			_ps.setDouble(4, ch.getCenter().getLatitude());
			_ps.setDouble(5, ch.getCenter().getLongitude());
			_ps.setInt(6, ch.getRange());
			executeUpdate(1);
			
			// Get ID
			if (ch.getID() == 0)
				ch.setID(getNewID());
			
			// Write roles
			prepareStatement("INSERT INTO acars.CHANNEL_ROLES (ID, ROLE, TYPE) VALUES (?, ?, ?)");
			_ps.setInt(1, ch.getID());
			
			// Write admin roles
			_ps.setInt(3, Channel.ADMIN_ROLE);
			for (String role : ch.getAdminRoles()) {
				_ps.setString(2, role);
				_ps.addBatch();
			}
			
			// Write talk roles
			_ps.setInt(3, Channel.TALK_ROLE);
			for (String role : ch.getTalkRoles()) {
				_ps.setString(2, role);
				_ps.addBatch();
			}
			
			// Write view roles
			_ps.setInt(3, Channel.JOIN_ROLE);
			for (String role : ch.getViewRoles()) {
				_ps.setString(2, role);
				_ps.addBatch();
			}
			
			// Commit
			_ps.executeBatch();
			_ps.close();
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