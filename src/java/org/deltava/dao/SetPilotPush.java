// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.PushEndpoint;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write push notification subscription data to the database.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class SetPilotPush extends PilotWriteDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetPilotPush(Connection c) {
		super(c);
	}

	/**
	 * Writes a push notification endpoint to the database
	 * @param ep a PushEndpoint bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(PushEndpoint ep) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO PILOT_PUSH (ID, CREATED, AUTH, PUB256DH, URL) VALUES (?, ?, ?, ?, ?)")) {
			ps.setInt(1, ep.getID());
			ps.setTimestamp(2, createTimestamp(ep.getCreatedOn()));
			ps.setString(3, ep.getAuth());
			ps.setString(4, ep.getPub256DH());
			ps.setString(5, ep.getURL());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(ep.getID()));
		}
	}
	
	/**
	 * Deletes a push notification endpoint from the database.
	 * @param pilotID the Pilot's database ID
	 * @param url the endpoint URL
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID, String url) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PILOT_PUSH WHERE (ID=?) AND (URL=?)")) {
			ps.setInt(1, pilotID);
			ps.setString(2, url);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(pilotID));
		}
	}
}