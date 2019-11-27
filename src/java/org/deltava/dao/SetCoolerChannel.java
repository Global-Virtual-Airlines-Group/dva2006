// Copyright 2005, 2008, 2011, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.cooler.Channel;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write Water Cooler Channel Profiles.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_CHANNELS (CHANNEL, DESCRIPTION, ACTIVE, ALLOW_NEW) VALUES (?, ?, ?, ?)")) {
				ps.setString(1, c.getName());
				ps.setString(2, c.getDescription());
				ps.setBoolean(3, c.getActive());
				ps.setBoolean(4, c.getAllowNewPosts());
				executeUpdate(ps, 1);
			}
			
			// Write data
			writeInfo(c);
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
		try {
			startTransaction();
			
			// Clear out the channel data and rewrite
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.COOLER_CHANNELINFO WHERE (CHANNEL=?)")) {
				ps.setString(1, c.getName());
				executeUpdate(ps, 0);
			}
			
			// Write data
			writeInfo(c);
			
			// Update the channel profile
			try (PreparedStatement ps = prepare("UPDATE common.COOLER_CHANNELS SET CHANNEL=?, DESCRIPTION=?, ACTIVE=?, ALLOW_NEW=? WHERE (CHANNEL=?)")) {
				ps.setString(1, newName);
				ps.setString(2, c.getDescription());
				ps.setBoolean(3, c.getActive());
				ps.setBoolean(4, c.getAllowNewPosts());
				ps.setString(5, c.getName());
				executeUpdate(ps, 1);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads");
			CacheManager.invalidate("CoolerChannels");
		}
	}
	
	/*
	 * Helper method to write metadata.
	 */
	private void writeInfo(Channel c) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_CHANNELINFO (CHANNEL, INFOTYPE, INFODATA) VALUES (?, ?, ?)")) {
			ps.setString(1, c.getName());
		
			// Dump read roles
			ps.setInt(2, Channel.InfoType.READ.ordinal());
			for (Iterator<String> i = c.getReadRoles().iterator(); i.hasNext(); ) {
				ps.setString(3, i.next());
				ps.addBatch();
			}
		
			// Dump write roles
			ps.setInt(2, Channel.InfoType.WRITE.ordinal());
			for (Iterator<String> i = c.getWriteRoles().iterator(); i.hasNext(); ) {
				ps.setString(3, i.next());
				ps.addBatch();
			}
		
			// Dump notify roles
			ps.setInt(2, Channel.InfoType.NOTIFY.ordinal());
			for (Iterator<String> i = c.getNotifyRoles().iterator(); i.hasNext(); ) {
				ps.setString(3, i.next());
				ps.addBatch();
			}
		
			// Dump airlines
			ps.setInt(2, Channel.InfoType.AIRLINE.ordinal());
			for (Iterator<String> i = c.getAirlines().iterator(); i.hasNext(); ) {
				ps.setString(3, i.next());
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, 0);
		}
	}
	
	/**
	 * Deletes a Water Cooler channel profile.
	 * @param c the Channel bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Channel c) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.COOLER_CHANNELS WHERE (CHANNEL=?)")) {
			ps.setString(1, c.getName());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}