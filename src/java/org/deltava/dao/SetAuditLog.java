// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.AuditLog;

/**
 * A Data Access Object to write the audit log for an object.
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public class SetAuditLog extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAuditLog(Connection c) {
		super(c);
	}

	/**
	 * Writes an audit log entry to the database.
	 * @param al the AuditLog bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(AuditLog al) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO AUDIT_LOG (CREATED, TYPE, ID, AUTHOR_ID, REMOTE_HOST, REMOTE_ADDR, DESCRIPTION) VALUES (NOW(), ?, ?, ?, ?, INET6_ATON(?), ?)");
			_ps.setString(1, al.getAuditType());
			_ps.setString(2, al.getAuditID());
			_ps.setInt(3, al.getAuthorID());
			_ps.setString(4, al.getRemoteHost());
			_ps.setString(5, al.getRemoteAddr());
			_ps.setString(6, al.getDescription());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}