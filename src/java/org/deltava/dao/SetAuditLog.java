// Copyright 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.AuditLog;

/**
 * A Data Access Object to write the audit log for an object.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AUDIT_LOG (CREATED, TYPE, ID, AUTHOR_ID, REMOTE_HOST, REMOTE_ADDR, DESCRIPTION) VALUES (NOW(), ?, ?, ?, ?, INET6_ATON(?), ?)")) {
			ps.setString(1, al.getAuditType());
			ps.setString(2, al.getAuditID());
			ps.setInt(3, al.getAuthorID());
			ps.setString(4, al.getRemoteHost());
			ps.setString(5, al.getRemoteAddr());
			ps.setString(6, al.getDescription());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Clears the audit log for a particular object.
	 * @param auditType the object type
	 * @param id the object ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clear(String auditType, String id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AUDIT_LOG WHERE (TYPE=?) AND (ID=?)")) {
			ps.setString(1, auditType);
			ps.setString(2, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}