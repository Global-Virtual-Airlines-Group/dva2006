//Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.tasks;

import java.io.Serializable;

import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletContext;

/**
 * A class to support scheduled tasks.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class Task extends Thread implements Serializable {

    public static final long MAX_RUNTIME = 30 * 60 * 1000; // 30 minutes
    
    protected ServletContext _ctxt;
    protected Properties _props;
    
    private boolean _enabled;
    
    private long _maxRunTime = Task.MAX_RUNTIME;
    private long _startTime;
    private long _lastRunTime;
    
    /**
     * Creates a new Scheduled Task with a given name. 
     * @param name the Task name
     */
    public Task(String name) {
        super(name);
        _props = new Properties();
    }
    
    /**
     * Returns how the last execution duration of this Task.
     * @return the last execution duration in milliseconds, or 0 if the Task has never been executed
     */
    public long getLastRunTime() {
        return _lastRunTime;
    }
    
    public boolean getEnabled() {
    	return _enabled;
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
     * Sets the Servlet Context for this Task.
     * @param ctx the Servlet Context
     */
    public void setContext(ServletContext ctx) {
        _ctxt = ctx;
    }
    
    /**
     * Marks the Task as Enabled.
     * @param isEnabled TRUE if the Task is enabled, otherwise FALSE
     */
    public void setEnabled(boolean isEnabled) {
    	_enabled = isEnabled;
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
     * Executes the Task. This logs execution start/stop times and calls each Task implementation's
     * {@link Task#execute()} method.
     */
    public final void run() {
        _startTime = System.currentTimeMillis();
        
        // Only execute if the task is enabled
        if (_enabled)
        	execute();
        
        _lastRunTime = (System.currentTimeMillis() - _startTime);
    }
    
    /**
     * Executes the Task.
     */
    protected abstract void execute();
}