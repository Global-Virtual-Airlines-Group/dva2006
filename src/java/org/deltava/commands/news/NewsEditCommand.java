// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.news;

import java.sql.Connection;

import org.deltava.beans.News;
import org.deltava.commands.*;

import org.deltava.dao.GetNews;
import org.deltava.dao.DAOException;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to edit System News entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NewsEditCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the command result
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/news/newsEdit.jsp");
      
      // Check if we're creating a new entry
      if (ctx.getID() == 0) {
         NewsAccessControl access = new NewsAccessControl(ctx, null);
         access.validate();
         if (!access.getCanCreateNews())
            throw new CommandSecurityException("Cannot create System News entry");
         
         // Save access controller and redirect to JSP
         ctx.setAttribute("access", access, REQUEST);
         result.setSuccess(true);
         return;
      }

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the existing System News entry
         GetNews dao = new GetNews(con);
         News nws = dao.getNews(ctx.getID());
         if (nws == null)
            throw new CommandException("Invalid System News entry - " + ctx.getID());
         
         // Check our access
         NewsAccessControl access = new NewsAccessControl(ctx, nws);
         access.validate();
         if (!access.getCanEdit())
            throw new CommandSecurityException("Cannot edit System News entry");
         
         // Save the news entry and the access controller
         ctx.setAttribute("entry", nws, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setSuccess(true);
   }
}