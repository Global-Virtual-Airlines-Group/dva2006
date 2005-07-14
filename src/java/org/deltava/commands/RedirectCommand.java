// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands;

import org.deltava.util.redirect.*;

/**
 * An internal Web Site Command to preserve request state across HTTP redirects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RedirectCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Restore the current session state
      String url;
      try {
         url = RequestStateHelper.restore(ctx.getRequest());
      } catch (IllegalStateException ise) {
         throw new CommandException(ise.getMessage());
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL(url);
      result.setSuccess(true);
   }
}