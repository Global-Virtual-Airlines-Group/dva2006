// Copyright 2005, 2006, 2007, 2009, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import com.newrelic.api.agent.NewRelic;

/**
 * A class to control execution of Scheduled Tasks. This operates much like a Unix-style cron daemon in that it checks whether a task should be executed once every 60 seconds.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class TaskScheduler implements Runnable, Thread.UncaughtExceptionHandler {

	private static final Logger log = LogManager.getLogger(TaskScheduler.class);

	private final Map<String, Task> _tasks = new HashMap<String, Task>();

	/**
	 * Initializes the Task scheduler with a group of Tasks.
	 * @param tasks a Collection of Tasks
	 */
	public TaskScheduler(Collection<Task> tasks) {
		super();
		tasks.forEach(t -> addTask(t));
	}
	
	@Override
	public String toString() {
		return SystemData.get("airline.code") + " Task Scheduler";
	}

	/**
	 * Schedules a Task for execution.
	 * @param t the Task to schedule
	 */
	public void addTask(Task t) {
		_tasks.put(t.getID(), t);
	}

	/**
	 * Retrieves a Task for manual execution.
	 * @param id the Task ID
	 * @return the Task, or null if not found
	 */
	public Task getTask(String id) {
		return _tasks.get(id);
	}

	@Override
	public final void run() {
		log.info("Starting");

		// Sleep for a while when we start
		ThreadUtils.sleep(10000);
		while (!Thread.currentThread().isInterrupted()) {
			TaskTimer ttm = new TaskTimer();

			// Check each task
			for (Task t : _tasks.values()) {
				log.debug("Checking {}", t.getName());

				// If the task is running, leave it alone. Only execute when it's supposed to
				if (t.isRunnable()) {
					Thread tt = new Thread(t, t.getName());
					tt.setDaemon(true);
					tt.setUncaughtExceptionHandler(this);
					tt.start();
					ThreadUtils.waitFor(tt, 5000);
				}
			}

			// Sleep until the next invocation
			try {
				Thread.sleep(Math.max(50, 60000 - ttm.stop()));	
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		log.info("Stopping");
	}

	/**
	 * Updates the last execution time and duration of a Scheduled Task.
	 * @param lr the TaskLastRun bean
	 */
	public void setLastRunTime(TaskLastRun lr) {
		Task t = _tasks.get(lr.getName());
		if (t != null) {
			t.setStartTime(lr.getLastRun());
			t.setLastExecTime(lr.getExecTime());
		}
	}

	/**
	 * Returns the current state of the Task Schedulder.
	 * @return a Collection of TaskInfo beans
	 */
	public Collection<TaskInfo> getTaskInfo() {
		Collection<Task> tasks = new TreeSet<Task>(_tasks.values());
		return tasks.stream().map(TaskInfo::new).collect(Collectors.toList());
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.atError().withThrowable(e).log("Uncaught Exception in {}", t.getName());
		NewRelic.noticeError(e, false);
	}
}