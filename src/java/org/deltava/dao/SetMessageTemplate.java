// Copyright 2005, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.MessageTemplate;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write e-mail Message Templates.
 * @author Luke
 * @version 5.0
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
			prepareStatement("REPLACE INTO MSG_TEMPLATES (NAME, SUBJECT, DESCRIPTION, BODY, ISHTML) VALUES (?, ?, ?, ?, ?)");
			_ps.setString(1, mt.getName());
			_ps.setString(2, mt.getSubject());
			_ps.setString(3, mt.getDescription());
			_ps.setString(4, mt.getBody());
			_ps.setBoolean(5, mt.getIsHTML());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("MsgTemplates", mt.cacheKey());
		}
	}
	
	/**
	 * Deletes a Message Template from the database.
	 * @param name the template name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String name) throws DAOException {
		try {
			prepareStatement("DELETE FROM MSG_TEMPLATES WHERE (NAME=?)");
			_ps.setString(1, name);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("MsgTemplates", name);
		}
	}
}