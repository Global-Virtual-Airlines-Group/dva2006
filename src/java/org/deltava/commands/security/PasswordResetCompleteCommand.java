// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.security;

import javax.servlet.http.HttpSession;

import org.deltava.commands.*;

/**
 * A Web Site Command to log Password Reset Completion.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PasswordResetCompleteCommand extends AbstractCommand {

   /**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {

      // If we're not authenticated, clear the temporary session
      if (!ctx.isAuthenticated()) {
         HttpSession s = ctx.getSession();
         if (s != null)
            s.invalidate();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/pilot/passwordResetComplete.jsp");
      result.setSuccess(true);
   }
}