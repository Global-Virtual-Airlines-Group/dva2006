// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.util.Date;

/**
 * A bean to store information about a scheduled task. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TaskInfo implements java.io.Serializable, Comparable {
   
   private String _id;
   private String _name;
   private String _className;
   private int _interval;
   private Date _lastStart;
   private Date _nextStart;
   private long _lastRunTime;
   private int _runCount;
   private boolean _enabled;

   /**
    * 
    */
   public TaskInfo(Task t) {
      super();
      _id = t.getID();
      _name = t.getName();
      _className = t.getClass().getName();
      _interval = t.getInterval();
      _lastStart = t.getStartTime();
      _nextStart = t.getNextStartTime();
      _lastRunTime = t.getLastRunTime();
      _runCount = t.getRunCount();
      _enabled = t.getEnabled();
   }

   /**
    * Returns the Task name.
    * @return the name
    */
   public String getName() {
      return _name;
   }
   
   /**
    * Returns the Task ID.
    * @return the ID
    */
   public String getID() {
      return _id;
   }
   
   /**
    * Returns the Task's class name.
    * @return the name of the class
    */
   public String getClassName() {
      return _className;
   }
   
   /**
    * Returns the interval between executions of this Task.
    * @return the interval in seconds
    * @see Task#getInterval()
    */
   public int getInterval() {
      return _interval;
   }
   
   /**
    * Returns the Task's last execution date.
    * @return the date/time the Task was last run, or null if never
    * @see Task#getStartTime()
    */
   public Date getLastStartTime() {
      return _lastStart;
   }
   
   /**
    * Returns the Task's next execution date.
    * @return the date/time the Task is scheduled to be executed
    * @see Task#getNextStartTime()
    */
   public Date getNextStartTime() {
     return _nextStart; 
   }
   
   /**
    * Returns the duration of the Task's last execution.
    * @return the execution time in milliseconds
    * @see Task#getLastRunTime()
    */
   public long getLastRunTime() {
      return _lastRunTime;
   }
   
   /**
    * Returns if the Task is enabled for execution.
    * @return TRUE if the Task is enabled, otherwise FALSE
    */
   public boolean getEnabled() {
      return _enabled;
   }
   
   /**
    * Returns the number of times the Task has been executed.
    * @return the execution count
    */
   public int getRunCount() {
      return _runCount;
   }
   
   /**
    * Compares two TaskInfo beans by comparing their next execution times.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      TaskInfo ti2 = (TaskInfo) o2;
      return _nextStart.compareTo(ti2.getNextStartTime());
   }
}