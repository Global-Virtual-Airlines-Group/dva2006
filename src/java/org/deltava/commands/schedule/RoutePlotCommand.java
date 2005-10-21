// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Collections;

import org.deltava.commands.*;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RoutePlotCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {

      // Generate empty list for JSP
      ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/schedule/routePlot.jsp");
      result.setSuccess(true);
   }
}