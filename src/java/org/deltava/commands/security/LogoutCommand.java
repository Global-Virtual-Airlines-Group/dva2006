package org.deltava.commands.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.deltava.commands.*;

/**
 * A Web Site Command to log off the user.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LogoutCommand extends AbstractCommand {
   
    /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs.
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Clear the security cookie
        Cookie c = new Cookie(CommandContext.AUTH_COOKIE_NAME, "");
        c.setMaxAge(1);
        ctx.getResponse().addCookie(c);
        
        // Invalidate the session
        HttpSession s = ctx.getSession();
        if (s != null)
            s.invalidate();
        
        // Mark the command complete
        CommandResult result = ctx.getResult();
        result.setURL("home.do");
        result.setType(CommandResult.REDIRECT);
        result.setSuccess(true);
    }
}