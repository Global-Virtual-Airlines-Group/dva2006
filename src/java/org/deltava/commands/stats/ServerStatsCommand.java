// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display HTTP Server totals.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ServerStatsCommand extends AbstractViewCommand {

   private static final String[] SORT_NAMES = { "Date", "Total Hits", "Server Time", "Home Page Hits", "Bandwidth" };
   private static final String[] SORT_CODE = { "DATE DESC", "REQUESTS DESC", "EXECTIME DESC", "HOMEHITS DESC",
         "BANDWIDTH DESC" };
   private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODE);

   /**
    * Execute the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs.
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view context
      ViewContext vc = initView(ctx);
      if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
		   vc.setSortType(SORT_CODE[0]);

      try {
         Connection con = ctx.getConnection();

         // Get the DAO
         GetSystemData dao = new GetSystemData(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());

         // Get the statistics
         vc.setResults(dao.getHTTPStats(vc.getSortType()));
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Save the sorter types in the request
      ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/stats/httpStats.jsp");
      result.setSuccess(true);
   }
}