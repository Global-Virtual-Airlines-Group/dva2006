// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.sql.Connection;

import org.deltava.beans.Notice;

import org.deltava.commands.*;

import org.deltava.dao.GetNews;
import org.deltava.dao.DAOException;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to edit NOTAMs.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class NOTAMEditCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   @Override
public void execute(CommandContext ctx) throws CommandException {

      // Get the command result
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/news/notamEdit.jsp");
      
      // Check if we're creating a new entry
      if (ctx.getID() == 0) {
         NewsAccessControl access = new NewsAccessControl(ctx, null);
         access.validate();
         if (!access.getCanCreateNOTAM())
            throw securityException("Cannot create NOTAM entry");
         
         // Save access controller and redirect to JSP
         ctx.setAttribute("access", access, REQUEST);
         result.setSuccess(true);
         return;
      }

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the existing System News entry
         GetNews dao = new GetNews(con);
         Notice notam = dao.getNOTAM(ctx.getID());
         if (notam == null)
            throw notFoundException("Invalid NOTAM entry - " + ctx.getID());
         
         // Check our access
         NewsAccessControl access = new NewsAccessControl(ctx, notam);
         access.validate();
         
         // Save the news entry and the access controller
         ctx.setAttribute("entry", notam, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
         
         // Figure out what JSP to display
         result.setURL(access.getCanEdit() ? "/jsp/news/notamEdit.jsp" : "/jsp/news/notamRead.jsp");
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setSuccess(true);
   }
}