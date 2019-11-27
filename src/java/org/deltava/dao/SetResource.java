// Copyright 2006, 2009, 2010, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.fleet.Resource;

/**
 * A Data Access Object to write Web Resources to a database.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetResource extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetResource(Connection c) {
		super(c);
	}

	/**
	 * Writes a Web Resource to the database.
	 * @param r the Resource bean
	 * @throws DAOException if a JDBC errror occurs
	 */
	public void write(Resource r) throws DAOException {
		try {
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.RESOURCES (ID, URL, TITLE, DOMAIN, REMARKS, CATEGORY, CREATEDON, AUTHOR, UPDATEDBY, HITCOUNT, ISPUBLIC) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, r.getID());
				ps.setString(2, r.getURL());
				ps.setString(3, r.getTitle());
				ps.setString(4, r.getDomain());
				ps.setString(5, r.getDescription());
				ps.setString(6, r.getCategory());
				ps.setTimestamp(7, createTimestamp(r.getCreatedOn()));
				ps.setInt(8, r.getAuthorID());
				ps.setInt(9, r.getLastUpdateID());
				ps.setInt(10, r.getHits());
				ps.setBoolean(11, r.getPublic());
				executeUpdate(ps, 1);
			}
			
			// Get new database ID
			if (r.getID() == 0)
				r.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs a link to a particular Web Resource.
	 * @param id the Resource database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void hit(int id) throws DAOException {
		try (PreparedStatement ps = prepare("UPDATE common.RESOURCES SET HITCOUNT=HITCOUNT+1 WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Web Resource from the database.
	 * @param id the Resource database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.RESOURCES WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}