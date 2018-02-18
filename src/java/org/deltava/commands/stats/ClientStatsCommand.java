// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;

import org.deltava.commands.*;

/**
 * A Web Site Command to display ACARS build statistics.
 * @author Luke
 * @version 8.2
 * @since 8.2
 */

public class ClientStatsCommand extends AbstractCommand {
	
	private static final List<String> WEEKS = List.of("8", "12", "26", "52", "78", "104", "156");

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Save options
		ctx.setAttribute("weeks", WEEKS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/acarsBuildStats.jsp");
		result.setSuccess(true);
	}
}