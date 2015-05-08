// Copyright 2007, 2008, 2010, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import org.deltava.commands.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display the ACARS dispatch map.
 * @author Luke
 * @version 6.0
 * @since 2.1
 */

public class MapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're getting this from a pilot or dispatch client
		ctx.setAttribute("isDispatch", Boolean.valueOf(ctx.getParameter("dispatchClient")), REQUEST);
		
		// Check the Google Maps API version requested
		int apiVersion = Math.max(3, StringUtils.parse(ctx.getParameter("api"), 3));
		ctx.setAttribute("mapAPIVersion", Integer.valueOf(apiVersion), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		ctx.setExpiry(0);
		result.setURL("/jsp/acars/dispatchMapV" + String.valueOf(apiVersion) + ".jsp");
		result.setSuccess(true);
	}
}