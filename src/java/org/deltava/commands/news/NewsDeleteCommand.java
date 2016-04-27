// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.sql.Connection;

import org.deltava.beans.News;
import org.deltava.commands.*;

import org.deltava.dao.GetNews;
import org.deltava.dao.SetNews;
import org.deltava.dao.DAOException;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to delete System News entries and NOTAMs.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class NewsDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   @Override
public void execute(CommandContext ctx) throws CommandException {
      
      // Check if we're deleting a NOTAM
      boolean isNOTAM = "notam".equals(ctx.getCmdParameter(Command.OPERATION, null));
      ctx.setAttribute("isNews", Boolean.valueOf(!isNOTAM), REQUEST);

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the News/NOTAM entry
         GetNews dao = new GetNews(con);
         News nws = isNOTAM ? dao.getNOTAM(ctx.getID()) : dao.getNews(ctx.getID());
         if (nws == null)
            throw notFoundException("Invalid System News/NOTAM - " + ctx.getID());
         
         // Check our access
         NewsAccessControl access = new NewsAccessControl(ctx, nws);
         access.validate();
         if (!access.getCanDelete())
            throw securityException("Cannot delete System News/NOTAM");
            
         // Get the write DAO and delete the entry
         SetNews wdao = new SetNews(con);
         wdao.delete(nws.getID(), isNOTAM);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set status attribute for the JSP
      ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/news/newsUpdate.jsp");
      result.setSuccess(true);
   }
}