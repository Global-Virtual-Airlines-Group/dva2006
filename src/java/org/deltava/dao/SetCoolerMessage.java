// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2013, 2014, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.Instant;

import org.deltava.beans.cooler.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to handle writing Water Cooler message threads and posts.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetCoolerMessage extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetCoolerMessage(Connection c) {
		super(c);
	}
	
	/**
	 * Writes a new Post to the Water Cooler.
	 * @param msg the Message post bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Message msg) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_POSTS (THREAD_ID, AUTHOR_ID, CREATED, REMOTE_ADDR, REMOTE_HOST, MSGBODY, CONTENTWARN) VALUES (?, ?, NOW(),"
					+ " INET6_ATON(?), ?, ?, ?)")) {
			ps.setInt(1, msg.getThreadID());
			ps.setInt(2, msg.getAuthorID());
			ps.setString(3, msg.getRemoteAddr());
			ps.setString(4, msg.getRemoteHost());
			ps.setString(5, msg.getBody());
			ps.setBoolean(6, msg.getContentWarning());
			executeUpdate(ps, 1);
			msg.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an existing Post in the Water Cooler.
	 * @param msg the Message post bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Message msg) throws DAOException {
		try (PreparedStatement ps = prepare("UPDATE common.COOLER_POSTS SET MSGBODY=?, REMOTE_HOST=?, REMOTE_ADDR=INET6_ATON(?), CONTENTWARN=? WHERE (THREAD_ID=?) AND (POST_ID=?)")) {
			ps.setString(1, msg.getBody());
			ps.setString(2, msg.getRemoteHost());
			ps.setString(3, msg.getRemoteAddr());
			ps.setBoolean(4, msg.getContentWarning());
			ps.setInt(5, msg.getThreadID());
			ps.setInt(6, msg.getID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Message Thread into the database.
	 * @param t the Message Thread
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalStateException if there are no Posts in the Thread
	 */
	public void write(MessageThread t) throws DAOException {

		// Get the first post in the thread
		Message msg = t.getPosts().get(0);
		if (msg == null)
			throw new IllegalStateException("Empty Message Thread");

		try {
			// Do the two INSERTs as a single transaction
			startTransaction();

			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_THREADS (ID, SUBJECT, CHANNEL, IMAGE_ID, STICKY, STICKY_CHANNEL, POSTS, AUTHOR, LASTUPDATE, "
				+ "LASTPOSTER, VIEWS, SORTDATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, IFNULL(?, ?))")) {
				ps.setInt(1, t.getID());
				ps.setString(2, t.getSubject());
				ps.setString(3, t.getChannel());
				ps.setInt(4, t.getImage());
				ps.setTimestamp(5, createTimestamp(t.getStickyUntil()));
				ps.setBoolean(6, t.getStickyInChannelOnly());
				ps.setInt(7, t.getPostCount());
				ps.setInt(8, msg.getAuthorID());
				ps.setTimestamp(9, createTimestamp(msg.getCreatedOn()));
				ps.setInt(10, msg.getAuthorID());
				ps.setTimestamp(11, createTimestamp(t.getStickyUntil()));
				ps.setTimestamp(12, createTimestamp(msg.getCreatedOn()));
				executeUpdate(ps, 1);
			}
			
			// Get the new thread ID
			t.setID(getNewID());
			msg.setThreadID(t.getID());
			
			// Mark thread as read by user
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_LASTREAD (ID, AUTHOR_ID, LASTREAD) VALUES (?, ?, NOW())")) {
				ps.setInt(1, t.getID());
				ps.setInt(2, t.getAuthorID());
				executeUpdate(ps, 1);
			}
			
			// If we have poll options, write them to the database
			if (!t.getOptions().isEmpty()) {
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_POLLS (ID, OPT_ID, NAME) VALUES (?, ?, ?)")) {
					ps.setInt(1, t.getID()); int optID = 0;
					for (PollOption opt : t.getOptions()) {
						ps.setInt(2, ++optID);
						ps.setString(3, opt.getName());
						ps.addBatch();
					}
				
					executeUpdate(ps, 1, t.getOptions().size());
				}
			}
			
			write(msg);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerChannels", t.getChannel());
		}
	}
	
	/**
	 * Writes a Discussion Thread update to the database.
	 * @param upd the ThreadUpdate bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(ThreadUpdate upd) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.COOLER_THREADHISTORY (ID, CREATED, AUTHOR, MESSAGE) VALUES (?, ?, ?, ?)")) {
			ps.setInt(1, upd.getID());
			ps.setTimestamp(2, createTimestamp(upd.getDate()));
			ps.setInt(3, upd.getAuthorID());
			ps.setString(4, upd.getDescription());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(upd.getID()));
		}
	}

	/**
	 * Marks a Message Thread as being read by a particular user.
	 * @param threadID the Message Thread ID
	 * @param userID the User dataase ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void markRead(int threadID, int userID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.COOLER_LASTREAD VALUES (?, ?, NOW())")) {
			ps.setInt(1, threadID);
			ps.setInt(2, userID);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Marks a Message Thread as being viewed.
	 * @param id the Message Thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void viewThread(int id) throws DAOException {
		try {
			startTransaction();
			lockThreadRow(id);
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.COOLER_THREADS SET STICKY=IF(STICKY < NOW(), NULL, STICKY), "
					+ "VIEWS=VIEWS+1, SORTDATE=IFNULL(STICKY, LASTUPDATE) WHERE (ID=?) LIMIT 1")) {
				ps.setInt(1, id);
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Changes the Channel for a Message Thread.
	 * @param threadID the Message Thread ID
	 * @param newChannel the new Channel name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setChannel(int threadID, String newChannel) throws DAOException {
		try {
			startTransaction();
			lockThreadRow(threadID);
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.COOLER_THREADS SET CHANNEL=? WHERE (ID=?) LIMIT 1")) {
				ps.setString(1, newChannel);
				ps.setInt(2, threadID);
				executeUpdate(ps, 1);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(threadID));
			CacheManager.invalidate("CoolerChannels", newChannel);
		}
	}
	
	/**
	 * Updates the Subject of a Message Thread.
	 * @param threadID the Message Thread ID
	 * @param subj the new Subject
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateSubject(int threadID, String subj) throws DAOException {
		try {
			startTransaction();
			lockThreadRow(threadID);
			try (PreparedStatement ps = 	prepareWithoutLimits("UPDATE common.COOLER_THREADS SET SUBJECT=? WHERE (ID=?) LIMIT 1")) {
				ps.setString(1, subj);
				ps.setInt(2, threadID);
				executeUpdate(ps, 1);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(threadID));
		}
	}

	/**
	 * Moderates a Thread by updating the locked and hidden flags.
	 * @param id the thread ID
	 * @param doHide TRUE if the thread should be hidden, otherwise FALSE
	 * @param doLock TRUE if the thread should be locked, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void moderateThread(int id, boolean doHide, boolean doLock) throws DAOException {
		try {
			startTransaction();
			lockThreadRow(id);
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.COOLER_THREADS SET HIDDEN=?, LOCKED=? WHERE (ID=?) LIMIT 1")) {
				ps.setBoolean(1, doHide);
				ps.setBoolean(2, doLock);
				ps.setInt(3, id);
				executeUpdate(ps, 1);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}
	
	/**
	 * Deletes a Message.
	 * @param threadID the Message Thread database ID
	 * @param postID the Post database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int threadID, int postID) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.COOLER_POSTS WHERE (THREAD_ID=?) AND (POST_ID=?)")) {
			ps.setInt(1, threadID);
			ps.setInt(2, postID);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(threadID));
		}
	}

	/**
	 * Deletes a Message Thread and all associated posts.
	 * @param id the Message Thread database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.COOLER_THREADS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}

	/**
	 * Recalculates Thread information by querying existing Thread Posts.
	 * @param mt the Message Thread
	 * @throws DAOException if a JDBC error occurs
	 */
	public void synchThread(MessageThread mt) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(DISTINCT P.POST_ID), (SELECT P.AUTHOR_ID FROM common.COOLER_POSTS P WHERE (P.THREAD_ID=T.ID) ORDER BY P.CREATED ASC LIMIT 1) AS AID, "
				+ "(SELECT P.AUTHOR_ID FROM common.COOLER_POSTS P WHERE (P.THREAD_ID=T.ID) ORDER BY P.CREATED DESC LIMIT 1) AS LUID, MAX(P.CREATED) FROM common.COOLER_THREADS T LEFT JOIN "
				+ "common.COOLER_POSTS P ON (P.THREAD_ID=T.ID) WHERE (T.ID=?) GROUP BY T.ID LIMIT 1")) {
				ps.setInt(1, mt.getID());

				// Get the thread Author ID
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						mt.setAuthorID(rs.getInt(2));
						mt.setLastUpdateID(rs.getInt(3));
						mt.setLastUpdatedOn(toInstant(rs.getTimestamp(4)));
						if (mt.getPosts().isEmpty())
							mt.setPostCount(rs.getInt(1));
					}
				}
			}

			// Clear out the sticky date
			if ((mt.getStickyUntil() != null) && (mt.getStickyUntil().toEpochMilli() < System.currentTimeMillis()))
				mt.setStickyUntil(null);
			
			// Lock the thread
			lockThreadRow(mt.getID());

			// Update the thread entry
			try (PreparedStatement ps = prepare("UPDATE common.COOLER_THREADS SET POSTS=?, AUTHOR=?, LASTPOSTER=?, LASTUPDATE=?, STICKY=? WHERE (ID=?)")) {
				ps.setInt(1, mt.getPostCount());
				ps.setInt(2, mt.getAuthorID());
				ps.setInt(3, mt.getLastUpdateID());
				ps.setTimestamp(4, createTimestamp(mt.getLastUpdatedOn()));
				ps.setTimestamp(5, createTimestamp(mt.getStickyUntil()));
				ps.setInt(6, mt.getID());
				executeUpdate(ps, 1);
			}
			
			// Update the sort date
			try (PreparedStatement ps = prepare("UPDATE common.COOLER_THREADS SET SORTDATE=IFNULL(STICKY, LASTUPDATE) WHERE (ID=?)")) {
				ps.setInt(1, mt.getID());
				executeUpdate(ps, 1);
			}

			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", mt.cacheKey());
		}
	}

	/**
	 * Unsticks a Water Cooler Message Thread.
	 * @param id the Message Thread's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void unstickThread(int id) throws DAOException {
		try {
			startTransaction();
			lockThreadRow(id);
			try (PreparedStatement ps = prepare("UPDATE common.COOLER_THREADS SET STICKY=NULL, STICKY_CHANNEL=?, SORTDATE=LASTUPDATE WHERE (ID=?)")) {
				ps.setBoolean(1, false);
				ps.setInt(2, id);
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}

	/**
	 * Updates the sticky date on a Water Cooler Message Thread. This will unstick the thread if the date
	 * is null or in the past.
	 * @param id the Message Thread's database ID
	 * @param sDate the new sticky date
	 * @param stickyChannel TRUE if sticky in channel only, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 * @see SetCoolerMessage#unstickThread(int)
	 */
	public void restickThread(int id, Instant sDate, boolean stickyChannel) throws DAOException {
		if ((sDate == null) || (sDate.isBefore(Instant.now()))) {
			unstickThread(id);
			return;
		}
		
		try {
			startTransaction();
			lockThreadRow(id);
			try (PreparedStatement ps = prepare("UPDATE common.COOLER_THREADS SET STICKY=?, STICKY_CHANNEL=?, SORTDATE=? WHERE (ID=?)")) {
				ps.setTimestamp(1, createTimestamp(sDate));
				ps.setBoolean(2, stickyChannel);
				ps.setTimestamp(3, createTimestamp(sDate));
				ps.setInt(4, id);
				executeUpdate(ps, 0);
			}
			
			commitTransaction();
		} catch(SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}
	
	/**
	 * Writes a content warning report for a particular Water Cooler discussion thread.
	 * @param mt the MessageThread bean
	 * @param id the user's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void report(MessageThread mt, int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.COOLER_REPORTS (THREAD_ID, AUTHOR_ID) VALUES (?, ?)")) {
			ps.setInt(1, mt.getID());
			ps.setInt(2, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}
	
	/**
	 * Writes a vote in a Water Cooler discussion poll.
	 * @param vote the PollVote bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void vote(PollVote vote) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.COOLER_VOTES (ID, PILOT_ID, OPT_ID) VALUES (?, ?, ?)")) {
			ps.setInt(1, vote.getID());
			ps.setInt(2, vote.getPilotID());
			ps.setInt(3, vote.getOptionID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(vote.getID()));
		}
	}
	
	/**
	 * Clears content warnings from all posts in a Water Cooler discussion thread.
	 * @param id the thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearWarning(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.COOLER_POSTS SET CONTENTWARN=? WHERE (THREAD_ID=?)")) {
			ps.setBoolean(1, false);
			ps.setInt(2, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}
	
	/**
	 * Clears content reports from a Water Cooler discussion thread.
	 * @param id the thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearReport(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.COOLER_REPORTS WHERE (THREAD_ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("CoolerThreads", Integer.valueOf(id));
		}
	}
	
	/*
	 * Helper method to lock a thread row.
	 */
	private void lockThreadRow(int id) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT VIEWS FROM common.COOLER_THREADS WHERE (ID=?) LIMIT 1 FOR UPDATE")) {
			ps.setInt(1, id);
			ps.execute();
		}
	}
}