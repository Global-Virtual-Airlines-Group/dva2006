package org.deltava.jdbc;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

/**
 * A Thread to monitor JDBC connections.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class ConnectionMonitor extends Thread {

	private static final Logger log = Logger.getLogger(ConnectionMonitor.class);
	
	private static List _sqlStatus = Arrays.asList(new String[] {"08003", "08S01"});

	private Collection _pool = Collections.EMPTY_LIST;
	private long _sleepTime = 180000; // 3 minute default
	private long _poolCheckCount;

	/**
	 * Creates a new Connection Monitor. The thread is set as a daemon.
	 * @see Thread#setDaemon(boolean)
	 */
	ConnectionMonitor() {
		super("JDBC Pool Monitor");
		setDaemon(true);
	}

	/**
	 * Creates a new Connection Monitor to monitor specific ConnectionPoolEntries.
	 * @param cPool a List of ConnectionPoolEntries
	 * @see ConnectionMonitor#setPool(List)
	 */
	ConnectionMonitor(Collection cPool) {
		this();
		setPool(cPool);
	}

	/**
	 * Returns the size of the connection pool being monitored.
	 * @return the size of the pool
	 */
	public int size() {
		return _pool.size();
	}

	/**
	 * Return the monitor interval.
	 * @return the interval <i>in minutes</i>.
	 */
	public int getInterval() {
		return (int) (_sleepTime / 60000);
	}

	/**
	 * Sets the monitor interval.
	 * @param sleepTime the interval <i>in minutes</i>
	 * @throws IllegalArgumentException if sleepTime is zero or negative
	 */
	public void setInterval(int sleepTime) {
		if (sleepTime < 1)
			throw new IllegalArgumentException("Sleep time cannot be zero or negative");

		_sleepTime = sleepTime * 60000; // Convert minutes into ms
	}

	/**
	 * Sets the JDBC connection pool to monitor.
	 * @param cPool a Collection of ConnectionPoolEntry objects
	 * @see ConnectionMonitor#addPool(List)
	 * @throws IllegalThreadStateException if the thread is alive
	 */
	public void setPool(Collection cPool) {
		if (isAlive())
			throw new IllegalThreadStateException("Connection Monitor already started");

		_pool = new ArrayList(cPool);
	}

	/**
	 * Adds another JDBC connection pool to monitor.
	 * @param cPool2 a Collection of ConnectionPoolEntry objects
	 * @see ConnectionMonitor#setPool(List)
	 * @throws IllegalThreadStateException if the thread is alive
	 */
	public void addPool(Collection cPool2) {
		if (isAlive())
			throw new IllegalThreadStateException("Connection Monitor already started");

		_pool.addAll(cPool2);
	}

	private boolean reconnect(ConnectionPoolEntry e) {
		log.info("JDBC Connection " + e + " disconnected");
		e.close();

		// Reconnect the connection if we can
		if (e.isRestartable()) {
			try {
				e.connect();
				return true;
			} catch (SQLException se) {
				log.warn("Error reconnecting Connection " + e + " - " + se.getMessage());
			}
		}

		return false;
	}

	/**
	 * Thread execution method.
	 * @see Thread#start()
	 */
	public void run() {
		log.info("Starting");

		// Check loop
		while (!isInterrupted()) {
			log.debug("Checking Connection Pool");
			_poolCheckCount++;

			for (Iterator i = _pool.iterator(); i.hasNext();) {
				ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();

				// Check if the entry has timed out
				if (cpe.inUse() && (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME)) {
					log.warn("Releasing stale JDBC Connection " + cpe);
					cpe.free();
				}

				// Check to ensure that the connection can still hit the back-end
				try {
					Connection c = cpe.getConnection();
					c.setAutoCommit(c.getAutoCommit());
				} catch (SQLException se) {
				   if (_sqlStatus.contains(se.getSQLState())) {
						log.warn("Reconnecting Connection " + cpe);

						// If we cannot reconnect, then remove from the pool
						if (!reconnect(cpe)) {
							log.warn("Cannot reconnect Connection " + cpe);
							i.remove();
						}
					} else {
						log.warn("Uknown SQL Error code - " + se.getSQLState(), se);
					}
				}
			}

			try {
				Thread.sleep(_sleepTime);
			} catch (InterruptedException ie) {
				log.debug("Interrupted while sleeping");
				interrupt();
			}
		}

		log.info("Stopping");
	}
}