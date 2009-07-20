// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;

/**
 * A bean to store information about a scheduled task. 
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class TaskInfo implements Comparable<TaskInfo> {
   
   private String _id;
   private String _name;
   private String _className;
   private Date _lastStart;
   private long _lastRunTime;
   private int _runCount;
   private boolean _enabled;
   
   private Map<String, Collection<Integer>> _runTimes;

   /**
    * Initializes the Task Information bean.
    * @param t the Task to display information about
    */
   public TaskInfo(Task t) {
      super();
      _id = t.getID();
      _name = t.getName();
      _className = t.getClass().getName();
      _lastStart = t.getStartTime();
      _lastRunTime = t.getLastRunTime();
      _runCount = t.getRunCount();
      _enabled = t.getEnabled();
      
      // Process run times
      Map<String, Collection<Integer>> runTimes = t.getRunTimes();
      _runTimes = new LinkedHashMap<String, Collection<Integer>>();
      for (Iterator<String> i = runTimes.keySet().iterator(); i.hasNext(); ) {
    	  String intervalType = i.next();
    	  Collection<Integer> intervals = runTimes.get(intervalType);
    	  if ((intervals != null) && (!intervals.contains(Task.ANY)))
    		  _runTimes.put(intervalType, intervals);
      }
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
    * Returns the times this Task is eligible to be run.
    * @return a Map of interval types and values
    * @see Task#setRunTimes(String, String)
    */
   public Map<String, Collection<Integer>> getRunTimes() {
	   return _runTimes;
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
    */
   public int compareTo(TaskInfo ti2) {
      return _lastStart.compareTo(ti2._lastStart);
   }
}