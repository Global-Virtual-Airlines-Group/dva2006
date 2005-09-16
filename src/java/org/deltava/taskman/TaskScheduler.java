// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.jdbc.ConnectionPool;
import org.deltava.jdbc.ConnectionPoolException;

import org.deltava.dao.SetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A class to control execution of Scheduled Tasks.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

// TODO Describe this class better
public class TaskScheduler extends Thread {

	private static final Logger log = Logger.getLogger(TaskScheduler.class);

	private Map _tasks = new HashMap();
	private int _interval = 60;
	private long _taskCheckCount;
	
	private ConnectionPool _pool;

	/**
	 * Initializes the Task Scheduler.
	 */
	public TaskScheduler() {
		super("Task Scheduler");
		setDaemon(true);
	}

	/**
	 * Initializes the Task scheduler with a group of Tasks.
	 * @param tasks a Collection of Tasks
	 */
	public TaskScheduler(Collection tasks) {
		this();
		for (Iterator i = tasks.iterator(); i.hasNext();)
			addTask((Task) i.next());
	}

	/**
	 * Schedules a Task for execution.
	 * @param t the Task to schedule
	 */
	public void addTask(Task t) {
		_tasks.put(t.getID(), t);
	}

	/**
	 * Updates the task pool check interval.
	 * @param interval the interval in seconds
	 * @throws IllegalArgumentException if interval is negative
	 */
	public void setInterval(int interval) {
		if (interval < 0)
			throw new IllegalArgumentException("Invalid check interval - " + interval);

		_interval = interval;
	}

	/**
	 * Retrieves a Task for manual execution.
	 * @param id the Task ID
	 * @return the Task, or null if not found
	 */
	public Task getTask(String id) {
		return (Task) _tasks.get(id);
	}

	/**
	 * Executes the Task Manager.
	 */
	public final void run() {
		log.info("Starting");
		
		// Get the JDBC Connection pool
		_pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		
		// Sleep for a while when we start
		ThreadUtils.sleep(10000);

		// Check loop
		while (!isInterrupted()) {
			log.debug("Checking Task Pool");
			_taskCheckCount++;

			for (Iterator i = _tasks.values().iterator(); i.hasNext();) {
				Task t = (Task) i.next();
				log.debug("Checking " + t.getName());

				// If the task is running, leave it alone. Only execute when it's supposed to
				Calendar now = Calendar.getInstance();
				if (t.isAlive()) {
					long execTime = System.currentTimeMillis() - t.getStartTime().getTime();

					// Kill zombie tasks
					if (execTime > t.getMaxRunTime()) {
						log.warn("Killing Zombie Task - executing for " + execTime + "ms, max=" + t.getMaxRunTime() + "ms");
						try {
							t.join(500);
						} catch (Exception e) { }
					} else {
						log.debug("Task executing since " + t.getStartTime());	
					}
				} else if (t.getNextStartTime().after(now.getTime())) {
					log.debug("Task not scheduled to execute until " + t.getNextStartTime());
				} else if (!t.getEnabled()) {
					log.debug("Task Disabled");
				} else if (t.isRunHour(now.get(Calendar.HOUR_OF_DAY))) {
					log.info(t.getName() + " queued for execution - next run time = " + t.getNextStartTime());
					
					// Log Execution date/time
					Connection c = null;
					try {
					   c = _pool.getConnection(true);
					   
					   // Get the DAO and log the execution time
					   SetSystemData dao = new SetSystemData(c);
					   dao.logTaskExecution(t.getID());
					} catch (DAOException de) {
					   log.error("Cannot save execution time", de);
					} finally {
					   _pool.release(c);
					}

					// Pass JDBC Connection to database tasks
					if (t instanceof DatabaseTask) {
						DatabaseTask dt = (DatabaseTask) t;
						dt.setRecycler(_pool);
						try {
							dt.setConnection(_pool.getConnection());
						} catch (ConnectionPoolException cpe) {
							log.error("Error reserving connection - " + cpe.getMessage());
						}
					}
					
					// Start the task
					log.info(t.getName() + " started");
					t.start();
				}
			}

			// Sleep until the next invocation
			try {
				Thread.sleep(_interval * 1000);
			} catch (InterruptedException ie) {
				log.debug("Interrupted");
				interrupt();
			}
		}

		log.info("Stopping");
	}
	
	/**
	 * Updates the last execution time of a Scheduled Task.
	 * @param lr the TaskLastRun bean
	 */
	public void setLastRunTime(TaskLastRun lr) {
	   Task t = (Task) _tasks.get(lr.getName());
	   if (t != null)
	      t.setStartTime(lr.getLastRun());
	}

	/**
	 * Returns the current state of the Task Schedulder.
	 * @return a Collection of TaskInfo beans
	 */
	public Collection getTaskInfo() {

		List results = new ArrayList();
		for (Iterator i = _tasks.values().iterator(); i.hasNext();) {
			Task t = (Task) i.next();
			results.add(new TaskInfo(t));
		}

		return results;
	}
}