// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.*;
import java.util.Properties;

import java.lang.reflect.*;

/**
 * A class to store JDBC connections in a connection pool and track usage.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class ConnectionPoolEntry implements Comparable {

   private Connection _c;
   private int _id;

   private String _url;
   private Properties _props;

   private boolean _inUse = false;
   private boolean _systemOwned = false;
   private boolean _dynamic = false;
   private boolean _autoCommit = true;

   private long _totalTime;
   private long _useTime;
   private long _startTime;
   private int _useCount;

   private Method _ping;

   /**
    * Create a new Connection Pool entry.
    * @param id the connection pool entry ID
    * @param url the JDBC URL to connect to
    * @param props JDBC connection properties
    */
   ConnectionPoolEntry(int id, String url, Properties props) {
      super();
      _id = id;
      _url = url;
      _props = props;
   }

   /**
    * Check if the connection is in use.
    * @return TRUE if this connection is in use, otherwise FALSE
    */
   public boolean inUse() {
      return _inUse;
   }

   /**
    * Returns if this connection is used by the system.
    * @return TRUE if the connection is system-owned, otherwise FALSE.
    * @see ConnectionPoolEntry#setSystemConnection(boolean)
    */
   public boolean isSystemConnection() {
      return _systemOwned;
   }

   /**
    * Returns if this connection can be reconnected by a connection monitor, or freed after use.
    * @return TRUE if the connection can be freed after use, otherwise FALSE
    * @see ConnectionPoolEntry#isSystemConnection()
    */
   public boolean isDynamic() {
      return _dynamic;
   }

   /**
    * Connects this entry to the JDBC data source.
    * @throws SQLException if a JDBC error occurs
    * @throws IllegalStateException if the entry is already connected
    */
   void connect() throws SQLException {
      if ((_c != null) && !_c.isClosed())
         throw new IllegalStateException("Connection " + toString() + " already Connected");

      // Create the connection
      _c = DriverManager.getConnection(_url, _props);
      _c.setAutoCommit(_autoCommit);
      _c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

      // Check if we have a ping method
      try {
         _ping = _c.getClass().getMethod("ping", (Class []) null);
      } catch (NoSuchMethodException nsme) {
         _ping = null;
      }
   }

   /**
    * Closes the JDBC connection, swallowing any errors.
    */
   void close() {
      try {
         _c.close();
      } catch (Exception e) {
      }
   }

   /**
    * Return the connection to the connection pool.
    */
   void free() {
      if (!inUse())
         return;

      // Reset auto-commit property
      try {
         _c.setAutoCommit(_autoCommit);
      } catch (Exception e) {
      }

      // Add the usage time to the total for this connection
      _useTime = getUseTime();
      _totalTime += _useTime;
      _inUse = false;
   }

   /**
    * Checks if the underlying JDBC connection is still connected.
    * @return TRUE if connected, FALSE if not connected
    */
   boolean checkConnection() {
      try {
         if (_ping != null) {
            _ping.invoke(_c, (Object []) null);
         } else {
            Statement s = _c.createStatement();
            ResultSet rs = s.executeQuery("SELECT 1");
            rs.close();
            s.close();
         }
         
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   /**
    * Returns the connection object behind this ConnectionPoolEntry. This is package protected since it should only be
    * accessed by the equals() method or the connection pool itself
    * @return the JDBC Connection
    * @see ConnectionPoolEntry#equals(Object)
    */
   Connection getConnection() {
      return _c;
   }

   /**
    * Returns the Conenction Pool Entry id.
    * @return the entry id
    */
   public int getID() {
      return _id;
   }

   /**
    * Sets the automatic commit setting for this connection. When set, all transactions will be committed to the JDBC
    * data source immediately. Data Access Objects may change the autoCommit property of the underlying JDBC connection,
    * but when the connection is returned to the pool its autoCommit property will be reset back to this value.
    * @param commit TRUE if connections should autoCommit by default, otherwise FALSE
    * @see Connection#setAutoCommit(boolean)
    */
   void setAutoCommit(boolean commit) {
      _autoCommit = commit;
   }

   /**
    * Marks this connection as being owned by the system. System-owned connections are not used for direct user queries,
    * instead for application components that need guaranteed access to a JDBC connection.
    * @param isOwnedBySystem TRUE if this is a system-owned connection, otherwise FALSE
    * @see ConnectionPoolEntry#isSystemConnection()
    */
   void setSystemConnection(boolean isOwnedBySystem) {
      _systemOwned = isOwnedBySystem;
   }

   /**
    * Marks this connection as dynamic.
    * @param dynamic TRUE if the connection is dynamic, otherwise FALSE
    * @see ConnectionPoolEntry#isDynamic()
    */
   void setDynamic(boolean dynamic) {
      _dynamic = dynamic;
   }

   /**
    * Reserve this Connection pool entry, and get the underlyig JDBC connection. This method is package private since it
    * only should be called by the ConnectionPool object
    * @return the JDBC Connection object
    * @throws IllegalStateException if the connection is already reserved
    */
   Connection reserve() {
      if (inUse())
         throw new IllegalStateException("Connection " + toString() + " already in use");

      // Mark the connection as in use, and return the SQL connection
      _startTime = System.currentTimeMillis();
      _inUse = true;
      _useCount++;
      return _c;
   }

   /**
    * Returns how long this connection was used the last time.
    * @return the time this Connection Entry was reserved, in milliseconds
    */
   public long getUseTime() {
      return inUse() ? (System.currentTimeMillis() - _startTime) : _useTime;
   }

   /**
    * Returns the number of times this connection has been reserved.
    * @return the number of times reserved
    */
   public int getUseCount() {
      return _useCount;
   }

   /**
    * Returns how long this connection was used since the Connection pool was started.
    * @return the total time this Connection Entry was reserved, in milliseconds
    */
   public long getTotalUseTime() {
      return _totalTime;
   }

   /**
    * This overrides equals behavior by comparing the underlying connection object. This allows us to get a
    * ConnectionPoolEntry from the pool when all we get back is the SQL Connection.
    */
   public boolean equals(Object o2) {
      if (o2 instanceof Connection) {
         return (_c == ((Connection) o2));
      } else if (o2 instanceof ConnectionPoolEntry) {
         return (compareTo(o2) == 0);
      } else {
         return false;
      }
   }

   /**
    * Compares two entries by comparing their ID.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      ConnectionPoolEntry e2 = (ConnectionPoolEntry) o2;
      return new Integer(_id).compareTo(new Integer(e2.getID()));
   }

   /**
    * This overrides hashcode behavior by returning the hashcode of the underlying JDBC connection.
    * @see ConnectionPoolEntry#equals(Object)
    */
   public int hashCode() {
      return _c.hashCode();
   }

   /**
    * Returns a text representation of the Connection ID.
    * @return the connection ID
    */
   public final String toString() {
      StringBuffer buf = new StringBuffer("#");
      if (isSystemConnection())
         buf.append("SYS");

      buf.append(String.valueOf(_id));
      return buf.toString();
   }
}