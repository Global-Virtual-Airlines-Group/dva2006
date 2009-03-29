// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

/**
 * A Data Access Object to retrieve Water Cooler threads and thread notifications.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class GetCoolerThreads extends CoolerThreadDAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCoolerThreads(Connection c) {
		super(c);
	}

	/**
	 * Get all Water Cooler threads from a particular Channel.
	 * @param channelName the Channle name
	 * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
	 * @return a List of MessageThread beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> getByChannel(String channelName, boolean showImgs) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM "
				+ "common.COOLER_THREADS T LEFT JOIN common.COOLER_IMGURLS I ON (T.ID=I.ID) AND (I.SEQ=1)");
		if (channelName != null)
			sqlBuf.append(" WHERE (T.CHANNEL=?)");
		if (!showImgs)
			sqlBuf.append(" HAVING (IMGID=0)");
		sqlBuf.append(" ORDER BY T.SORTDATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			if (channelName != null)
				_ps.setString(1, channelName);
			
			return getByID(executeIDs());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Screen Shot message threads.
	 * @return a List of MessageThread beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> getScreenShots() throws DAOException {
		try {
			prepareStatement("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T LEFT JOIN "
				+ "common.COOLER_IMGURLS I ON (T.ID=I.ID) AND (I.SEQ=1) HAVING (IMGID > 0) ORDER BY T.SORTDATE DESC");
			return getByID(executeIDs());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Get all Water Cooler threads from a particular Author.
	 * @param userID the Author's database ID
	 * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
	 * @return a List of MessageThread beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> getByAuthor(int userID, boolean showImgs) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T "
				+ "LEFT JOIN common.COOLER_IMGURLS I ON (T.ID=I.ID) AND (I.SEQ=1) WHERE (T.AUTHOR=?)");
		if (!showImgs)
			sqlBuf.append(" HAVING (IMGID=0)");
		sqlBuf.append(" ORDER BY T.SORTDATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, userID);
			return getByID(executeIDs());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Get all Water Cooler threads where a particular user has signed up for notifications.
	 * @param userID the User's database ID
	 * @return a List of MessageThread beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> getByNotification(int userID) throws DAOException {
		try {
			prepareStatement("SELECT T.ID FROM common.COOLER_NOTIFY N, common.COOLER_THREADS T "
					+ "WHERE (N.USER_ID=?) AND (T.ID=N.THREAD_ID) ORDER BY T.SORTDATE DESC");
			_ps.setInt(1, userID);
			return getByID(executeIDs());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Water Cooler threads updated since a particular date.
	 * @param sd the date/time
	 * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
	 * @return a List of MessageThreads
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> getSince(java.util.Date sd, boolean showImgs) throws DAOException {
		if (sd == null)
			return getByChannel(null, showImgs);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM "
			+ "common.COOLER_THREADS T LEFT JOIN common.COOLER_IMGURLS I ON (T.ID=I.ID) AND (I.SEQ=1) "
			+ "WHERE (T.SORTDATE > ?)");
		if (showImgs)
			sqlBuf.append(" HAVING (IMGID=0)");
		sqlBuf.append(" ORDER BY T.SORTDATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setTimestamp(1, createTimestamp(sd));
			return getByID(executeIDs());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves a particular discussion thread with posts and warnings.
	 * @param id the thread ID
	 * @return a MessageThread bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @see GetCoolerThreads#getThread(int, boolean)
	 */
	public MessageThread getThread(int id) throws DAOException {
		return getThread(id, true);
	}

	/**
	 * Retrieves a particular discussion thread.
	 * @param id the thread ID
	 * @param loadPosts TRUE if posts/warnings should be loaded, otherwise FALSE
	 * @return a MessageThread bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public MessageThread getThread(int id, boolean loadPosts) throws DAOException {
		
		// Load from the cache
		Integer ID = new Integer(id);
		MessageThread mt = _tCache.get(ID);
		if (mt == null) {
			List<MessageThread> threads = getByID(Collections.singleton(ID));
			if (threads.isEmpty())
				return null;
			
			mt = threads.get(0);
		}
		
		// Return just the thread if asked
		if (!loadPosts || (!mt.getPosts().isEmpty()))
			return mt;

		try {
			// Fetch the thread posts
			prepareStatementWithoutLimits("SELECT POST_ID, AUTHOR_ID, CREATED, INET_NTOA(REMOTE_ADDR), "
					+ "REMOTE_HOST, MSGBODY, CONTENTWARN FROM common.COOLER_POSTS WHERE (THREAD_ID=?) "
					+ "ORDER BY CREATED");
			_ps.setInt(1, id);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Message msg = new Message(rs.getInt(2));
				msg.setThreadID(id);
				msg.setID(rs.getInt(1));
				msg.setCreatedOn(rs.getTimestamp(3));
				msg.setRemoteAddr(rs.getString(4));
				msg.setRemoteHost(rs.getString(5));
				msg.setBody(rs.getString(6));
				msg.setContentWarning(rs.getBoolean(7));
				mt.addPost(msg);
			}

			// Clean up
			rs.close();
			_ps.close();
			
			// Fetch the thread updates
			prepareStatementWithoutLimits("SELECT CREATED, AUTHOR, MESSAGE FROM common.COOLER_THREADHISTORY "
					+ "WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query
			rs = _ps.executeQuery();
			while (rs.next()) {
				ThreadUpdate upd = new ThreadUpdate(id);
				upd.setDate(rs.getTimestamp(1));
				upd.setAuthorID(rs.getInt(2));
				upd.setMessage(rs.getString(3));
				mt.addUpdate(upd);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Fetch the thread reports
			prepareStatementWithoutLimits("SELECT AUTHOR_ID FROM common.COOLER_REPORTS WHERE (THREAD_ID=?)");
			_ps.setInt(1, id);

			// Execute the query
			rs = _ps.executeQuery();
			while (rs.next())
				mt.addReportID(rs.getInt(1));

			// Clean up
			rs.close();
			_ps.close();
			return mt;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns thread notifications for a particular message thread.
	 * @param id the message thread database ID
	 * @return a ThreadNotifications bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public ThreadNotifications getNotifications(int id) throws DAOException {
		ThreadNotifications result = new ThreadNotifications(id);
		try {
			prepareStatementWithoutLimits("SELECT USER_ID FROM common.COOLER_NOTIFY WHERE (THREAD_ID=?)");
			_ps.setInt(1, id);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				result.addUser(rs.getInt(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}