// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.system.LogEntry;

import org.deltava.commands.*;

import org.deltava.dao.GetSystemLog;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to view the System Log.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LogViewCommand extends AbstractViewCommand {
   
   private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the start/view/count
      ViewContext vc = initView(ctx);
      
      // Get the command result
      CommandResult result = ctx.getResult();
      
      // Save the priority codes
      ctx.setAttribute("priorities", Arrays.asList(LogEntry.PRIORITY), REQUEST);
      
      // If we have no log selected, redirect to the JSP
      String logName = (String) ctx.getCmdParameter(ID, null);
      if (logName == null) {
         result.setURL("/jsp/admin/logView.jsp");
         result.setSuccess(true);
         return;
      }
      
      // Get the start/end dates
      Date sd = null;
      Date ed = null;
      try {
         synchronized (_df) {
            sd = _df.parse(ctx.getParameter("startDate") + " " + ctx.getParameter("startTime"));
            ed = _df.parse(ctx.getParameter("endDate")  + " " + ctx.getParameter("endTime"));
         }
      } catch (ParseException pe) {
      }
      
		if ((sd != null) && (ed == null))
  			ed = new Date();

      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the log entries
         GetSystemLog dao = new GetSystemLog(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Set the filter criteria
         dao.setName(logName);
         dao.setPriority(StringUtils.arrayIndexOf(LogEntry.PRIORITY, ctx.getParameter("priority")));
         dao.setDateRange(sd, ed);
         
         // Figure out the DAO call to make
         if (!StringUtils.isEmpty(ctx.getParameter("loggerClass"))) {
         	vc.setResults(dao.getByType(ctx.getParameter("loggerClass")));
         } else {
         	vc.setResults(dao.getAll());
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setURL("/jsp/admin/logView.jsp");
      result.setSuccess(true);
   }
}