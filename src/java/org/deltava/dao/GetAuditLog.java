// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.AuditLog;

/**
 * A Data Access Object to read the audit log for an object.
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public class GetAuditLog extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAuditLog(Connection c) {
		super(c);
	}

	/**
	 * Loads all Audit log entries for a given object.
	 * @param type the object type
	 * @param id the object ID
	 * @return a Collection of AuditLog beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<AuditLog> getEntries(String type, String id) throws DAOException {
		try {
			prepareStatement("SELECT TYPE, ID, CREATED, AUTHOR_ID, REMOTE_HOST, INET6_NTOA(REMOTE_ADDR), DESCRIPTION FROM AUDIT_LOG WHERE (TYPE=?) AND (ID=?)");
			_ps.setString(1, type);
			_ps.setString(2, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Audit log entries for a given object type
	 * @param type the object type
	 * @return a Collection of AuditLog beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<AuditLog> getEntries(String type) throws DAOException {
		try {
			prepareStatement("SELECT TYPE, ID, CREATED, AUTHOR_ID, REMOTE_HOST, INET6_NTOA(REMOTE_ADDR), DESCRIPTION FROM AUDIT_LOG WHERE (TYPE=?)");
			_ps.setString(1, type);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Result set parsing helper method.
	 */
	private List<AuditLog> execute() throws SQLException {
		List<AuditLog> results = new ArrayList<AuditLog>();
		try (ResultSet rs = _ps.executeQuery()) {
			AuditLog ae = new AuditLog(rs.getString(1), rs.getString(2), rs.getInt(4));
			ae.setDate(toInstant(rs.getTimestamp(3)));
			ae.setRemoteHost(rs.getString(5));
			ae.setRemoteAddr(rs.getString(6));
			ae.setDescription(rs.getString(7));
			results.add(ae);
		}
		
		_ps.close();
		return results;
	}
}