// Copyright 2007, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import org.deltava.commands.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display the ACARS dispatch map.
 * @author Luke
 * @version 3.4
 * @since 2.1
 */

public class MapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're getting this from a pilot or dispatch client
		ctx.setAttribute("isDispatch", Boolean.valueOf(ctx.getParameter("dispatchClient")), REQUEST);
		
		// Check the Google Maps API version requested
		int apiVersion = StringUtils.parse(ctx.getParameter("api"), 2);
		ctx.setAttribute("mapAPIVersion", Integer.valueOf(apiVersion), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/dispatchMapV" + String.valueOf(apiVersion) + ".jsp");
		result.setSuccess(true);
	}
}