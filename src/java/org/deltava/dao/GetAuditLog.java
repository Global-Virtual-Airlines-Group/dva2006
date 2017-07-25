// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

/**
 * A Data Access Object to read the audit log for an object.
 * @author Luke
 * @version 7.5
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
	 * @param a the Auditable object
	 * @return a Collection of AuditLog beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<AuditLog> getEntries(Auditable a) throws DAOException {
		try {
			prepareStatement("SELECT TYPE, ID, CREATED, AUTHOR_ID, REMOTE_HOST, INET6_NTOA(REMOTE_ADDR), DESCRIPTION FROM common.AUDIT_LOG WHERE (TYPE=?) AND (ID=?)");
			_ps.setString(1, a.getAuditType());
			_ps.setString(2, a.getAuditID());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Audit log entries for a given object type
	 * @return a Collection of AuditLog beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<AuditLog> getEntries() throws DAOException {
		try {
			prepareStatement("SELECT TYPE, ID, CREATED, AUTHOR_ID, REMOTE_HOST, INET6_NTOA(REMOTE_ADDR), DESCRIPTION FROM common.AUDIT_LOG ORDER BY CREATED DESC");
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
			while (rs.next()) {
				AuditLog ae = new AuditLog(rs.getString(1), rs.getString(2), rs.getInt(4));
				ae.setDate(toInstant(rs.getTimestamp(3)));
				ae.setRemoteHost(rs.getString(5));
				ae.setRemoteAddr(rs.getString(6));
				ae.setDescription(rs.getString(7));
				results.add(ae);
			}
		}
		
		_ps.close();
		return results;
	}
}