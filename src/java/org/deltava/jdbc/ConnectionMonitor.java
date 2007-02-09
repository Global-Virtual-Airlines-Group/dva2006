// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.util.system.SystemData;

/**
 * A daemon to monitor JDBC connections.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class ConnectionMonitor implements Runnable {

   private static final Logger log = Logger.getLogger(ConnectionMonitor.class);
   
   private static final List<String> _sqlStatus = Arrays.asList(new String[] { "08003", "08S01" });

   private ConnectionPool _pool;
   private final Collection<ConnectionPoolEntry> _entries = new TreeSet<ConnectionPoolEntry>();
   private long _sleepTime = 60000; // 1 minute default
   private long _poolCheckCount;

   /**
    * Creates a new Connection Monitor.
    * @param interval the sleep time <i>in minutes </i>
    * @param pool the ConnectionPool to monitor
    */
   ConnectionMonitor(int interval, ConnectionPool pool) {
      super();
      _pool = pool;
      _sleepTime = interval * 60000; // Convert minutes into ms
   }

   /**
    * Returns the size of the connection pool being monitored.
    * @return the size of the pool
    */
   public int size() {
      return _entries.size();
   }

   /**
    * Return the monitor interval.
    * @return the interval <i>in minutes </i>.
    */
   public int getInterval() {
      return (int) (_sleepTime / 60000);
   }

   /**
    * Adds a JDBC connection to monitor.
    * @param cpe a ConnectionPoolEntry object
    */
   public synchronized void addConnection(ConnectionPoolEntry cpe) {
         _entries.add(cpe);
   }

   /**
    * Removes a JDBC connection from the monitor.
    * @param cpe a ConnectionPoolEntry object
    */
   public synchronized void removeConnection(ConnectionPoolEntry cpe) {
         _entries.remove(cpe);
   }

   /**
    * Manually check the connection pool.
    */
   protected synchronized void checkPool() {
      _poolCheckCount++;
      if (log.isDebugEnabled())
    	  log.debug("Checking Connection Pool");
      
      // Loop through the entries
      Collection<ConnectionPoolEntry> entries = new ArrayList<ConnectionPoolEntry>(_entries); 
      for (Iterator<ConnectionPoolEntry> i = entries.iterator(); i.hasNext(); ) {
         ConnectionPoolEntry cpe = i.next();

         // Check if the entry has timed out
         if (cpe.inUse() && (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME)) {
        	 log.error("Releasing stale Connection " + cpe, cpe.getStackInfo());
            _pool.release(cpe.getConnection());
         } else if (cpe.isDynamic() && !cpe.inUse()) {
            log.error("Releasing stale dyanmic Connection " + cpe, cpe.getStackInfo());
            _pool.release(cpe.getConnection());
         } else if (!cpe.inUse() && !cpe.checkConnection()) {
            log.warn("Reconnecting Connection " + cpe);
            cpe.close();
            
            try {
               cpe.connect();
            } catch (SQLException se) {
               if (_sqlStatus.contains(se.getSQLState())) {
                  log.warn("Transient SQL Error - " + se.getSQLState());
               } else {
                  log.warn("Unknown SQL Error code - " + se.getSQLState());
               }
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

      // Check loop
      while (!Thread.currentThread().isInterrupted()) {
         checkPool();
         try {
        	 Thread.sleep(_sleepTime);
         } catch (InterruptedException ie) {
        	 Thread.currentThread().interrupt();
         }
      }

      log.info("Stopping");
   }
}