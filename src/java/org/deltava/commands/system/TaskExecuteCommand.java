// Copyright 2005, 2006, 2011, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.apache.logging.log4j.*;
import org.deltava.commands.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manually execute Scheduled Tasks. Unlike the Task Scheduler, which spawns a new Thread to
 * execute a Scheduled Task, this Command will execute the Task using the same Thread.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class TaskExecuteCommand extends AbstractCommand {

	private static final Logger log = LogManager.getLogger(TaskExecuteCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Task Scheduler and the Task
		String taskID = (String) ctx.getCmdParameter(Command.ID, null);
		TaskScheduler tSched = (TaskScheduler) SystemData.getObject(SystemData.TASK_POOL);
		Task t = tSched.getTask(taskID);
		if (t == null)
			throw notFoundException("Invalid Scheduled Task - " + taskID);

		try {
			t.run(ctx.getUser());
		} catch (RuntimeException rte) {
			Throwable tc = rte.getCause();
			if (tc != null)
			   log.error("Scheduled Task threw {} - {}", tc.getClass().getName(), tc.getMessage());
			
			ctx.setAttribute("ex", (tc == null) ? rte : tc, REQUEST);
		} catch (Exception e) {
			log.error("Scheduled Task threw {} - {}", e.getClass().getName(), e.getMessage());
			ctx.setAttribute("ex", e, REQUEST);
		}

		// Save the task info in the request
		ctx.setAttribute("task", new TaskInfo(t), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/taskExecute.jsp");
		result.setSuccess(true);
	}
}