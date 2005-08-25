// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.util.Date;

/**
 * A bean to store last execution data for a Scheduled Task.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TaskLastRun implements java.io.Serializable, Comparable {
   
   private String _name;
   private Date _lastRun;

   /**
    * Creates a new Task execution data bean.
    * @param name the name of the Task
    * @param dt the last execution date/time
    * @throws NullPointerException if name is null
    * @see TaskLastRun#getName()
    * @see TaskLastRun#getLastRun()
    */
   public TaskLastRun(String name, Date dt) {
      super();
      _name = name.trim();
      _lastRun = dt;
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
    * Compares two task execution beans by comparing their names and last run date/times.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      TaskLastRun tlr2 = (TaskLastRun) o2;
      
      int tmpResult = _name.compareTo(tlr2.getName());
      if ((tmpResult == 0) && (_lastRun == null)) {
         return (tlr2.getLastRun() == null) ? 0 : -1;
      } else if ((tmpResult == 0) && (_lastRun != null)) {
         return (tlr2.getLastRun() == null) ? 1 : 0;
      } else if (tmpResult == 0) {
         return _lastRun.compareTo(tlr2.getLastRun());
      }
      
      return tmpResult;
   }
}