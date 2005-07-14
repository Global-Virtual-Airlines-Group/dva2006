// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.security;

import javax.servlet.http.HttpSession;

import org.deltava.commands.*;

/**
 * A web site command to ensure that session cookies are set correctly.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CookieCheckCommand extends AbstractCommand {

    /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs.
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get the comamnd result
        CommandResult result = ctx.getResult();
        
        // Check if our session cookie is OK
        boolean isOK = (ctx.getRequest().isRequestedSessionIdFromCookie() &&
                ctx.getRequest().isRequestedSessionIdValid());

        // If we're not OK, redirect to the warning JSP
        if (!isOK) {
            result.setURL("/jsp/cookieCheck.jsp");
            return;
        }
        
        // Get the next resource to go to
        HttpSession s = ctx.getSession();
        String nextURL = (String) s.getAttribute("next_url");
        s.removeAttribute("next_url");
        
        // Redirect to the next URL
        result.setURL(nextURL);
        result.setType(CommandResult.REDIRECT);
        result.setSuccess(true);
    }
}