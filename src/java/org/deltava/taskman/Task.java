//Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;

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
    private Calendar _startTime = Calendar.getInstance();
    private Calendar _nextStartTime = Calendar.getInstance();
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
       return _nextStartTime.getTime(); 
    }
    
    /**
     * Returns the when this Task was last started on.
     * @return the date/time the Task was last started, or null if the Task has never been executed
     */
    public Date getStartTime() {
        return _startTime.getTime();
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
    
    /**
     * Returns if the Task is allowed to be executed.
     * @return TRUE if the Task can be executed, otherwise FALSE
     * @see Task#setEnabled(boolean)
     */
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
    
    /**
     * Marks the Task as enabled to execute.
     * @param enabled TRUE if the task is enabled, otherwise FALES
     * @see Task#getEnabled()
     */
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
     * Overrides the last execution time for this Task.
     * @param dt the date/time this Task last executed
     * @see Task#getStartTime()
     * @see Task#getNextStartTime()
     */
    public void setStartTime(Date dt) {
       if (dt != null) {
          _startTime.setTime(dt);
          _nextStartTime.setTimeInMillis(dt.getTime() + _interval);
       }
    }
    
    /**
     * Executes the Task. This logs execution start/stop times and calls each Task implementation's
     * {@link Task#execute()} method.
     */
    public void run() {
        setStartTime(new Date());
        execute();
        _lastRunTime = (System.currentTimeMillis() - _startTime.getTimeInMillis());
    }
    
    /**
     * Executes the Task.
     */
    protected abstract void execute();
}