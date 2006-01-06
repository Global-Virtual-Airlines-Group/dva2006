// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetSystemLog;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

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
         
         // Get the DAO and the log entries
         GetSystemLog dao = new GetSystemLog(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Do the query
         if (ctx.getID() != 0) {
        	 
         } else {
        	 
         }
         
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setURL("/jsp/admin/cmdLogView.jsp");
      result.setSuccess(true);
   }
}