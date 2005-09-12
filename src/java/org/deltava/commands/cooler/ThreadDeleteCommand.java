// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to delete Water Cooler message threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Thread
         GetCoolerThreads dao = new GetCoolerThreads(con);
         MessageThread mt = dao.getThread(ctx.getID());
         if (mt == null)
            throw new CommandException("Invalid Message Thread - " + ctx.getID());
         
         // Get the Cooler Channel
         GetCoolerChannels cdao = new GetCoolerChannels(con);
         Channel c = cdao.get(mt.getChannel());
         if (c == null)
            throw new CommandException("Invalid Channel - " + mt.getChannel());
         
         // Validate our access
         CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
         access.updateContext(mt, c);
         access.validate();
         if (!access.getCanDelete())
            throw securityException("Cannot delete Thread");
         
         // Start a JDBC transaction
         ctx.startTX();
         
         // If there's an image, nuke the image
         if (mt.getImage() != 0) {
            SetGalleryImage iwdao = new SetGalleryImage(con);
            iwdao.delete(mt.getImage());
         }
         
         // Get the write DAO and delete the thread
         SetCoolerMessage twdao = new SetCoolerMessage(con);
         twdao.deleteThread(mt.getID());
         
         // Save the thread in the request
         ctx.setAttribute("thread", mt, REQUEST);
         
         // Commit the transaction
         ctx.commitTX();
      } catch (DAOException de) {
         ctx.rollbackTX();
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set status attribute
      ctx.setAttribute("isDeleted", Boolean.TRUE, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/cooler/threadUpdate.jsp");
      result.setSuccess(true);
   }
}