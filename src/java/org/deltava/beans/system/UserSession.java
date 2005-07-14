package org.deltava.beans.system;

import java.util.Date;

/**
 * A system bean to store user session data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserSession implements Comparable {

   private Date _startTime;
   private Date _endTime;
   private String _pilotName;
   private int _pilotID;
   private String _remoteAddr;
   private String _remoteHost;
   private String _sessionID;

   /**
    * Create a new User Session bean.
    * @param id the session ID
    * @see UserSession#getSessionID()
    */
   public UserSession(String id) {
      super();
      _sessionID = id;
   }

   /**
    * Returns the start time of the Session.
    * @return the date/time the Session was created
    * @see UserSession#setStartTime(Date)
    * @see UserSession#getEndTime()
    */
   public Date getStartTime() {
      return _startTime;
   }

   /**
    * Returns the end time of the Session.
    * @return the date/time the Session was terminated
    * @see UserSession#setEndTime(Date)
    * @see UserSession#getStartTime()
    */
   public Date getEndTime() {
      return _endTime;
   }

   /**
    * Returns the Session ID.
    * @return the ID
    */
   public String getSessionID() {
      return _sessionID;
   }

   /**
    * Returns the User's IP Address.
    * @return the IP Address
    * @see UserSession#setRemoteAddr(String)
    */
   public String getRemoteAddr() {
      return _remoteAddr;
   }

   /**
    * Returns the User's host name.
    * @return the host name
    * @see UserSession#setRemoteHost(String)
    */
   public String getRemoteHost() {
      return _remoteHost;
   }

   /**
    * Returns the Pilot Name.
    * @return the User's name
    * @see UserSession#setPilotName(String, String)
    */
   public String getPilotName() {
      return _pilotName;
   }

   /**
    * Returns the User's database ID.
    * @return the database ID
    * @see UserSession#setPilotID(int)
    */
   public int getPilotID() {
      return _pilotID;
   }

   /**
    * Returns the length of the session.
    * @return the session duration in milliseconds
    */
   public long getLength() {
      long eTime = (_endTime == null) ? System.currentTimeMillis() : _endTime.getTime();
      return (eTime - _startTime.getTime());
   }

   /**
    * Updates the Session's start time.
    * @param dt the date/time the session started
    * @throws IllegalArgumentException if dt is null
    * @see UserSession#getStartTime()
    * @see UserSession#setStartTime(Date)
    */
   public void setStartTime(Date dt) {
      if (dt == null) throw new IllegalArgumentException("Start Date/Time cannot be null");

      _startTime = dt;
   }

   /**
    * Updates the Session's end time.
    * @param dt the date/time the session ended
    * @throws IllegalArgumentException if dt is before the start time
    * @see UserSession#getEndTime()
    * @see UserSession#getStartTime()
    */
   public void setEndTime(Date dt) {
      if ((dt != null) && (dt.before(_startTime)))
            throw new IllegalArgumentException("End Date/Time cannot be before Start Date/Time");

      _endTime = dt;
   }

   /**
    * Updates the User's IP address.
    * @param addr the IP address
    * @see UserSession#getRemoteAddr()
    */
   public void setRemoteAddr(String addr) {
      _remoteAddr = addr;
   }

   /**
    * Updates the User's host name.
    * @param host the host name
    * @see UserSession#getRemoteHost()
    */
   public void setRemoteHost(String host) {
      _remoteHost = host;
   }

   /**
    * Updates the User's name.
    * @param fName the first (given) name
    * @param lName the last (family) name
    * @see UserSession#getPilotName()
    */
   public void setPilotName(String fName, String lName) {
      _pilotName = fName + " " + lName;
   }

   /**
    * Updates the User's Database ID.
    * @param id the pilot database ID
    * @throws IllegalArgumentException if id is zero or negative
    * @see UserSession#getPilotID()
    */
   public void setPilotID(int id) {
      if (id <= 0)
         throw new IllegalArgumentException("Invalid Pilot ID - " + id);
      
      _pilotID = id;
   }

   /**
    * Compares two Sessions by comparing their start date/times.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      UserSession us2 = (UserSession) o2;
      return _startTime.compareTo(us2.getStartTime());
   }
   
   /**
    * Returns the Session ID's hash code.
    */
   public int hashCode() {
      return _sessionID.hashCode();
   }
}