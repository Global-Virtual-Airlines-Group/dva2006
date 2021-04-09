// Copyright 2017, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.AuditLog;

/**
 * A Data Access Object to write the audit log for an object.
 * @author Luke
 * @version 10.0
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AUDIT_LOG (CREATED, TYPE, ID, APPNAME, AUTHOR_ID, REMOTE_HOST, REMOTE_ADDR, DESCRIPTION) VALUES (NOW(), ?, ?, ?, ?, ?, INET6_ATON(?), ?)")) {
			ps.setString(1, al.getAuditType());
			ps.setString(2, al.getAuditID());
			ps.setString(3, al.getApplication());
			ps.setInt(4, al.getAuthorID());
			ps.setString(5, al.getRemoteHost());
			ps.setString(6, al.getRemoteAddr());
			ps.setString(7, al.getDescription());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}