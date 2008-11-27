// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.servlet.CommandLog;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view the Command History Log.
 * @author Luke
 * @version 2.3
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
      ViewContext vc = initView(ctx, 150);
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
        	 Collection<Pilot> users = pdao.getByName(ctx.getParameter("pilotName"), SystemData.get("airline.db"));
        	 for (Iterator<Pilot> i = users.iterator(); i.hasNext(); ) {
        		 Pilot usr = i.next();
        		 IDs.add(new Integer(usr.getID()));
        	 }
         } else
        	 IDs.add(new Integer(id));
         
         // Get the DAO and the log entries
         GetSystemData dao = new GetSystemData(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Do the query
         Collection<CommandLog> results = (id != 0) ? dao.getCommands(IDs) : dao.getCommands(addr);
         vc.setResults(results);
         
         // Load the pilot IDs
         Collection<Integer> ids = new HashSet<Integer>();
         for (Iterator<CommandLog> i = results.iterator(); i.hasNext(); ) {
        	 CommandLog cl = i.next();
        	 ids.add(new Integer(cl.getPilotID()));
         }
         
         // Get the pilot IDs
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