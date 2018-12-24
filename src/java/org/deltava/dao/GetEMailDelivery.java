// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.system.DeliveryType;
import org.deltava.beans.system.EMailDelivery;

/**
 * A Data Access Object to load e-mail delivery reports from the database.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

public class GetEMailDelivery extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetEMailDelivery(Connection c) {
		super(c);
	}

	/**
	 * Loads e-mail delivery reports for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a List of EMailDelivery beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EMailDelivery> getByPilot(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT ID, MSG_ID, SEND_TIME, RCPT_TIME, EMAIL, PROCESS_TIME, NOTIFY_TYPE, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, RESPONSE FROM EMAIL_DELIVERY WHERE (ID=?) ORDER BY SEND_TIME");
			_ps.setInt(1, pilotID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads e-mail delivery reports for a particular date.
	 * @param dt the date
	 * @return a List of EMailDelivery beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EMailDelivery> getByDate(Instant dt) throws DAOException {
		try {
			prepareStatement("SELECT ID, MSG_ID, SEND_TIME, RCPT_TIME, EMAIL, PROCESS_TIME, NOTIFY_TYPE, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, RESPONSE FROM EMAIL_DELIVERY WHERE DATE(SEND_TIME)=DATE(?) ORDER BY SEND_TIME");
			_ps.setTimestamp(1, createTimestamp(dt));
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads e-mail delivery reports for a particular domain.
	 * @param domain the e-mail domain
	 * @return a List of EMailDelivery beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EMailDelivery> getByDomain(String domain) throws DAOException {
		try {
			prepareStatement("SELECT ID, MSG_ID, SEND_TIME, RCPT_TIME, EMAIL, PROCESS_TIME, NOTIFY_TYPE, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, RESPONSE FROM EMAIL_DELIVERY WHERE (ADDR LIKE ?) ORDER BY SEND_TIME");
			_ps.setString(1, "%" + domain);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private List<EMailDelivery> execute() throws SQLException {
		List<EMailDelivery> results = new ArrayList<EMailDelivery>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				EMailDelivery dv = new EMailDelivery(DeliveryType.values()[rs.getInt(7)], rs.getInt(1), toInstant(rs.getTimestamp(4)));
				dv.setMessageID(rs.getString(2));
				dv.setSendTime(toInstant(rs.getTimestamp(3)));
				dv.setEmail(rs.getString(5));
				dv.setProcessTime(rs.getInt(6));
				dv.setRemoteAddress(rs.getString(8));
				dv.setRemoteHost(rs.getString(9));
				dv.setResponse(rs.getString(10));
				results.add(dv);
			}
		}
		
		_ps.close();
		return results;
	}
}