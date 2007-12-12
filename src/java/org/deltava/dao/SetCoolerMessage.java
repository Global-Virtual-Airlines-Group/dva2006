// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

/**
 * A Data Access Object to handle writing Water Cooler message threads and posts.
 * @author Luke
 * @version 1.0
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
		try {
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_POSTS (THREAD_ID, AUTHOR_ID, CREATED, "
					+ "REMOTE_ADDR, REMOTE_HOST, MSGBODY, CONTENTWARN) VALUES (?, ?, ?, INET_ATON(?), ?, ?, ?)");
			_ps.setInt(1, msg.getThreadID());
			_ps.setInt(2, msg.getAuthorID());
			_ps.setTimestamp(3, createTimestamp(msg.getCreatedOn()));
			_ps.setString(4, msg.getRemoteAddr());
			_ps.setString(5, msg.getRemoteHost());
			_ps.setString(6, msg.getBody());
			_ps.setBoolean(7, msg.getContentWarning());

			// Update the database
			executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_POSTS SET MSGBODY=?, REMOTE_HOST=?, "
					+ "REMOTE_ADDR=INET_ATON(?), CONTENTWARN=? WHERE (THREAD_ID=?) AND (POST_ID=?)");
			_ps.setString(1, msg.getBody());
			_ps.setString(2, msg.getRemoteHost());
			_ps.setString(3, msg.getRemoteAddr());
			_ps.setBoolean(4, msg.getContentWarning());
			_ps.setInt(5, msg.getThreadID());
			_ps.setInt(6, msg.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Message Thread into the database
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

			prepareStatementWithoutLimits("INSERT INTO common.COOLER_THREADS (ID, SUBJECT, CHANNEL, IMAGE_ID, STICKY, "
					+ "STICKY_CHANNEL, POSTS, AUTHOR, LASTUPDATE, LASTPOSTER, VIEWS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)");
			_ps.setInt(1, t.getID());
			_ps.setString(2, t.getSubject());
			_ps.setString(3, t.getChannel());
			_ps.setInt(4, t.getImage());
			_ps.setTimestamp(5, createTimestamp(t.getStickyUntil()));
			_ps.setBoolean(6, t.getStickyInChannelOnly());
			_ps.setInt(7, t.getPostCount());
			_ps.setInt(8, msg.getAuthorID());
			_ps.setTimestamp(9, createTimestamp(msg.getCreatedOn()));
			_ps.setInt(10, msg.getAuthorID());

			// Write the thread to the database and get the new ID
			executeUpdate(1);
			t.setID(getNewID());
			
			// If we have poll options, write them to the database
			if (!t.getOptions().isEmpty()) {
				prepareStatementWithoutLimits("INSERT INTO common.COOLER_POLLS (ID, NAME) VALUES (?, ?)");
				_ps.setInt(1, t.getID());
				for (Iterator<PollOption> i = t.getOptions().iterator(); i.hasNext(); ) {
					PollOption opt = i.next();
					_ps.setString(2, opt.getName());
					_ps.addBatch();
				}
				
				_ps.executeBatch();
				_ps.close();
				_ps = null;
			}
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}

		// Save the message thread ID
		msg.setThreadID(t.getID());

		try {
			write(msg);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} catch (DAOException de) {
			rollbackTransaction();
			throw de;
		}
	}
	
	/**
	 * Writes a Discussion Thread update to the database.
	 * @param upd the ThreadUpdate bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(ThreadUpdate upd) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO common.COOLER_THREADHISTORY (ID, CREATED, AUTHOR, MESSAGE) "
					+ "VALUES (?, ?, ?, ?)");
			_ps.setInt(1, upd.getID());
			_ps.setTimestamp(2, createTimestamp(upd.getDate()));
			_ps.setInt(3, upd.getAuthorID());
			_ps.setString(4, upd.getMessage());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Marks a Message Thread has being viewed.
	 * @param id the Message Thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void viewThread(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_THREADS SET VIEWS=VIEWS+1 WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Changes the Channel for a Message Thread.
	 * @param id the Message Thread ID
	 * @param newChannel the new Channel name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setChannel(int id, String newChannel) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_THREADS SET CHANNEL=? WHERE (ID=?) LIMIT 1");
			_ps.setString(1, newChannel);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates the Subject of a Message Thread.
	 * @param id the Message Thread ID
	 * @param subj the new Subject
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateSubject(int id, String subj) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_THREADS SET SUBJECT=? WHERE (ID=?) LIMIT 1");
			_ps.setString(1, subj);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
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
			prepareStatementWithoutLimits("UPDATE common.COOLER_THREADS SET HIDDEN=?, LOCKED=? WHERE "
					+ "(ID=?) LIMIT 1");
			_ps.setBoolean(1, doHide);
			_ps.setBoolean(2, doLock);
			_ps.setInt(3, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Message.
	 * @param threadID the Message Thread database ID
	 * @param postID the Post database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int threadID, int postID) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.COOLER_POSTS WHERE (THREAD_ID=?) AND (POST_ID=?)");
			_ps.setInt(1, threadID);
			_ps.setInt(2, postID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Message Thread and all associated posts.
	 * @param id the Message Thread database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.COOLER_THREADS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
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
			prepareStatementWithoutLimits("SELECT COUNT(DISTINCT P.POST_ID), (SELECT P.AUTHOR_ID FROM common.COOLER_POSTS P "
					+ "WHERE (P.THREAD_ID=T.ID) ORDER BY P.CREATED ASC LIMIT 1) AS AID, (SELECT P.AUTHOR_ID FROM "
					+ "common.COOLER_POSTS P WHERE (P.THREAD_ID=T.ID) ORDER BY P.CREATED DESC LIMIT 1) AS LUID, "
					+ "MAX(P.CREATED) FROM common.COOLER_THREADS T LEFT JOIN common.COOLER_POSTS P ON (P.THREAD_ID=T.ID) "
					+ "WHERE (T.ID=?) GROUP BY T.ID LIMIT 1");
			_ps.setInt(1, mt.getID());

			// Get the thread Author ID
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				throw new DAOException("Message Thread is empty");
			}

			// Save the post count
			if (mt.getPosts().isEmpty())
				mt.setPostCount(rs.getInt(1));

			// Update the author/last update and clean up
			mt.setAuthorID(rs.getInt(2));
			mt.setLastUpdateID(rs.getInt(3));
			mt.setLastUpdatedOn(rs.getTimestamp(4));
			rs.close();
			_ps.close();

			// Update the thread entry
			prepareStatement("UPDATE common.COOLER_THREADS SET POSTS=?, AUTHOR=?, LASTPOSTER=?, LASTUPDATE=? WHERE (ID=?)");
			_ps.setInt(1, mt.getPostCount());
			_ps.setInt(2, mt.getAuthorID());
			_ps.setInt(3, mt.getLastUpdateID());
			_ps.setTimestamp(4, createTimestamp(mt.getLastUpdatedOn()));
			_ps.setInt(5, mt.getID());
			executeUpdate(1);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Unsticks a Water Cooler Message Thread.
	 * @param id the Message Thread's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void unstickThread(int id) throws DAOException {
		try {
			prepareStatement("UPDATE common.COOLER_THREADS SET STICKY=NULL, STICKY_CHANNEL=? WHERE (ID=?)");
			_ps.setBoolean(1, false);
			_ps.setInt(2, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates the sticky date on a Water Cooler Message Thread. This will unstick the thread if the date
	 * is null or in the past.
	 * @param id the Message Thread's database ID
	 * @param sDate the new sticky date
	 * @throws DAOException if a JDBC error occurs
	 * @see SetCoolerMessage#unstickThread(int)
	 */
	public void restickThread(int id, java.util.Date sDate) throws DAOException {
		if ((sDate == null) || (sDate.before(new java.util.Date())))
			unstickThread(id);
		
		try {
			prepareStatement("UPDATE common.COOLER_THREADS SET STICKY=? WHERE (ID=?)");
			_ps.setTimestamp(1, createTimestamp(sDate));
			_ps.setInt(2, id);
			executeUpdate(0);
		} catch(SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a content warning report for a particular Water Cooler discussion thread.
	 * @param mt the MessageThread bean
	 * @param id the user's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void report(MessageThread mt, int id) throws DAOException {
		try {
			prepareStatement("REPLACE INTO common.COOLER_REPORTS (THREAD_ID, AUTHOR_ID) VALUES (?, ?)");
			_ps.setInt(1, mt.getID());
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a vote in a Water Cooler discussion poll.
	 * @param vote the PollVote bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void vote(PollVote vote) throws DAOException {
		try {
			prepareStatement("REPLACE INTO common.COOLER_VOTES (ID, PILOT_ID, OPT_ID) VALUES (?, ?, ?)");
			_ps.setInt(1, vote.getID());
			_ps.setInt(2, vote.getPilotID());
			_ps.setInt(3, vote.getOptionID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Clears content warnings from all posts in a Water Cooler discussion thread.
	 * @param threadID the thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearWarning(int threadID) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.COOLER_POSTS SET CONTENTWARN=? WHERE (THREAD_ID=?)");
			_ps.setBoolean(1, false);
			_ps.setInt(2, threadID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Clears content reports from a Water Cooler discussion thread.
	 * @param threadID the thread ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearReport(int threadID) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.COOLER_REPORTS WHERE (THREAD_ID=?)");
			_ps.setInt(1, threadID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}