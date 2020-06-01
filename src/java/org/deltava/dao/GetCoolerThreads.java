// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2013, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.cooler.*;

import org.deltava.comparators.ArbitraryComparator;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Water Cooler threads and thread notifications.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetCoolerThreads extends DAO {
	
	private static final Logger log = Logger.getLogger(GetCoolerThreads.class);
	private static final Cache<MessageThread> _tCache = CacheManager.get(MessageThread.class, "CoolerThreads"); 
	
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
		StringBuilder sqlBuf = new StringBuilder("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_IMGURLS I ON ((T.ID=I.ID) AND (I.SEQ=1)) "
			+ "LEFT JOIN common.COOLER_CHANNELINFO CI ON ((T.CHANNEL=CI.CHANNEL) AND (CI.INFOTYPE=?)) WHERE (CI.INFODATA=?) ");
		if (channelName != null)
			sqlBuf.append(" AND (T.CHANNEL=?)");
		if (!showImgs)
			sqlBuf.append(" HAVING (IMGID=0)");
		sqlBuf.append(" ORDER BY T.SORTDATE DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, Channel.InfoType.AIRLINE.ordinal());
			ps.setString(2, SystemData.get("airline.code"));
			if (channelName != null)
				ps.setString(3, channelName);
			
			return getByID(executeIDs(ps));
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
		try (PreparedStatement ps = prepare("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_IMGURLS I ON ((T.ID=I.ID) AND (I.SEQ=1)) "
			+ "LEFT JOIN common.COOLER_CHANNEL_INFO CI ON ((T.CHANNEL=CI.CHANNEL)) AND (CI.INFOTYPE=?)) WHERE (CI.INFODATA=?) HAVING (IMGID > 0) ORDER BY T.SORTDATE DESC")) {
			ps.setInt(1, Channel.InfoType.AIRLINE.ordinal());
			ps.setString(2, SystemData.get("airline.code"));
			return getByID(executeIDs(ps));
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
		StringBuilder sqlBuf = new StringBuilder("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_IMGURLS I ON (T.ID=I.ID) AND (I.SEQ=1) WHERE (T.AUTHOR=?)");
		if (!showImgs)
			sqlBuf.append(" HAVING (IMGID=0)");
		sqlBuf.append(" ORDER BY T.SORTDATE DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, userID);
			return getByID(executeIDs(ps));
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
		try (PreparedStatement ps = prepare("SELECT T.ID FROM common.COOLER_NOTIFY N, common.COOLER_THREADS T WHERE (N.USER_ID=?) AND (T.ID=N.THREAD_ID) ORDER BY T.SORTDATE DESC")) {
			ps.setInt(1, userID);
			return getByID(executeIDs(ps));
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
	public List<MessageThread> getSince(java.time.Instant sd, boolean showImgs) throws DAOException {
		if (sd == null)
			return getByChannel(null, showImgs);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.ID, IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_IMGURLS I ON ((T.ID=I.ID) AND (I.SEQ=1)) "
			+ "LEFT JOIN common.COOLER_CHANNELINFO CI ON ((CI.CHANNEL=T.CHANNEL) AND (C.INFOTYPE=?)) WHERE (CI.INFODATA=?) AND (T.SORTDATE > ?)");
		if (!showImgs)
			sqlBuf.append(" HAVING (IMGID=0)");
		sqlBuf.append(" ORDER BY T.SORTDATE DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, Channel.InfoType.AIRLINE.ordinal());
			ps.setString(2, SystemData.get("airline.code"));
			ps.setTimestamp(3, createTimestamp(sd));
			return getByID(executeIDs(ps));
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
		Integer ID = Integer.valueOf(id);
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

		// Fetch the thread posts
		try {
			try (PreparedStatement ps = prepareWithoutLimits("SELECT POST_ID, AUTHOR_ID, CREATED, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, MSGBODY, CONTENTWARN FROM common.COOLER_POSTS WHERE (THREAD_ID=?) ORDER BY CREATED")) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						Message msg = new Message(rs.getInt(2));
						msg.setThreadID(id);
						msg.setID(rs.getInt(1));
						msg.setCreatedOn(rs.getTimestamp(3).toInstant());
						msg.setRemoteAddr(rs.getString(4));
						msg.setRemoteHost(rs.getString(5));
						msg.setBody(rs.getString(6));
						msg.setContentWarning(rs.getBoolean(7));
						mt.addPost(msg);
					}
				}
			}

			// Fetch the thread updates
			try (PreparedStatement ps = prepareWithoutLimits("SELECT CREATED, AUTHOR, MESSAGE FROM common.COOLER_THREADHISTORY WHERE (ID=?)")) {	
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						ThreadUpdate upd = new ThreadUpdate(id);
						upd.setDate(rs.getTimestamp(1).toInstant());
						upd.setAuthorID(rs.getInt(2));
						upd.setDescription(rs.getString(3));
						mt.addUpdate(upd);
					}
				}
			}
			
			// Fetch the thread reports
			try (PreparedStatement ps = prepareWithoutLimits("SELECT AUTHOR_ID FROM common.COOLER_REPORTS WHERE (THREAD_ID=?)")) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						mt.addReportID(rs.getInt(1));
				}
			}

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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT USER_ID FROM common.COOLER_NOTIFY WHERE (THREAD_ID=?)")) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					result.addUser(rs.getInt(1));
			}

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
		StringBuilder buf = new StringBuilder("SELECT DISTINCT T.ID FROM common.COOLER_THREADS T, common.COOLER_POSTS P LEFT JOIN common.COOLER_CHANNELINFO CI ON ((CI.CHANNEL=T.CHANNEL) AND (CI.INFOTYPE=?))"
			+ " WHERE (T.ID=P.THREAD_ID) AND (CI.INFODATA=?) ");
		
		// Check for text / subject search
		boolean hasQuery = !StringUtils.isEmpty(criteria.getSearchTerm());
		if (hasQuery) {
			buf.append("AND ((MATCH(P.MSGBODY) AGAINST (? IN NATURAL LANGUAGE MODE)) ");
			if (criteria.getSearchSubject())
				buf.append("OR (LOCATE(?, T.SUBJECT) > 0)) ");
			else
				buf.append(") ");
		}

		// Add channel/author criteria
		if (!Channel.ALL.getName().equals(criteria.getChannel()))
			buf.append("AND (T.CHANNEL=?) ");
		if (criteria.getMinimumDate() != null)
			buf.append("AND (T.LASTUPDATE > ?)");
		if (!hasQuery)
			buf.append("ORDER BY T.LASTUPDATE DESC");
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			ps.setInt(1, Channel.InfoType.AIRLINE.ordinal());
			ps.setString(2, SystemData.get("airline.code"));
			int psOfs = 2;
			if (hasQuery) {
				ps.setString(++psOfs, criteria.getSearchTerm());
				if (criteria.getSearchSubject())
					ps.setString(++psOfs, criteria.getSearchTerm());
			}
			
			if (!Channel.ALL.getName().equals(criteria.getChannel()))
				ps.setString(++psOfs, criteria.getChannel());
			if (criteria.getMinimumDate() != null)
				ps.setTimestamp(++psOfs, createTimestamp(criteria.getMinimumDate()));
			
			return getByID(executeIDs(ps));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads a number of message threads based on their ID.
	 * @param IDs a Collection of database IDs
	 * @return a List of MessageThread beans
	 * @throws DAOException if a JDBC error occurs
	 */
	protected List<MessageThread> getByID(Collection<Integer> IDs) throws DAOException {
		if (CollectionUtils.isEmpty(IDs))
			return Collections.emptyList();
		
		// Build the comparator
		Comparator<DatabaseBean> cmp = new ArbitraryComparator(IDs);
		if (log.isDebugEnabled())
			log.debug("Raw set size = " + IDs.size());
		
		// Check the cache
		List<MessageThread> results = new ArrayList<MessageThread>(IDs.size());
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			MessageThread mt = _tCache.get(id);
			if (mt != null) {
				results.add(mt);
				i.remove();
			}
		}
		
		// Check if we got everything from the cache - if so, sort and return
		if (CollectionUtils.isEmpty(IDs)) {
			Collections.sort(results, cmp);
			return results;
		}
		
		// Build the SQL query
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT T.*, 0, IF(T.STICKY, IF(T.STICKY < NOW(), T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID), "
			+ "IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID) LEFT JOIN common.COOLER_IMGURLS I "
			+ "ON (T.ID=I.ID) AND (I.SEQ=1) WHERE T.ID IN (");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(") GROUP BY T.ID");
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			results.addAll(execute(ps));
			
			// Sort and return
			Collections.sort(results, cmp);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load result rows.
	 */
	private static List<MessageThread> execute(PreparedStatement ps) throws SQLException {
		List<MessageThread> results = new ArrayList<MessageThread>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasImgCount = (rs.getMetaData().getColumnCount() > 17);
			while (rs.next()) {
				MessageThread t = new MessageThread(rs.getString(2));
				t.setID(rs.getInt(1));
				t.setChannel(rs.getString(3));
				t.setImage(rs.getInt(hasImgCount ? 18 : 4));
				t.setStickyUntil(toInstant(rs.getTimestamp(5)));
				t.setHidden(rs.getBoolean(6));
				t.setLocked(rs.getBoolean(7));
				t.setStickyInChannelOnly(rs.getBoolean(8));
				t.setViews(rs.getInt(9));
				t.setPostCount(rs.getInt(10));
				t.setAuthorID(rs.getInt(11));
				t.setLastUpdatedOn(toInstant(rs.getTimestamp(12)));
				t.setLastUpdateID(rs.getInt(13));
				t.setReportCount(rs.getInt(15));
				t.setPoll(rs.getInt(17) > 0);

				// Clean out sticky if less than SD column
				if ((t.getStickyUntil() != null) && (t.getLastUpdatedOn().isAfter(t.getStickyUntil())))
					t.setStickyUntil(null);

				results.add(t);
				_tCache.add(t);
			}
		}

		return results;
	}
}