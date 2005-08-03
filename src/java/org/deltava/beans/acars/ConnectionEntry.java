// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.Pilot;

/**
 * A bean to store an ACARS Connection record.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionEntry implements ACARSLogEntry {

   private long _id;
   private int _pilotID;
   private Pilot _usr;
   private Date _dt;
   
   private String _remoteHost;
   private String _remoteAddr;
   
   private int _msgCount;
   private int _infoCount;
   private int _posCount;
   
   /**
    * Creates a new ACARS Connection entry.
    * @param id the connection id
    * @throws IllegalArgumentException if id is zero or negative
    */
   public ConnectionEntry(long id) {
      super();
      setID(id);
   }
   
   /**
    * Returns the date/time of this connection.
    * @return the date/time
    * @see ConnectionEntry#setStartTime(Date)
    */
   public Date getStartTime() {
      return _dt;
   }
   
   /**
    * Returns this connection's ID.
    * @return the connection ID
    * @see ConnectionEntry#setID(long)
    */
   public long getID() {
      return _id;
   }
   
   /**
    * Returns the database ID of the user who made this connection. This may be set to
    * 0 if the user was never authenticated. If a Pilot bean has been passed in, this will
    * return the Database ID of that bean.
    * @return the database ID
    * @see ConnectionEntry#setPilotID(int)
    * @see ConnectionEntry#getUser()
    */
   public int getPilotID() {
      return (_usr != null) ? _usr.getID() : _pilotID;
   }
   
   /**
    * Returns the Pilot bean for the connection's authenticated user. This may not be set.
    * @return the Pilot who made this connection
    * @see ConnectionEntry#getPilotID()
    * @see ConnectionEntry#setUser(Pilot)
    */
   public Pilot getUser() {
      return _usr;
   }
   
   /**
    * Returns the IP address for this connection.
    * @return the IP Address
    * @see ConnectionEntry#setRemoteAddr(String)
    * @see ConnectionEntry#getRemoteHost()
    */
   public String getRemoteAddr() {
      return _remoteAddr;
   }
   
   /**
    * Returns the host name for this connection.
    * @return the host name
    * @see ConnectionEntry#getRemoteAddr()
    * @see ConnectionEntry#setRemoteHost(String)
    */
   public String getRemoteHost() {
      return _remoteHost;
   }
   
   /**
    * Returns the number of text messages sent by this connection. 
    * @return the number of messages
    * @see ConnectionEntry#setMessageCount(int)
    * @see ConnectionEntry#getFlightInfoCount()
    * @see ConnectionEntry#getPositionCount()
    */
   public int getMessageCount() {
      return _msgCount;
   }
   
   /**
    * Returns the number of flight information messages sent by this connection.
    * @return the number of messages
    * @see ConnectionEntry#setFlightInfoCount(int)
    * @see ConnectionEntry#getMessageCount()
    * @see ConnectionEntry#getPositionCount()
    */
   public int getFlightInfoCount() {
      return _infoCount;
   }
   
   /**
    * Returns the number of aircraft position messages sent by this connection.
    * @return the number of messages
    * @see ConnectionEntry#setPositionCount(int)
    * @see ConnectionEntry#getMessageCount()
    * @see ConnectionEntry#getFlightInfoCount()
    */
   public int getPositionCount() {
      return _posCount;
   }
   
   /**
    * Updates the ACARS connection ID.
    * @param id the connection ID
    * @throws IllegalArgumentException if id is negative
    * @see ConnectionEntry#getID()
    */
   public void setID(long id) {
      if (id < 0)
         throw new IllegalArgumentException("Invalid connection ID - " + id);
      
      _id = id;
   }
   
   /**
    * Returns the date/time the connection was started.
    * @param dt the date/time
    * @see ConnectionEntry#getStartTime()
    */
   public void setStartTime(Date dt) {
      _dt = dt;
   }
   
   /**
    * Updates the IP address for this connection.
    * @param addr the IP address
    * @see ConnectionEntry#getRemoteAddr()
    * @see ConnectionEntry#setRemoteHost(String)
    */
   public void setRemoteAddr(String addr) {
      _remoteAddr = addr;
   }
   
   /**
    * Updates the host name for this connection.
    * @param host the host name
    * @see ConnectionEntry#getRemoteHost()
    * @see ConnectionEntry#setRemoteAddr(String)
    */
   public void setRemoteHost(String host) {
      _remoteHost = host;
   }
   
   /**
    * Updates the Pilot bean for the user who created this connection.
    * @param usr the Pilot bean
    * @see ConnectionEntry#getUser()
    * @see ConnectionEntry#setPilotID(int)
    */
   public void setUser(Pilot usr) {
      _usr = usr;
   }
   
   /**
    * Updates the number of text messages sent by this connection.
    * @param msgs the number of messages
    * @throws IllegalArgumentException if msgs is negative
    * @see ConnectionEntry#getMessageCount()
    * @see ConnectionEntry#setFlightInfoCount(int)
    * @see ConnectionEntry#setPositionCount(int)
    */
   public void setMessageCount(int msgs) {
      if (msgs < 0)
         throw new IllegalArgumentException("Invalid message count - " + msgs);
      
      _msgCount = msgs;
   }
   
   /**
    * Updates the number of flight information messages sent by this connection.
    * @param msgs the number of messages
    * @throws IllegalArgumentException if msgs is negative
    * @see ConnectionEntry#getFlightInfoCount()
    * @see ConnectionEntry#setMessageCount(int)
    * @see ConnectionEntry#setPositionCount(int)
    */
   public void setFlightInfoCount(int msgs) {
      if (msgs < 0)
         throw new IllegalArgumentException("Invalid information count - " + msgs);
      
      _infoCount = msgs;
   }
   
   /**
    * Updates the number of aircraft position messages sent by this connection.
    * @param msgs the number of messages
    * @throws IllegalArgumentException if msgs is negative
    * @see ConnectionEntry#getPositionCount()
    * @see ConnectionEntry#setFlightInfoCount(int)
    * @see ConnectionEntry#setMessageCount(int)
    */
   public void setPositionCount(int msgs) {
      if (msgs < 0)
         throw new IllegalArgumentException("Invalid position count - " + msgs);
      
      _posCount = msgs;
   }
   
   /**
    * Updates the Database ID of the user who created this connection.
    * @param id the database ID
    * @throws IllegalStateException if setUser() has already been called
    * @throws IllegalArgumentException if id is zero or negative
    * @see ConnectionEntry#getPilotID()
    * @see ConnectionEntry#setUser(Pilot)
    */
   public void setPilotID(int id) {
      if (_usr != null)
         throw new IllegalStateException("User bean already set");
      
      DatabaseBean.validateID(_pilotID, id);
      _pilotID = id;
   }
   
   /**
    * Compares two connections by comparing their date/times.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      ConnectionEntry ce2 = (ConnectionEntry) o2;
      return _dt.compareTo(ce2.getStartTime());
   }
}