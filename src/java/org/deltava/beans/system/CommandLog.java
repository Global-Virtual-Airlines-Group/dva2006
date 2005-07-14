// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;
import java.io.Serializable;

import org.deltava.beans.ViewEntry;
import org.deltava.commands.CommandResult;

/**
 * An object to log Web Site Command invocations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandLog implements Serializable, Comparable, ViewEntry {
   
   private Date _d;
   private String _cmdName;
   private String _remoteAddr;
   private String _remoteHost;
   private String _result;
   
   private int _pilotID;
   private boolean _success;
   private int _totalTime;
   private int _backEndTime;

   /**
    * Creates a new Command log entry.
    * @param d the date/time of the command invocation.
    * @see CommandLog#getDate() 
    */
   public CommandLog(Date d) {
      super();
      _d = d;
   }

   // TODO JavaDoc
   public CommandLog(String cmdName, CommandResult cr) {
      this(new Date());
      _cmdName = cmdName;
      _success = cr.getSuccess();
      _backEndTime = (int) cr.getBackEndTime();
      _totalTime = cr.getTime();
   }
   
   public Date getDate() {
      return _d;
   }
   
   public String getName() {
      return _cmdName;
   }
   
   public String getRemoteAddr() {
      return _remoteAddr;
   }
   
   public String getRemoteHost() {
      return _remoteHost;
   }
   
   public String getResult() {
      return _result;
   }
   
   public int getPilotID() {
      return _pilotID;
   }
   
   public boolean getSuccess() {
      return _success;
   }
   
   public int getBackEndTime() {
      return _backEndTime;
   }
   
   public int getTime() {
      return _totalTime;
   }
   
   public void setName(String cmdName) {
      _cmdName = cmdName;
   }
   
   public void setRemoteAddr(String addr) {
      _remoteAddr = addr;
   }
   
   public void setRemoteHost(String hostName) {
      _remoteHost = hostName;
   }
   
   public void setResult(String msg) {
      _result = msg;
   }
   
   public void setPilotID(int id) {
      _pilotID = id;
   }
   
   public void setSuccess(boolean isOK) {
      _success = isOK;
   }
   
   public void setBackEndTime(int time) {
      _backEndTime = time;
   }
   
   public void setTime(int time) {
      _totalTime = time;
   }
   
   /**
    * Compares two log entries by comparing their dates.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      CommandLog cl2 = (CommandLog) o2;
      return _d.compareTo(cl2.getDate());
   }
   
   public String getRowClassName() {
      return _success ? null : "warn";
   }
}
