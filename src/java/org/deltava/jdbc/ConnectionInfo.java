// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.jdbc;

import java.io.Serializable;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store information about a JDBC connection pool entry.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionInfo implements Serializable, Comparable, ViewEntry {
   
   private int _id;
   private boolean _isSystem;
   private boolean _inUse;
   private int _useCount;
   private long _totalUse;
   private long _currentUse;

   /**
    * Creates a new ConnectionInfo object from a Connection Pool entry.
    * @param entry the Connection Pool entry.
    */
   ConnectionInfo(ConnectionPoolEntry entry) {
      super();
      _id = entry.getID();
      _isSystem = entry.isSystemConnection();
      _inUse = entry.inUse();
      _useCount = entry.getUseCount();
      _totalUse = entry.getTotalUseTime();
      _currentUse = entry.getUseTime();
   }
   
   /**
    * Returns the Connection's ID.
    * @return the connection ID
    */
   public int getID() {
      return _id;
   }
   
   /**
    * Returns if the Connection is used by the System.
    * @return TRUE if the Connection is used by the System, otherwise FALSE
    * @see ConnectionPoolEntry#isSystemConnection()
    */
   public boolean getSystem() {
      return _isSystem;
   }
   
   /**
    * Returns if the Connection is currently in use.
    * @return TRUE if the Connection has been reserved, otherwise FALSE
    */
   public boolean getInUse() {
      return _inUse;
   }
   
   /**
    * Returns the number of times the Connection has been used. 
    * @return the number of times the Connection was reserved
    */
   public int getUseCount() {
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
    * Compares two ConnectionInfo objects by comparing their IDs.
    * @see Comparable#compareTo(Object) 
    */
   public int compareTo(Object o2) {
      ConnectionInfo ci2 = (ConnectionInfo) o2;
      return new Integer(_id).compareTo(new Integer(ci2.getID()));
   }
   
   /**
    * Returns the CSS class name used to display this bean in a view table.
    * @return the CSS class name
    */
   public String getRowClassName() {
      if (_inUse) {
         return "opt1";
      } else if (_isSystem) {
         return "opt2";
      } else {
         return null;
      }
   }
}