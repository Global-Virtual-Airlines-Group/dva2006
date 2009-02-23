// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;

/**
 * A Web Site Command to log off the user. If a superuser is currently impersonating a user, then
 * the impersonation will end and the user will revert back to their true credentials.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class LogoutCommand extends AbstractCommand {
   
    /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs.
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get the session
        HttpSession s = ctx.getSession();
        if (s != null) {
        	// Check if we're impersonating a user
        	Pilot suUsr = (Pilot) s.getAttribute(HTTPContext.SU_ATTR_NAME);
            if (suUsr != null) {
            	ctx.setAttribute(HTTPContext.USER_ATTR_NAME, suUsr, SESSION);
            	ctx.getSession().removeAttribute(HTTPContext.SU_ATTR_NAME);
            } else {
            	s.invalidate();
            	
                // Clear the security cookie
                Cookie c = new Cookie(CommandContext.AUTH_COOKIE_NAME, "");
                c.setMaxAge(1);
                ctx.getResponse().addCookie(c);
            }
        }
            
        // Mark the command complete
        CommandResult result = ctx.getResult();
        result.setURL("home.do");
        result.setType(ResultType.REDIRECT);
        result.setSuccess(true);
    }
}