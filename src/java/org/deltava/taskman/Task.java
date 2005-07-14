//Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.util.Date;
import java.util.Properties;

/**
 * A class to support scheduled tasks.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class Task extends Thread implements java.io.Serializable {

    public static final long MAX_RUNTIME = 30 * 60 * 1000; // 30 minutes
    
    protected String _id;
    protected Properties _props;
    private boolean _enabled;
    
    private long _maxRunTime = Task.MAX_RUNTIME;
    private long _startTime;
    private long _lastRunTime;
    private int _interval;
    
    /**
     * Creates a new Scheduled Task with a given name. 
     * @param name the Task name
     */
    public Task(String name) {
        super(name);
        setDaemon(true);
        _props = new Properties();
    }
    
    /**
     * Returns the Task ID.
     * @return the ID
     */
    public String getID() {
       return _id;
    }
    
    /**
     * Returns how the last execution duration of this Task.
     * @return the last execution duration in milliseconds, or 0 if the Task has never been executed
     */
    public long getLastRunTime() {
        return _lastRunTime;
    }
    
    /**
     * Returns the next execution time for this Task.
     * @return the date/time this Task is scheduled for execution
     */
    public Date getNextStartTime() {
       return (_startTime == 0) ? new Date() : new Date(_startTime + (_interval * 1000)); 
    }
    
    /**
     * Returns the when this Task was last started on.
     * @return the date/time the Task was last started, or null if the Task has never been executed
     */
    public Date getStartTime() {
        return (_startTime == 0) ? null : new Date(_startTime);
    }

    /**
     * Returns the maximum execution time for this Task. 
     * @return the maximum execution time in milliseconds
     * @see Task#setMaxRunTime(long)
     */
    public long getMaxRunTime() {
        return _maxRunTime;
    }
    
    /**
     * Returns the interval between executions of this Task.
     * @return the interval in seconds
     */
    public int getInterval() {
       return _interval;
    }
    
    public boolean getEnabled() {
       return _enabled;
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
     * Sets a Task's configuration property.
     * @param pName the property name
     * @param pValue the property values
     */
    public void setProperty(String pName, String pValue) {
        _props.setProperty(pName, pValue);
    }

    /**
     * Updates the maximum execution time for this Task. If the Task runs longer than this period of time, the
     * Task controller may interrupt the thread without warning.
     * @param maxTime the maximum execution time in milliseconds
     * @throws IllegalArgumentException if maxTime is negative
     */
    public void setMaxRunTime(long maxTime) {
        if ((maxTime < 0) || (maxTime > MAX_RUNTIME))
            throw new IllegalArgumentException("Invalid Maximum Runtime - " + maxTime);
        
       _maxRunTime = maxTime; 
    }
    
    public void setEnabled(boolean enabled) {
       _enabled = enabled;
    }
    
    /**
     * Updates the execution interval for this Task.
     * @param interval the interval in seconds
     * @throws IllegalArgumentException if interval is negative
     */
    public void setInterval(int interval) {
       if (interval < 0)
          throw new IllegalArgumentException("Invalid execution interval - " + interval);
       
       _interval = interval;
    }
    
    /**
     * Executes the Task. This logs execution start/stop times and calls each Task implementation's
     * {@link Task#execute()} method.
     */
    public void run() {
        _startTime = System.currentTimeMillis();
        execute();
        _lastRunTime = (System.currentTimeMillis() - _startTime);
    }
    
    /**
     * Executes the Task.
     */
    protected abstract void execute();
}