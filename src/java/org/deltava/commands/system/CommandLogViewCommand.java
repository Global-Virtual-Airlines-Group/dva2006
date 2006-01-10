// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view the Command History Log.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandLogViewCommand extends AbstractViewCommand {
   
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the start/view/count and command result
      ViewContext vc = initView(ctx);
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
         int id = ctx.getID();
         if ((id == 0) && (ctx.getParameter("pilotName") != null)) {
        	 GetPilot pdao = new GetPilot(con);
        	 Pilot usr = pdao.getByName(ctx.getParameter("pilotName"), SystemData.get("airline.db"));
        	 if (usr != null) {
        		 ctx.setAttribute("pilot", usr, REQUEST);
        		 id = usr.getID();
        	 }
         }
         
         // Get the DAO and the log entries
         GetSystemData dao = new GetSystemData(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Do the query
         if (id != 0) {
        	 vc.setResults(dao.getCommands(id));
         } else {
        	 vc.setResults(dao.getCommands(addr));
         }
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