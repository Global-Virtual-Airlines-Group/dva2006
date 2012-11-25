// Copyright 2006, 2007, 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.cooler.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write and update Water Cooler image URLs.
 * @author Luke
 * @version 5.0
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
		if ((t == null) || (t.getImageURLs().isEmpty()))
			return;
		
		try {
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_IMGURLS (ID, SEQ, URL, COMMENTS) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, t.getID());
			for (LinkedImage img : t.getImageURLs()) {
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
		} finally {
			CacheManager.invalidate("CoolerThreads", t.cacheKey());
		}
	}
	
	/**
	 * Adds a Linked Image to an existing discussion thread.
	 * @param threadID the MessageThread database ID
	 * @param img the LinkedImage bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void add(int threadID, LinkedImage img) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_IMGURLS (ID, SEQ, URL, COMMENTS) VALUES (?, ?, ?, ?)");
			_ps.setInt(1, threadID);
			_ps.setInt(2, img.getID());
			_ps.setString(3, img.getURL());
			_ps.setString(4, img.getDescription());
			executeUpdate(1);
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(threadID));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an image URL associated with a particular Message Thread.
	 * @param threadID the Message Thread database ID 
	 * @param url the Image URL to delete
	 * @throws DAOException if a JDBC error occurs
	 * @see SetCoolerLinks#delete(int, int)
	 */
	public void delete(int threadID, String url) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM common.COOLER_IMGURLS WHERE (ID=?)");
		if (url != null)
			sqlBuf.append(" AND (URL=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, threadID);
			if (url != null)
				_ps.setString(2, url);
			
			executeUpdate(0);
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(threadID));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an image URL associated with a particular Message Thread.
	 * @param threadID the Message Thread database ID
	 * @param seq the Image URL sequence ID
	 * @throws DAOException if a JDBC error occurs
	 * @see SetCoolerLinks#delete(int, String)
	 */
	public void delete(int threadID, int seq) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.COOLER_IMGURLS WHERE (ID=?) AND (SEQ=?)");
			_ps.setInt(1, threadID);
			_ps.setInt(2, seq);
			executeUpdate(1);
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(threadID));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}