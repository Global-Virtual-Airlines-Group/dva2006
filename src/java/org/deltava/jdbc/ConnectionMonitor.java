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

   private static List _sqlStatus = Arrays.asList(new String[] { "08003", "08S01" });

   private Set _pool = new TreeSet();
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
   public void addConnection(ConnectionPoolEntry cpe) {
      synchronized (_pool) {
         _pool.add(cpe);
      }
   }

   /**
    * Removes a JDBC connection from the monitor.
    * @param cpe a ConnectionPoolEntry object
    */
   public void removeConnection(ConnectionPoolEntry cpe) {
      synchronized (_pool) {
         _pool.remove(cpe);
      }
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

         synchronized (_pool) {
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