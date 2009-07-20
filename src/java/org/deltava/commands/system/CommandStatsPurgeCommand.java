// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to purge the Command log.
 * @author Luke
 * @version 2.6
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
      int days = StringUtils.parse(ctx.getParameter("purgeDays"), 100);
      try {
         SetSystemData wdao = new SetSystemData(ctx.getConnection());
         wdao.purge("COMMANDS", "CMDDATE", days);
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