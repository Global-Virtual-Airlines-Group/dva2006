// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.acars.ConnectionEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete ACARS Connection log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionInfoDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Convert the ID into a longint
      String conID = (String) ctx.getCmdParameter(ID, "0");
      long id = conID.startsWith("0x") ? Long.parseLong(conID, 16) : Long.parseLong(conID);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the ACARS log entry
         GetACARSLog dao = new GetACARSLog(con);
         ConnectionEntry c = dao.getConnection(id);
         if (c == null)
            throw new CommandException("Invalid ACARS Connection ID - " + conID);
         
         // Check for a Flight Information Report
         if (c.getFlightInfoCount() > 0) {
            ctx.setAttribute("info", dao.getInfo(id), REQUEST);
            ctx.setAttribute("conDelete", Boolean.FALSE, REQUEST);
         } else {
            SetACARSLog wdao = new SetACARSLog(con);
            wdao.deleteConnection(id);
            
            // Set the status attribute
            ctx.setAttribute("conDelete", Boolean.TRUE, REQUEST);
         }
         
         // Save the connection in the request
         ctx.setAttribute("con", c, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/acars/logEntryDelete.jsp");
      result.setSuccess(true);
   }
}