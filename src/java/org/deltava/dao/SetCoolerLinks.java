// Copyright 2006, 2007, 2008, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.cooler.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write and update Water Cooler image URLs.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class SetCoolerLinks extends DAO {
	
	private static final String CACHE_ID = "CoolerThreads";

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
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_IMGURLS (ID, SEQ, DISABLED, URL, COMMENTS) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, t.getID());
			_ps.setBoolean(3, false);
			for (LinkedImage img : t.getImageURLs()) {
				_ps.setInt(2, img.getID());
				_ps.setString(4, img.getURL());
				_ps.setString(5, img.getDescription());
				_ps.addBatch();
			}

			executeBatchUpdate(1, t.getImageURLs().size());
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate(CACHE_ID, t.cacheKey());
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
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_IMGURLS (ID, SEQ, DISABLED, URL, COMMENTS) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, threadID);
			_ps.setInt(2, img.getID());
			_ps.setBoolean(3,  false);
			_ps.setString(4, img.getURL());
			_ps.setString(5, img.getDescription());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate(CACHE_ID, Integer.valueOf(threadID));
		}
	}
	
	/**
	 * Restores disabled Linked Images within an existing discussion thread.
	 * @param threadID the MessageThread database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void restore(int threadID) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_IMGURLS SET DISABLED=? WHERE (ID=?) AND (DISABLED=?)");
			_ps.setBoolean(1, false);
			_ps.setInt(2, threadID);
			_ps.setBoolean(3, true);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an image URL associated with a particular Message Thread.
	 * @param threadID the MessageThread database ID
	 * @param seq the Image URL sequence ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int threadID, int seq) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.COOLER_IMGURLS WHERE (ID=?) AND (SEQ=?)");
			_ps.setInt(1, threadID);
			_ps.setInt(2, seq);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate(CACHE_ID, Integer.valueOf(threadID));
		}
	}

	/***
	 * Disables an image URL associated with a particular Message Thread.
	 * @param threadID the MessageThread database ID
	 * @param seq the Image URL sequence ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void disable(int threadID, int seq) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_IMGURLS SET DISABLED=? WHERE (ID=?) AND (SEQ=?)");
			_ps.setBoolean(1, true);
			_ps.setInt(2, threadID);
			_ps.setInt(3, seq);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate(CACHE_ID, Integer.valueOf(threadID));
		}
	}	
}