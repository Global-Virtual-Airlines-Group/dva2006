// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.net.*;

import org.deltava.commands.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to clear a user's Water Cooler thread unread marks. 
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class UnreadClearCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command result
		CommandResult result = ctx.getResult();
		
		// Update a lastread attribute
		ctx.setAttribute(CommandContext.THREADREADOV_ATTR_NAME, new Date(), SESSION);
		
		// Clear unread marks
		Map<?, ?> threadIDs = (Map<?, ?>) ctx.getSession().getAttribute(CommandContext.THREADREAD_ATTR_NAME);
		if (threadIDs != null)
			threadIDs.clear();
		
		// Determine where we are referring from, if on the site return back there
		String referer = ctx.getRequest().getHeader("Referer");
		if (!StringUtils.isEmpty(referer) && (!referer.contains("login"))) {
			try {
				URL url = new URL(referer);
				if (SystemData.get("airline.url").equalsIgnoreCase(url.getHost()))
					result.setURL(referer);
			} catch (MalformedURLException mue) {
				// empty
			}
		}
		
		// Return to the previous page
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}