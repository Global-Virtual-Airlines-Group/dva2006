// Copyright 2017, 2019, 2021, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read the audit log for an object.
 * @author Luke
 * @version 12.5
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
		if (!a.isCrossApp()) sqlBuf.append(" AND (APPNAME=?)");
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
	
	/**
	 * Returns all Admin Log entries.
	 * @return a SequencedCollection of AdminLogEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public SequencedCollection<AdminLogEntry> getAdminEntries() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT CREATED, NAME, ID, AUTHOR_ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST FROM ADMIN_LOG ORDER BY CREATED DESC")) {
			return executeAdmin(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Admin Log entries for a particular User.
	 * @param userID the User's database ID
	 * @return a SequencedCollection of AdminLogEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public SequencedCollection<AdminLogEntry> getAdminEntries(int userID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM ADMIN_LOG WHERE (AUTHOR_ID=?) ORDER BY CREATED DESC")) {
			ps.setInt(userID, userID);
			return executeAdmin(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns aggregated Admin Log statistics, by user.
	 * @param daysBack the number of days back to aggregate
	 * @return a Map of Integer counts, keyed by User ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Integer> getAdminStats(int daysBack) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT AUTHOR_ID, COUNT(*) FROM ADMIN_LOG WHERE (CREATED > DATE_SUB(NOW(), INTERVAL ? DAY)) GROUP BY AUTHOR_ID")) {
			ps.setInt(1, daysBack);
			
			Map<Integer, Integer> results = new HashMap<Integer, Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Audit Log result set parsing helper method.
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

	/*
	 * Admin Log result set parsing helper method.
	 */
	private static List<AdminLogEntry> executeAdmin(PreparedStatement ps) throws SQLException {
		List<AdminLogEntry> results = new ArrayList<AdminLogEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				AdminLogEntry ae = new AdminLogEntry(rs.getString(2), toInstant(rs.getTimestamp(1)), rs.getString(3));
				ae.setAuthorID(rs.getInt(4));
				ae.setRemoteAddress(rs.getString(5), rs.getString(6));
				results.add(ae);
			}
		}
		
		return results;
	}
}