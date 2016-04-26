// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import org.deltava.util.redirect.*;

import org.apache.log4j.Logger;

/**
 * An internal Web Site Command to preserve request state across HTTP redirects.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RedirectCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(RedirectCommand.class);

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
	@Override
   public void execute(CommandContext ctx) throws CommandException {
	   
	   // Get the command results
	   CommandResult result = ctx.getResult();

      // Restore the current session state
      try {
         result.setURL(RequestStateHelper.restore(ctx.getRequest()));
      } catch (IllegalStateException ise) {
    	  String referer = ctx.getRequest().getHeader("Referer");
    	  log.warn("No HTTP Session redirecting from " + referer);
    	  ctx.setAttribute("referer", referer, REQUEST);
    	  result.setURL("/jsp/error/redirectError.jsp");
      }
      
      // Forward to the JSP
      result.setSuccess(true);
   }
}