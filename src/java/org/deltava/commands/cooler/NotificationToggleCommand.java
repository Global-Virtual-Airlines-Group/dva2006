// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to toggle Water Cooler thread update notifications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NotificationToggleCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the thread ID
      int id = ctx.getID();

      try {
         Connection con = ctx.getConnection();
         
         // Get the message thread
         GetCoolerThreads tdao = new GetCoolerThreads(con);
         MessageThread thread = tdao.getThread(id);
         if (thread == null)
            throw notFoundException("Invalid Message Thread - " + id);

         // Get the Channel profile
         GetCoolerChannels cdao = new GetCoolerChannels(con);
         Channel c = cdao.get(thread.getChannel());
         if (c == null)
            throw notFoundException("Invalid Channel - " + thread.getChannel());
         
         // Get the Notifications for this thread, and if we're doing an add or a remove
         ThreadNotifications nt = tdao.getNotifications(id);
         boolean isRemove = nt.getIDs().contains(new Integer(ctx.getUser().getID()));
         
         // Check our access - only if we're reading
         CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
         access.updateContext(thread, c);
         access.validate();
         if (!access.getCanRead() && !isRemove)
            throw securityException("Cannot read Message Thread");
         
         // Get the DAO and update the database
         SetCoolerNotification wdao = new SetCoolerNotification(con);
         if (isRemove) {
            wdao.delete(id, ctx.getUser().getID());
         } else {
            wdao.add(id, ctx.getUser().getID());
         }
         
         // Set status attributes
         ctx.setAttribute("notifyUpdate", Boolean.TRUE, REQUEST);
         ctx.setAttribute("notifyToggle", Boolean.valueOf(!isRemove), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the Thread
      CommandResult result = ctx.getResult();
      result.setType(ResultType.REQREDIRECT);
      result.setURL("thread", null, id);
      result.setSuccess(true);
   }
}