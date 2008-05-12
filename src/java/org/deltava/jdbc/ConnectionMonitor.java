// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.util.system.SystemData;

/**
 * A daemon to monitor JDBC connections.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

class ConnectionMonitor implements java.io.Serializable, Runnable {

	private static transient final Logger log = Logger.getLogger(ConnectionMonitor.class);

	private static final Collection<String> _sqlStatus = Arrays.asList("08003", "08S01");

	private transient ConnectionPool _pool;
	private transient final Collection<ConnectionPoolEntry> _entries = new TreeSet<ConnectionPoolEntry>();
	private long _sleepTime;
	
	private long _poolCheckCount;
	private long _lastPoolCheck;

	/**
	 * Creates a new Connection Monitor.
	 * @param interval the sleep time <i>in seconds</i>
	 * @param pool the ConnectionPool to monitor
	 */
	ConnectionMonitor(int interval, ConnectionPool pool) {
		super();
		_pool = pool;
		_sleepTime = Math.min(10, interval) * 1000; // Convert seconds into ms
	}

	/**
	 * Returns the size of the connection pool being monitored.
	 * @return the size of the pool
	 */
	public int size() {
		return _entries.size();
	}
	
	/**
	 * Returns the number of times the connection pool has been validated.
	 * @return the number of validation runs
	 */
	public long getCheckCount() {
		return _poolCheckCount;
	}
	
	/**
	 * Returns the last time the connection pool was validated.
	 * @return the date/time of the last validation run, or null if never
	 */
	public java.util.Date getLastCheck() {
		return (_lastPoolCheck == 0) ? null : new java.util.Date(_lastPoolCheck);
	}

	/**
	 * Adds a JDBC connection to monitor.
	 * @param cpe a ConnectionPoolEntry object
	 */
	synchronized void addConnection(ConnectionPoolEntry cpe) {
		_entries.add(cpe);
	}

	/**
	 * Removes a JDBC connection from the monitor.
	 * @param cpe a ConnectionPoolEntry object
	 */
	synchronized void removeConnection(ConnectionPoolEntry cpe) {
		_entries.remove(cpe);
	}
	
	/**
	 * Alerts the thread to immediately check the connection pool. 
	 */
	synchronized void execute() {
		notify();
	}

	/**
	 * Manually check the connection pool.
	 */
	protected synchronized void checkPool() {
		_poolCheckCount++;
		_lastPoolCheck = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("Checking Connection Pool");

		// Loop through the entries
		Collection<ConnectionPoolEntry> entries = new ArrayList<ConnectionPoolEntry>(_entries);
		for (Iterator<ConnectionPoolEntry> i = entries.iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			boolean isStale = (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME);

			// Check if the entry has timed out
			if (!cpe.isActive()) {
				_entries.remove(cpe);
				if (log.isDebugEnabled())
					log.debug("Skipping inactive connection " + cpe);
			} else if (cpe.inUse() && isStale) {
				log.error("Releasing stale Connection " + cpe, cpe.getStackInfo());
				_pool.release(cpe.getConnection());
			} else if (cpe.isDynamic() && !cpe.inUse()) {
				if (isStale)
					log.warn("Releasing dynamic Connection " + cpe, cpe.getStackInfo());
				else if (log.isDebugEnabled())
					log.debug("Releasing dynamic Connection " + cpe);
				
				cpe.close();
				removeConnection(cpe);
			} else if (!cpe.inUse() && !cpe.checkConnection()) {
				log.warn("Reconnecting Connection " + cpe);
				cpe.close();

				try {
					cpe.connect();
				} catch (SQLException se) {
					if (_sqlStatus.contains(se.getSQLState()))
						log.warn("Transient SQL Error - " + se.getSQLState());
					else
						log.warn("Unknown SQL Error code - " + se.getSQLState());
				} catch (Exception e) {
					log.error("Error reconnecting " + cpe, e);
				}
			}
		}
	}

	/**
	 * Returns the thread name.
	 */
	public String toString() {
		return SystemData.get("airline.code") + " JDBC Connection Monitor";
	}

	/**
	 * Executes the Thread.
	 */
	public void run() {
		log.info("Starting");
		while (!Thread.currentThread().isInterrupted()) {
			checkPool();
			synchronized (this) {
				try {
					wait(_sleepTime);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		log.info("Stopping");
	}
}