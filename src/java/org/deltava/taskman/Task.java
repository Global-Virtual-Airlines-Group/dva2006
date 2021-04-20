// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.dao.*;
import org.deltava.util.*;
import org.deltava.util.log.*;
import org.deltava.util.system.SystemData;

import com.newrelic.api.agent.*;

/**
 * A class to support Scheduled Tasks. Scheduled Tasks are similar to UNIX cron jobs, and are scheduled for
 * execution in much the same way.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public abstract class Task implements Runnable, Comparable<Task>, Thread.UncaughtExceptionHandler {
	
	/**
	 * Time interval options.
	 */
	public static final String[] TIME_OPTS = {"min", "hour", "mday", "month", "wday"};
	private static final ChronoField[] TIME_FIELDS = {ChronoField.MINUTE_OF_HOUR, ChronoField.HOUR_OF_DAY, ChronoField.DAY_OF_MONTH, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_WEEK};
	
	/**
	 * Wildcard for &quot;All Intervals&quot;
	 */
	static final Integer ANY = Integer.valueOf(-1);
	private static final String ALL_TIMES = "*";

    protected final Logger log;
    
    private String _id;
    private final String _name;
    
    private long _lastRunTime;
    private Instant _lastStartTime;
    
    private boolean _enabled;
    private int _runCount;
    
    private final Map<String, Collection<Integer>> _runTimes = new HashMap<String, Collection<Integer>>();
    
    /**
     * Creates a new Scheduled Task with a given class name.
     * @param name the task name 
     * @param loggerClass the logger Class for Log4J
     */
    protected Task(String name, Class<?> loggerClass) {
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
    public Instant getStartTime() {
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
     * Returns whether the Scheduled Task can be executed at the present time.
     * @return TRUE if the Task can be executed, otherwise FALSE
     * @see Task#isRunnable(Instant)
     * @see Task#setRunTimes(String, String)
     */
    public boolean isRunnable() {
    	return isRunnable(Instant.now());
    }
    
    /**
     * Determines whether this Scheduled Task is runnable at a particular date/time. <i>This is package private
     * for unit testing purposes.</i>
     * @param dt the date/time to execute the task at
     * @return TRUE if the Task can be executed, otherwise FALSE
     * @see Task#isRunnable()
     * @see Task#setRunTimes(String, String)
     */
    boolean isRunnable(Instant dt) {
    	if (!_enabled || _runTimes.isEmpty())
    		return false;
    	
    	// Check the time options
    	ZonedDateTime zdt = ZonedDateTime.ofInstant(dt, ZoneOffset.UTC);
    	for (int x = 0; x < TIME_OPTS.length; x++) {
    		Collection<Integer> runTimes = _runTimes.get(TIME_OPTS[x]);
    		int timeField = zdt.get(TIME_FIELDS[x]);
    		if (log.isDebugEnabled())
    			log.debug(TIME_OPTS[x] + ", now = " + timeField + ", allowed = " + runTimes);
    		
    		// Make sure we qualify to run
    		if ((runTimes != null) && (!runTimes.contains(ANY)) && (!runTimes.contains(Integer.valueOf(timeField))))
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
    		intervals = new TreeSet<Integer>();
    		_runTimes.put(intervalType, intervals);
    	} else
    		intervals.clear();
    	
    	// Split the values
    	if (ALL_TIMES.equals(values))
    		intervals.add(ANY);
    	else if (values.startsWith("*/")) {
    		int interval = StringUtils.parse(values.substring(2), 0);
    		if (interval != 0) {
    			for (int m = 0; m < 60; m++) {
    				if ((m % interval) == 0)
    					intervals.add(Integer.valueOf(m));
    			}
    		}
    	} else {
    		for (Iterator<String> i = StringUtils.split(values, ",").iterator(); i.hasNext(); ) {
    			String value = i.next();
    			int v = StringUtils.parse(value, -1);
    			if (v != -1)
    				intervals.add(Integer.valueOf(v));
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
    public void setStartTime(Instant dt) {
       if (dt != null)
          _lastStartTime = dt;
    }
    
    /**
     * Overrides the last execution duration for this Task.
     * @param execTime the last execution duration in milliseconds
     * @see Task#getLastRunTime()
     */
    public void setLastExecTime(long execTime) {
    	_lastRunTime = Math.max(0, execTime);
    }
    
    /**
     * Executes the Task. This logs execution start/stop times and calls each Task implementation's
     * {@link Task#execute(TaskContext)} method.
     */
    @Override
    public void run() {
    	run(null);
    }
    
    /**
     * Executes the Task. This logs execution start/stop times and calls each Task implementation's
     * {@link Task#execute(TaskContext)} method.
     * @param usr overrides the user executing the Task if not null
     */
    @Trace(dispatcher=true)
    public void run(Pilot usr) {
    	setStartTime(Instant.now());
    	_runCount++;
    	log.info(_name + " starting ");
    	
    	TaskContext ctxt = new TaskContext();
    	try {
    		Connection con = ctxt.getConnection();
    		
			// Load the author and last run
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			GetSystemData sddao = new GetSystemData(con);
			ctxt.setUser((usr == null) ? pdao.getByCode(SystemData.get("users.tasks_by")) : usr);
			ctxt.setLastRun(sddao.getLastRun(_id));
			
	    	// Log task starting
    		SetSystemData dao = new SetSystemData(con);
    		dao.logTaskExecution(getID(), 0);
    	} catch (Exception e) {
    		log.error("Cannot log Task start - " + e.getMessage(), e);
    	} finally {
    		ctxt.release();
    	}

    	// Execute the task
        execute(ctxt);
        _lastRunTime = (System.currentTimeMillis() - _lastStartTime.toEpochMilli());
        log.info(getName() + " completed - " + _lastRunTime + " ms");
        NewRelic.setRequestAndResponse(new SyntheticRequest(_name, (usr == null) ? "SYSTEM" : usr.getPilotCode()), new SyntheticResponse());
        NewRelic.setTransactionName("Task", _name);
        NewRelic.recordResponseTimeMetric(_name, _lastRunTime);
        
        // Log execution time
    	try {
    		SetSystemData dao = new SetSystemData(ctxt.getConnection());
    		dao.logTaskExecution(getID(), _lastRunTime);
    	} catch (Exception e) {
    		log.error("Cannot log Task completion - " + e.getMessage(), e);
    	} finally {
    		ctxt.release();
    	}
    }
    
    @Override
    public int compareTo(Task t2) {
    	return _name.compareTo(t2._name);
    }
    
    @Override
    public int hashCode() {
    	return _name.hashCode();
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
    	log.error("Error in child thread " + t.getName(), e);
    }
    
    /**
     * Executes the Task.
     * @param ctx the TaskContext
     */
    protected abstract void execute(TaskContext ctx);
}