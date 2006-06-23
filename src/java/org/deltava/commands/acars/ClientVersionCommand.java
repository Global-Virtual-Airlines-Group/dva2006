// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import org.deltava.commands.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to control minimum ACARS client versions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ClientVersionCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Ensure ACARS is enabled
		if (!SystemData.getBoolean("acars.enabled"))
			throw notFoundException("ACARS Server not enabled");

		// Get Command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("minBuild") == null) {
			result.setURL("/jsp/acars/clientVersion.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the minimum/latest client builds
		try {
			int minBuild = Integer.parseInt(ctx.getParameter("minBuild"));
			int latestBuild = Integer.parseInt(ctx.getParameter("latestBuild"));
			
			// Update the system model
			SystemData.add("acars.build.minimum", new Integer(minBuild));
			SystemData.add("acars.build.latest", new Integer(latestBuild));
		} catch (NumberFormatException nfe) {
			ctx.setMessage(nfe.getMessage());
		}
		
		// Return to success page
		ctx.setMessage("Client Versions updated");
		result.setURL("/jsp/acars/clientVersion.jsp");
		result.setSuccess(true);
	}
}