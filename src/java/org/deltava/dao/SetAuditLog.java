// Copyright 2017, 2019, 2021, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;

/**
 * A Data Access Object to write the audit log for an object.
 * @author Luke
 * @version 12.5
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
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AUDIT_LOG (CREATED, TYPE, ID, APPNAME, AUTHOR_ID, REMOTE_HOST, REMOTE_ADDR, DESCRIPTION) VALUES (NOW(),?,?,?,?,? INET6_ATON(?),?)")) {
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
	
	/**
	 * Writes an Admin audit log entry to the database.
	 * @param al the AdmingLogEntry bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(AdminLogEntry al) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO ADMIN_LOG (CREATED, TYPE, ID, AUTHOR_ID, REMOTE_HOST, REMOTE_ADDR) VALUES (?,?,?,?,?,INET6_ATON(?))")) {
			ps.setTimestamp(1, createTimestamp(al.getDate()));
			ps.setString(2, al.getAuditType());
			ps.setString(3, al.getAuditID());
			ps.setInt(4, al.getAuthorID());
			ps.setString(5, al.getRemoteHost());
			ps.setString(6, al.getRemoteAddr());
			executeUpdate(ps, 1);			
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}