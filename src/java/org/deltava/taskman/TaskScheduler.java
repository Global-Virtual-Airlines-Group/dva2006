// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.jdbc.ConnectionPool;
import org.deltava.jdbc.ConnectionPoolException;

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

		// Check loop
		while (!isInterrupted()) {
			log.debug("Checking Task Pool");
			_taskCheckCount++;

			for (Iterator i = _tasks.values().iterator(); i.hasNext();) {
				Task t = (Task) i.next();
				log.debug("Checking " + t.getName());

				// If the task is running, leave it alone. Only execute when it's supposed to
				Date now = new Date();
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
				} else if (t.getNextStartTime().after(now)) {
					log.debug("Task not scheduled to execute until " + t.getNextStartTime());
				} else if (!t.getEnabled()) {
					log.debug("Task Disabled");
				} else {
					log.info(t.getName() + " queued for execution");

					// Pass JDBC Connection to database tasks
					// TODO How do we get the connection back
					if (t instanceof DatabaseTask) {
						DatabaseTask dt = (DatabaseTask) t;
						ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
						try {
							dt.setConnection(pool.getConnection());
							t.start();
						} catch (ConnectionPoolException cpe) {
							log.error("Error reserving connection - " + cpe.getMessage());
						}
					} else {
						t.start();
					}
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
	 * Returns the current state of the Task Schedulder.
	 * @return a Collection of TaskInfo beans
	 */
	public Collection getTaskInfo() {

		Set results = new TreeSet();
		for (Iterator i = _tasks.values().iterator(); i.hasNext();) {
			Task t = (Task) i.next();
			results.add(new TaskInfo(t));
		}

		return results;
	}
}