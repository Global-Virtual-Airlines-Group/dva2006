// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

/**
 * A Data Access Object to write and update Water Cooler image URLs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetCoolerLinks extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetCoolerLinks(Connection c) {
		super(c);
	}

	/**
	 * Writes Image URLs for a Message Thread.
	 * @param t the Message Thread bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(MessageThread t) throws DAOException {
		if (t.getImageURLs().isEmpty())
			return;
		
		try {
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_IMGURLS (ID, SEQ, URL, COMMENTS) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, t.getID());
			for (Iterator<LinkedImage> i = t.getImageURLs().iterator(); i.hasNext(); ) {
				LinkedImage img = i.next();
				_ps.setInt(2, img.getID());
				_ps.setString(3, img.getURL());
				_ps.setString(4, img.getDescription());
				_ps.addBatch();
			}

			// Execute the trasnaction
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an image URL associated with a particular Message Thread.
	 * @param id the Message Thread database ID 
	 * @param url the Image URL to delete
	 * @throws DAOException if a JDBC error occurs
	 * @see SetCoolerLinks#delete(int, int)
	 */
	public void delete(int id, String url) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM common.COOLER_IMGURLS WHERE (ID=?)");
		if (url != null)
			sqlBuf.append(" AND (URL=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, id);
			if (url != null)
				_ps.setString(2, url);
			
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an image URL associated with a particular Message Thread.
	 * @param id the Message Thread database ID
	 * @param seq the Image URL sequence ID
	 * @throws DAOException if a JDBC error occurs
	 * @see SetCoolerLinks#delete(int, String)
	 */
	public void delete(int id, int seq) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.COOLER_IMGURLS WHERE (ID=?) AND (SEQ=?)");
			_ps.setInt(1, id);
			_ps.setInt(2, seq);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}