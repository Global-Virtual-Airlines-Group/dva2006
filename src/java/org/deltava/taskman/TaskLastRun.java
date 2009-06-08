// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.Date;

/**
 * A bean to store last execution data for a Scheduled Task.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class TaskLastRun implements java.io.Serializable, Comparable<TaskLastRun> {
   
   private String _name;
   private Date _lastRun;
   private long _execTime;

   /**
    * Creates a new Task execution data bean.
    * @param name the name of the Task
    * @param dt the last execution date/time
    * @param execTime the execution duration in milliseconds
    * @throws NullPointerException if name is null
    * @see TaskLastRun#getName()
    * @see TaskLastRun#getLastRun()
    */
   public TaskLastRun(String name, Date dt, long execTime) {
      super();
      _name = name.trim();
      _lastRun = dt;
      _execTime = Math.max(0, execTime);
   }
   
   /**
    * Returns the Schedule Task name.
    * @return the Task name
    */
   public String getName() {
      return _name;
   }
   
   /**
    * Returns the last execution time for the Scheduled Task.
    * @return the date/time the task last started, or null if never run
    */
   public Date getLastRun() {
      return _lastRun;
   }
   
   /**
    * Returns the task's last execution duration.
    * @return the duration in milliseconds
    */
   public long getExecTime() {
	   return _execTime;
   }

   /**
    * Compares two task execution beans by comparing their names and last run date/times.
    */
   public int compareTo(TaskLastRun tlr2) {
      int tmpResult = _name.compareTo(tlr2._name);
      if ((tmpResult == 0) && (_lastRun == null))
         return (tlr2.getLastRun() == null) ? 0 : -1;
      else if ((tmpResult == 0) && (_lastRun != null))
         return (tlr2.getLastRun() == null) ? 1 : 0;
      else if (tmpResult == 0)
         return _lastRun.compareTo(tlr2.getLastRun());
      
      return tmpResult;
   }
}