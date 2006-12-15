// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.util.ThreadUtils;

/**
 * A daemon Thread to monitor JDBC connections.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class ConnectionMonitor implements Runnable {

   private static final Logger log = Logger.getLogger(ConnectionMonitor.class);
   
   /**
    * Thread name.
    */
   static final String NAME = "JDBC Connection Monitor";

   private static final List<String> _sqlStatus = Arrays.asList(new String[] { "08003", "08S01" });

   private final Collection<ConnectionPoolEntry> _pool = new TreeSet<ConnectionPoolEntry>();
   private long _sleepTime = 60000; // 1 minute default
   private long _poolCheckCount;

   /**
    * Creates a new Connection Monitor.
    * @param interval the sleep time <i>in minutes </i>
    */
   ConnectionMonitor(int interval) {
      super();
      _sleepTime = interval * 60000; // Convert minutes into ms
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
         _pool.add(cpe);
   }

   /**
    * Removes a JDBC connection from the monitor.
    * @param cpe a ConnectionPoolEntry object
    */
   public synchronized void removeConnection(ConnectionPoolEntry cpe) {
         _pool.remove(cpe);
   }

   /**
    * Manually check the connection pool.
    */
   protected synchronized void checkPool() {
      _poolCheckCount++;
      if (log.isDebugEnabled())
    	  log.debug("Checking Connection Pool");
      
      // Loop through the entries
      for (Iterator<ConnectionPoolEntry> i = _pool.iterator(); i.hasNext(); ) {
         ConnectionPoolEntry cpe = i.next();

         // Check if the entry has timed out
         if (cpe.inUse() && (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME)) {
            log.warn("Releasing stale Connection " + cpe);
            cpe.free();
         } else if (cpe.isDynamic() && !cpe.inUse()) {
            log.warn("Releasing stale dyanmic Connection " + cpe);
            cpe.free();
            i.remove();
         } else if (!cpe.checkConnection()) {
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
    * Thread execution method.
    * @see Thread#start()
    */
   public void run() {
      log.info("Starting");

      // Check loop
      while (!Thread.currentThread().isInterrupted()) {
         checkPool();
         ThreadUtils.sleep(_sleepTime);
      }

      log.info("Stopping");
   }
}