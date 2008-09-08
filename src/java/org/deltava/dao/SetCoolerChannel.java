// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.cooler.Channel;

/**
 * A Data Access Object to write Water Cooler Channel Profiles.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class SetCoolerChannel extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetCoolerChannel(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Channel Profile to the database.
	 * @param c the Channel Profile bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Channel c) throws DAOException {
		try {
			startTransaction();
			
			// Write the channel profile
			prepareStatement("INSERT INTO common.COOLER_CHANNELS (CHANNEL, DESCRIPTION, ACTIVE, ALLOW_NEW) "
					+ "VALUES (?, ?, ?, ?)");
			_ps.setString(1, c.getName());
			_ps.setString(2, c.getDescription());
			_ps.setBoolean(3, c.getActive());
			_ps.setBoolean(4, c.getAllowNewPosts());
			executeUpdate(1);
			
			// Write the channel data
			prepareStatement("INSERT INTO common.COOLER_CHANNELINFO (CHANNEL, INFOTYPE, INFODATA) VALUES (?, ?, ?)");
			_ps.setString(1, c.getName());
			
			// Dump read roles
			_ps.setInt(2, Channel.INFOTYPE_RROLE);
			for (Iterator<String> i = c.getReadRoles().iterator(); i.hasNext(); ) {
				_ps.setString(3, i.next());
				_ps.addBatch();
			}
			
			// Dump write roles
			_ps.setInt(2, Channel.INFOTYPE_WROLE);
			for (Iterator<String> i = c.getWriteRoles().iterator(); i.hasNext(); ) {
				_ps.setString(3, i.next());
				_ps.addBatch();
			}
			
			// Dump airlines
			_ps.setInt(2, Channel.INFOTYPE_AIRLINE);
			for (Iterator<String> i = c.getAirlines().iterator(); i.hasNext(); ) {
				_ps.setString(3, i.next());
				_ps.addBatch();
			}
			
			// Execute the update and commit
			_ps.executeBatch();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an existing Channel Profile within the database.
	 * @param c the Channel Profile bean
	 * @param newName the new Channel name if renaming, otherwise the old name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Channel c, String newName) throws DAOException {
		CoolerThreadDAO.invalidateAll();
		try {
			startTransaction();
			
			// Clear out the channel data and rewrite
			prepareStatement("DELETE FROM common.COOLER_CHANNELINFO WHERE (CHANNEL=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Write the channel data
			prepareStatement("INSERT INTO common.COOLER_CHANNELINFO (CHANNEL, INFOTYPE, INFODATA) VALUES (?, ?, ?)");
			_ps.setString(1, c.getName());
			
			// Dump roles
			_ps.setInt(2, Channel.INFOTYPE_RROLE);
			for (Iterator<String> i = c.getReadRoles().iterator(); i.hasNext(); ) {
				_ps.setString(3, i.next());
				_ps.addBatch();
			}
			
			// Dump write roles
			_ps.setInt(2, Channel.INFOTYPE_WROLE);
			for (Iterator<String> i = c.getWriteRoles().iterator(); i.hasNext(); ) {
				_ps.setString(3, i.next());
				_ps.addBatch();
			}
			
			// Dump airlines
			_ps.setInt(2, Channel.INFOTYPE_AIRLINE);
			for (Iterator<String> i = c.getAirlines().iterator(); i.hasNext(); ) {
				_ps.setString(3, i.next());
				_ps.addBatch();
			}

			// write the channel data
			_ps.executeBatch();
			
			// Update the channel profile
			prepareStatement("UPDATE common.COOLER_CHANNELS SET CHANNEL=?, DESCRIPTION=?, ACTIVE=?, "
					+ "ALLOW_NEW=? WHERE (CHANNEL=?)");
			_ps.setString(1, newName);
			_ps.setString(2, c.getDescription());
			_ps.setBoolean(3, c.getActive());
			_ps.setBoolean(4, c.getAllowNewPosts());
			_ps.setString(5, c.getName());
			
			// Execute the update and commit - this may take a moment since it'll take a while for the key change to cascade
			executeUpdate(1);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Water Cooler channel profile.
	 * @param c the Channel bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Channel c) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.COOLER_CHANNELS WHERE (CHANNEL=?)");
			_ps.setString(1, c.getName());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}