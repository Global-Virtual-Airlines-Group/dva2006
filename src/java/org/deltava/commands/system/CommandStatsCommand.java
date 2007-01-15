// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display Web Site Command statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandStatsCommand extends AbstractCommand {
   
   private static final List sortOptions = ComboUtils.fromArray(new String[] {"Command Name", "Average Time",
         "Database Time", "Maximum Time", "Frequency", "Success"}, new String[] {"NAME", "AVGT DESC", "BE DESC",
         "MAXTOTAL DESC", "TC DESC", "SC DESC"});

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the sort column
      String sortBy = ctx.getParameter("sortBy");
      if (sortBy == null)
         sortBy = "NAME";

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the statistics
         GetSystemData dao = new GetSystemData(con);
         ctx.setAttribute("stats", dao.getCommandStats(sortBy), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Save sort options
      ctx.setAttribute("sortType", sortBy, REQUEST);
      ctx.setAttribute("sortOptions", sortOptions, REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/admin/cmdStats.jsp");
      result.setSuccess(true);
   }
}