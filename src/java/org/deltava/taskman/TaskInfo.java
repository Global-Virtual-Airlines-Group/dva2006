// Copyright 2005, 2006, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store information about a scheduled task. 
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class TaskInfo implements java.io.Serializable, Comparable<TaskInfo> {
   
   private final String _id;
   private final String _name;
   private final String _className;
   private final Instant _lastStart;
   private final long _lastRunTime;
   private final int _runCount;
   private final boolean _enabled;
   
   private final Map<String, Collection<Integer>> _runTimes = new LinkedHashMap<String, Collection<Integer>>();

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
      for (Iterator<Map.Entry<String, Collection<Integer>>> i = runTimes.entrySet().iterator(); i.hasNext(); ) {
    	  Map.Entry<String, Collection<Integer>> te = i.next();
    	  if ((te.getValue() != null) && (!te.getValue().contains(Task.ANY)))
    		  _runTimes.put(te.getKey(), te.getValue());
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
   public Instant getLastStartTime() {
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
    * Compares two TaskInfo beans by comparing their next execution times and IDs.
    */
   @Override
   public int compareTo(TaskInfo ti2) {
      int tmpResult = _lastStart.compareTo(ti2._lastStart);
      if (tmpResult == 0)
    	  tmpResult = _id.compareTo(ti2._id);
      
      return tmpResult;
   }
}