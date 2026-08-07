// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import org.deltava.commands.*;

import org.deltava.tasks.AVStackDownloadTask;

/**
 * A Web Site Command to manually pull AviationStack schedule data.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AVStackDownloadCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/avStackImport.jsp");
		if (!Boolean.parseBoolean(ctx.getParameter("doImport"))) {
			result.setSuccess(true);
			return;
		}
		
		// Create the task in a runnable
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				AVStackDownloadTask t = new AVStackDownloadTask();
				t.run(ctx.getUser());
			}
		};
		
		// Execute the task
		Thread t = Thread.ofVirtual().name("AVStack Download").unstarted(r);
		t.start();
		
		// Go to the JSP
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		result.setSuccess(true);
	}
}