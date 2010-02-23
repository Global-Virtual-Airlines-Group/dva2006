// Copyright 2006, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.fleet.Resource;

/**
 * A Data Access Object to write Web Resources to a database.
 * @author Luke
 * @version 3.0
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
			prepareStatement("REPLACE INTO common.RESOURCES (ID, URL, TITLE, DOMAIN, REMARKS, CATEGORY, "
					+ "CREATEDON, AUTHOR, UPDATEDBY, HITCOUNT, ISPUBLIC) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, r.getID());
			_ps.setString(2, r.getURL());
			_ps.setString(3, r.getTitle());
			_ps.setString(4, r.getDomain());
			_ps.setString(5, r.getDescription());
			_ps.setString(6, r.getCategory());
			_ps.setTimestamp(7, createTimestamp(r.getCreatedOn()));
			_ps.setInt(8, r.getAuthorID());
			_ps.setInt(9, r.getLastUpdateID());
			_ps.setInt(10, r.getHits());
			_ps.setBoolean(11, r.getPublic());
			executeUpdate(1);
			
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
		try {
			prepareStatement("UPDATE common.RESOURCES SET HITCOUNT=HITCOUNT+1 WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
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
		try {
			prepareStatement("DELETE FROM common.RESOURCES WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}