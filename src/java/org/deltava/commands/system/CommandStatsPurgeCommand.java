// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to purge the Command log.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandStatsPurgeCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the number of days to purge
      int days = 100;
      try {
         days = Integer.parseInt(ctx.getParameter("purgeDays"));
      } catch (NumberFormatException nfe) { }

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and purge the log
         SetSystemData wdao = new SetSystemData(con);
         int rowsPurged = wdao.purge("COMMANDS", "CMDDATE", days);
         
         // Log message
         ctx.setMessage(String.valueOf(rowsPurged) + " command entries purged");
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the Command statistics
      CommandResult result = ctx.getResult();
      result.setType(ResultType.REDIRECT);
      result.setURL("cmdstats", null, null);
      result.setSuccess(true);
   }
}