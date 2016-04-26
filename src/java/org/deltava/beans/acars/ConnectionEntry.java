// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store an ACARS Connection record.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ConnectionEntry extends ACARSLogEntry implements TimeSpan {

   private long _id;
   private int _pilotID;
   private Pilot _usr;
   private Instant _st;
   private Instant _et;
   
   private int _msgCount;
   private boolean _isCompressed;
   
   private ConnectionStats _tcpStats;
   private ConnectionStats _udpStats;
   
   private FlightInfo _fInfo;
   private String _flightPhase;
   private boolean _isHidden;
   private boolean _isVoice; 
   
   private class LocalConnectionStats extends ConnectionStats {
	   
	   LocalConnectionStats(ConnectionStats cs) {
		   super(cs);
	   }
   }
   
   /**
    * Creates a new ACARS Connection entry.
    * @param id the connection id
    * @throws IllegalArgumentException if id is zero or negative
    */
   public ConnectionEntry(long id) {
      super();
      setConnectionID(id);
   }
   
   /**
    * Returns the start date/time of this connection.
    * @return the date/time
    * @see ConnectionEntry#setStartTime(Instant)
    * @see ConnectionEntry#getEndTime()
    */
   @Override
   public Instant getStartTime() {
      return _st;
   }
   
   /**
    * Returns the end date/time of this connection.
    * @return the date/time
    * @see ConnectionEntry#setEndTime(Instant)
    * @see ConnectionEntry#getStartTime()
    */
   @Override
   public Instant getEndTime() {
	   return _et;
   }
   
   /**
    * Returns the length of the connection.
    * @return the length of the connection in seconds, or -1 if endTime is null
    */
   public long getTime() {
	   if (_et == null)
		   return -1;
	   
	   return (_et.toEpochMilli() - _st.toEpochMilli()) / 1000;
   }
   
   @Override
   public Instant getDate() {
	   return _st;
   }
   
   /**
    * Returns this connection's ID.
    * @return the connection ID
    * @see ConnectionEntry#setConnectionID(long)
    */
   public long getConnectionID() {
      return _id;
   }
   
   /**
    * Returns the database ID of the user who made this connection. This may be set to
    * 0 if the user was never authenticated. If a Pilot bean has been passed in, this will
    * return the Database ID of that bean.
    * @return the database ID
    * @see ConnectionEntry#getUser()
    */
   public int getPilotID() {
      return (_usr != null) ? _usr.getID() : _pilotID;
   }
   
   @Override
   public int getAuthorID() {
	   return getPilotID();
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
    * Returns if this is a Dispatch connection.
    * @return FALSE
    */
   @SuppressWarnings("static-method")
   public boolean getDispatch() {
	   return false; 
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
    * Returns whether voice is enabled.
    * @return TRUE if voice enabled, otherwise FALSE
    * @see ConnectionEntry#setVoice(boolean)
    */
   public boolean getVoice() {
	   return _isVoice;
   }
   
   /**
    * Returns whether data compression is enabled.
    * @return TRUE if data compression enabled, otherwise FALSE
    * @see ConnectionEntry#setCompressed(boolean)
    */
   public boolean getCompressed() {
	   return _isCompressed;
   }
   
   /**
    * Returns the flight information for the flight.
    * @return a FlightInformation bean
    */
   public FlightInfo getFlightInfo() {
	   return _fInfo;
   }
   
   /**
    * Returns the flight phase.
    * @return the phase of flight
    * @see ConnectionEntry#setFlightPhase(String)
    */
   public String getFlightPhase() {
	   return _flightPhase;
   }
   
   /**
    * Returns control statistics.
    * @return a ConnectionStats bean
    */
   public ConnectionStats getTCPStatistics() {
	   return _tcpStats;
   }
   
   /**
    * Returns voice statistics.
    * @return a ConnectionStats bean
    */
   public ConnectionStats getUDPStatistics() {
	   return _udpStats;
   }
   
   /**
    * Returns the number of text messages sent by this connection. 
    * @return the number of messages
    * @see ConnectionEntry#setMessageCount(int)
    */
   public int getMessageCount() {
      return _msgCount;
   }
   
   /**
    * Updates connection statistics.
    * @param tcp TCP statistics
    * @param udp UDP statistics
    */
   public void setStatistics(ConnectionStats tcp, ConnectionStats udp) {
	   _tcpStats = new LocalConnectionStats(tcp);
	   if (udp != null)
		   _udpStats = new LocalConnectionStats(udp);
   }
   
   /**
    * Updates the ACARS connection ID.
    * @param id the connection ID
    * @throws IllegalArgumentException if id is negative
    * @see ConnectionEntry#getID()
    */
   public void setConnectionID(long id) {
      _id = Math.max(0, id);
   }
   
   /**
    * Returns the date/time the connection was started.
    * @param dt the date/time
    * @see ConnectionEntry#getStartTime()
    */
   public void setStartTime(Instant dt) {
      _st = dt;
   }
   
   /**
    * Returns the date/time the connection was ended.
    * @param dt the date/time
    * @see ConnectionEntry#getEndTime()
    */
   public void setEndTime(Instant dt) {
	   _et = dt;
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
    * Sets the flight phase
    * @param phase the phase of flight
    * @see ConnectionEntry#getFlightPhase()
    */
   public void setFlightPhase(String phase) {
	   _flightPhase = phase;
   }
   
   /**
    * Updates the Pilot bean for the user who created this connection.
    * @param usr the Pilot bean
    * @see ConnectionEntry#getUser()
    */
   public void setUser(Pilot usr) {
      _usr = usr;
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
    * Marks this connection as voice-enabled.
    * @param isVoice TRUE if voice enabled, otherwise FALSE
    * @see ConnectionEntry#getVoice()
    */
   public void setVoice(boolean isVoice) {
	   _isVoice = isVoice;
   }
   
   /**
    * Marks this connection as compressed.
    * @param isCompressed TRUE if data compression enabled, otherwise FALSE
    * @see ConnectionEntry#getCompressed()
    */
   public void setCompressed(boolean isCompressed) {
	   _isCompressed = isCompressed;
   }
   
   /**
    * Updates the number of text messages sent by this connection.
    * @param msgs the number of messages
    * @see ConnectionEntry#getMessageCount()
    */
   public void setMessageCount(int msgs) {
      _msgCount = Math.max(0, msgs);
   }
   
   /**
    * Updates the Database ID of the user who created this connection.
    * @param id the database ID
    * @throws IllegalStateException if setUser() has already been called
    * @throws IllegalArgumentException if id is zero or negative
    * @see ConnectionEntry#getPilotID()
    * @see ConnectionEntry#setUser(Pilot)
    */
   @Override
   public void setAuthorID(int id) {
      if (_usr != null)
         throw new IllegalStateException("User bean already set");
      
      DatabaseBean.validateID(_pilotID, id);
      _pilotID = id;
   }
   
   /**
    * Compares two connections by comparing their date/times.
    */
   @Override
   public int compareTo(Object o2) {
	   CalendarEntry c2 = (CalendarEntry) o2;
	   return _st.compareTo(c2.getDate());
   }
}