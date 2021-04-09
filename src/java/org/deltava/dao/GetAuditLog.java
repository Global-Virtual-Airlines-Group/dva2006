// Copyright 2017, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read the audit log for an object.
 * @author Luke
 * @version 10.0
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
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT TYPE, ID, CREATED, APPNAME, AUTHOR_ID, REMOTE_HOST, INET6_NTOA(REMOTE_ADDR), DESCRIPTION FROM common.AUDIT_LOG WHERE (TYPE=?) AND (ID=?)");
		if (!a.isCrossApp())
			sqlBuf.append(" AND (APPNAME=?)");
		
		sqlBuf.append(" ORDER BY CREATED DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, a.getAuditType());
			ps.setString(2, a.getAuditID());
			if (!a.isCrossApp())
				ps.setString(3, SystemData.get("airline.code"));
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Audit log entries.
	 * @return a Collection of AuditLog beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<AuditLog> getEntries() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT TYPE, ID, CREATED, APPNAME, AUTHOR_ID, REMOTE_HOST, INET6_NTOA(REMOTE_ADDR), DESCRIPTION FROM common.AUDIT_LOG WHERE ((APPNAME=?) OR (APPNAME=?)) ORDER BY CREATED DESC")) {
			ps.setString(1, AuditLog.COMMON);
			ps.setString(2, SystemData.get("airline.code"));
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Result set parsing helper method.
	 */
	private static List<AuditLog> execute(PreparedStatement ps) throws SQLException {
		List<AuditLog> results = new ArrayList<AuditLog>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				AuditLog ae = new AuditLog(rs.getString(1), rs.getString(2), rs.getInt(5));
				ae.setDate(toInstant(rs.getTimestamp(3)));
				ae.setApplication(rs.getString(4));
				ae.setRemoteHost(rs.getString(6));
				ae.setRemoteAddr(rs.getString(7));
				ae.setDescription(rs.getString(8));
				results.add(ae);
			}
		}
		
		return results;
	}
}