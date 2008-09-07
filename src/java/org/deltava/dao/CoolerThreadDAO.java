// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.cooler.MessageThread;

import org.deltava.comparators.ArbitraryComparator;

import org.deltava.util.CollectionUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to read/write Water Cooler threads.
 * @author luke
 * @version 2.1
 * @since 2.1
 */

public abstract class CoolerThreadDAO extends DAO implements CachingDAO {

	private static final Logger log = Logger.getLogger(CoolerThreadDAO.class);
	
	protected static final Cache<MessageThread> _tCache = new ExpiringCache<MessageThread>(320, 900); 

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected CoolerThreadDAO(Connection c) {
		super(c);
	}
	
	/**
	 * Removes a Message Thread from the cache.
	 * @param threadID the Message Thread database ID
	 */
	protected static void invalidate(int threadID) {
		_tCache.remove(new Integer(threadID));
	}
	
	/**
	 * Clears the cache.
	 */
	static void invalidateAll() {
		_tCache.clear();
	}

	public int getHits() {
		return _tCache.getHits();
	}

	public int getRequests() {
		return _tCache.getRequests();
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
		int size = IDs.size();
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
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT T.*, 0, IF(T.STICKY, IF(T.STICKY < NOW(), "
				+ "T.LASTUPDATE, T.STICKY), T.LASTUPDATE) AS SD, COUNT(O.OPT_ID), "
				+ "IFNULL(I.SEQ, T.IMAGE_ID) AS IMGID FROM common.COOLER_THREADS T "
				+ "LEFT JOIN common.COOLER_POLLS O ON (T.ID=O.ID) LEFT JOIN common.COOLER_IMGURLS I "
				+ "ON (T.ID=I.ID) AND (I.SEQ=1) WHERE T.ID IN (");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(") GROUP BY T.ID");
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			results.addAll(execute());
			
			// Sort and return
			Collections.sort(results, cmp);
			if (results.size() != size)
				log.warn("Raw = " + size + ", IDs = " + IDs.size() + ", threads = " + results.size());
				
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load thread IDs.
	 */
	protected List<Integer> executeIDs() throws SQLException {
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next())
			IDs.add(new Integer(rs.getInt(1)));
		
		// Clean up and return
		rs.close();
		_ps.close();
		return new ArrayList<Integer>(IDs);
	}
	
	/**
	 * Helper method to load result rows.
	 */
	private List<MessageThread> execute() throws SQLException {
		List<MessageThread> results = new ArrayList<MessageThread>();

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasImgCount = (rs.getMetaData().getColumnCount() > 16);
		while (rs.next()) {
			MessageThread t = new MessageThread(rs.getString(2));
			t.setID(rs.getInt(1));
			t.setChannel(rs.getString(3));
			t.setImage(rs.getInt(hasImgCount ? 17 : 4));
			t.setStickyUntil(rs.getTimestamp(5));
			t.setHidden(rs.getBoolean(6));
			t.setLocked(rs.getBoolean(7));
			t.setStickyInChannelOnly(rs.getBoolean(8));
			t.setViews(rs.getInt(9));
			t.setPostCount(rs.getInt(10));
			t.setAuthorID(rs.getInt(11));
			t.setLastUpdatedOn(rs.getTimestamp(12));
			t.setLastUpdateID(rs.getInt(13));
			t.setReportCount(rs.getInt(15));
			t.setPoll(rs.getInt(17) > 0);

			// Clean out sticky if less than SD column
			if ((t.getStickyUntil() != null) && (t.getLastUpdatedOn().after(t.getStickyUntil())))
				t.setStickyUntil(null);

			// Add to results
			results.add(t);
			_tCache.add(t);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}