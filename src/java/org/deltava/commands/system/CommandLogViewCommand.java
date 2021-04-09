// Copyright 2005, 2008, 2016, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.servlet.CommandLog;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view the Command History Log.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class CommandLogViewCommand extends AbstractViewCommand {
   
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   @Override
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the start/view/count and command result
      ViewContext<CommandLog> vc = initView(ctx, CommandLog.class, 150);
      CommandResult result = ctx.getResult();
      
      // If we have no address selected, redirect to the JSP
      String addr = ctx.getParameter("addr");
      if (addr == null) {
         result.setURL("/jsp/admin/cmdLogView.jsp");
         result.setSuccess(true);
         return;
      }
      
      try {
         Connection con = ctx.getConnection();
         
         // If we're searching for a pilot name, get it
         Collection<Integer> IDs = new HashSet<Integer>();
         GetPilotDirectory pdao = new GetPilotDirectory(con);
         int id = ctx.getID();
         if ((id == 0) && (ctx.getParameter("pilotName") != null)) {
        	 Collection<Pilot> users = pdao.getByName(ctx.getParameter("pilotName"), ctx.getDB());
        	 users.forEach(usr -> IDs.add(Integer.valueOf(usr.getID())));
         } else
        	 IDs.add(Integer.valueOf(id));
         
         // Get the DAO and the log entries
         GetSystemLog dao = new GetSystemLog(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Do the query
         Collection<CommandLog> results = (IDs.size() > 0 ) ? dao.getCommands(IDs) : dao.getCommands(addr);
         vc.setResults(results);
         
         // Load the pilot IDs
         Collection<Integer> ids = results.stream().map(CommandLog::getPilotID).collect(Collectors.toSet());
         ctx.setAttribute("pilots", pdao.getByID(ids, "PILOTS"), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set search attribute
      ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
      
      // Forward to the JSP
      result.setURL("/jsp/admin/cmdLogView.jsp");
      result.setSuccess(true);
   }
}