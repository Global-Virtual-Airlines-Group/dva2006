// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.EMailDelivery;

/**
 * A Data Access Object to handle message delivery reports.
 * @author Luke
 * @version 9.0
 * @since 8.5
 */

public class SetEMailDelivery extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetEMailDelivery(Connection c) {
		super(c);
	}

	/**
	 * Writes an e-mail delivery report to the database.
	 * @param dv the EMailDelivery
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(EMailDelivery dv) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO EMAIL_DELIVERY (ID, MSG_ID, SEND_TIME, RCPT_TIME, EMAIL, PROCESS_TIME, NOTIFY_TYPE, REMOTE_ADDR, REMOTE_HOST, RESPONSE) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?), ?, ?)")) {
			ps.setInt(1, dv.getID());
			ps.setString(2, dv.getMessageID());
			ps.setTimestamp(3, createTimestamp(dv.getSendTime()));
			ps.setTimestamp(4, createTimestamp(dv.getDeliveryTime()));
			ps.setString(5, dv.getEmail());
			ps.setInt(6, dv.getProcessTime());
			ps.setInt(7, dv.getType().ordinal());
			ps.setString(8, dv.getRemoteAddress());
			ps.setString(9, dv.getRemoteHost());
			ps.setString(10, dv.getResponse());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}