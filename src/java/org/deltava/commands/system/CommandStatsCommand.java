// Copyright 2005, 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display Web Site Command statistics.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CommandStatsCommand extends AbstractCommand {
   
	private static final String[] SORT_CODES = new String[] {"NAME", "AVGT DESC", "BE DESC", "MAXTOTAL DESC", "TC DESC", "SC DESC"};
	private static final List<?> SORT_OPTS = ComboUtils.fromArray(new String[] {"Command Name", "Average Time", "Database Time", "Maximum Time", "Frequency", "Success"}, SORT_CODES); 

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   @Override
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the sort column
      String sortBy = ctx.getParameter("sortBy");
      if (StringUtils.arrayIndexOf(SORT_CODES, sortBy) == -1)
         sortBy = SORT_CODES[0];

      try {
         GetSystemData dao = new GetSystemData(ctx.getConnection());
         ctx.setAttribute("stats", dao.getCommandStats(sortBy), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Save sort options
      ctx.setAttribute("sortType", sortBy, REQUEST);
      ctx.setAttribute("sortOptions", SORT_OPTS, REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/admin/cmdStats.jsp");
      result.setSuccess(true);
   }
}