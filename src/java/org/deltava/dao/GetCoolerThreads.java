// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

import org.deltava.util.*;

/**
 * A Data Access Object to retrieve Water Cooler threads and thread notifications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetCoolerThreads extends DAO {

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
		StringBuilder sqlBuf = new StringBuilder(
				"SELECT T.*, IF(T.STICKY, IF(DATE_ADD(T.STICKY, INTERVAL 12 HOUR) < NOW(), "
						+ "T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID) FROM common.COOLER_THREADS T "
						+ "LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID) WHERE (T.CHANNEL=?)");
		if (!showImgs)
			sqlBuf.append(" AND (T.IMAGE_ID=0)");
		sqlBuf.append(" GROUP BY T.ID ORDER BY SD DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, channelName);
			return execute();
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
		StringBuilder sqlBuf = new StringBuilder(
				"SELECT T.*, IF(T.STICKY, IF(DATE_ADD(T.STICKY, INTERVAL 12 HOUR) < NOW(), "
						+ "T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID) FROM common.COOLER_THREADS T "
						+ "LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID) WHERE (T.AUTHOR=?)");
		if (!showImgs)
			sqlBuf.append(" AND (T.IMAGE_ID=0)");
		sqlBuf.append(" GROUP BY T.ID ORDER BY SD DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, userID);
			return execute();
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
			prepareStatement("SELECT T.*, IF(T.STICKY, IF(DATE_ADD(T.STICKY, INTERVAL 12 HOUR) < NOW(), T.LASTUPDATE, "
					+ "T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID) FROM common.COOLER_THREADS T, "
					+ "common.COOLER_NOTIFY N LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID) WHERE (N.USER_ID=?) "
					+ "AND (T.ID=N.THREAD_ID) GROUP BY T.ID ORDER BY SD DESC");
			_ps.setInt(1, userID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Get all Water Cooler threads.
	 * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
	 * @return a List of MessageThreads
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> getAll(boolean showImgs) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(
				"SELECT T.*, IF(T.STICKY, IF(DATE_ADD(T.STICKY, INTERVAL 12 HOUR) < NOW(), "
						+ "T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID) FROM common.COOLER_THREADS T "
						+ "LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID)");
		if (!showImgs)
			sqlBuf.append(" WHERE (T.IMAGE_ID=0)");

		sqlBuf.append(" GROUP BY T.ID ORDER BY SD DESC");

		try {
			prepareStatement(sqlBuf.toString());
			return execute();
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
			return getAll(showImgs);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(
				"SELECT T.*, IF(T.STICKY, IF(DATE_ADD(T.STICKY, INTERVAL 12 HOUR) < NOW(), "
						+ "T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID) FROM common.COOLER_THREADS T "
						+ "LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID)");
		if (!showImgs)
			sqlBuf.append("WHERE (T.IMAGE_ID=0) ");

		sqlBuf.append("HAVING (SD > ?) GROUP BY T.ID ORDER BY SD DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setTimestamp(1, createTimestamp(sd));
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves a particular discussion thread.
	 * @param id the thread ID
	 * @return a MessageThread
	 * @throws DAOException if a JDBC error occurs
	 */
	public MessageThread getThread(int id) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.COOLER_THREADS WHERE (ID=?)");
			_ps.setInt(1, id);

			// Execute the query - if id not found return null
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				return null;
			}

			// Populate the thread data
			MessageThread t = new MessageThread(rs.getString(2));
			t.setID(id);
			t.setChannel(rs.getString(3));
			t.setImage(rs.getInt(4));
			t.setStickyUntil(rs.getTimestamp(5));
			t.setHidden(rs.getBoolean(6));
			t.setLocked(rs.getBoolean(7));
			t.setStickyInChannelOnly(rs.getBoolean(8));
			t.setViews(rs.getInt(9));

			// Clean up
			rs.close();
			_ps.close();

			// Fetch the thread posts
			prepareStatementWithoutLimits("SELECT THREAD_ID, POST_ID, AUTHOR_ID, CREATED, INET_NTOA(REMOTE_ADDR), "
					+ "REMOTE_HOST, MSGBODY FROM common.COOLER_POSTS WHERE (THREAD_ID=?) ORDER BY CREATED");
			_ps.setInt(1, id);

			// Execute the query
			rs = _ps.executeQuery();
			while (rs.next()) {
				Message msg = new Message(rs.getInt(3));
				msg.setThreadID(id);
				msg.setID(rs.getInt(2));
				msg.setCreatedOn(rs.getTimestamp(4));
				msg.setRemoteAddr(rs.getString(5));
				msg.setRemoteHost(rs.getString(6));
				msg.setBody(rs.getString(7));
				t.addPost(msg);
			}

			// Clean up
			rs.close();
			_ps.close();
			return t;
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

		// Build the result bean
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

	/**
	 * Returns all Message Threads matching particular search criteria.
	 * @param criteria the search criteria
	 * @return a List of MessageThreads
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageThread> search(SearchCriteria criteria) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT T.*, IF(T.STICKY, IF(DATE_ADD(T.STICKY, "
				+ "INTERVAL 12 HOUR) < NOW(), T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID) "
				+ "FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_POSTS P ON (T.ID=P.THREAD_ID) "
				+ "LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID) WHERE (P.MSGBODY LIKE ?) ");
		if (!Channel.ALL.equals(criteria.getChannel()))
			sqlBuf.append("AND (T.CHANNEL=?) ");
		if (criteria.getSearchSubject())
			sqlBuf.append("AND (T.SUBJECT LIKE ?) ");
		if (!CollectionUtils.isEmpty(criteria.getIDs())) {
			sqlBuf.append("AND (T.AUTHOR IN (");
			sqlBuf.append(StringUtils.listConcat(criteria.getIDs(), ","));
			sqlBuf.append(")) ");
		}

		sqlBuf.append(" GROUP BY T.ID ORDER BY SD DESC");

		try {
			prepareStatement(sqlBuf.toString());
			int psOfs = 0;
			_ps.setQueryTimeout(25);
			_ps.setString(++psOfs, "%" + criteria.getSearchTerm() + "%");
			if (!Channel.ALL.equals(criteria.getChannel()))
				_ps.setString(++psOfs, criteria.getChannel());
			if (criteria.getSearchSubject())
				_ps.setString(++psOfs, "%" + criteria.getSearchTerm() + "%");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to load result rows.
	 */
	private List<MessageThread> execute() throws SQLException {
		List<MessageThread> results = new ArrayList<MessageThread>();

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			MessageThread t = new MessageThread(rs.getString(2));
			t.setID(rs.getInt(1));
			t.setChannel(rs.getString(3));
			t.setImage(rs.getInt(4));
			t.setStickyUntil(expandDate(rs.getDate(5)));
			t.setHidden(rs.getBoolean(6));
			t.setLocked(rs.getBoolean(7));
			t.setStickyInChannelOnly(rs.getBoolean(8));
			t.setViews(rs.getInt(9));
			t.setPostCount(rs.getInt(10));
			t.setAuthorID(rs.getInt(11));
			t.setLastUpdatedOn(rs.getTimestamp(12));
			t.setLastUpdateID(rs.getInt(13));
			t.setPoll(rs.getInt(15) > 0);

			// Clean out sticky if less than SD column
			java.util.Date sd = rs.getTimestamp(14);
			if ((t.getStickyUntil() != null) && (sd.after(t.getStickyUntil())))
				t.setStickyUntil(null);

			// Add to results
			results.add(t);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}