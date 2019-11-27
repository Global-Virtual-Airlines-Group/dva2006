// Copyright 2011, 2012, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.mvs.Channel;
import org.deltava.beans.system.AirlineInformation;

/**
 * A Data Access Object to write permanent voice channel data.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.CHANNELS (ID, NAME, DESCRIPTION, RATE, RNG, MAXUSERS) VALUES (?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, ch.getID());
				ps.setString(2, ch.getName());
				ps.setString(3, ch.getDescription());
				ps.setInt(4, ch.getSampleRate().getRate());
				ps.setInt(5, ch.getRange());
				ps.setInt(6, ch.getMaxUsers());
				executeUpdate(ps, 1);
			}
			
			// Get ID
			if (ch.getID() == 0)
				ch.setID(getNewID());
			
			// Write airlines
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO acars.CHANNEL_AIRLINES (ID, CODE) VALUES (?, ?)")) {
				ps.setInt(1, ch.getID());
				for (AirlineInformation ai : ch.getAirlines()) {
					ps.setString(2, ai.getCode());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, ch.getAirlines().size());
			}
			
			// Write roles
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO acars.CHANNEL_ROLES (ID, ROLE, TYPE) VALUES (?, ?, ?)")) {
				ps.setInt(1, ch.getID());
				for (Channel.Access a : Channel.Access.values()) {
					ps.setInt(3, a.ordinal());
					for (String role : ch.getRoles(a)) {
						ps.setString(2, role);
						ps.addBatch();
					}
				}

				executeUpdate(ps, 1, 0);
			}
			
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
		try (PreparedStatement ps = prepare("DELETE FROM acars.CHANNELS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	} 
}