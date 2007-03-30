// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.util.*;

/**
 * A class to support Scheduled Tasks. Scheduled Tasks are similar to UNIX cron jobs, and are scheduled for
 * execution in much the same way.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class Task implements Runnable, Comparable {
	
	/**
	 * Time interval options.
	 */
	public static final String[] TIME_OPTS = {"min", "hour", "mday", "month", "wday"};
	private static final int[] TIME_FIELDS = {Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH,
		Calendar.MONTH, Calendar.DAY_OF_WEEK};
	
	/**
	 * Wildcard for &quot;All Intervals&quot;
	 */
	static final Integer ANY = new Integer(-1);
	private static final String ALL_TIMES = "*";

    protected final Logger log;
    
    private String _id;
    private String _name;
    
    private long _lastRunTime;
    private Date _lastStartTime;
    
    private boolean _enabled;
    private int _runCount;
    
    private final Map<String, Collection<Integer>> _runTimes = new HashMap<String, Collection<Integer>>();
    
    /**
     * Creates a new Scheduled Task with a given class name.
     * @param name the task name 
     * @param loggerClass the logger Class for Log4J
     */
    protected Task(String name, Class loggerClass) {
        super();
        _name = name.trim();
        log = Logger.getLogger(loggerClass);
    }
    
    /**
     * Returns the Task ID.
     * @return the ID
     */
    public String getID() {
       return _id;
    }
    
    /**
     * Returns the Task name.
     * @return the name
     */
    public String getName() {
       return _name;
    }
    
    /**
     * Returns how the last execution duration of this Task.
     * @return the last execution duration in milliseconds, or 0 if the Task has never been executed
     */
    public long getLastRunTime() {
        return _lastRunTime;
    }
    
    /**
     * Returns the number of times this Task has been executed.
     * @return the number of executions
     */
    public int getRunCount() {
       return _runCount;
    }
    
    /**
     * Returns the when this Task was last started on.
     * @return the date/time the Task was last started, or null if the Task has never been executed
     */
    public Date getStartTime() {
        return _lastStartTime;
    }

    /**
     * Returns if the Task is allowed to be executed.
     * @return TRUE if the Task can be executed, otherwise FALSE
     * @see Task#setEnabled(boolean)
     */
    public boolean getEnabled() {
       return _enabled;
    }
    
    /**
     * Returns the times this Task is eligible to be run. 
     * @return a Map of interval types and values
     * @see Task#setRunTimes(String, String)
     */
    Map<String, Collection<Integer>> getRunTimes() {
    	return new LinkedHashMap<String, Collection<Integer>>(_runTimes);
    }
    
    /**
     * Returns wether the Scheduled Task can be executed at the present time.
     * @return TRUE if the Task can be executed, otherwise FALSE
     * @see Task#isRunnable(Calendar)
     * @see Task#setRunTimes(String, String)
     */
    public boolean isRunnable() {
    	return isRunnable(CalendarUtils.getInstance(null));
    }
    
    /**
     * Determines wether this Scheduled Task is runnable at a particular date/time. <i>This is package private
     * for unit testing purposes.</i>
     * @param dt the date/time to execute the task at
     * @return TRUE if the Task can be executed, otherwise FALSE
     * @see Task#isRunnable()
     * @see Task#setRunTimes(String, String)
     */
    boolean isRunnable(Calendar dt) {
    	if ((!_enabled) || _runTimes.isEmpty())
    		return false;
    	
    	// Check the time options
    	for (int x = 0; x < TIME_OPTS.length; x++) {
    		Collection<Integer> runTimes = _runTimes.get(TIME_OPTS[x]);
    		int timeField = dt.get(TIME_FIELDS[x]);
    		if (log.isDebugEnabled())
    			log.debug(TIME_OPTS[x] + ", now = " + timeField + ", allowed = " + runTimes);
    		
    		// Make sure we qualify to run
    		if ((runTimes != null) && (!runTimes.contains(ANY)) && (!runTimes.contains(new Integer(timeField))))
    			return false;
    	}
    	
    	// If we made it this far, we're eligible to run
    	return true;
    }
    
    /**
     * Sets the Task ID.
     * @param id the ID
     * @throws NullPointerException if id is null
     */
    public void setID(String id) {
       _id = id.trim();
    }
    
    /**
     * Sets the time of the day this Task may run in.
     * @param intervalType the time interval Type
     * @param values a comma-delimited set of numbers
     * @see Task#TIME_OPTS
     * @see Task#isRunnable()
     * @see Task#getRunTimes()
     */
    public void setRunTimes(String intervalType, String values) {
    	if (StringUtils.arrayIndexOf(TIME_OPTS, intervalType) == -1)
    		throw new IllegalArgumentException("Invalid Time interval type - " + intervalType);
    	
    	// Get the interval Collection
    	Collection<Integer> intervals = _runTimes.get(intervalType);
    	if (intervals == null) {
    		intervals = new HashSet<Integer>();
    		_runTimes.put(intervalType, intervals);
    	} else
    		intervals.clear();
    	
    	// Split the values
    	if (ALL_TIMES.equals(values))
    		intervals.add(ANY);
    	else {
    		for (Iterator<String> i = StringUtils.split(values, ",").iterator(); i.hasNext(); ) {
    			String value = i.next();
    			int v = StringUtils.parse(value, -1);
    			if (v != -1)
    				intervals.add(new Integer(v));
    		}
    	}
    }
    
    /**
     * Marks the Task as enabled to execute.
     * @param enabled TRUE if the task is enabled, otherwise FALES
     * @see Task#getEnabled()
     */
    public void setEnabled(boolean enabled) {
       _enabled = enabled;
    }
    
    /**
     * Overrides the last execution time for this Task.
     * @param dt the date/time this Task last executed
     * @see Task#getStartTime()
     */
    public void setStartTime(Date dt) {
       if (dt != null)
          _lastStartTime = dt;
    }
    
    /**
     * Executes the Task. This logs execution start/stop times and calls each Task implementation's
     * {@link Task#execute(TaskContext)} method.
     */
    public void run() {
        setStartTime(new Date());
        _runCount++;
        log.info(_name + " starting ");
        execute(new TaskContext());
        _lastRunTime = (System.currentTimeMillis() - _lastStartTime.getTime());
    }
    
    /**
     * Compares two Tasks by comparing their names.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
    	Task t2 = (Task) o2;
    	return _name.compareTo(t2._name);
    }
    
    /**
     * Executes the Task.
     */
    protected abstract void execute(TaskContext ctx);
}