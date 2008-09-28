// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import javax.servlet.http.*;

import org.deltava.commands.*;

/**
 * A Web Site Command to ensure that session cookies are set correctly.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CookieCheckCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the comamnd result
		CommandResult result = ctx.getResult();

		// Check if our session cookie is OK
		HttpServletRequest req = ctx.getRequest();
		boolean isOK = (req.isRequestedSessionIdFromCookie() && req.isRequestedSessionIdValid());

		// If we're not OK, redirect to the warning JSP
		if (!isOK) {
			result.setURL("/jsp/error/cookieCheck.jsp");
			return;
		}

		// Get the next resource to go to
		HttpSession s = ctx.getSession();
		String nextURL = (String) s.getAttribute("next_url");
		s.removeAttribute("next_url");

		// Check for invalid address bean
		if (s.getAttribute("addr") != null) {
			ctx.setAttribute("addr", s.getAttribute("addr"), REQUEST);
			s.removeAttribute("addr");
			result.setType(ResultType.REQREDIRECT);
		} else
			result.setType(ResultType.REDIRECT);

		// Redirect to the next URL
		result.setURL(nextURL);
		result.setSuccess(true);
	}
}