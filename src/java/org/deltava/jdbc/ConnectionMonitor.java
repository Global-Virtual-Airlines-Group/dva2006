// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.jdbc;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

/**
 * A daemon Thread to monitor JDBC connections.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class ConnectionMonitor extends Thread {

   private static final Logger log = Logger.getLogger(ConnectionMonitor.class);

   private static List _sqlStatus = Arrays.asList(new String[] { "08003", "08S01" });

   private Set<ConnectionPoolEntry> _pool = new TreeSet<ConnectionPoolEntry>();
   private long _sleepTime = 180000; // 3 minute default
   private long _poolCheckCount;

   /**
    * Creates a new Connection Monitor. The thread is set as a daemon.
    * @param interval the sleep time <i>in minutes </i>
    * @see Thread#setDaemon(boolean)
    */
   ConnectionMonitor(int interval) {
      super("JDBC Pool Monitor");
      setDaemon(true);
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
      log.debug("Checking Connection Pool");
      
      // Loop through the entries
      for (Iterator i = _pool.iterator(); i.hasNext();) {
         ConnectionPoolEntry cpe = (ConnectionPoolEntry) i.next();

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
                  log.warn("Uknown SQL Error code - " + se.getSQLState());
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
      while (!isInterrupted()) {
         checkPool();

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