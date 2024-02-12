// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import static org.deltava.commands.CommandContext.AUTH_COOKIE_NAME;

import javax.servlet.http.*;

import org.deltava.commands.*;
import org.deltava.security.*;

/**
 * A Web Site Command to display decoded security cookie data.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class CookieDataCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the CommandContext
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the decoded version
		HttpSession s = ctx.getRequest().getSession(true);
		SecurityCookieData cd = (SecurityCookieData) s.getAttribute(AUTH_COOKIE_NAME);
		ctx.setAttribute("cd", cd, REQUEST);
		
		// Decode the auth cookie if found
		Cookie c = ctx.getCookie(CommandContext.AUTH_COOKIE_NAME);
		if (c != null) {
			try {
				SecurityCookieData cd2 = SecurityCookieGenerator.readCookie(c.getValue());
				ctx.setAttribute("cd2", cd2, REQUEST);
			} catch (SecurityException se) {
				ctx.setAttribute("ex", se, REQUEST);
			}
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/cookieData.jsp");
		result.setSuccess(true);
	}
}