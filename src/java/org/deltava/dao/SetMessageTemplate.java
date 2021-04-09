// Copyright 2005, 2012, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.*;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write e-mail Message Templates.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class SetMessageTemplate extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetMessageTemplate(Connection c) {
		super(c);
	}

	/**
	 * Writes a Message Template to the database. This can handle INSERTs and UPDATEs.
	 * @param mt the MessageTemplate object to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(MessageTemplate mt) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO MSG_TEMPLATES (NAME, SUBJECT, DESCRIPTION, PUSHCTX, BODY, ISHTML, TTL) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, mt.getName());
				ps.setString(2, mt.getSubject());
				ps.setString(3, mt.getDescription());
				ps.setString(4, mt.getNotifyContext());
				ps.setString(5, mt.getBody());
				ps.setBoolean(6, mt.getIsHTML());
				ps.setInt(7, mt.getNotificationTTL());
				executeUpdate(ps, 1);
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO MSG_TEMPLATE_ACTIONS (NAME, ACTION) VALUES (?, ?)")) {
				ps.setString(1, mt.getName());
				for (NotifyActionType nt : mt.getActionTypes()) {
					ps.setInt(2, nt.ordinal());
					ps.addBatch();
				}
				
				executeUpdate(ps, 1, mt.getActionTypes().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("MessageTemplate", mt.cacheKey());
		}
	}
	
	/**
	 * Deletes a Message Template from the database.
	 * @param name the template name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String name) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM MSG_TEMPLATES WHERE (NAME=?)")) {
			ps.setString(1, name);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("MessageTemplate", name);
		}
	}
}