// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.util.Date;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store information about a JDBC connection pool entry.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ConnectionInfo implements java.io.Serializable, Comparable<ConnectionInfo>, ViewEntry {
   
   private int _id;
   private boolean _isDynamic;
   private boolean _isConnected;
   private boolean _inUse;
   private long _useCount;
   private long _totalUse;
   private long _currentUse;
   private Date _lastUsed;
   private Throwable _trace;

   /**
    * Creates a new ConnectionInfo object from a Connection Pool entry.
    * @param entry the Connection Pool entry.
    */
   ConnectionInfo(ConnectionPoolEntry entry) {
      super();
      _id = entry.getID();
      _isDynamic = entry.isDynamic();
      _isConnected = entry.isConnected();
      _inUse = entry.inUse();
      _useCount = entry.getUseCount();
      _totalUse = entry.getTotalUseTime();
      _currentUse = entry.getUseTime();
      _trace = entry.getStackInfo();
      if (entry.getLastUseTime() > 0)
    	  _lastUsed = new Date(entry.getLastUseTime());
   }
   
   /**
    * Returns the Connection's ID.
    * @return the connection ID
    */
   public int getID() {
      return _id;
   }
   
   /**
    * Returns the Connection's stack trace
    * @return a Throwable with the stack trace
    * @see Throwable#getStackTrace()
    */
   public Throwable getStackInfo() {
	   return _trace;
   }
   
   /**
    * Returns if the Connection is currently in use.
    * @return TRUE if the Connection has been reserved, otherwise FALSE
    */
   public boolean getInUse() {
      return _inUse;
   }
   
   /**
    * Returns if the Connection is currently active.
    * @return TRUE if connected, otherwise FALSE
    */
   public boolean getConnected() {
	   return _isConnected;
   }
   
   /**
    * Returns if the Connection is a Dynamic connection.
    * @return TRUE if dynamic, otherwise FALSE
    */
   public boolean getDynamic() {
	   return _isDynamic;
   }
   
   /**
    * Returns the number of times the Connection has been used. 
    * @return the number of times the Connection was reserved
    */
   public long getUseCount() {
      return _useCount;
   }
   
   /**
    * Returns the total usage time for the Connection.
    * @return the total usage time in milliseconds
    */
   public long getTotalUse() {
      return _totalUse;
   }
   
   /**
    * Returns the usage time of the last, or current, reservation of this Connection.
    * @return the usage time in milliseconds
    */
   public long getCurrentUse() {
      return _currentUse;
   }
   
   /**
    * Returns the last time this connection was reserved.
    * @return the last use date/time, or null if never
    */
   public Date getLastUsed() {
	   return _lastUsed;
   }
   
   /**
    * Returns the connection type for rendering in a JSP.
    * @return the connection type
    */
   public String getTypeName() {
	   StringBuilder buf = new StringBuilder(_isDynamic ? "Dynamic" : "Persistent");
	   buf.append(" User");
	   return buf.toString();
   }

   /**
    * Compares two ConnectionInfo objects by comparing their IDs and usage counts.
    */
   public int compareTo(ConnectionInfo ci2) {
      int tmpResult = Integer.valueOf(_id).compareTo(Integer.valueOf(ci2._id));
      return (tmpResult == 0) ? new Long(_useCount).compareTo(new Long(ci2._useCount)) : tmpResult;
   }
   
   public int hashCode() {
	   return Integer.valueOf(_id).hashCode();
   }
   
   /**
    * Returns the CSS class name used to display this bean in a view table.
    * @return the CSS class name
    */
   public String getRowClassName() {
      return _inUse ? "opt1" : null;
   }
}