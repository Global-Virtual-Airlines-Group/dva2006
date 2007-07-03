// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
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

public class ConnectionEntry implements java.io.Serializable, ACARSLogEntry {

   private long _id;
   private int _pilotID;
   private Pilot _usr;
   private Date _dt;
   
   private String _remoteHost;
   private String _remoteAddr;
   
   private int _clientBuild;
   
   private int _msgCount;
   private int _infoCount;
   private int _posCount;
   
   private int _msgsIn;
   private int _msgsOut;
   private long _bytesIn;
   private long _bytesOut;
   private int _bufferWrites;
   
   private FlightInfo _fInfo;
   private boolean _isDispatch;
   private boolean _isHidden;
   
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
    * Returns the ACARS client build number.
    * @return the client build number
    * @see ConnectionEntry#setClientBuild(int)
    */
   public int getClientBuild() {
      return _clientBuild;
   }
   
   /**
    * Returns if this is a Dispatch connection.
    * @return TRUE if this is a Dispatch connection, otherwise FALSE
    * @see ConnectionEntry#setDispatch(boolean)
    */
   public boolean getDispatch() {
	   return _isDispatch; 
   }
   
   /**
    * Returns if this is a Hidden connection.
    * @return TRUE if this is a Hidden connection, otherwise FALSE
    * @see ConnectionEntry#setUserHidden(boolean)
    */
   public boolean getUserHidden() {
	   return _isHidden;
   }
   
   /**
    * Returns the flight information for the flight.
    * @return a FlightInformation bean
    */
   public FlightInfo getFlightInfo() {
	   return _fInfo;
   }
   
   /**
    * Returns the number of ACARS messages sent by the client.
    * @return the number of messages
    */
   public int getMsgsIn() {
	   return _msgsIn;
   }
   
   /**
    * Returns the number of ACARS messages sent by the server.
    * @return the number of messages
    */
   public int getMsgsOut() {
	   return _msgsOut;
   }
   
   /**
    * Returns the number of bytes sent by the client.
    * @return the number of bytes
    */
   public long getBytesIn() {
	   return _bytesIn;
   }
   
   /**
    * Returns the number of bytes sent by the server.
    * @return the number of bytes
    */
   public long getBytesOut() {
	 return _bytesOut;  
   }
   
   /**
    * Returns the number of output I/O buffer writes completed by the server.
    * @return the number of writes
    */
   public int getBufferWrites() {
	   return _bufferWrites;
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
    * Sets the flight information for this connection.
    * @param info the flight information
    * @see ConnectionEntry#getFlightInfo()
    */
   public void setFlightInfo(FlightInfo info) {
	   _fInfo = info;
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
    * Updates the number of network I/O buffer writes
    * @param writes the number of writes
    * @see ConnectionEntry#getBufferWrites()
    */
   public void setBufferWrites(int writes) {
	   _bufferWrites = writes;
   }
   
   /**
    * Updates the number of messages sent on this connection.
    * @param in the number of messages received by the server
    * @param out the number of messages sent by the server
    * @see ConnectionEntry#getMsgsIn()
    * @see ConnectionEntry#getMsgsOut()
    */
   public void setMessages(int in, int out) {
	   _msgsIn = in;
	   _msgsOut = out;
   }
   
   
   /**
    * Updates the number of bytes sent on this connection.
    * @param in the number of bytes received by the server
    * @param out the number of bytes sent by the server
    * @see ConnectionEntry#getBytesIn()
    * @see ConnectionEntry#getBytesOut()
    */
   public void setBytes(long in, long out) {
	   _bytesIn = in;
	   _bytesOut = out;
   }
   
   /**
    * Updates the ACARS client build number.
    * @param ver the build number
    * @see ConnectionEntry#getClientBuild()
    */
   public void setClientBuild(int ver) {
      _clientBuild = ver;
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
    * Marks the connection as a Dispatch connection.
    * @param isDispatch TRUE if this is a Dispatcher connection, otherwise FALSE
    * @see ConnectionEntry#getDispatch()
    */
   public void setDispatch(boolean isDispatch) {
	   _isDispatch = isDispatch;
   }
   
   /**
    * Marks this connection as a Hidden connection.
    * @param isHidden TRUE if this is a hidden connection, otherwise FALSE
    * @see ConnectionEntry#getUser()
    */
   public void setUserHidden(boolean isHidden) {
	   _isHidden = isHidden;
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
    */
   public int compareTo(ACARSLogEntry e2) {
      return _dt.compareTo(e2.getStartTime());
   }
}