// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetPilotRecognition;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display Pilots eligible for Promotion.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PromotionQueueCommand extends AbstractCommand {

	/**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Promotion Queue
         GetPilotRecognition dao = new GetPilotRecognition(con);
         ctx.setAttribute("queue", dao.getPromotionQueue(), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/roster/promotionQueue.jsp");
      result.setSuccess(true);
   }
}